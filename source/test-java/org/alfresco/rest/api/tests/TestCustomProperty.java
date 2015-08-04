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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModelConstraint;
import org.alfresco.rest.api.model.CustomModelNamedValue;
import org.alfresco.rest.api.model.CustomModelProperty;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Tests the REST API of the properties of the {@link CustomModelService}.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class TestCustomProperty extends BaseCustomModelApiTest
{
    @Test
    public void testCreateProperties() throws Exception
    {
        String modelName = "testModel" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        {
            // Create aspect
            String aspectName = "testAspect1" + System.currentTimeMillis();
            CustomAspect aspect = createTypeAspect(CustomAspect.class, modelName, aspectName, null, null, null);

            // Update the Aspect by adding property
            CustomAspect payload = new CustomAspect();
            String aspectPropName = "testAspect1Prop1" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property title");
            aspectProp.setMultiValued(true);
            aspectProp.setIndexed(true);
            aspectProp.setFacetable(Facetable.TRUE);
            aspectProp.setIndexTokenisationMode(IndexTokenisationMode.BOTH);
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            payload.setProperties(props);
            // Try to update the aspect as a non Admin user
            put("cmm/" + modelName + "/aspects", nonAdminUserName, aspectName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 403);

            // Try to update the aspect as a Model Administrator
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 400); // Type name is mandatory

            // Add the mandatory aspect name to the payload
            payload.setName(aspectName);
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 200);

            // Retrieve the updated aspect
            HttpResponse response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspect.getName(), 200);
            CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
            // Check the aspect's added property
            assertEquals(1, returnedAspect.getProperties().size());
            CustomModelProperty customModelProperty = returnedAspect.getProperties().get(0);
            assertEquals(aspectPropName, customModelProperty.getName());
            assertEquals("property title", customModelProperty.getTitle());
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + aspectPropName, customModelProperty.getPrefixedName());
            assertEquals("Default data type is 'd:text'.", "d:text", customModelProperty.getDataType());
            assertNull(customModelProperty.getDescription());
            assertTrue(customModelProperty.isMultiValued());
            assertFalse(customModelProperty.isMandatory());
            assertFalse(customModelProperty.isMandatoryEnforced());
            assertNull(customModelProperty.getDefaultValue());
            assertTrue(customModelProperty.isIndexed());
            assertEquals(Facetable.TRUE, customModelProperty.getFacetable());
            assertEquals(IndexTokenisationMode.BOTH, customModelProperty.getIndexTokenisationMode());

            // Test duplicate property name
            aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName); // Existing name
            aspectProp.setTitle("new property title");
            props = new ArrayList<>(1);
            props.add(aspectProp);
            payload.setProperties(props);
            // Try to update the aspect as a Model Administrator
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 409); // property name already exists
        }

        {
            // Create type
            String typeName = "testType1" + System.currentTimeMillis();
            CustomType type = createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", "test type1 Desc", "cm:content");

            // Update the Type by adding property
            CustomType payload = new CustomType();
            String typePropName = "testType1Prop1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setTitle("property title");
            typeProp.setDataType("d:int");
            typeProp.setIndexed(false);
            typeProp.setFacetable(Facetable.FALSE);
            typeProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            payload.setProperties(props);

            // Try to update the type as a non Admin user
            put("cmm/" + modelName + "/types", nonAdminUserName, typeName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 403);

            // Try to update the type as a Model Administrator
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 400); // Type name is mandatory

            // Add the mandatory type name to the payload
            payload.setName(typeName);
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 200);

            // Retrieve the updated type
            HttpResponse response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName(), 200);
            CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            // Check the type's added property
            assertEquals(1, returnedType.getProperties().size());
            CustomModelProperty customModelProperty = returnedType.getProperties().get(0);
            assertEquals(typePropName, customModelProperty.getName());
            assertEquals("property title", customModelProperty.getTitle());
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typePropName, customModelProperty.getPrefixedName());
            assertEquals("d:int", customModelProperty.getDataType());
            assertNull(customModelProperty.getDescription());
            assertFalse(customModelProperty.isMultiValued());
            assertFalse(customModelProperty.isMandatory());
            assertFalse(customModelProperty.isMandatoryEnforced());
            assertNull(customModelProperty.getDefaultValue());
            assertFalse(customModelProperty.isIndexed());
            assertEquals(Facetable.FALSE, customModelProperty.getFacetable());
            assertEquals(IndexTokenisationMode.FALSE, customModelProperty.getIndexTokenisationMode());

            // Retrieve the updated type with all the properties (include inherited)
            response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName()+SELECT_ALL_PROPS, 200);
            returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            assertEquals(3, returnedType.getProperties().size());
            // Check for the inherited properties
            assertNotNull(getProperty(returnedType.getProperties(), "content")); // cm:content
            assertNotNull(getProperty(returnedType.getProperties(), "name")); // cm:name
   
            // Create another property and set all of its attributes
            payload = new CustomType();
            payload.setName(typeName);
            String typePropName2 = "testType1Prop2" + System.currentTimeMillis();
            typeProp = new CustomModelProperty();
            typeProp.setName(typePropName2);
            typeProp.setTitle("property2 title");
            typeProp.setDescription("property2 desciption");
            typeProp.setDataType("d:int");
            typeProp.setDefaultValue("0");
            typeProp.setMultiValued(false);
            typeProp.setMandatory(true);
            typeProp.setMandatoryEnforced(true);
            props = new ArrayList<>(1);
            props.add(typeProp);
            payload.setProperties(props);
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 200);

            // Retrieve the updated type
            response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName(), 200);
            returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            // Check the type's added property
            assertEquals(2, returnedType.getProperties().size());
            customModelProperty = getProperty(returnedType.getProperties(), typePropName2);
            assertNotNull(customModelProperty);
            assertEquals(typePropName2, customModelProperty.getName());
            assertEquals("property2 title", customModelProperty.getTitle());
            assertEquals(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typePropName2, customModelProperty.getPrefixedName());
            assertEquals("d:int", customModelProperty.getDataType());
            assertEquals("property2 desciption", customModelProperty.getDescription());
            assertFalse(customModelProperty.isMultiValued());
            assertTrue(customModelProperty.isMandatory());
            assertTrue(customModelProperty.isMandatoryEnforced());
            assertEquals("0", customModelProperty.getDefaultValue());

            // Test duplicate property name
            typeProp = new CustomModelProperty();
            typeProp.setName(typePropName2); // Existing name
            typeProp.setTitle("new property title");
            typeProp.setDataType("d:text");
            props = new ArrayList<>(1);
            props.add(typeProp);
            payload.setProperties(props);
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 409); // property name already exists
        }
    }

    @Test
    public void testDeleteProperty() throws Exception
    {
        String modelName = "testModelDeleteProp" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        /*
         * Create aspect and update it by adding two properties
         */
        String aspectName = "testAspect1" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, aspectName, null, null, null);
        // Update the Aspect by adding property - property one
        CustomAspect aspectPayload = new CustomAspect();
        aspectPayload.setName(aspectName);

        String aspectPropNameOne = "testAspect1Prop1" + System.currentTimeMillis();
        CustomModelProperty aspectPropOne = new CustomModelProperty();
        aspectPropOne.setName(aspectPropNameOne);
        aspectPropOne.setTitle("aspect property one title");
        aspectPropOne.setMultiValued(true);
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(aspectPropOne);
        aspectPayload.setProperties(props);
        // create property one
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 200);

        // Update the Aspect by adding another property - property two
        aspectPayload = new CustomAspect();
        aspectPayload.setName(aspectName);

        String aspectPropNameTwo = "testAspect1Prop2" + System.currentTimeMillis();
        CustomModelProperty aspectPropTwo = new CustomModelProperty();
        aspectPropTwo.setName(aspectPropNameTwo);
        aspectPropTwo.setTitle("aspect property two title");
        aspectPropTwo.setMandatory(true);
        aspectPropTwo.setDataType("d:int");
        aspectPropTwo.setDefaultValue("1");
        props = new ArrayList<>(1);
        props.add(aspectPropTwo);
        aspectPayload.setProperties(props);
        // create property two
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 200);

        // Retrieve the updated aspect
        HttpResponse response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 200);
        CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
        // Check the aspect's added properties
        assertEquals(2, returnedAspect.getProperties().size());

        /*
         * Create type and update it by adding two properties
         */
        String typeName = "testType1" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", null, "cm:content");

        // Update the Type by adding property - property one
        CustomType typePayload = new CustomType();
        typePayload.setName(typeName);

        String typePropNameOne = "testType1Prop1" + System.currentTimeMillis();
        CustomModelProperty typePropOne = new CustomModelProperty();
        typePropOne.setName(typePropNameOne);
        typePropOne.setTitle("type property one title");
        props = new ArrayList<>(1);
        props.add(typePropOne);
        typePayload.setProperties(props);
        // create property one
        put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 200);

        // Update the Type by adding another property - property two
        typePayload = new CustomType();
        typePayload.setName(typeName);

        // Create inline MINMAX constraint
        CustomModelConstraint inlineMinMaxConstraint = new CustomModelConstraint();
        inlineMinMaxConstraint.setType("MINMAX");
        inlineMinMaxConstraint.setTitle("test MINMAX title");
        // Create the MinMax constraint's parameters
        List<CustomModelNamedValue> parameters = new ArrayList<>(2);
        parameters.add(buildNamedValue("maxValue", "100.0"));
        parameters.add(buildNamedValue("minValue", "0.0"));
        // Add the parameters into the constraint
        inlineMinMaxConstraint.setParameters(parameters);

        String typePropNameTwo = "testType1Prop2" + System.currentTimeMillis();
        CustomModelProperty typePropTwo = new CustomModelProperty();
        typePropTwo.setName(typePropNameTwo);
        typePropTwo.setTitle("type property two title");
        typePropTwo.setDataType("d:int");
        typePropTwo.setConstraints(Arrays.asList(inlineMinMaxConstraint)); // add the inline constraint
        props = new ArrayList<>(1);
        props.add(typePropTwo);
        typePayload.setProperties(props);
        // create property one
        put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 200);

        // Retrieve the updated type
        response = getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 200);
        CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
        // Check the type's added properties
        assertEquals(2, returnedType.getProperties().size());

        // Delete aspect's property one - model is inactive
        {
            final String deletePropOneAspectQS = getPropDeleteUpdateQS(aspectPropNameOne, true);
            // Try to delete propertyOne from aspect
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, null, deletePropOneAspectQS, 400); // missing payload

            CustomAspect deletePropAspectPayload = new CustomAspect();
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(deletePropAspectPayload), deletePropOneAspectQS, 400); // missing aspect name

            deletePropAspectPayload.setName(aspectName);
            put("cmm/" + modelName + "/aspects", nonAdminUserName, aspectName, RestApiUtil.toJsonAsString(deletePropAspectPayload), deletePropOneAspectQS, 403); // unauthorised
            // Delete as a Model Administrator
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(deletePropAspectPayload), deletePropOneAspectQS, 200);

            // Check the property has been deleted
            response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 200);
            returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
            assertEquals(1, returnedAspect.getProperties().size());
            assertFalse("Property one should have been deleted.", aspectPropNameOne.equals(returnedAspect.getProperties().get(0).getName()));

            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(deletePropAspectPayload), deletePropOneAspectQS, 404); //Not found
        }

        // Delete type's property two - model is inactive
        {
            final String deletePropTwoTypeQS = getPropDeleteUpdateQS(typePropNameTwo, true);
            // Try to delete propertyOne from type
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, null, deletePropTwoTypeQS, 400); // missing payload

            CustomType deletePropTypePayload = new CustomType();
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(deletePropTypePayload), deletePropTwoTypeQS,
                        400); // missing type name

            deletePropTypePayload.setName(typeName);
            put("cmm/" + modelName + "/types", nonAdminUserName, typeName, RestApiUtil.toJsonAsString(deletePropTypePayload), deletePropTwoTypeQS, 403); // unauthorised
            // Delete as a Model Administrator
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(deletePropTypePayload), deletePropTwoTypeQS, 200);

            // Check the property has been deleted
            response = getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 200);
            returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            assertEquals(1, returnedType.getProperties().size());
            assertFalse("Property two should have been deleted.", typePropNameTwo.equals(returnedType.getProperties().get(0).getName()));

            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(deletePropTypePayload), deletePropTwoTypeQS, 404); //Not found
        }

        // Note: at the time of writing, we can't delete a property of an active model, as ModelValidatorImpl.validateIndexedProperty depends on Solr

    }

    @Test
    public void testUpdateProperty() throws Exception
    {
        String modelName = "testModelUpdateProp" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        /*
         * Create aspect and update it by adding a property
         */
        String aspectName = "testAspect1" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, aspectName, null, null, null);
        // Update the Aspect by adding property
        CustomAspect aspectPayload = new CustomAspect();
        aspectPayload.setName(aspectName);

        String aspectPropName = "testAspect1Prop" + System.currentTimeMillis();
        CustomModelProperty aspectProp = new CustomModelProperty();
        aspectProp.setName(aspectPropName);
        aspectProp.setTitle("aspect property title");
        aspectProp.setMultiValued(true);
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(aspectProp);
        aspectPayload.setProperties(props);
        // create property
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 200);

        // Retrieve the updated aspect
        HttpResponse response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 200);
        CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
        // Check the aspect's added property
        assertEquals(1, returnedAspect.getProperties().size());

        /*
         * Create type and update it by adding a property
         */
        String typeName = "testType1" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", null, "cm:content");

        // Update the Type by adding property - property one
        CustomType typePayload = new CustomType();
        typePayload.setName(typeName);

        // Create inline MINMAX constraint
        CustomModelConstraint inlineMinMaxConstraint = new CustomModelConstraint();
        inlineMinMaxConstraint.setType("MINMAX");
        inlineMinMaxConstraint.setTitle("test MINMAX title");
        // Create the MinMax constraint's parameters
        List<CustomModelNamedValue> parameters = new ArrayList<>(2);
        parameters.add(buildNamedValue("maxValue", "100.0"));
        parameters.add(buildNamedValue("minValue", "0.0"));
        // Add the parameters into the constraint
        inlineMinMaxConstraint.setParameters(parameters);

        String typePropName = "testType1Prop" + System.currentTimeMillis();
        CustomModelProperty typeProp = new CustomModelProperty();
        typeProp.setName(typePropName);
        typeProp.setDataType("d:int");
        typeProp.setTitle("type property title");
        typeProp.setDefaultValue("0");
        typeProp.setConstraints(Arrays.asList(inlineMinMaxConstraint)); // add the inline constraint
        props = new ArrayList<>(1);
        props.add(typeProp);
        typePayload.setProperties(props);
        // create property
        put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 200);

        // Retrieve the updated type
        response = getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 200);
        CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
        // Check the type's added property
        assertEquals(1, returnedType.getProperties().size());

        // Update aspect's property - model is inactive
        {
            final String updatePropOneAspectQS = getPropDeleteUpdateQS(aspectPropName, false);
            // Try to update property from aspect
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, null, updatePropOneAspectQS, 400); // missing payload

            CustomAspect updatePropAspectPayload = new CustomAspect();
            CustomModelProperty propertyAspect = new CustomModelProperty();
            propertyAspect.setTitle("new Title");
            propertyAspect.setDescription("new Desc");
            propertyAspect.setDataType("d:int"); // the original value was d:text
            propertyAspect.setMultiValued(false); // the original value was true
            propertyAspect.setMandatory(true); // the original value was false
            propertyAspect.setDefaultValue("10");
            List<CustomModelProperty> modifiedProp = new ArrayList<>(1);
            modifiedProp.add(propertyAspect);
            updatePropAspectPayload.setProperties(modifiedProp);
 
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(updatePropAspectPayload), updatePropOneAspectQS, 400); // missing aspect name

            // set a random name
            updatePropAspectPayload.setName(aspectName + System.currentTimeMillis());
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(updatePropAspectPayload), updatePropOneAspectQS, 404); // Aspect not found

            // set the correct name
            updatePropAspectPayload.setName(aspectName);
            // the requested property name dose not match the payload
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(updatePropAspectPayload), updatePropOneAspectQS, 400);

            // set the property name that matches the requested property
            propertyAspect.setName(aspectPropName);
            put("cmm/" + modelName + "/aspects", nonAdminUserName, aspectName, RestApiUtil.toJsonAsString(updatePropAspectPayload), updatePropOneAspectQS, 403); // unauthorised
            // Update as a Model Administrator
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(updatePropAspectPayload), updatePropOneAspectQS, 200);

            // Check the property has been updated
            response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 200);
            returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);
            assertEquals(1, returnedAspect.getProperties().size());
            CustomModelProperty modifiedAspectProperty = returnedAspect.getProperties().get(0);
            compareCustomModelProperties(propertyAspect, modifiedAspectProperty, "prefixedName", "indexTokenisationMode");
        }

        // Activate the model
        CustomModel statusPayload = new CustomModel();
        statusPayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(statusPayload), SELECT_STATUS_QS, 200);

        // Update type's property - model is active
        {
            final String updatePropTwoTypeQS = getPropDeleteUpdateQS(typePropName, false);
            CustomType updatePropTypePayload = new CustomType();
            updatePropTypePayload.setName(typeName);
            CustomModelProperty propertyType = new CustomModelProperty();
            propertyType.setName(typePropName);
            propertyType.setTitle("new Title");
            propertyType.setDescription("new Desc");
            propertyType.setDataType("d:long"); // the original value was d:int
            propertyType.setDefaultValue("5");
            List<CustomModelProperty> modifiedProp = new ArrayList<>(1);
            modifiedProp.add(propertyType);
            updatePropTypePayload.setProperties(modifiedProp);

            // Unauthorised
            put("cmm/" + modelName + "/types", nonAdminUserName, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 403);
            // Try to update an active model as a Model Administrator - Cannot change the data type of the property of an active model
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 409);

            // Set the data type with its original value
            propertyType.setDataType("d:int");
            propertyType.setMultiValued(true);// the original value was false
            // Cannot change the multi-valued option of the property of an active model
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 409);

            propertyType.setMultiValued(false);
            propertyType.setMandatory(true);// the original value was false
            // Cannot change the mandatory option of the property of an active model
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 409);

            propertyType.setMandatory(false);
            propertyType.setMandatoryEnforced(true);// the original value was false
            // Cannot change the mandatory-enforced option of the property of an active model
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 409);

            // Set the mandatory-enforced with its original value
            propertyType.setMandatoryEnforced(false);
            // Update the MinMax constraint's parameters
            parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxValue", "120.0")); // the original value was 100.0
            parameters.add(buildNamedValue("minValue", "20.0")); // the original value was 0.0
            // Add the parameters into the constraint
            inlineMinMaxConstraint.setParameters(parameters);
            propertyType.setConstraints(Arrays.asList(inlineMinMaxConstraint)); // add the updated inline constraint

            // Try to Update - constraint violation. The default value is 5 which is not in the MinMax range [20, 120]
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 409);

            propertyType.setDefaultValue("25"); // we changed the MinMax constraint to be [20, 120]
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 200);

            // Check the property has been updated
            response = getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 200);
            returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            assertEquals(1, returnedType.getProperties().size());
            CustomModelProperty modifiedTypeProperty = returnedType.getProperties().get(0);
            assertEquals("new Title", modifiedTypeProperty.getTitle());
            assertEquals("new Desc", modifiedTypeProperty.getDescription());
            assertEquals("25", modifiedTypeProperty.getDefaultValue());
            assertEquals("Shouldn't be able to change the data type of the property of an active model." ,"d:int", modifiedTypeProperty.getDataType());
            assertFalse(modifiedTypeProperty.isMandatory());
            assertFalse(modifiedTypeProperty.isMultiValued());
            assertFalse(modifiedTypeProperty.isMandatoryEnforced());
            assertEquals(1, modifiedTypeProperty.getConstraints().size());
            CustomModelConstraint modifiedConstraint = modifiedTypeProperty.getConstraints().get(0);
            assertEquals("MINMAX", modifiedConstraint.getType());
            assertEquals("120.0", getParameterSimpleValue(modifiedConstraint.getParameters(), "maxValue"));
            assertEquals("20.0", getParameterSimpleValue(modifiedConstraint.getParameters(), "minValue"));

            // Change the constraint type and parameter
            inlineMinMaxConstraint.setType("LENGTH");
            inlineMinMaxConstraint.setTitle("test LENGTH title");
            parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxLength", "256"));
            parameters.add(buildNamedValue("minLength", "0"));
            // Add the parameters into the constraint
            inlineMinMaxConstraint.setParameters(parameters);
            propertyType.setConstraints(Arrays.asList(inlineMinMaxConstraint));
            // LENGTH can only be used with textual data type
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 400);

            //update the property by removing the constraint
            propertyType.setConstraints(Collections.<CustomModelConstraint>emptyList());
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(updatePropTypePayload), updatePropTwoTypeQS, 200);

            response = getSingle("cmm/" + modelName + "/types", customModelAdmin, typeName, 200);
            returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);
            assertEquals(1, returnedType.getProperties().size());
            modifiedTypeProperty = returnedType.getProperties().get(0);
            assertEquals(0, modifiedTypeProperty.getConstraints().size());
        }
    }

    @Test
    public void testValidatePropertyDefaultValue() throws Exception
    {
        String modelName = "testModelPropDefaultValue" + System.currentTimeMillis();
        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        /*
         * Create aspect and update it by adding a property
         */
        String aspectName = "testAspect1" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, aspectName, null, null, null);
        // Update the Aspect by adding property
        CustomAspect aspectPayload = new CustomAspect();
        aspectPayload.setName(aspectName);

        String aspectPropName = "testAspectProp" + System.currentTimeMillis();
        final String updatePropAspectQS = getPropDeleteUpdateQS(aspectPropName, false);

        CustomModelProperty aspectProp = new CustomModelProperty();
        aspectProp.setName(aspectPropName);
        aspectProp.setTitle("aspect property title");
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(aspectProp);
        aspectPayload.setProperties(props);

        // d:int tests
        {
            aspectProp.setDataType("d:int");
            aspectProp.setDefaultValue(" ");// space

            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

            aspectProp.setDefaultValue("abc"); // text
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

            aspectProp.setDefaultValue("1.0"); // double
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

            aspectProp.setDefaultValue("1,2,3"); // text
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);
        }

        // d:float tests
        {
            aspectProp.setDataType("d:float");
            aspectProp.setDefaultValue(" ");// space

            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

            aspectProp.setDefaultValue("abc"); // text
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

            aspectProp.setDefaultValue("1,2,3"); // text
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

            aspectProp.setDefaultValue("1.0"); // float
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 200);

            aspectProp.setDefaultValue("1.0f"); // float - update
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 200);

            aspectProp.setDefaultValue("1.0d"); // double - update
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 200);
        }

        // d:boolean tests
        {
            aspectProp.setDataType("d:boolean");
            aspectProp.setDefaultValue(" ");// space
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 400);

            aspectProp.setDefaultValue("abc"); // text
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 400);

            aspectProp.setDefaultValue("1"); // number
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 400);
            
            aspectProp.setDefaultValue("true"); // valid value
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 200);
 
            aspectProp.setDefaultValue("false"); // valid value
            // create property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), updatePropAspectQS, 200);
        }
    }

    private String getPropDeleteUpdateQS(String propName, boolean isDelete)
    {
        String req = (isDelete ? "&delete=" : "&update=");
        return SELECT_PROPS_QS + req + propName;
    }
}
