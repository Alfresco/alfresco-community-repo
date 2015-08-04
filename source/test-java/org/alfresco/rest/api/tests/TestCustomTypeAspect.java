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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModelProperty;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Tests the REST API of the types and aspects of the {@link CustomModelService}.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class TestCustomTypeAspect extends BaseCustomModelApiTest
{

    @Test
    public void testCreateAspectsAndTypes_ExistingModel() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        final String aspectName = "testAspect1" + System.currentTimeMillis();
        {
            // Add aspect
            CustomAspect aspect = new CustomAspect();
            aspect.setName(aspectName);

            // Try to create aspect as a non Admin user
            post("cmm/" + modelName + "/aspects", nonAdminUserName, RestApiUtil.toJsonAsString(aspect), 403);

            // Set the aspect's parent with a type name!
            aspect.setParentName("cm:content");
            // Try to create an invalid aspect as a Model Administrator
            post("cmm/" + modelName + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 409);

            // Create aspect as a Model Administrator
            aspect.setParentName(null);
            post("cmm/" + modelName + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 201);

            // Create the aspect again - duplicate name
            post("cmm/" + modelName + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 409);

            // Retrieve the created aspect
            HttpResponse response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspect.getName(), 200);
            CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
            compareCustomTypesAspects(aspect, returnedAspect, "prefixedName");
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName, returnedAspect.getPrefixedName());
        }

        String typeName = "testType" + System.currentTimeMillis();
        {
            // Add type
            CustomType type = new CustomType();
            type.setName(typeName);
            type.setDescription("test type Desc");
            type.setTitle("test type title");
            type.setParentName("cm:content");

            // Try to create type as a non Admin user
            post("cmm/" + modelName + "/types", nonAdminUserName, RestApiUtil.toJsonAsString(type), 403);

            // Set the type's parent with an aspect name!
            type.setParentName("cm:titled");
            // Try to create an invalid type as a Model Administrator
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 409);

            // Create type as a Model Administrator
            type.setParentName("cm:content");
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 201);

            // Create the type again - duplicate name
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 409);

            // Retrieve the created type
            HttpResponse response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName(), 200);
            CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            compareCustomTypesAspects(type, returnedType, "prefixedName");
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName, returnedType.getPrefixedName());

            // for now this is the simplest way to check for the imported namespaces
            CustomModelDefinition modelDef = getModelDefinition(modelName);
            assertNotNull(modelDef);
            Collection<NamespaceDefinition> importedNamespaces = modelDef.getImportedNamespaces();

            assertTrue(hasNamespaceUri(importedNamespaces, "http://www.alfresco.org/model/content/1.0"));
            assertTrue(hasNamespacePrefix(importedNamespaces, "cm"));
        }

        // Existing name
        {
            // Add aspect
            CustomAspect aspect = new CustomAspect();
            // Set the aspect name with an existing type name. The model
            // cannot have a type and an aspect with the same name.
            aspect.setName(typeName);
            post("cmm/" + modelName + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 409);

            CustomType type = new CustomType();
            // Set the type name with an existing aspect name
            type.setName(aspectName);
            type.setParentName("cm:content");
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 409);
        }
    }

    @Test
    public void testCreateModel_WithAspectsAndTypes_Invalid() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Add type
        {
            final String typeURL = "cmm/" + modelName + "/types";
            CustomType type = new CustomType();
            type.setName(null);
            type.setDescription("test type Desc");

            // Try to create a model with an invalid type name (null)
            post(typeURL, customModelAdmin, RestApiUtil.toJsonAsString(type), 400);

            // Try to create a model with an invalid type name (name contains ':')
            type.setName("prefix:someTypename");
            post(typeURL, customModelAdmin, RestApiUtil.toJsonAsString(type), 400);

            // Try to create a model with an invalid type name (name is empty)
            type.setName("");
            post(typeURL, customModelAdmin, RestApiUtil.toJsonAsString(type), 400);

            // Try to create a model with an invalid type name (name contains '<')
            type.setName("testType<name");
            post(typeURL, customModelAdmin, RestApiUtil.toJsonAsString(type), 400);
        }

        // Add aspect
        {
            final String aspectURL = "cmm/" + modelName + "/aspects";
            CustomAspect aspect = new CustomAspect();
            aspect.setName(null);
            aspect.setTitle("test aspect title");

            // Try to create a model with an invalid aspect name (null)
            post(aspectURL, customModelAdmin, RestApiUtil.toJsonAsString(aspect), 400);

            // Try to create a model with an invalid aspect name (name contains ':')
            aspect.setName("prefix:someAspectname");
            post(aspectURL, customModelAdmin, RestApiUtil.toJsonAsString(aspect), 400);

            // Try to create a model with an invalid aspect name (name is empty)
            aspect.setName("");
            post(aspectURL, customModelAdmin, RestApiUtil.toJsonAsString(aspect), 400);

            // Try to create a model with an invalid aspect name (name contains '>')
            aspect.setName("testType>name");
            post(aspectURL, customModelAdmin, RestApiUtil.toJsonAsString(aspect), 400);
        }
    }

    @Test
    public void testCreateAspectsAndTypesWithProperties() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        {
            // Add aspect with property
            String aspectName = "testAspect1" + System.currentTimeMillis();
            CustomAspect aspect = new CustomAspect();
            aspect.setName(aspectName);

            String aspectPropName = "testAspect1Prop1" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property title");
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            aspect.setProperties(props);

            // Create aspect as a Model Administrator
            post("cmm/" + modelName + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 201);
            // Retrieve the created aspect
            HttpResponse response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspect.getName(), 200);
            CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
            compareCustomTypesAspects(aspect, returnedAspect, "prefixedName", "dataType", "indexTokenisationMode");
            assertEquals(1, returnedAspect.getProperties().size());
            CustomModelProperty customModelProperty = returnedAspect.getProperties().get(0);
            assertEquals(aspectPropName, customModelProperty.getName());
            assertEquals("property title", customModelProperty.getTitle());
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectPropName, customModelProperty.getPrefixedName());
            assertEquals("Default data type is 'd:text'.", "d:text", customModelProperty.getDataType());
            // Test default indexing options
            assertTrue(customModelProperty.isIndexed());
            assertEquals(Facetable.UNSET, customModelProperty.getFacetable());
            assertEquals(IndexTokenisationMode.TRUE, customModelProperty.getIndexTokenisationMode());
        }

        {
            // Add type with properties
            String typeName = "testType1" + System.currentTimeMillis();
            CustomType type = new CustomType();
            type.setName(typeName);
            type.setDescription("test type1 Desc");
            type.setTitle("test type1 title");
            type.setParentName("cm:content");

            String typePropName = "testType1Prop1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setDataType("int"); // data type without dictionary prefix
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            type.setProperties(props);

            // Create type as a Model Administrator
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 400);

            typeProp.setDataType("d:int");
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 201);

            // Retrieve the created type
            HttpResponse response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName(), 200);
            CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            compareCustomTypesAspects(type, returnedType, "prefixedName", "indexTokenisationMode");
            assertEquals("Shouldn't list the inherited properties from 'cm:content'.", 1, returnedType.getProperties().size());
            CustomModelProperty customModelProperty = returnedType.getProperties().get(0);
            assertEquals(typePropName, customModelProperty.getName());
            assertEquals("d:int", customModelProperty.getDataType());
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typePropName, customModelProperty.getPrefixedName());
            // Test default indexing options
            assertTrue(customModelProperty.isIndexed());
            assertEquals(Facetable.UNSET, customModelProperty.getFacetable());
            assertEquals(IndexTokenisationMode.TRUE, customModelProperty.getIndexTokenisationMode());
        }

        {
            // Add more types with the same parent
            // Test to make sure the inherited properties are excluded properly.
            // As without parent’s property exclusion, we won’t be able to
            // create multiple types|aspects with the same parent.
            String typeName2 = "testType2" + System.currentTimeMillis();
            CustomType type2 = new CustomType();
            type2.setName(typeName2);
            type2.setDescription("test type2 Desc");
            type2.setTitle("test type2 title");
            type2.setParentName("cm:content");
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type2), 201);

            String typeName3 = "testType3" + System.currentTimeMillis();
            CustomType type3 = new CustomType();
            type3.setName(typeName3);
            type3.setDescription("test type3 Desc");
            type3.setTitle("test type3 title");
            type3.setParentName("cm:content");
            post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type3), 201);
        }

        {
            // Retrieve the created model
            HttpResponse response = getSingle("cmm", customModelAdmin, modelName, 200);
            CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
            assertNull(returnedModel.getTypes());
            assertNull(returnedModel.getAspects());
            
            // Retrieve the created model with its types and aspects
            response = getSingle("cmm", customModelAdmin, modelName + SELECT_ALL, 200);
            returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
            assertNotNull(returnedModel.getTypes());
            assertEquals(3, returnedModel.getTypes().size());
            assertNotNull(returnedModel.getAspects());
            assertEquals(1, returnedModel.getAspects().size());
        }
    }

    @Test
    //SHA-679
    public void testCustomModelTypesAspectsDependencies() throws Exception
    {
        String modelNameOne = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelNameOne, namespacePair, ModelStatus.DRAFT, null, "Mark Moe");

        // Add type
        String typeBaseName = "testTypeBase" + System.currentTimeMillis();
        final String typeBaseNameWithPrefix = namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeBaseName;
        createTypeAspect(CustomType.class, modelNameOne, typeBaseName, "test typeBase title", "test typeBase Desc", "cm:content");

        // Activate the model
        CustomModel modelOneStatusPayload = new CustomModel();
        modelOneStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 200);

        HttpResponse response = getSingle("cmm", customModelAdmin, modelNameOne, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(ModelStatus.ACTIVE, returnedModel.getStatus());

        // Add another type with 'typeBaseName' as its parent
        String typeName2 = "testTypeChild" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelNameOne, typeName2, "test typeChild title", "test typeChild Desc", typeBaseNameWithPrefix);

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        response = getAll("cmm/" + modelNameOne + "/types", customModelAdmin, paging, 200);
        List<CustomType> returnedTypes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomType.class);
        assertEquals(2, returnedTypes.size());

        // Create another model
        String modelNameTwo = "testModelTwo" + System.currentTimeMillis();
        Pair<String, String> modelTwoNamespacePair = getTestNamespaceUriPrefixPair();
        createCustomModel(modelNameTwo, modelTwoNamespacePair, ModelStatus.DRAFT, null, "Admin");

        // Add a type with 'typeBaseName' from the modelOne as its parent
        String modelTwoTypeName = "testModelTwoChild" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelNameTwo, modelTwoTypeName, "test model two type child title", null, typeBaseNameWithPrefix);

        // Try to deactivate modelOne
        modelOneStatusPayload = new CustomModel();
        modelOneStatusPayload.setStatus(ModelStatus.DRAFT);
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 409); // ModelTwo depends on ModelOne

        // Activate modelTwo
        CustomModel modelTwoStatusPayload = new CustomModel();
        modelTwoStatusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelNameTwo, RestApiUtil.toJsonAsString(modelTwoStatusPayload), SELECT_STATUS_QS, 200);
  
        // Try to deactivate modelOne again. The dependent model is Active now, however, the result should be the same.
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 409); // ModelTwo depends on ModelOne

        // Deactivate modelTwo
        modelTwoStatusPayload = new CustomModel();
        modelTwoStatusPayload.setStatus(ModelStatus.DRAFT);
        put("cmm", customModelAdmin, modelNameTwo, RestApiUtil.toJsonAsString(modelTwoStatusPayload), SELECT_STATUS_QS, 200);

        // Delete the modelTwo's type as a Model Administrator
        delete("cmm/" + modelNameTwo + "/types", customModelAdmin, modelTwoTypeName, 204);

        // Try to deactivate modelOne again. There is no dependency
        put("cmm", customModelAdmin, modelNameOne, RestApiUtil.toJsonAsString(modelOneStatusPayload), SELECT_STATUS_QS, 200);

    }

    @Test
    public void testDeleteTypeAspect() throws Exception
    {
        final String modelName = "testDeleteTypeModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT, null, "Joe Bloggs");

        // Add type
        final String typeName = "testType" + System.currentTimeMillis();
        final String typeNameWithPrefix = namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName;
        CustomType type = createTypeAspect(CustomType.class, modelName, typeName, "test type title", "test type Desc", "cm:content");

        // Add aspect
        final String aspectName = "testAspect" + System.currentTimeMillis();
        final String aspectNameWithPrefix = namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectName;
        CustomAspect aspect = createTypeAspect(CustomAspect.class, modelName, aspectName, null, null, null);

        // Delete type
        {
            // Try to delete the model's type as a non Admin user
            delete("cmm/" + modelName + "/types", nonAdminUserName, typeName, 403);
            // Delete the model's type as a Model Administrator
            delete("cmm/" + modelName + "/types", customModelAdmin, typeName, 204);
            // Try to retrieve the deleted type
            getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 404);
        }
        // Delete Aspect
        {
            // Try to delete the model's aspect as a non Admin user
            delete("cmm/" + modelName + "/aspects", nonAdminUserName, aspectName, 403);
            // Delete the model's aspect as a Model Administrator
            delete("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 204);
            // Try to retrieve the deleted aspect
            getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 404);
        }

        // Create the type again
        post("cmm/" + modelName + "/types", customModelAdmin, RestApiUtil.toJsonAsString(type), 201);
        // Retrieve the created type
        HttpResponse response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName(), 200);
        CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
        compareCustomTypesAspects(type, returnedType, "prefixedName");

        // Create the aspect again
        post("cmm/" + modelName + "/aspects", customModelAdmin, RestApiUtil.toJsonAsString(aspect), 201);
        // Retrieve the created aspect
        response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspect.getName(), 200);
        CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
        compareCustomTypesAspects(aspect, returnedAspect, "prefixedName");

        // Update the Type by adding property
        CustomType payload = new CustomType();
        payload.setName(typeName);

        String typePropName = "testType1Prop1" + System.currentTimeMillis();
        CustomModelProperty typeProp = new CustomModelProperty();
        typeProp.setName(typePropName);
        typeProp.setTitle("property title");
        typeProp.setDataType("d:int");
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(typeProp);
        payload.setProperties(props);
        put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 200);

        // Activate the model
        CustomModel statusPayload = new CustomModel();
        statusPayload.setStatus(ModelStatus.ACTIVE);
        // Activate the model as a Model Administrator
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

        // Test for SHA-703
        // Add another type with 'typeName' as its parent
        final String childTypeName = "testChildType" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelName, childTypeName, "test child type title", null, typeNameWithPrefix);

        // Add another aspect with 'aspectName' as its parent
        final String childAspectName = "testChildAspect" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, childAspectName, "test child aspect title", null, aspectNameWithPrefix);

        // Delete the model's type as a Model Administrator
        delete("cmm/" + modelName + "/types", customModelAdmin, typeName, 409); // Cannot delete a type of an active model

        // Delete the model's aspect as a Model Administrator
        delete("cmm/" + modelName + "/aspects", customModelAdmin, childAspectName, 409); // Cannot delete an aspect of an active model

        // Deactivate the model
        statusPayload = new CustomModel();
        statusPayload.setStatus(ModelStatus.DRAFT);
        // Deactivate the model as a Model Administrator
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

        // Delete type
        {
            // Try to delete the model's type (parent) as a Model Administrator
            delete("cmm/" + modelName + "/types", customModelAdmin, typeName, 409); // conflict: childTypeName depends on typeName

            // Delete the child type first
            delete("cmm/" + modelName + "/types", customModelAdmin, childTypeName, 204); 
            // Try to retrieve the deleted child type
            getSingle("cmm/" + modelName + "/types", customModelAdmin, childTypeName, 404);

            // Now delete the parent type
            delete("cmm/" + modelName + "/types", customModelAdmin, typeName, 204); 
            // Try to retrieve the deleted parent type
            getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 404);
            
        }

        // Delete Aspect
        {
            // Try to delete the model's aspect (parent) as a Model Administrator
            delete("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 409); // conflict: childAspectName depends on aspectName
            
            // Delete the child aspect first
            delete("cmm/" + modelName + "/aspects", customModelAdmin, childAspectName, 204);
            // Try to retrieve the deleted child aspect
            getSingle("cmm/" + modelName + "/aspects", customModelAdmin, childAspectName, 404);

            // Now delete the parent aspect
            delete("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 204);
            // Try to retrieve the deleted parent aspect
            getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 404);
        }
    }

    @Test
    public void testUpdateAspectsTypes() throws Exception
    {
        String modelName = "testModeEditAspectType" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Test update aspect
        {
            // Create aspect
            String aspectName = "testAspect" + System.currentTimeMillis();
            createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);
            // Update the aspect
            CustomAspect aspectPayload = new CustomAspect();
            aspectPayload.setDescription(null);
            aspectPayload.setTitle("title modified");
            aspectPayload.setParentName("cm:titled");

            // Try to update the aspect as a non Admin user
            put("cmm/" + modelName + "/aspects", nonAdminUserName, aspectName, RestApiUtil.toJsonAsString(aspectPayload), null, 403);

            // Modify the name
            aspectPayload.setName(aspectName + "Modified");
            // Try to update the aspect as a Model Administrator
            // Note: aspect/type name cannot be modified
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), null, 404);

            aspectPayload.setName(aspectName);
            // Update the aspect as a Model Administrator
            HttpResponse response = put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), null, 200);
            CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
            compareCustomTypesAspects(aspectPayload, returnedAspect, "prefixedName");

            // Update the aspect with an invalid parent
            aspectPayload.setParentName("cm:titled" + System.currentTimeMillis());
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), null, 409);

            // Activate the model
            CustomModel statusPayload = new CustomModel();
            statusPayload.setStatus(ModelStatus.ACTIVE);
            put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

            // Remove the aspect's parent
            // Note: cannot update the parent of an ACTIVE type/aspect.
            aspectPayload.setParentName(null);
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), null, 409);

            statusPayload = new CustomModel();
            statusPayload.setStatus(ModelStatus.DRAFT);
            put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

            // now update the aspect's parent - model is inactive
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), null, 200);
        }
        
        // Test update type
        { 
            // Create type
            String typeName = "testType" + System.currentTimeMillis();
            createTypeAspect(CustomType.class, modelName, typeName, "title", "desc", "cm:content");

            // Add property
            CustomType addPropertyPayload = new CustomType();
            addPropertyPayload.setName(typeName);
            String typePropName = "testTypeProp1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setTitle("property title");
            typeProp.setDataType("d:text");
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            addPropertyPayload.setProperties(props);
            // Create the property
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(addPropertyPayload), SELECT_PROPS_QS, 200);

            // Update the type
            CustomType typePayload = new CustomType();
            typePayload.setDescription("desc modified");
            typePayload.setTitle("title modified");

            // Try to update the type as a non Admin user
            put("cmm/" + modelName + "/types", nonAdminUserName, typeName, RestApiUtil.toJsonAsString(typePayload), null, 403);

            // Modify the name
            typePayload.setName(typeName + "Modified");
            // Try to update the type as a Model Administrator
            // Note: type/type name cannot be modified
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), null, 404);

            typePayload.setName(typeName);
            // Update the type as a Model Administrator
            HttpResponse response = put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), null, 200);
            CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            assertEquals(typePayload.getDescription(), returnedType.getDescription());
            assertEquals(typePayload.getTitle(), returnedType.getTitle());
            // Check that properties are unmodified
            assertNotNull(returnedType.getProperties());
            assertEquals(1, returnedType.getProperties().size());
            assertEquals(typePropName, returnedType.getProperties().iterator().next().getName());

            // Update the type with an invalid parent
            typePayload.setParentName("cm:folder" + System.currentTimeMillis());
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), null, 409);

            // Activate the model
            CustomModel statusPayload = new CustomModel();
            statusPayload.setStatus(ModelStatus.ACTIVE);
            put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

            // Remove the type's parent
            // Note: cannot update the parent of an ACTIVE type/type.
            typePayload.setParentName("cm:folder");
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), null, 409);

            statusPayload = new CustomModel();
            statusPayload.setStatus(ModelStatus.DRAFT);
            put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

            // now update the type's parent - model is inactive
            response = put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), null, 200);
            returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            assertEquals(typePayload.getParentName(), returnedType.getParentName());
        }
    }
}
