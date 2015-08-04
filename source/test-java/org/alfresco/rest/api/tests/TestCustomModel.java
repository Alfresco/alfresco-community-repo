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

package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Tests the REST API of the models of the {@link CustomModelService}.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class TestCustomModel extends BaseCustomModelApiTest
{

    @Test
    public void testCreateBasicModel() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();

        CustomModel customModel = new CustomModel();
        customModel.setName(modelName);
        customModel.setNamespaceUri(namespacePair.getFirst());
        customModel.setNamespacePrefix(namespacePair.getSecond());
        customModel.setDescription("Test model description");
        customModel.setStatus(CustomModel.ModelStatus.DRAFT);

        // Try to create the model as a non Admin user
        post("cmm", nonAdminUserName, RestApiUtil.toJsonAsString(customModel), 403);

        // Create the model as a Model Administrator
        post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 201);

        // Retrieve the created model
        HttpResponse response = getSingle("cmm", customModelAdmin, modelName, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        // Check the retrieved model is the expected model.
        // Note: since we didn't specify the Author when created the Model,
        // we have to exclude it from the objects comparison. Because,
        // the system will add the current authenticated user as the author
        // of the model, if the Author hasn't been set.
        compareCustomModels(customModel, returnedModel, "author");
    }

    @Test
    public void testCreateBasicModel_Invalid() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();

        CustomModel customModel = new CustomModel();
        customModel.setName(modelName);
        customModel.setNamespaceUri(namespacePair.getFirst());
        customModel.setNamespacePrefix(namespacePair.getSecond());

        // Test invalid inputs
        {
            customModel.setName(modelName + "<script>alert('oops')</script>");
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400);

            customModel.setName("prefix:" + modelName);
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // Invalid name. Contains ':'

            customModel.setName("prefix " + modelName);
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // Invalid name. Contains space

            customModel.setName(modelName);
            customModel.setNamespacePrefix(namespacePair.getSecond()+" space");
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // Invalid prefix. Contains space

            customModel.setNamespacePrefix(namespacePair.getSecond()+"invalid/");
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // Invalid prefix. Contains '/'

            customModel.setNamespacePrefix(namespacePair.getSecond());
            customModel.setNamespaceUri(namespacePair.getFirst()+" space");
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // Invalid URI. Contains space

            customModel.setNamespaceUri(namespacePair.getFirst()+"\\");
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // Invalid URI. Contains '\'
        }

        // Test mandatory properties of the model
        {
            customModel.setName("");
            customModel.setNamespacePrefix(namespacePair.getSecond());
            customModel.setNamespaceUri(namespacePair.getFirst());
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // name is mandatory

            customModel.setName(modelName);
            customModel.setNamespaceUri(null);
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // namespaceUri is mandatory

            customModel.setName(modelName);
            customModel.setNamespaceUri(namespacePair.getFirst());
            customModel.setNamespacePrefix(null);
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 400); // namespacePrefix is mandatory
        }

        // Test duplicate model name
        {
            // Test create a model with the same name as the bootstrapped model
            customModel.setName("contentmodel");
            customModel.setNamespaceUri(namespacePair.getFirst());
            customModel.setNamespacePrefix(namespacePair.getSecond());
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 409);

            // Create the model
            customModel.setName(modelName);
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 201);

            // Create a duplicate model
            // Set a new namespace to make sure the 409 status code is returned
            // because of a name conflict rather than namespace URI
            namespacePair = getTestNamespaceUriPrefixPair();
            customModel.setNamespaceUri(namespacePair.getFirst());
            customModel.setNamespacePrefix(namespacePair.getSecond());
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 409);
        }

        // Test duplicate namespaceUri
        {
            String modelNameTwo = "testModelTwo" + System.currentTimeMillis();
            Pair<String, String> namespacePairTwo = getTestNamespaceUriPrefixPair();

            CustomModel customModelTwo = new CustomModel();
            customModelTwo.setName(modelNameTwo);
            customModelTwo.setNamespaceUri(namespacePairTwo.getFirst());
            customModelTwo.setNamespacePrefix(namespacePairTwo.getSecond());
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModelTwo), 201);

            String modelNameThree = "testModelThree" + System.currentTimeMillis();
            Pair<String, String> namespacePairThree = getTestNamespaceUriPrefixPair();
            CustomModel customModelThree = new CustomModel();
            customModelThree.setName(modelNameThree);
            customModelThree.setNamespaceUri(namespacePairTwo.getFirst()); // duplicate URI
            customModelThree.setNamespacePrefix(namespacePairThree.getSecond());

            // Try to create a model with a namespace uri which has already been used.
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModelThree), 409);
            
            customModelThree.setNamespaceUri(namespacePairThree.getFirst());
            customModelThree.setNamespacePrefix(namespacePairTwo.getSecond()); // duplicate prefix

            // Try to create a model with a namespace prefix which has already been used.
            post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModelThree), 409);
        }
    }

    @Test
    public void testListBasicModels() throws Exception
    {
        String modelName_1 = "testModel1" + System.currentTimeMillis();
        // Create the model as a Model Administrator
        CustomModel customModel_1 = createCustomModel(modelName_1, getTestNamespaceUriPrefixPair(), ModelStatus.DRAFT);

        String modelName_2 = "testModel2" + System.currentTimeMillis();
        CustomModel customModel_2 = createCustomModel(modelName_2, getTestNamespaceUriPrefixPair(), ModelStatus.DRAFT);

        String modelName_3 = "testModel3" + System.currentTimeMillis();
        CustomModel customModel_3 = createCustomModel(modelName_3, getTestNamespaceUriPrefixPair(), ModelStatus.DRAFT);
 
        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll("cmm", customModelAdmin, paging, 200);
        List<CustomModel> models = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModel.class);

        assertTrue(models.size() >= 3);
        assertTrue(models.contains(customModel_1));
        assertTrue(models.contains(customModel_2));
        assertTrue(models.contains(customModel_3));
    }

    @Test
    public void testActivateCustomModel() throws Exception
    {
        String modelNameOne = "testActivateModelOne" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        CustomModel customModelOne = createCustomModel(modelNameOne, namespacePair, ModelStatus.DRAFT, "Test model description", "Jane Doe");

        // Retrieve the created model and check its status (the default is DRAFT)
        HttpResponse response = getSingle("cmm", customModelAdmin, modelNameOne, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.DRAFT, returnedModel.getStatus());

        // We only want to update the status, so ignore the other properties
        CustomModel updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.ACTIVE);

        // Try to activate the model as a non Admin user
        put("cmm", nonAdminUserName, modelNameOne, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 403);

        // Activate the model as a Model Administrator
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

        response = getSingle("cmm", customModelAdmin, modelNameOne, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.ACTIVE, returnedModel.getStatus());
        // Check other properties have not been modified
        compareCustomModels(customModelOne, returnedModel, "status");

        // Try to activate the already activated model as a Model Administrator
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 500);

        // Create another Model
        String modelNameTwo = "testActivateModelTwo" + System.currentTimeMillis();
        Pair<String, String> namespacePairTwo = getTestNamespaceUriPrefixPair();
        CustomModel customModelTwo = createCustomModel(modelNameTwo, namespacePairTwo, ModelStatus.DRAFT, null, "John Doe");

        // Activate the model as a Model Administrator
        customModelTwo.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameTwo, RestApiUtil.toJsonAsString(customModelTwo), SELECT_STATUS_QS, 200);

        response = getSingle("cmm", customModelAdmin, modelNameTwo, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.ACTIVE, returnedModel.getStatus());
        // Check other properties have not been modified
        compareCustomModels(customModelTwo, returnedModel, "status");
    }

    @Test
    public void testDeactivateCustomModel() throws Exception
    {
        String modelNameOne = "testDeactivateModelOne" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
         // Create the model as a Model Administrator
        CustomModel customModelOne =  createCustomModel(modelNameOne, namespacePair, ModelStatus.ACTIVE, null, "Mark Moe");

        // Retrieve the created model and check its status
        HttpResponse response = getSingle("cmm", customModelAdmin, modelNameOne, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.ACTIVE, returnedModel.getStatus());

        // We only want to update the status (Deactivate), so ignore the other properties
        CustomModel updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.DRAFT);

        // Try to deactivate the model as a non Admin user
        put("cmm", nonAdminUserName, modelNameOne, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 403);

        // Deactivate the model as a Model Administrator
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

        response = getSingle("cmm", customModelAdmin, modelNameOne, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.DRAFT, returnedModel.getStatus());
        // Check other properties have not been modified
        compareCustomModels(customModelOne, returnedModel, "status");

        // Try to deactivate the already deactivated model as a Model Administrator
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 500);

        // Activate/Deactivate a model with an aspect 
        {
            // Create another Model
            final String modelNameTwo = "testDeactivateModelTwo" + System.currentTimeMillis();
            Pair<String, String> namespacePairTwo = getTestNamespaceUriPrefixPair();
            CustomModel customModelTwo = createCustomModel(modelNameTwo, namespacePairTwo, ModelStatus.DRAFT, null, "Mark Moe");

            // Aspect
            CustomAspect aspect = new CustomAspect();
            aspect.setName("testMarkerAspect");
            post("cmm/" + modelNameTwo + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 201);
            // Retrieve the created aspect
            getSingle("cmm/" + modelNameTwo + "/aspects", customModelAdmin, aspect.getName(), 200);

            // Activate the model as a Model Administrator
            customModelTwo.setStatus(ModelStatus.ACTIVE);
            put("cmm", customModelAdmin, modelNameTwo, RestApiUtil.toJsonAsString(customModelTwo), SELECT_STATUS_QS, 200);

            response = getSingle("cmm", customModelAdmin, modelNameTwo, 200);
            returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
            assertEquals(ModelStatus.ACTIVE, returnedModel.getStatus());

            updatePayload = new CustomModel();
            updatePayload.setStatus(ModelStatus.DRAFT);
            // Deactivate the model as a Model Administrator
            put("cmm", customModelAdmin, modelNameTwo, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

            response = getSingle("cmm", customModelAdmin, modelNameTwo, 200);
            returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
            assertEquals(ModelStatus.DRAFT, returnedModel.getStatus());
        }
    }

    @Test
    public void testDeleteCustomModel() throws Exception
    {
        String modelName = "testDeleteModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        CustomModel customModel = createCustomModel(modelName, namespacePair, ModelStatus.DRAFT, null, "Joe Bloggs");

        // Retrieve the created model
        HttpResponse response = getSingle("cmm", customModelAdmin, modelName, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        compareCustomModels(customModel, returnedModel);

        // Try to delete the model as a non Admin user
        delete("cmm", nonAdminUserName, modelName, 403);

        // Delete the model as a Model Administrator
        delete("cmm", customModelAdmin, modelName, 204);

        // Create the model again
        post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 201);
        
        // Activated the model
        CustomModel updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);
        
        // Try to delete the active model
        delete("cmm", customModelAdmin, modelName, 409);
        
        // Deactivate and then delete the model
        updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.DRAFT);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);
        delete("cmm", customModelAdmin, modelName, 204);
    }

    @Test
    public void testUpdateBasicModel() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT, "Test model description", null);

        //Test update name
        CustomModel updatePayload = new CustomModel();
        String newName = modelName + "Modified";
        updatePayload.setName(newName);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 400); // Cannot update the model name

        // Test update the namespace URI (already in-use)
        updatePayload = new CustomModel();
        updatePayload.setNamespaceUri("http://www.alfresco.org/model/content/1.0");
        updatePayload.setNamespacePrefix("newPrefix");
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 409); // The namespace uri has already been used

        // Test update the namespace Prefix (already in-use)
        updatePayload = new CustomModel();
        updatePayload.setNamespaceUri(getTestNamespaceUriPrefixPair().getFirst());
        updatePayload.setNamespacePrefix("cm");
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 409); // The namespace prefix has already been used

        // Test update the namespace URI (without sending the namespace prefix)
        updatePayload = new CustomModel();
        updatePayload.setNamespaceUri(getTestNamespaceUriPrefixPair().getFirst());
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 400); // The namespace prefix is mandatory

        // Test update the namespace URI only
        updatePayload = new CustomModel();
        updatePayload.setNamespacePrefix( namespacePair.getSecond());
        Pair<String, String> newURI = getTestNamespaceUriPrefixPair();
        updatePayload.setNamespaceUri(newURI.getFirst());
        HttpResponse response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(newURI.getFirst(), returnedModel.getNamespaceUri());
        assertEquals("The namespace prefix shouldn't have changed.", namespacePair.getSecond(), returnedModel.getNamespacePrefix());

        // Test update the namespace prefix (without sending the namespace URI)
        updatePayload = new CustomModel();
        updatePayload.setNamespacePrefix("newPrefix");
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 400); // The namespce uri is mandatory
        
        // Test update the namespace prefix only
        updatePayload = new CustomModel();
        updatePayload.setNamespaceUri(namespacePair.getFirst());
        Pair<String, String> newPrefix = getTestNamespaceUriPrefixPair();
        updatePayload.setNamespacePrefix( newPrefix.getSecond());
        response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(newPrefix.getSecond(), returnedModel.getNamespacePrefix());
        assertEquals("The namespace URI shouldn't have changed.", namespacePair.getFirst(), returnedModel.getNamespaceUri());

        // Test a valid update
        updatePayload = new CustomModel();
        Pair<String, String> newNamespacePair = getTestNamespaceUriPrefixPair();
        updatePayload.setNamespaceUri(newNamespacePair.getFirst());
        updatePayload.setNamespacePrefix(newNamespacePair.getSecond());
        updatePayload.setDescription("Test model description Modified");
        updatePayload.setAuthor("John Moe");
        updatePayload.setStatus(ModelStatus.ACTIVE); // This should be ignored

        // Try to update the model as a non Admin user
        put("cmm", nonAdminUserName, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 403);
        // Update the model as a Model Administrator
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 200);

        // Retrieve the updated model
        response = getSingle("cmm", customModelAdmin, modelName, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        compareCustomModels(updatePayload, returnedModel, "name", "status");
        assertEquals("The model status should only be updated via '?select=status' request.", ModelStatus.DRAFT, returnedModel.getStatus());

        // Activate the model as a Model Administrator
        updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.ACTIVE);
        response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.ACTIVE, returnedModel.getStatus());

        // Try to update the ACTIVE model's namespace URI
        updatePayload = new CustomModel();
        newNamespacePair = getTestNamespaceUriPrefixPair();
        updatePayload.setNamespaceUri(newNamespacePair.getFirst());
        updatePayload.setNamespacePrefix(returnedModel.getNamespacePrefix());
        // Cannot update the namespace uri and/or namespace prefix when the model is Active.
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 409);

        // Try to update the ACTIVE model's namespace Prefix
        updatePayload = new CustomModel();
        updatePayload.setNamespaceUri(returnedModel.getNamespaceUri());
        updatePayload.setNamespacePrefix("myNewPrefix");
        // Cannot update the namespace uri and/or namespace prefix when the model is Active.
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 409);

        // Test a valid update of an Active model (you can only update desc and author)
        updatePayload = new CustomModel();
        updatePayload.setNamespaceUri(returnedModel.getNamespaceUri());
        updatePayload.setNamespacePrefix(returnedModel.getNamespacePrefix());
        updatePayload.setDescription("Test modifying active model description");
        updatePayload.setAuthor("Mark Miller");
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 200);

        // Retrieve the updated active model
        response = getSingle("cmm", customModelAdmin, modelName, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        compareCustomModels(updatePayload, returnedModel, "name", "status");
    }

    @Test
    //SHA-726
    public void testUpdateModel_WithAspectsAndTypes() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Add type
        String typeBaseName = "testTypeBase" + System.currentTimeMillis();
        final String typeBaseNameWithPrefix = namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeBaseName;
        createTypeAspect(CustomType.class, modelName, typeBaseName, "test typeBase title", null, "cm:content");

        // Add aspect
        final String aspectName = "testAspect" + System.currentTimeMillis();
        final String aspectNameWithPrefix = namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName;
        createTypeAspect(CustomAspect.class, modelName, aspectName, null, null, null);

        // Activate the model
        CustomModel modelOneStatusPayload = new CustomModel();
        modelOneStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 200);

        // Add another type with 'typeBaseName' as its parent
        String childTypeName = "testTypeChild" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelName, childTypeName, "test typeChild title", "test typeChild Desc", typeBaseNameWithPrefix);

        // Add another aspect with 'aspectName' as its parent
        final String childAspectName = "testChildAspect" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, childAspectName, "test child aspect title", null, aspectNameWithPrefix);

         // Deactivate the model
        modelOneStatusPayload = new CustomModel();
        modelOneStatusPayload.setStatus(ModelStatus.DRAFT);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 200);

        // Test update the namespace prefix
        CustomModel updatePayload = new CustomModel();
        String modifiedPrefix = namespacePair.getSecond() + "Modified";
        updatePayload.setNamespacePrefix(modifiedPrefix);
        updatePayload.setNamespaceUri(namespacePair.getFirst());
        HttpResponse response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(modifiedPrefix, returnedModel.getNamespacePrefix());
        assertEquals("The namespace URI shouldn't have changed.", namespacePair.getFirst(), returnedModel.getNamespaceUri());

        // Test update the namespace URI
        updatePayload = new CustomModel();
        updatePayload.setNamespacePrefix(modifiedPrefix);
        String modifiedURI = namespacePair.getFirst() + "Modified";
        updatePayload.setNamespaceUri(modifiedURI);
        response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), null, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(modifiedURI, returnedModel.getNamespaceUri());
        assertEquals("The namespace prefix shouldn't have changed.", modifiedPrefix, returnedModel.getNamespacePrefix());

        // Retrieve the child type
        response = getSingle("cmm/" + modelName + "/types", customModelAdmin, childTypeName,  200);
        CustomType returnedChildType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
        final String newTypeParentName = modifiedPrefix + QName.NAMESPACE_PREFIX + typeBaseName;
        assertEquals("The parent name prefix should have been updated.", newTypeParentName, returnedChildType.getParentName());

        // Retrieve the child aspect
        response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, childAspectName,  200);
        CustomAspect returnedChildAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
        final String newAspectParentName = modifiedPrefix + QName.NAMESPACE_PREFIX + aspectName;
        assertEquals("The parent name prefix should have been updated.", newAspectParentName, returnedChildAspect.getParentName());
    }

    // SHA-808
    @Test
    public void testModelsCircularDependency() throws Exception
    {
        // Model One
        String modelNameOne = "testModelOne" + System.currentTimeMillis();
        Pair<String, String> namespacePairOne = getTestNamespaceUriPrefixPair();
        // Create the modelOne as a Model Administrator
        createCustomModel(modelNameOne, namespacePairOne, ModelStatus.DRAFT);

        // Add typeA_M1 into modelOne
        String typeA_M1 = "testTypeA_M1" + System.currentTimeMillis();
        final String typeA_M1_WithPrefix = namespacePairOne.getSecond() + QName.NAMESPACE_PREFIX + typeA_M1;
        createTypeAspect(CustomType.class, modelNameOne, typeA_M1, "test typeA_M1 title", null, "cm:content");

        // Activate modelOne
        CustomModel modelOneStatusPayload = new CustomModel();
        modelOneStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 200);

        // Add another type into modelOne with 'typeA_M1' as its parent
        String typeB_M1 = "testTypeB_M1" + System.currentTimeMillis();
        final String typeB_M1_WithPrefix = namespacePairOne.getSecond() + QName.NAMESPACE_PREFIX + typeB_M1;
        createTypeAspect(CustomType.class, modelNameOne, typeB_M1, "test typeB_M1 title", "test typeB_M1 Desc", typeA_M1_WithPrefix);

        // Model Two
        String modelNameTwo = "testModelTwo" + System.currentTimeMillis();
        Pair<String, String> namespacePairTwo = getTestNamespaceUriPrefixPair();
        // Create the modelTwo as a Model Administrator
        createCustomModel(modelNameTwo, namespacePairTwo, ModelStatus.DRAFT);

        // Add type1_M2 into modelTwo with 'typeB_M1' (from modelOne) as its parent
        String type1_M2 = "testType1_M2" + System.currentTimeMillis();
        final String type1_M2_WithPrefix = namespacePairTwo.getSecond() + QName.NAMESPACE_PREFIX + type1_M2;
        createTypeAspect(CustomType.class, modelNameTwo, type1_M2, "test type1_M2 title", null, typeB_M1_WithPrefix );

        // Activate modelTwo
        CustomModel modelTwoStatusPayload = new CustomModel();
        modelTwoStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameTwo, RestApiUtil.toJsonAsString(modelTwoStatusPayload), SELECT_STATUS_QS, 200);

        // Test that the API can handle "circular dependency" - (modelOne depends on modelTwo)
        {
            // Add another type into modelOne with 'type1_M2' (from modelTwo) as its parent
            String typeC_M1 = "testTypeC_M1" + System.currentTimeMillis();
            CustomType typeC_M1_Payload = new CustomType();
            typeC_M1_Payload.setName(typeC_M1);
            typeC_M1_Payload.setTitle("test typeC_M1 title");
            typeC_M1_Payload.setParentName(type1_M2_WithPrefix); // => 'type1_M2' (from modelTwo)

            // Try to create typeC_M1 which has 'circular dependency'
            post("cmm/" + modelNameOne + "/types", customModelAdmin, RestApiUtil.toJsonAsString(typeC_M1_Payload), 409); //Constraint violation 
        }

        // Model Three
        String modelNameThree = "testModelThree" + System.currentTimeMillis();
        Pair<String, String> namespacePairThree = getTestNamespaceUriPrefixPair();
        // Create the modelThree as a Model Administrator
        createCustomModel(modelNameThree, namespacePairThree, ModelStatus.DRAFT);

        // Add type1_M3 into modelThree with 'type1_M2' (from modelTwo) as its parent
        String type1_M3 = "testType1_M3" + System.currentTimeMillis();
        final String type1_M3_WithPrefix = namespacePairThree.getSecond() + QName.NAMESPACE_PREFIX + type1_M3;
        createTypeAspect(CustomType.class, modelNameThree, type1_M3, "test type1_M3 title", null, type1_M2_WithPrefix );

        // Activate modelThree
        CustomModel modelThreeStatusPayload = new CustomModel();
        modelThreeStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameThree, RestApiUtil.toJsonAsString(modelThreeStatusPayload), SELECT_STATUS_QS, 200);

        // Test that the API can handle "circular dependency" - (modelOne depends on modelThree)
        {
            // Add another type into modelOne with 'type1_M3' (from modelThree) as its parent
            String typeC_M1 = "testTypeC_M1" + System.currentTimeMillis();
            CustomType typeC_M1_Payload = new CustomType();
            typeC_M1_Payload.setName(typeC_M1);
            typeC_M1_Payload.setTitle("test typeC_M1 title");
            typeC_M1_Payload.setParentName(type1_M3_WithPrefix); // => 'type1_M3' (from modelThree)

            // Try to create typeC_M1 which has 'circular dependency'
            post("cmm/" + modelNameOne + "/types", customModelAdmin, RestApiUtil.toJsonAsString(typeC_M1_Payload), 409); //Constraint violation 
        }

        // Model Three
        String modelNameFour = "testModelFour" + System.currentTimeMillis();
        Pair<String, String> namespacePairFour = getTestNamespaceUriPrefixPair();
        // Create the modelFour as a Model Administrator
        createCustomModel(modelNameFour, namespacePairFour, ModelStatus.DRAFT);

        // Add type1_M4 into modelFour with 'type1_M3' (from modelThree) as its parent
        String type1_M4 = "testType1_M4" + System.currentTimeMillis();
        final String type1_M4_WithPrefix = namespacePairFour.getSecond() + QName.NAMESPACE_PREFIX + type1_M4;
        createTypeAspect(CustomType.class, modelNameFour, type1_M4, "test type1_M4 title", null, type1_M3_WithPrefix );

        // Activate modelFour
        CustomModel modelFourStatusPayload = new CustomModel();
        modelFourStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameFour, RestApiUtil.toJsonAsString(modelFourStatusPayload), SELECT_STATUS_QS, 200);

        // Test that the API can handle "circular dependency" - (modelOne depends on modelFour)
        {
            // Add another type into modelOne with 'type1_M4' (from modelFour) as its parent
            String typeC_M1 = "testTypeC_M1" + System.currentTimeMillis();
            CustomType typeC_M1_Payload = new CustomType();
            typeC_M1_Payload.setName(typeC_M1);
            typeC_M1_Payload.setTitle("test typeC_M1 title");
            typeC_M1_Payload.setParentName(type1_M4_WithPrefix); // => 'type1_M4' (from modelFour)

            // Try to create typeC_M1 which has 'circular dependency'
            post("cmm/" + modelNameOne + "/types", customModelAdmin, RestApiUtil.toJsonAsString(typeC_M1_Payload), 409); //Constraint violation 
        }

        // Test that the API can handle "circular dependency" - (modelTwo depends on modelFour)
        {
            // Add another type into modelTwo with 'type1_M4' (from modelFour) as its parent
            String type2_M2 = "testType2_M2" + System.currentTimeMillis();
            CustomType type2_M2_Payload = new CustomType();
            type2_M2_Payload.setName(type2_M2);
            type2_M2_Payload.setTitle("test type2_M2 title");
            type2_M2_Payload.setParentName(type1_M4_WithPrefix); // => 'type1_M4' (from modelFour)

            // Try to create type2_M2 which has 'circular dependency'
            post("cmm/" + modelNameTwo + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type2_M2_Payload), 409); //Constraint violation 
        }
    }
}
