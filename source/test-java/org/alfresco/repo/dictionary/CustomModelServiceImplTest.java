/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelException.ActiveModelConstraintException;
import org.alfresco.service.cmr.dictionary.CustomModelException.InvalidNamespaceException;
import org.alfresco.service.cmr.dictionary.CustomModelException.ModelDoesNotExistException;
import org.alfresco.service.cmr.dictionary.CustomModelException.NamespaceConstraintException;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration tests for {@link CustomModelServiceImpl}
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelServiceImplTest
{

    private static final long PAUSE_TIME = 1000;

    @ClassRule
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

    @Rule
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());

    private static CustomModelService customModelService;
    private static RetryingTransactionHelper transactionHelper;
    private static PersonService personService;
    private static AuthorityService authorityService;
    private static CMMDownloadTestUtil cmmDownloadTestUtil;

    private List<String> modelNames = new ArrayList<>();

    @BeforeClass
    public static void initStaticData() throws Exception
    {
        customModelService = APP_CONTEXT_INIT.getApplicationContext().getBean("customModelService", CustomModelService.class);
        transactionHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        personService = APP_CONTEXT_INIT.getApplicationContext().getBean("personService", PersonService.class);
        authorityService = APP_CONTEXT_INIT.getApplicationContext().getBean("authorityService", AuthorityService.class);
        cmmDownloadTestUtil = new CMMDownloadTestUtil(APP_CONTEXT_INIT.getApplicationContext());
    }

    @AfterClass
    public static void cleanUp()
    {
        cmmDownloadTestUtil.cleanup();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @After
    public void tearDown() throws Exception
    {
        final List<String> activeModels = new ArrayList<>();
        for (final String model : modelNames)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Exception
                {
                    try
                    {
                        customModelService.deleteCustomModel(model);
                    }
                    catch (ActiveModelConstraintException ex)
                    {
                        activeModels.add(model);
                    }
                    catch (Exception e)
                    {
                     // Ignore
                    }
                    return null;
                }
            });
        }

        for(final String am: activeModels)
        {
            // Try to deactivate and delete again
            try
            {
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Exception
                    {
                        customModelService.deactivateCustomModel(am);
                        return null;
                    }
                });
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Exception
                    {
                        customModelService.deleteCustomModel(am);
                        return null;
                    }
                });
            }
            catch (Exception ex)
            {
                // Ignore
            }
        }
        modelNames.clear();
    }

    @Test
    public void testCreateBasicInactiveModel() throws Exception
    {
        final String modelName1 = makeUniqueName("testCustomModel1");
        final String desc = "This is test custom model desc";

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName1);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setDescription(desc);
        model.setAuthor("John Doe");

        // Create the 1st model
        CustomModelDefinition modelDefinition = createModel(model, false);

        assertNotNull(modelDefinition);
        assertEquals(modelName1, modelDefinition.getName().getLocalName());
        assertTrue("There is no imported namespace." ,modelDefinition.getImportedNamespaces().isEmpty());

        NamespaceDefinition namespaceDefinition = modelDefinition.getNamespaces().iterator().next();
        assertNotNull(namespaceDefinition);
        assertEquals(namespacePair.getFirst(), namespaceDefinition.getUri());
        assertEquals(namespacePair.getSecond(), namespaceDefinition.getPrefix());

        assertEquals(desc, modelDefinition.getDescription());
        assertEquals("John Doe", modelDefinition.getAuthor());

        final String modelName2 = makeUniqueName("testCustomModel2");
        model = M2Model.createModel(namespacePair.getSecond()  + QName.NAMESPACE_PREFIX + modelName2);
        model.createNamespace(namespacePair.getFirst(), "newTestPrefix");

        // Create the 2nd model - duplicate namespace URI
        try
        {
            createModel(model, false);
            fail("Shouldn't be able to create a model with an already in-use namespace uri.");
        }
        catch (NamespaceConstraintException ex)
        {
            // Expected
        }

        final String modelName3 = makeUniqueName("testCustomModel3");
        model = M2Model.createModel(namespacePair.getSecond()  + QName.NAMESPACE_PREFIX + modelName3);
        model.createNamespace(getTestNamespacePrefixPair().getFirst(), namespacePair.getSecond());

        // Create the 2nd model - duplicate namespace Prefix
        try
        {
            createModel(model, false);
            fail("Shouldn't be able to create a model with an already in-use namespace prefix.");
        }
        catch (NamespaceConstraintException ex)
        {
            // Expected
        }

        namespacePair = getTestNamespacePrefixPair();
        model = M2Model.createModel(namespacePair.getSecond()  + QName.NAMESPACE_PREFIX + modelName2);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        modelDefinition = createModel(model, false);
        assertNotNull(modelDefinition);
        assertEquals(modelName2, modelDefinition.getName().getLocalName());
        namespaceDefinition = modelDefinition.getNamespaces().iterator().next();
        assertNotNull(namespaceDefinition);
        assertEquals(namespacePair.getFirst(), namespaceDefinition.getUri());
        assertEquals(namespacePair.getSecond(), namespaceDefinition.getPrefix());

        try
        {
            // Test duplicate model
            createModel(model, false);
            fail("Shouldn't be able to create a duplicate model.");
        }
        catch (Exception e)
        {
            // Expected
        }

        try
        {
            // Test creating a model with the same name as the bootstrapped model
            model.setName("contentmodel");
            createModel(model, false);
            fail("Shouldn't be able to create a model with the same name as the bootstrapped model.");
        }
        catch (Exception e)
        {
            // Expected
        }

        // Test list all models
        try
        {
            customModelService.getCustomModels(null);
            fail("Should have thrown IllegalArgumentException as PagingRequest was null.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
        final PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE);
        PagingResults<CustomModelDefinition> result = transactionHelper.doInTransaction(new RetryingTransactionCallback<PagingResults<CustomModelDefinition>>()
        {
            public PagingResults<CustomModelDefinition> execute() throws Exception
            {
                    return customModelService.getCustomModels(pagingRequest);
            }
        });

        assertTrue(result.getTotalResultCount().getFirst() >= 2);
    }

    @Test
    public void testListTypesAspects_Empty() throws Exception
    {
        final String modelName = makeUniqueName("testCustomModel");
        Pair<String, String> namespacePair = getTestNamespacePrefixPair();

        final M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());

        createModel(model, false);

        // Retrieve the created model
        CustomModelDefinition modelDefinition = getModel(modelName);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());

        // List all of the model's types
        Collection<TypeDefinition> types = modelDefinition.getTypeDefinitions();
        assertEquals(0, types.size());

        // List all of the model's aspects
        Collection<AspectDefinition> aspects = modelDefinition.getAspectDefinitions();
        assertEquals(0, aspects.size());
    }

    @Test
    public void testModelAdmin() throws Exception
    {
        assertFalse(customModelService.isModelAdmin(null));

        final String userName = "testUser" + System.currentTimeMillis();
        final PropertyMap testUser = new PropertyMap();
        testUser.put(ContentModel.PROP_USERNAME, userName);
        testUser.put(ContentModel.PROP_FIRSTNAME, "John");
        testUser.put(ContentModel.PROP_LASTNAME, "Doe");
        testUser.put(ContentModel.PROP_PASSWORD, "password");

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                personService.createPerson(testUser);

                assertFalse(customModelService.isModelAdmin(userName));

                // Add the user to the group
                authorityService.addAuthority(CustomModelServiceImpl.GROUP_ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY, userName);
                assertTrue(customModelService.isModelAdmin(userName));
                return null;
            }
        });
    }

    @Test
    public void testActivateModel() throws Exception
    {
        final String modelName = makeUniqueName("testCustomModel");
        final String desc = "This is test custom model desc";

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setDescription(desc);
        model.setAuthor("John Doe");

        // Create the model
        CustomModelDefinition modelDefinition = createModel(model, false);

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertFalse(modelDefinition.isActive());

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                // Activate the model
                customModelService.activateCustomModel(modelName);
                return null;
            }
        });

        // Retrieve the model
        modelDefinition = getModel(modelName);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertTrue(modelDefinition.isActive());

        // Try to activate the model again
        try
        {
            customModelService.activateCustomModel(modelName);
            fail("Shouldn't be able to activate an already activated model.");
        }
        catch (Exception ex)
        {
            // Expected
        }
    }

    @Test
    public void testisNamespaceUriExists()
    {
        final String modelName = makeUniqueName("testCustomModel");

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setAuthor("John Doe");

        assertNull(customModelService.getCustomModelByUri(namespacePair.getFirst()));
        // Create the model
        CustomModelDefinition modelDefinition = createModel(model, false);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());

        assertTrue(customModelService.isNamespaceUriExists(namespacePair.getFirst()));
        ModelDefinition modelDefinitionByUri = customModelService.getCustomModelByUri(namespacePair.getFirst());
        assertNotNull(modelDefinitionByUri);
        assertEquals(modelName, modelDefinitionByUri.getName().getLocalName());
    }

    @Test
    public void testCreateModelWithTypesAndAspects() throws Exception
    {
        String modelName = makeUniqueName("testCustomModelFailed");
        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setAuthor("Average Joe");

        // Type
        String typeName = "testType";
        M2Type m2Type = model.createType(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName);
        m2Type.setTitle("Test type title");
        m2Type.setParentName("cm:content");

        // Aspect
        String aspectName = "testMarkerAspect";
        model.createAspect(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName);

        try
        {
            createModel(model, false);
            fail("Shouldn't be able to create a model without namespace imports, when type and/or aspect have parent name.");
        }
        catch (Exception ex)
        {
            // Expected
        }

        model.setName(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createImport("http://www.alfresco.org/model/content/1.0", "cm");

        CustomModelDefinition modelDefinition = createModel(model, false);

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());

        assertEquals(1, modelDefinition.getTypeDefinitions().size());
        assertEquals(typeName, modelDefinition.getTypeDefinitions().iterator().next().getName().getLocalName());
        assertEquals("cm:content", modelDefinition.getTypeDefinitions().iterator().next().getParentName().toPrefixString());

        assertEquals(1, modelDefinition.getAspectDefinitions().size());
        assertEquals(aspectName, modelDefinition.getAspectDefinitions().iterator().next().getName().getLocalName());

        // list all custom models's types and aspects
        {
            try
            {
                customModelService.getAllCustomTypes(null);
                fail("Should have thrown IllegalArgumentException as PagingRequest was null.");
            }
            catch (IllegalArgumentException e)
            {
                // Expected
            }
            try
            {
                customModelService.getAllCustomAspects(null);
                fail("Should have thrown IllegalArgumentException as PagingRequest was null.");
            }
            catch (IllegalArgumentException e)
            {
                // Expected
            }
            final PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE);
            PagingResults<TypeDefinition> allTypes = transactionHelper.doInTransaction(new RetryingTransactionCallback<PagingResults<TypeDefinition>>()
            {
                public PagingResults<TypeDefinition> execute() throws Exception
                {
                    return customModelService.getAllCustomTypes(pagingRequest);
                }
            });
            assertTrue(allTypes.getTotalResultCount().getFirst() >= 1);

            PagingResults<AspectDefinition> allAspects = transactionHelper.doInTransaction(new RetryingTransactionCallback<PagingResults<AspectDefinition>>()
            {
                public PagingResults<AspectDefinition> execute() throws Exception
                {
                      return  customModelService.getAllCustomAspects(pagingRequest);
                }
            });
            assertTrue(allAspects.getTotalResultCount().getFirst() >= 1);
        }

        // Retrieve the aspect by the aspect QName
        QName aspectQName = QName.createQName("{" + namespacePair.getFirst() + "}" + aspectName);
        AspectDefinition aspectDefinition = customModelService.getCustomAspect(aspectQName);
        assertNotNull(aspectDefinition);
        assertEquals(1, getModel(modelName).getAspectDefinitions().size());

        // Retrieve the type by the type QName
        QName typeQName = QName.createQName("{" + namespacePair.getFirst()+ "}" + typeName);
        TypeDefinition typeDefinition = customModelService.getCustomType(typeQName);
        assertNotNull(typeDefinition);
        assertEquals(1, getModel(modelName).getTypeDefinitions().size());

        // Test update model by adding an aspect
        String aspectName2 = "testMarkerAspect2";
        model.createAspect(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName2);
        updateModel(modelName, model, false);

        // Retrieve the created aspect
        aspectQName = QName.createQName("{" + namespacePair.getFirst() + "}" + aspectName2);
        aspectDefinition = customModelService.getCustomAspect(aspectQName);
        assertNotNull(aspectDefinition);
        assertEquals(aspectQName, aspectDefinition.getName());
        assertEquals(2, getModel(modelName).getAspectDefinitions().size());

        // Test update model by adding a type
        String typeName2 = "testType2";
        model.createType(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName2);
        updateModel(modelName, model, false);

        // Retrieve the created type
        typeQName = QName.createQName("{" + namespacePair.getFirst() + "}" + typeName2);
        typeDefinition = customModelService.getCustomType(typeQName);
        assertNotNull(typeDefinition);
        assertEquals(typeQName, typeDefinition.getName());
        assertEquals(2, getModel(modelName).getTypeDefinitions().size());

        {
            // Create an aspect with an identical name as an already defined
            // type name within this model
            model.createAspect(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName);
            try
            {
                updateModel(modelName, model, false);
                fail("Shouldn't be able to create a type and an aspect with the same name in a model.");
            }
            catch (Exception ex)
            {
                // Expected
            }
        }
    }

    @Test
    public void testDeactivateModel() throws Exception
    {
        final String modelName = makeUniqueName("testDeactivateCustomModel");
        final String desc = "This is test custom model desc";

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        final M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setDescription(desc);
        model.setAuthor("John Doe");

        // Create the model
        CustomModelDefinition modelDefinition = createModel(model, true);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertTrue(modelDefinition.isActive());

        // Deactivate the model
        customModelService.deactivateCustomModel(modelName);

        // Retrieve the model
        modelDefinition = transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Exception
            {
                return customModelService.getCustomModel(modelName);
            }
        });

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertFalse(modelDefinition.isActive());

        // Try to deactivate the model again
        try
        {
            customModelService.deactivateCustomModel(modelName);
            fail("Shouldn't be able to deactivate an already deactivated model.");
        }
        catch (Exception ex)
        {
            // Expected
        }
    }

    @Test
    public void testDeleteModel() throws Exception
    {
        final String modelName = makeUniqueName("testDeleteCustomModel");

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        final M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setAuthor("John Doe");

        // Create the model
        CustomModelDefinition modelDefinition = createModel(model, false);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertFalse(modelDefinition.isActive());

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                // delete non-existing model
                try
                {
                    customModelService.deleteCustomModel(modelName + "someModel");
                    fail("Should have thrown ModelDoesNotExistException.");
                }
                catch (ModelDoesNotExistException ex)
                {
                    // Expected
                }
                return null;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                // Activate the model
                customModelService.activateCustomModel(modelName);
                return null;
            }
        });

        // delete an active model
        try
        {
            customModelService.deleteCustomModel(modelName);
            fail("Shouldn't be able to delete an active model.");
        }
        catch (ActiveModelConstraintException ex)
        {
            // Expected
        }

        // Deactivate the model
        customModelService.deactivateCustomModel(modelName);

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                customModelService.deleteCustomModel(modelName);
                return null;
            }
        });

        modelDefinition = getModel(modelName);
        assertNull(modelDefinition);
    }

    @Test
    public void testUpdateModel() throws Exception
    {
        final String modelName = makeUniqueName("testUpdateCustomModel");
        final String desc = "This is test custom model desc";

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setDescription(desc);
        model.setAuthor("John Doe");
        // Add aspect
        String aspectName = "testMarkerAspect";
        model.createAspect(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName);

        // Create the model
        CustomModelDefinition modelDefinition = createModel(model, false);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertFalse(modelDefinition.isActive());
        NamespaceDefinition namespaceDefinition = modelDefinition.getNamespaces().iterator().next();
        assertEquals(namespacePair.getFirst(), namespaceDefinition.getUri());
        assertEquals(namespacePair.getSecond(), namespaceDefinition.getPrefix());
        assertEquals(desc, modelDefinition.getDescription());
        assertEquals("John Doe", modelDefinition.getAuthor());
        assertEquals(1, modelDefinition.getAspectDefinitions().size());

        // Update the model by removing the namespace
        model.removeNamespace(namespacePair.getFirst());
        try
        {
            updateModel(modelName, model, false);
            fail("Shouldn't be able to update a custom model with an empty namespace.");
        }
        catch (InvalidNamespaceException ex)
        {
            // Expected
        }

        // Update the model by removing the namespace prefix
        model.createNamespace(namespacePair.getFirst(), null);
        try
        {
            updateModel(modelName, model, false);
            fail("Model validation should have failed, as the namespace prefix is null.");
        }
        catch (IllegalArgumentException ex)
        {
            // Expected
        }

        // Update the model by adding more namespace URIs
        model.createNamespace("http://www.alfresco.org/model/contenttest/1.0", namespacePair.getSecond());
        try
        {
            updateModel(modelName, model, false);
            fail("Shouldn't be able to add more than one namespace URI into a custom model.");
        }
        catch (InvalidNamespaceException ex)
        {
            // Expected
        }

        // Update the namespace with a URI that has already been used
        model.removeNamespace(namespacePair.getFirst());
        model.removeNamespace("http://www.alfresco.org/model/contenttest/1.0");
        model.createNamespace("http://www.alfresco.org/model/content/1.0", namespacePair.getSecond());
        try
        {
            updateModel(modelName, model, false);
            fail("Shouldn't be able to update a model with an already in-use namespace URI.");
        }
        catch (NamespaceConstraintException ex)
        {
            // Expected
        }

        // Update the namespace with a Prefix that has already been used
        model.removeNamespace("http://www.alfresco.org/model/content/1.0");
        model.createNamespace(namespacePair.getFirst(), "cm");
        try
        {
            updateModel(modelName, model, false);
            fail("Shouldn't be able to update a model with an already in-use namespace Prefix.");
        }
        catch (NamespaceConstraintException ex)
        {
            // Expected
        }

        // New namespace
        Pair<String, String> newNamespacePair = getTestNamespacePrefixPair();
        model = M2Model.createModel(newNamespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(newNamespacePair.getFirst(), newNamespacePair.getSecond());
        model.setDescription(desc);
        // Update non-existing model
        try
        {
            updateModel(modelName + "non-existing model", model, false);
            fail("Should have thrown ModelDoesNotExistException.");
        }
        catch (ModelDoesNotExistException ex)
        {
            // Expected
        }
        modelDefinition = updateModel(modelName, model, false);
        namespaceDefinition = modelDefinition.getNamespaces().iterator().next();
        assertEquals(newNamespacePair.getFirst(), namespaceDefinition.getUri());
        assertEquals(newNamespacePair.getSecond(), namespaceDefinition.getPrefix());
        assertEquals(desc, modelDefinition.getDescription());
        assertNull(modelDefinition.getAuthor());
        assertEquals(0, modelDefinition.getAspectDefinitions().size());

        // Test that the cache is updated correctly. This means the cache should have removed the old namespace URI.
        QName aspectQName = QName.createQName("{" + namespacePair.getFirst() + "}" + aspectName);
        AspectDefinition aspectDefinition = customModelService.getCustomAspect(aspectQName);
        assertNull(aspectDefinition);

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                // Activate the model
                customModelService.activateCustomModel(modelName);
                return null;
            }
        });

        // Retrieve the model
        modelDefinition = getModel(modelName);
        assertNotNull(modelDefinition);
        assertTrue(modelDefinition.isActive());

        // Try to update only the namespace URI of an active model
        Pair<String, String> activeModelNamespacePair = getTestNamespacePrefixPair();
        model = M2Model.createModel(newNamespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(activeModelNamespacePair.getFirst(), newNamespacePair.getSecond());
        try
        {
            updateModel(modelName, model, true); // true => as we activated the model
            fail("Shouldn't be able to update the namespace URI of an active model.");
        }
        catch (ActiveModelConstraintException ax)
        {
            // Expected
        }

        // Try to update only the namespace prefix of an active model
        activeModelNamespacePair = getTestNamespacePrefixPair();
        model = M2Model.createModel(activeModelNamespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(newNamespacePair.getFirst(), activeModelNamespacePair.getSecond());
        try
        {
            updateModel(modelName, model, true); // true => as we activated the model
            fail("Shouldn't be able to update the namespace prefix of an active model.");
        }
        catch (ActiveModelConstraintException ax)
        {
            // Expected
        }

        // Try to update both the namespace URI and prefix of an active model
        activeModelNamespacePair = getTestNamespacePrefixPair();
        model = M2Model.createModel(activeModelNamespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(activeModelNamespacePair.getFirst(), activeModelNamespacePair.getSecond());
        try
        {
            updateModel(modelName, model, true); // true => as we activated the model
            fail("Shouldn't be able to update the namespace URI and namespace prefix of an active model.");
        }
        catch (ActiveModelConstraintException ax)
        {
            // Expected
        }

        // Update active model's desc and author
        modelDefinition = getModel(modelName);
        namespaceDefinition = modelDefinition.getNamespaces().iterator().next();
        model = M2Model.createModel(namespaceDefinition.getPrefix() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespaceDefinition.getUri(), namespaceDefinition.getPrefix());
        model.setDescription(desc);
        model.setAuthor("Admin Admin");

        modelDefinition = updateModel(modelName, model, true);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertTrue(modelDefinition.isActive());
        assertEquals(desc, modelDefinition.getDescription());
        assertEquals("Admin Admin", modelDefinition.getAuthor());

    }

    @Test
    public void testCreateDownload() throws Exception
    {
        final String modelName = makeUniqueName("testDownloadCustomModel");
        final String modelExportFileName = modelName + ".xml";
        final String shareExtExportFileName = "CMM_" + modelName + "_module.xml";

        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setAuthor("Admin");
        model.createImport("http://www.alfresco.org/model/content/1.0", "cm");

        // Add Type
        String typeName = "testType";
        M2Type m2Type = model.createType(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName);
        m2Type.setTitle("Test type title");
        m2Type.setParentName("cm:content");

        // Create the model
        CustomModelDefinition modelDefinition = createModel(model, false);

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());

        NodeRef downloadNode = createDownload(modelName, false);
        assertNotNull(downloadNode);

        DownloadStatus status = cmmDownloadTestUtil.getDownloadStatus(downloadNode);
        while (status.getStatus() == DownloadStatus.Status.PENDING)
        {
            Thread.sleep(PAUSE_TIME);
            status = cmmDownloadTestUtil.getDownloadStatus(downloadNode);
        }

        Set<String> entries = cmmDownloadTestUtil.getDownloadEntries(downloadNode);
        assertEquals(1, entries.size());
        String modelEntry = cmmDownloadTestUtil.getDownloadEntry(entries, modelExportFileName);
        assertNotNull(modelEntry);
        assertEquals(modelEntry, modelExportFileName);

        // Create Share extension module
        cmmDownloadTestUtil.createShareExtModule(modelName);

        downloadNode = createDownload(modelName, true);
        assertNotNull(downloadNode);

        status = cmmDownloadTestUtil.getDownloadStatus(downloadNode);
        while (status.getStatus() == DownloadStatus.Status.PENDING)
        {
            Thread.sleep(PAUSE_TIME);
            status = cmmDownloadTestUtil.getDownloadStatus(downloadNode);
        }

        entries = cmmDownloadTestUtil.getDownloadEntries(downloadNode);
        assertEquals(2, entries.size());

        modelEntry = cmmDownloadTestUtil.getDownloadEntry(entries, modelExportFileName);
        assertNotNull(modelEntry);
        assertEquals(modelEntry, modelExportFileName);

        String shareExtEntry = cmmDownloadTestUtil.getDownloadEntry(entries, shareExtExportFileName);
        assertNotNull(shareExtEntry);
        assertEquals(shareExtEntry, shareExtExportFileName);

        // Create Share extension module - this will override the existing module
        cmmDownloadTestUtil.createShareExtModule(modelName + System.currentTimeMillis());

        // The module id dose not exist, so the CMM service logs the error
        // (warning) and creates a zip containing only the model.
        downloadNode = createDownload(modelName, true);
        assertNotNull(downloadNode);

        status = cmmDownloadTestUtil.getDownloadStatus(downloadNode);
        while (status.getStatus() == DownloadStatus.Status.PENDING)
        {
            Thread.sleep(PAUSE_TIME);
            status = cmmDownloadTestUtil.getDownloadStatus(downloadNode);
        }

        entries = cmmDownloadTestUtil.getDownloadEntries(downloadNode);
        assertEquals(1, entries.size());
        modelEntry = cmmDownloadTestUtil.getDownloadEntry(entries, modelExportFileName);
        assertNotNull(modelEntry);
        assertEquals(modelEntry, modelExportFileName);
    }

    @Test
    public void testModelsInfo() throws Exception
    {
        CustomModelsInfo info = transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelsInfo>()
        {
            public CustomModelsInfo execute() throws Exception
            {
                return customModelService.getCustomModelsInfo();
            }
        });

        final String modelName = makeUniqueName("testCustomModelsInfo");
        Pair<String, String> namespacePair = getTestNamespacePrefixPair();
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setAuthor("Admin");
        model.createImport("http://www.alfresco.org/model/content/1.0", "cm");

        // Add Type
        String typeName = "testType";
        M2Type m2Type = model.createType(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName);
        m2Type.setTitle("Test type title");
        m2Type.setParentName("cm:content");

        // Add Aspect
        String aspectName = "testMarkerAspect";
        model.createAspect(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName);

        // Create the model
        createModel(model, true);

        CustomModelsInfo newInfo = transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelsInfo>()
        {
            public CustomModelsInfo execute() throws Exception
            {
                return customModelService.getCustomModelsInfo();
            }
        });

        assertEquals(info.getNumberOfActiveModels() + 1, newInfo.getNumberOfActiveModels());
        assertEquals(info.getNumberOfActiveTypes() + 1, newInfo.getNumberOfActiveTypes());
        assertEquals(info.getNumberOfActiveAspects() + 1, newInfo.getNumberOfActiveAspects());

        // Add another aspect
        String aspectNameTwo = "testMarkerAspectTwo";
        model.createAspect(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectNameTwo);
        // Update the model
        updateModel(modelName, model, true);

        // Get the models' info
        newInfo = transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelsInfo>()
        {
            public CustomModelsInfo execute() throws Exception
            {
                return customModelService.getCustomModelsInfo();
            }
        });
        assertEquals(info.getNumberOfActiveModels() + 1, newInfo.getNumberOfActiveModels());
        assertEquals(info.getNumberOfActiveTypes() + 1, newInfo.getNumberOfActiveTypes());
        // 2 => we added two aspects
        assertEquals(info.getNumberOfActiveAspects() + 2, newInfo.getNumberOfActiveAspects());
    }

    private Pair<String, String> getTestNamespacePrefixPair()
    {
        long timeMillis = System.currentTimeMillis();
        String uri = "http://www.alfresco.org/model/testcmmservicenamespace" + timeMillis + "/1.0";
        String prefix = "testcmmservice" + timeMillis;

        return new Pair<String, String>(uri, prefix);
    }

    private String makeUniqueName(String modelName)
    {
        String name = modelName + System.currentTimeMillis();
        modelNames.add(name);

        return name;
    }

    private CustomModelDefinition createModel(final M2Model m2Model, final boolean activate)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Exception
            {
                return customModelService.createCustomModel(m2Model, activate);
            }
        });
    }

    private CustomModelDefinition updateModel(final String modelName, final M2Model m2Model, final boolean activate)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Exception
            {
                return customModelService.updateCustomModel(modelName, m2Model, activate);
            }
        });
    }

    private CustomModelDefinition getModel(final String modelName)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Exception
            {
                return customModelService.getCustomModel(modelName);
            }
        });
    }

    private NodeRef createDownload(final String modelName, final boolean withShareExtModule)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                NodeRef nodeRef = customModelService.createDownloadNode(modelName, withShareExtModule);
                return nodeRef;
            }
        });
    }
}
