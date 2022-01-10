/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeCreateReference;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference;
import org.alfresco.module.org_alfresco_module_rm.admin.CustomMetadataException;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipType;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.springframework.util.CollectionUtils;

/**
 * This test class tests the definition and use of a custom RM elements at the Java services layer.
 *
 * @author Neil McErlean, janv, Roy Wetherall
 */
public class RecordsManagementAdminServiceImplTest extends    BaseRMTestCase
                                         		   implements RecordsManagementModel,
                                                    	      BeforeCreateReference,
                                                              OnCreateReference
{

    private final static long testRunID = System.currentTimeMillis();

    private List<QName> createdCustomProperties;
    private List<QName> madeCustomisable;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        createdCustomProperties = new ArrayList<>();
        madeCustomisable = new ArrayList<>();
        super.setUp();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestData()
     */
    @Override
    protected void setupTestData()
    {
        super.setupTestData();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                for (QName createdCustomProperty : createdCustomProperties)
                {
                    rmAdminService.removeCustomPropertyDefinition(createdCustomProperty);
                }

                for (QName customisable : madeCustomisable)
                {
                    rmAdminService.unmakeCustomisable(customisable);
                }

                return null;
            }
        });

    	super.tearDown();
    }

    /**
     * @see RecordsManagementAdminService#getCustomisable()
     */
    public void testGetCustomisable() throws Exception
    {
        // Get the customisable types
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Set<QName> list = rmAdminService.getCustomisable();
                assertNotNull(list);
                assertTrue(list.containsAll(
                    CollectionUtils.arrayToList(new QName[]
                    {
                        ASPECT_RECORD,
                        TYPE_RECORD_FOLDER,
                        TYPE_NON_ELECTRONIC_DOCUMENT,
                        TYPE_RECORD_CATEGORY
                    })));

                return null;
            }
        });
    }

    /**
     * @see RecordsManagementAdminService#isCustomisable(QName)
     */
    public void testIsCustomisable() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertFalse(rmAdminService.isCustomisable(TYPE_CONTENT));
                assertFalse(rmAdminService.isCustomisable(ASPECT_DUBLINCORE));
                assertTrue(rmAdminService.isCustomisable(TYPE_RECORD_FOLDER));
                assertTrue(rmAdminService.isCustomisable(ASPECT_RECORD));

            	return null;
            }
        });
	}

	/**
	 * @see RecordsManagementAdminService#existsCustomProperty(QName)
	 * @see RecordsManagementAdminService#addCustomPropertyDefinition(QName, QName, String, QName, String, String, String, boolean, boolean, boolean, QName)
	 * @see RecordsManagementAdminService#addCustomPropertyDefinition(QName, QName, String, QName, String, String)
	 */
    public void testAddCustomPropertyDefinition() throws Exception
    {
        // Add property to Record (id specified, short version)
        doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                // Check the property does not exist
                assertFalse(rmAdminService.existsCustomProperty(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myRecordProp1")));

                return rmAdminService.addCustomPropertyDefinition(
                                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myRecordProp1"),
                                        ASPECT_RECORD,
                                        "Label1",
                                        DataTypeDefinition.TEXT,
                                        "Title",
                                        "Description");
            }

            @Override
            public void test(QName result) throws Exception
            {
                try
                {
                    // Check the property QName is correct
                    assertNotNull(result);
                    assertEquals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myRecordProp1"), result);
                    assertTrue(rmAdminService.existsCustomProperty(result));

                    // Check that property is available as a custom property
                    Map<QName, PropertyDefinition> propDefs = rmAdminService.getCustomPropertyDefinitions(ASPECT_RECORD);
                    assertNotNull(propDefs);
                    assertTrue(propDefs.containsKey(result));

                    // Check the property definition
                    PropertyDefinition propDef = propDefs.get(result);
                    assertNotNull(propDef);
                    assertEquals(DataTypeDefinition.TEXT, propDef.getDataType().getName());
                    assertEquals("Description", propDef.getDescription(dictionaryService));
                    assertEquals("Label1", propDef.getTitle(dictionaryService));
                }
                finally
                {
                    // Store the created property for cleanup later
                    createdCustomProperties.add(result);
                }
            }
        });

        // Add property to record (no id, short version)
        doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                return rmAdminService.addCustomPropertyDefinition(
                                        null,
                                        ASPECT_RECORD,
                                        "Label2",
                                        DataTypeDefinition.TEXT,
                                        "Title",
                                        "Description");
            }

            @Override
            public void test(QName result) throws Exception
            {
                try
                {
                    // Check the property QName is correct
                    assertNotNull(result);
                    assertEquals(RecordsManagementCustomModel.RM_CUSTOM_URI, result.getNamespaceURI());
                    assertTrue(rmAdminService.existsCustomProperty(result));

                    // Check that property is available as a custom property
                    Map<QName, PropertyDefinition> propDefs = rmAdminService.getCustomPropertyDefinitions(ASPECT_RECORD);
                    assertNotNull(propDefs);
                    assertTrue(propDefs.containsKey(result));

                    // Check the property definition
                    PropertyDefinition propDef = propDefs.get(result);
                    assertNotNull(propDef);
                    assertEquals(DataTypeDefinition.TEXT, propDef.getDataType().getName());
                    assertEquals("Description", propDef.getDescription(dictionaryService));
                    assertEquals("Label2", propDef.getTitle(dictionaryService));
                }
                finally
                {
                    // Store the created property for cleanup later
                    createdCustomProperties.add(result);
                }
            }
        });

        // Add property to record (long version)
        doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                return rmAdminService.addCustomPropertyDefinition(
                                        null,
                                        ASPECT_RECORD,
                                        "Label3",
                                        DataTypeDefinition.TEXT,
                                        "Title",
                                        "Description",
                                        "default",
                                        false,
                                        false,
                                        false,
                                        null);
            }

            @Override
            public void test(QName result) throws Exception
            {
                try
                {
                    // Check the property QName is correct
                    assertNotNull(result);
                    assertEquals(RecordsManagementCustomModel.RM_CUSTOM_URI, result.getNamespaceURI());
                    assertTrue(rmAdminService.existsCustomProperty(result));

                    // Check that property is available as a custom property
                    Map<QName, PropertyDefinition> propDefs = rmAdminService.getCustomPropertyDefinitions(ASPECT_RECORD);
                    assertNotNull(propDefs);
                    //assertEquals(3, propDefs.size());
                    assertTrue(propDefs.containsKey(result));

                    // Check the property definition
                    PropertyDefinition propDef = propDefs.get(result);
                    assertNotNull(propDef);
                    assertEquals(DataTypeDefinition.TEXT, propDef.getDataType().getName());
                    assertEquals("Description", propDef.getDescription(dictionaryService));
                    assertEquals("Label3", propDef.getTitle(dictionaryService));
                    assertEquals("default", propDef.getDefaultValue());
                    assertFalse(propDef.isMandatory());
                    assertFalse(propDef.isMultiValued());
                    assertFalse(propDef.isProtected());

                }
                finally
                {
                    // Store the created property for cleanup later
                    createdCustomProperties.add(result);
                }
            }
        });

        // Failure: Add a property with the same name twice
        doTestInTransaction(new FailureTest
        (
            "Can not create a property with the same id twice",
            CustomMetadataException.class
        )
        {
            @Override
            public void run() throws Exception
            {
                rmAdminService.addCustomPropertyDefinition(
                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myRecordProp1"),
                        ASPECT_RECORD,
                        "Label1",
                        DataTypeDefinition.TEXT,
                        "Title",
                        "Description");
            }
        });

        // Failure: Try and add a property to a type that isn't customisable
        doTestInTransaction(new FailureTest
        (
            "Can not add a custom property to a type that isn't registered as customisable",
            CustomMetadataException.class
        )
        {
            @Override
            public void run() throws Exception
            {
                rmAdminService.addCustomPropertyDefinition(
                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myContentProp"),
                        TYPE_CONTENT,
                        "Label1",
                        DataTypeDefinition.TEXT,
                        "Title",
                        "Description");
            }
        });
    }

    /**
     * @see RecordsManagementAdminService#makeCustomisable(QName)
     */
    public void testMakeCustomisable() throws Exception
    {
        doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                // Make a type customisable
                assertFalse(rmAdminService.isCustomisable(TYPE_CUSTOM_TYPE));
                rmAdminService.makeCustomisable(TYPE_CUSTOM_TYPE);
                madeCustomisable.add(TYPE_CUSTOM_TYPE);
                assertTrue(rmAdminService.isCustomisable(TYPE_CUSTOM_TYPE));

                // Add a custom property
                return rmAdminService.addCustomPropertyDefinition(
                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myNewProperty"),
                        TYPE_CUSTOM_TYPE,
                        "Label",
                        DataTypeDefinition.TEXT,
                        "Title",
                        "Description");
            }

            @Override
            public void test(QName result) throws Exception
            {
                // Check the property QName is correct
                assertNotNull(result);
                assertEquals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myNewProperty"), result);
                assertTrue(rmAdminService.existsCustomProperty(result));

                // Check that property is available as a custom property
                Map<QName, PropertyDefinition> propDefs = rmAdminService.getCustomPropertyDefinitions(TYPE_CUSTOM_TYPE);
                assertNotNull(propDefs);
                assertEquals(1, propDefs.size());
                assertTrue(propDefs.containsKey(result));

                // Check the property definition
                PropertyDefinition propDef = propDefs.get(result);
                assertNotNull(propDef);
                assertEquals(DataTypeDefinition.TEXT, propDef.getDataType().getName());
                assertEquals("Description", propDef.getDescription(dictionaryService));
                assertEquals("Label", propDef.getTitle(dictionaryService));

            }
        });

    	doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                // Make an aspect customisable
                assertFalse(rmAdminService.isCustomisable(ASPECT_CUSTOM_ASPECT));
                rmAdminService.makeCustomisable(ASPECT_CUSTOM_ASPECT);
                madeCustomisable.add(ASPECT_CUSTOM_ASPECT);
                assertTrue(rmAdminService.isCustomisable(ASPECT_CUSTOM_ASPECT));

                // Add a custom property
                return rmAdminService.addCustomPropertyDefinition(
                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myNewAspectProperty"),
                        ASPECT_CUSTOM_ASPECT,
                        "Label",
                        DataTypeDefinition.TEXT,
                        "Title",
                        "Description");
            }

            @Override
            public void test(QName result) throws Exception
            {
                // Check the property QName is correct
                assertNotNull(result);
                assertEquals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myNewAspectProperty"), result);
                assertTrue(rmAdminService.existsCustomProperty(result));

                // Check that property is available as a custom property
                Map<QName, PropertyDefinition> propDefs = rmAdminService.getCustomPropertyDefinitions(ASPECT_CUSTOM_ASPECT);
                assertNotNull(propDefs);
                assertEquals(1, propDefs.size());
                assertTrue(propDefs.containsKey(result));

                // Check the property definition
                PropertyDefinition propDef = propDefs.get(result);
                assertNotNull(propDef);
                assertEquals(DataTypeDefinition.TEXT, propDef.getDataType().getName());
                assertEquals("Description", propDef.getDescription(dictionaryService));
                assertEquals("Label", propDef.getTitle(dictionaryService));
            }
        });
    }

    public void testUseCustomProperty() throws Exception
    {
        // Create custom property on type and aspect
        doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                rmAdminService.makeCustomisable(TYPE_CUSTOM_TYPE);
                madeCustomisable.add(TYPE_CUSTOM_TYPE);
                rmAdminService.addCustomPropertyDefinition(
                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myNewProperty"),
                        TYPE_CUSTOM_TYPE,
                        "Label",
                        DataTypeDefinition.TEXT,
                        "Title",
                        "Description");
                rmAdminService.makeCustomisable(ASPECT_CUSTOM_ASPECT);
                madeCustomisable.add(ASPECT_CUSTOM_ASPECT);
                rmAdminService.addCustomPropertyDefinition(
                        QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "myNewAspectProperty"),
                        ASPECT_CUSTOM_ASPECT,
                        "Label",
                        DataTypeDefinition.TEXT,
                        "Title",
                        "Description");

                return null;
            }
        });

        // Create nodes using custom type and aspect
        doTestInTransaction(new Test<QName>()
        {
            @Override
            public QName run() throws Exception
            {
                NodeRef customInstance1 = nodeService.createNode(
                            folder,
                            ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myCustomInstance1"),
                            TYPE_CUSTOM_TYPE).getChildRef();
                NodeRef customInstance2 = nodeService.createNode(
                            folder,
                            ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myCustomInstance2"),
                            TYPE_CONTENT).getChildRef();
                nodeService.addAspect(customInstance2, ASPECT_CUSTOM_ASPECT, null);

                // Assert that both instances have the custom aspects applied
                assertTrue(nodeService.hasAspect(customInstance1, QName.createQName(RM_CUSTOM_URI, "rmtcustomTypeCustomProperties")));
                assertTrue(nodeService.hasAspect(customInstance2, QName.createQName(RM_CUSTOM_URI, "rmtcustomAspectCustomProperties")));

                // Remove the custom aspect
                nodeService.removeAspect(customInstance2, ASPECT_CUSTOM_ASPECT);

                // Assert the custom property aspect is no longer applied applied
                assertTrue(nodeService.hasAspect(customInstance1, QName.createQName(RM_CUSTOM_URI, "rmtcustomTypeCustomProperties")));
                assertFalse(nodeService.hasAspect(customInstance2, QName.createQName(RM_CUSTOM_URI, "rmtcustomAspectCustomProperties")));

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }


    public void testCreateAndUseCustomChildReference() throws Exception
    {
        long now = System.currentTimeMillis();
        createAndUseCustomReference(RelationshipType.PARENTCHILD, null, "superseded" + now, "superseding" + now);
    }

    public void testCreateAndUseCustomNonChildReference() throws Exception
    {
        long now = System.currentTimeMillis();
        createAndUseCustomReference(RelationshipType.BIDIRECTIONAL, "supporting" + now, null, null);
    }

    private void createAndUseCustomReference(final RelationshipType refType, final String label, final String source, final String target) throws Exception
    {
        final NodeRef testRecord1 = retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                NodeRef result = utils.createRecord(rmFolder, "testRecordA" + System.currentTimeMillis());
                return result;
            }
        });
        final NodeRef testRecord2 = retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                NodeRef result = utils.createRecord(rmFolder, "testRecordB" + System.currentTimeMillis());
                return result;
            }
        });

        final QName generatedQName = retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<QName>()
                {
                    public QName execute() throws Throwable
                    {
                        utils.completeRecord(testRecord1);
                        utils.completeRecord(testRecord2);

                        Map <String, Serializable> params = new HashMap<>();
                        params.put("referenceType", refType.toString());
                        if (label != null) params.put("label", label);
                        if (source != null) params.put("source", source);
                        if (target != null) params.put("target", target);

                        // Create the relationship display name
                        RelationshipDisplayName displayName;
                        if (label != null)
                        {
                            // A bidirectional reference
                            displayName = new RelationshipDisplayName(label, label);
                        }
                        else
                        {
                            // A parent/child reference
                            displayName = new RelationshipDisplayName(source, target);
                        }

                        // Create the relationship definition
                        RelationshipDefinition relationshipDefinition = relationshipService.createRelationshipDefinition(displayName);

                        // Get the qualified name
                        QName qNameResult = QName.createQName(RM_CUSTOM_PREFIX, relationshipDefinition.getUniqueName(), namespaceService);

                        System.out.println("Creating new " + refType + " reference definition: " + qNameResult);
                        System.out.println("  params- label: '" + label + "' source: '" + source + "' target: '" + target + "'");

                        return qNameResult;
                    }
                });

        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        RelationshipDefinition relationshipDefinition = relationshipService.getRelationshipDefinition(generatedQName.getLocalName());
                        assertNotNull("Relationship definition from relationshipService was null.", relationshipDefinition);
                        assertEquals(generatedQName.getLocalName(), relationshipDefinition.getUniqueName());
                        assertTrue(refType.equals(relationshipDefinition.getType()));

                        // Now we need to use the custom reference.
                        // So we apply the aspect containing it to our test records.
                        nodeService.addAspect(testRecord1, ASPECT_CUSTOM_ASSOCIATIONS, null);

                        if (RelationshipType.PARENTCHILD.equals(refType))
                        {
                            nodeService.addChild(testRecord1, testRecord2, generatedQName, generatedQName);
                        }
                        else
                        {
                            nodeService.createAssociation(testRecord1, testRecord2, generatedQName);
                        }
                        return null;
                    }
                });

        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Read back the reference value to make sure it was correctly applied.
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(testRecord1);
                        List<AssociationRef> retrievedAssocs = nodeService.getTargetAssocs(testRecord1, RegexQNamePattern.MATCH_ALL);

                        Object newlyAddedRef = null;
                        if (RelationshipType.PARENTCHILD.equals(refType))
                        {
                            for (ChildAssociationRef caRef : childAssocs)
                            {
                                QName refInstanceQName = caRef.getQName();
                                if (generatedQName.equals(refInstanceQName)) newlyAddedRef = caRef;
                            }
                        }
                        else
                        {
                            for (AssociationRef aRef : retrievedAssocs)
                            {
                                QName refQName = aRef.getTypeQName();
                                if (generatedQName.equals(refQName)) newlyAddedRef = aRef;
                            }
                        }
                        assertNotNull("newlyAddedRef was null.", newlyAddedRef);

                        // Check that the reference has appeared in the data dictionary
                        AspectDefinition customAssocsAspect = dictionaryService.getAspect(ASPECT_CUSTOM_ASSOCIATIONS);
                        assertNotNull(customAssocsAspect);
                        if (RelationshipType.PARENTCHILD.equals(refType))
                        {
                            assertNotNull("The customReference is not returned from the dictionaryService.",
                                    customAssocsAspect.getChildAssociations().get(generatedQName));
                        }
                        else
                        {
                            assertNotNull("The customReference is not returned from the dictionaryService.",
                                    customAssocsAspect.getAssociations().get(generatedQName));
                        }
                        return null;
                    }
                });
	}

    public void testGetAllProperties()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Just dump them out for visual inspection
                        System.out.println("Available custom properties:");
                        Map<QName, PropertyDefinition> props = rmAdminService.getCustomPropertyDefinitions();
                        for (QName prop : props.keySet())
                        {
                            System.out.println("   - " + prop.toString());

                            String propId = props.get(prop).getTitle(dictionaryService);
                            assertNotNull("null client-id for " + prop, propId);

                            System.out.println("       " + propId);
                        }
                        return null;
                    }
                });
    }

    public void testGetAllReferences()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Just dump them out for visual inspection
                        System.out.println("Available relationship definitions:");
                        Set<RelationshipDefinition> relationshipDefinitions = relationshipService.getRelationshipDefinitions();
                        for (RelationshipDefinition relationshipDefinition : relationshipDefinitions)
                        {
                            String uniqueName = relationshipDefinition.getUniqueName();
                            RelationshipDisplayName displayName = relationshipDefinition.getDisplayName();

                            System.out.println("    - " + uniqueName);
                            System.out.println("      " + displayName.toString());
                        }

                        return null;
                    }
                });
    }

    public void testGetAllConstraints()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Just dump them out for visual inspection
                        System.out.println("Available custom constraints:");
                        List<ConstraintDefinition> constraints = rmAdminService.getCustomConstraintDefinitions(RecordsManagementCustomModel.RM_CUSTOM_MODEL);
                        for (ConstraintDefinition constraint : constraints)
                        {
                            System.out.println("   - " + constraint.getName());
                            System.out.println("       " + constraint.getTitle(dictionaryService));
                        }
                        return null;
                    }
                });
    }

	private boolean beforeMarker = false;
    private boolean onMarker = false;
    @SuppressWarnings("unused")
    private boolean inTest = false;

	public void testCreateReference() throws Exception
	{
	    inTest = true;
        try
        {
            // Create the necessary test objects in the db: two records.
            final Pair<NodeRef, NodeRef> testRecords = retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Pair<NodeRef, NodeRef>>()
                    {
                        public Pair<NodeRef, NodeRef> execute() throws Throwable
                        {
                            NodeRef rec1 = utils.createRecord(rmFolder, "testRecordA" + System.currentTimeMillis());
                            NodeRef rec2 = utils.createRecord(rmFolder, "testRecordB" + System.currentTimeMillis());
                            Pair<NodeRef, NodeRef> result = new Pair<>(rec1, rec2);
                            return result;
                        }
                    });
            final NodeRef testRecord1 = testRecords.getFirst();
            final NodeRef testRecord2 = testRecords.getSecond();

            retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            utils.completeRecord(testRecord1);
                            utils.completeRecord(testRecord2);

                            policyComponent.bindClassBehaviour(
                                    RecordsManagementPolicies.BEFORE_CREATE_REFERENCE,
                                    this,
                                    new JavaBehaviour(RecordsManagementAdminServiceImplTest.this, "beforeCreateReference", NotificationFrequency.EVERY_EVENT));
                            policyComponent.bindClassBehaviour(
                                    RecordsManagementPolicies.ON_CREATE_REFERENCE,
                                    this,
                                    new JavaBehaviour(RecordsManagementAdminServiceImplTest.this, "onCreateReference", NotificationFrequency.EVERY_EVENT));

                            assertFalse(beforeMarker);
                            assertFalse(onMarker);

                            relationshipService.addRelationship(CUSTOM_REF_VERSIONS.getLocalName(), testRecord1, testRecord2);

                            assertTrue(beforeMarker);
                            assertTrue(onMarker);
                            return null;
                        }
                    });
        }
        finally
        {
            inTest = false;
        }
    }

	public void beforeCreateReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        beforeMarker = true;
    }

    public void onCreateReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        onMarker = true;
    }

    public void testCreateCustomConstraints() throws Exception
    {
       final int beforeCnt =
            retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
                {
                    public Integer execute() throws Throwable
                    {
                        List<ConstraintDefinition> result = rmAdminService.getCustomConstraintDefinitions(RecordsManagementCustomModel.RM_CUSTOM_MODEL);
                        assertNotNull(result);
                        return result.size();
                    }
                });

        final String conTitle = "test title - "+testRunID;
        final List<String> allowedValues = new ArrayList<>(3);
        allowedValues.add("RED");
        allowedValues.add("AMBER");
        allowedValues.add("GREEN");

        final QName testCon = retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<QName>()
                {
                    public QName execute() throws Throwable
                    {
                        String conLocalName = "test-"+testRunID;

                        final QName result = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, conLocalName);

                        rmAdminService.addCustomConstraintDefinition(result, conTitle, true, allowedValues, MatchLogic.AND);
                        return result;
                    }
                });


        // Set the current security context as System - to see allowed values (unless caveat config is also updated for admin)
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<ConstraintDefinition> customConstraintDefs = rmAdminService.getCustomConstraintDefinitions(RecordsManagementCustomModel.RM_CUSTOM_MODEL);
                        assertEquals(beforeCnt+1, customConstraintDefs.size());

                        boolean found = false;
                        for (ConstraintDefinition conDef : customConstraintDefs)
                        {
                            if (conDef.getName().equals(testCon))
                            {
                                assertEquals(conTitle, conDef.getTitle(dictionaryService));

                                Constraint con = conDef.getConstraint();
                                assertTrue(con instanceof RMListOfValuesConstraint);

                                assertEquals("LIST", ((RMListOfValuesConstraint)con).getType());
                                assertEquals(3, ((RMListOfValuesConstraint)con).getAllowedValues().size());

                                found = true;
                                break;
                            }
                        }
                        assertTrue(found);
                        return null;
                    }
                });


        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        allowedValues.clear();
                        allowedValues.add("RED");
                        allowedValues.add("YELLOW");

                        rmAdminService.changeCustomConstraintValues(testCon, allowedValues);
                        return null;
                    }
                });

        // Set the current security context as System - to see allowed values (unless caveat config is also updated for admin)
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<ConstraintDefinition> customConstraintDefs = rmAdminService.getCustomConstraintDefinitions(RecordsManagementCustomModel.RM_CUSTOM_MODEL);
                        assertEquals(beforeCnt+1, customConstraintDefs.size());

                        boolean found = false;
                        for (ConstraintDefinition conDef : customConstraintDefs)
                        {
                            if (conDef.getName().equals(testCon))
                            {
                                assertEquals(conTitle, conDef.getTitle(dictionaryService));

                                Constraint con = conDef.getConstraint();
                                assertTrue(con instanceof RMListOfValuesConstraint);

                                assertEquals("LIST", ((RMListOfValuesConstraint)con).getType());
                                assertEquals(2, ((RMListOfValuesConstraint)con).getAllowedValues().size());

                                found = true;
                                break;
                            }
                        }
                        assertTrue(found);
                        return null;
                    }
                });


        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Add custom property to record with test constraint
        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                String propLocalName = "myProp-"+testRunID;

                QName dataType = DataTypeDefinition.TEXT;
                String propTitle = "My property title";
                String description = "My property description";
                String defaultValue = null;
                boolean multiValued = false;
                boolean mandatory = false;
                boolean isProtected = false;

                QName propName = rmAdminService.addCustomPropertyDefinition(null, ASPECT_RECORD, propLocalName, dataType, propTitle, description, defaultValue, multiValued, mandatory, isProtected, testCon);
                createdCustomProperties.add(propName);
                return null;
            }
        });
    }
}
