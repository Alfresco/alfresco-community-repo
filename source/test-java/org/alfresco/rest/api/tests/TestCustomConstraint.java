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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModelConstraint;
import org.alfresco.rest.api.model.CustomModelNamedValue;
import org.alfresco.rest.api.model.CustomModelProperty;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Tests the REST API of the constraints of the {@link CustomModelService}.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class TestCustomConstraint extends BaseCustomModelApiTest
{

    @Test
    public void testCreateConstraints() throws Exception
    {
        final Paging paging = getPaging(0, Integer.MAX_VALUE);

        String modelName = "testModelConstraint" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Create RegEx constraint
        {
            String regExConstraintName = "testFileNameRegEx" + System.currentTimeMillis();
            CustomModelConstraint regExConstraint = new CustomModelConstraint();
            regExConstraint.setName(regExConstraintName);
            regExConstraint.setType("REGEX");
            regExConstraint.setTitle("test RegEx title");
            regExConstraint.setDescription("test RegEx desc");
            // Create the RegEx constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("expression", "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)"));
            parameters.add(buildNamedValue("requiresMatch", "false"));
            // Add the parameters into the constraint
            regExConstraint.setParameters(parameters);

            // Try to create constraint as a non Admin user
            post("cmm/" + modelName + "/constraints", nonAdminUserName, RestApiUtil.toJsonAsString(regExConstraint), 403);

            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(regExConstraint), 201);

            // Retrieve the created RegEx constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, regExConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);
            compareCustomModelConstraints(regExConstraint, returnedConstraint, "prefixedName");

            // Try to create a duplicate constraint
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(regExConstraint), 409);

            // Retrieve all the model's constraints
            response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals(1, constraints.size());
        }

        // Try to create invalid RegEx constraint
        {
            String regExConstraintName = "testFileNameInvalidRegEx" + System.currentTimeMillis();
            CustomModelConstraint regExConstraint = new CustomModelConstraint();
            regExConstraint.setName(regExConstraintName);
            regExConstraint.setType("REGEX");
            // Create the RegEx constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("expression", "*******"));
            parameters.add(buildNamedValue("requiresMatch", "false"));
            // Add the parameters into the constraint
            regExConstraint.setParameters(parameters);

            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(regExConstraint), 400);
        }

        // Create MINMAX constraint
        {
            String minMaxConstraintName = "testMinMaxConstraint" + System.currentTimeMillis();
            CustomModelConstraint minMaxConstraint = new CustomModelConstraint();
            minMaxConstraint.setName(minMaxConstraintName);
            minMaxConstraint.setTitle("test MinMax title");
            minMaxConstraint.setDescription("test MinMax desc");
            // Create the MinMax constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxValue", "100.0"));
            parameters.add(buildNamedValue("minValue", "0.0"));
            // Add the parameters into the constraint
            minMaxConstraint.setParameters(parameters);

            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(minMaxConstraint), 400); // constraint's type is mandatory

            minMaxConstraint.setType("MINMAX");
            parameters.clear();
            parameters.add(buildNamedValue("maxValue", "abc")); // invalid number
            parameters.add(buildNamedValue("minValue", "0.0"));
            // Add the parameters into the constraint
            minMaxConstraint.setParameters(parameters);
            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(minMaxConstraint), 400);

            parameters.clear();
            parameters.add(buildNamedValue("maxValue", "100"));
            parameters.add(buildNamedValue("minValue", "text")); // invalid number
            // Add the parameters into the constraint
            minMaxConstraint.setParameters(parameters);
            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(minMaxConstraint), 400);

            parameters.clear();
            parameters.add(buildNamedValue("maxValue", "100.0"));
            parameters.add(buildNamedValue("minValue", "0.0"));
            // Add the parameters into the constraint
            minMaxConstraint.setParameters(parameters);
            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(minMaxConstraint), 201);

            // Retrieve the created MINMAX constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, minMaxConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);
            compareCustomModelConstraints(minMaxConstraint, returnedConstraint, "prefixedName");

            // Retrieve all the model's constraints
            response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals(2, constraints.size());
        }

        // Create LENGTH constraint
        {
            String lengthConstraintName = "testLengthConstraint" + System.currentTimeMillis();
            CustomModelConstraint lengthConstraint = new CustomModelConstraint();
            lengthConstraint.setName(lengthConstraintName);
            lengthConstraint.setType("LENGTH");
            lengthConstraint.setTitle("test Length title");
            lengthConstraint.setDescription("test Length desc");
            // Create the Length constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxLength", "text")); // invalid number
            parameters.add(buildNamedValue("minLength", "0"));
            // Add the parameters into the constraint
            lengthConstraint.setParameters(parameters);

            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(lengthConstraint), 400);

            parameters.clear();
            parameters.add(buildNamedValue("maxLength", "256"));
            parameters.add(buildNamedValue("minLength", "1.0")); // double number
            // Add the parameters into the constraint
            lengthConstraint.setParameters(parameters);
            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(lengthConstraint), 400);

            parameters.clear();
            parameters.add(buildNamedValue("maxLength", "256"));
            parameters.add(buildNamedValue("minLength", "0"));
            // Add the parameters into the constraint
            lengthConstraint.setParameters(parameters);
            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(lengthConstraint), 201);

            // Retrieve the created LENGTH constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, lengthConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);
            compareCustomModelConstraints(lengthConstraint, returnedConstraint, "prefixedName");

            // Retrieve all the model's constraints
            response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals(3, constraints.size());
        }

        // Create LIST constraint
        {
            String listConstraintName = "testListConstraint" + System.currentTimeMillis();
            CustomModelConstraint listConstraint = new CustomModelConstraint();
            listConstraint.setName(listConstraintName);
            listConstraint.setType("LIST");
            listConstraint.setTitle("test List title");
            listConstraint.setDescription("test List desc");
            // Create the List constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(3);
            parameters.add(buildNamedValue("allowedValues", null, "High", "Normal", "Low"));// list value
            parameters.add(buildNamedValue("sorted", "false"));
            // Add the parameters into the constraint
            listConstraint.setParameters(parameters);

            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(listConstraint), 201);

            // Retrieve the created List constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, listConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);
            compareCustomModelConstraints(listConstraint, returnedConstraint, "prefixedName", "parameters");
            String sorted = getParameterSimpleValue(returnedConstraint.getParameters(), "sorted");
            assertEquals("false", sorted);
            List<String> listValues = getParameterListValue(returnedConstraint.getParameters(), "allowedValues");
            assertNotNull(listValues);
            assertEquals(3, listValues.size());
            assertEquals("High", listValues.get(0));
            assertEquals("Normal", listValues.get(1));
            assertEquals("Low", listValues.get(2));

            // Retrieve all the model's constraints
            response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals(4, constraints.size());
        }

        // Create authorityName constraint
        {
            String authorityNameConstraintName = "authorityNameConstraint" + System.currentTimeMillis();
            CustomModelConstraint authorityNameConstraint = new CustomModelConstraint();
            authorityNameConstraint.setName(authorityNameConstraintName);
            authorityNameConstraint.setType("org.alfresco.repo.dictionary.constraint.AuthorityNameConstraint");
            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(authorityNameConstraint), 201);

            // Retrieve the created authorityName constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, authorityNameConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);
            compareCustomModelConstraints(authorityNameConstraint, returnedConstraint, "prefixedName");

            // Retrieve all the model's constraints
            response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals(5, constraints.size());
        }

        // Create Invalid constraint
        {
            String invalidConstraintName = "testInvalidConstraint" + System.currentTimeMillis();
            CustomModelConstraint invalidConstraint = new CustomModelConstraint();
            invalidConstraint.setName(invalidConstraintName);
            invalidConstraint.setType("InvalidConstraintType"+ System.currentTimeMillis());
            invalidConstraint.setTitle("test Invalid title");
            invalidConstraint.setDescription("test Invalid desc");
            // Create the MinMax constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxValue", "100.0"));
            parameters.add(buildNamedValue("minValue", "0.0"));
            // Add the parameters into the constraint
            invalidConstraint.setParameters(parameters);

            // Try to create an invalid constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(invalidConstraint), 400);

            // Retrieve all the model's constraints
            HttpResponse response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals(5, constraints.size());
        }

        // Activate the model
        CustomModel updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

        // Retrieve all the model's constraints
        HttpResponse response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
        List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
        assertEquals(5, constraints.size());

        // Deactivate the model
        updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.DRAFT);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

        // Retrieve all the model's constraints
        response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
        constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
        assertEquals(5, constraints.size());
    }

    @Test
    public void testCreateConstraintAndAddToProperty() throws Exception
    {
        String modelName = "testModelConstraint" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Create RegEx constraint
        String regExConstraintName = "testFileNameRegEx" + System.currentTimeMillis();
        CustomModelConstraint regExConstraint = new CustomModelConstraint();
        regExConstraint.setName(regExConstraintName);
        regExConstraint.setType("REGEX");
        regExConstraint.setTitle("test RegEx title");
        regExConstraint.setDescription("test RegEx desc");
        // Create the RegEx constraint's parameters
        List<CustomModelNamedValue> parameters= new ArrayList<>(2);
        parameters.add(buildNamedValue("expression", "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)"));
        parameters.add(buildNamedValue("requiresMatch", "false"));
        // Add the parameters into the constraint
        regExConstraint.setParameters(parameters);

        // Create constraint as a Model Administrator
        post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(regExConstraint), 201);

        // Retrieve the created constraint
        HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, regExConstraintName, 200);
        CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);

        // Retrieve all the model's constraints
        Paging paging = getPaging(0, Integer.MAX_VALUE);
        response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
        List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
        assertEquals(1, constraints.size());

        // Create aspect
        String aspectName = "testAspect1" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

        // Update the Aspect by adding property
        CustomAspect payload = new CustomAspect();
        payload.setName(aspectName);
        final String aspectPropName = "testAspect1Prop1" + System.currentTimeMillis();
        CustomModelProperty aspectProp = new CustomModelProperty();
        aspectProp.setName(aspectPropName);
        aspectProp.setTitle("property title");
        aspectProp.setDataType("d:text");
        aspectProp.setConstraintRefs(Arrays.asList(returnedConstraint.getPrefixedName()));// Add the constraint ref
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(aspectProp);
        payload.setProperties(props);

        // Create the property
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(payload), SELECT_PROPS_QS, 200);

        // Activate the model 
        CustomModel updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.ACTIVE);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

        // Retrieve all the model's constraints
        // Test to see if the API took care of duplicate constraints when referencing a constraint within a property.
        response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
        constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
        assertEquals(1, constraints.size());

        // Test RegEx constrain enforcement
        {
            final NodeService nodeService = repoService.getNodeService();
            final QName aspectQName = QName.createQName("{" + namespacePair.getFirst() + "}" + aspectName);

            TestNetwork testNetwork = getTestFixture().getRandomNetwork();
            TestPerson person = testNetwork.createUser();
            final String siteName = "site" + System.currentTimeMillis();

            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PRIVATE);
                    TestSite site = repoService.createSite(null, siteInfo);

                    NodeRef nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc", "Test Content");

                    nodeService.addAspect(nodeRef, aspectQName, null);
                    assertTrue(nodeService.hasAspect(nodeRef, aspectQName));

                    try
                    {
                        QName propQName = QName.createQName("{" + namespacePair.getFirst() + "}" + aspectPropName);
                        nodeService.setProperty(nodeRef, propQName, "Invalid$Char.");
                        fail("Invalid property value. Should have caused integrity violations.");
                    }
                    catch (Exception e)
                    {
                        // Expected
                    }

                    // Permanently remove model from repository
                    nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    nodeService.deleteNode(nodeRef);

                    return null;
                }
            }, person.getId(), testNetwork.getId());
        }

        // Deactivate the model 
        updatePayload = new CustomModel();
        updatePayload.setStatus(ModelStatus.DRAFT);
        put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updatePayload), SELECT_STATUS_QS, 200);

        // Test update the namespace prefix (test to see if the API updates the constraints refs with this new prefix)
        CustomModel updateModelPayload = new CustomModel();
        String modifiedPrefix = namespacePair.getSecond() + "Modified";
        updateModelPayload.setNamespacePrefix(modifiedPrefix);
        updateModelPayload.setNamespaceUri(namespacePair.getFirst());
        response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updateModelPayload), null, 200);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(modifiedPrefix, returnedModel.getNamespacePrefix());
        assertEquals("The namespace URI shouldn't have changed.", namespacePair.getFirst(), returnedModel.getNamespaceUri());

        // Test update the namespace URI
        updateModelPayload = new CustomModel();
        updateModelPayload.setNamespacePrefix(modifiedPrefix);
        String modifiedURI = namespacePair.getFirst() + "Modified";
        updateModelPayload.setNamespaceUri(modifiedURI);
        response = put("cmm", customModelAdmin, modelName, RestApiUtil.toJsonAsString(updateModelPayload), null, 200);
        returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        assertEquals(modifiedURI, returnedModel.getNamespaceUri());
        assertEquals("The namespace prefix shouldn't have changed.", modifiedPrefix, returnedModel.getNamespacePrefix());
    }

    @Test
    public void testCreateInlineConstraint() throws Exception
    {
        String modelName = "testModelInlineConstraint" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        String regExConstraintName = "testInlineFileNameRegEx" + System.currentTimeMillis();
        {
            // Create RegEx constraint
            CustomModelConstraint inlineRegExConstraint = new CustomModelConstraint();
            inlineRegExConstraint.setName(regExConstraintName);
            inlineRegExConstraint.setType("REGEX");
            // Create the inline RegEx constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("expression", "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)"));
            parameters.add(buildNamedValue("requiresMatch", "false"));
            // Add the parameters into the constraint
            inlineRegExConstraint.setParameters(parameters);

            // Create aspect
            String aspectName = "testAspect1" + System.currentTimeMillis();
            createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

            // Update the Aspect by adding property
            CustomAspect aspectPayload = new CustomAspect();
            aspectPayload.setName(aspectName);
            final String aspectPropName = "testAspect1Prop1" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property title");
            aspectProp.setDataType("d:text");
            aspectProp.setConstraints(Arrays.asList(inlineRegExConstraint));// Add inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            aspectPayload.setProperties(props);

            // Create the property
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 200);

            // Retrieve all the model's constraints
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            HttpResponse response = getAll("cmm/" + modelName + "/constraints", customModelAdmin, paging, 200);
            List<CustomModelConstraint> constraints = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), CustomModelConstraint.class);
            assertEquals("Inline constraints should not be included with the model defined constraints.", 0, constraints.size());

            // Retrieve the updated aspect
            response = getSingle("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, 200);
            CustomAspect returnedAspect = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomAspect.class);

            // Check the aspect's added property
            assertEquals(1, returnedAspect.getProperties().size());
            CustomModelProperty customModelProperty = returnedAspect.getProperties().get(0);
            assertEquals(aspectPropName, customModelProperty.getName());

            assertEquals(0, customModelProperty.getConstraintRefs().size());
            List<CustomModelConstraint> inlineConstraints = customModelProperty.getConstraints();
            assertEquals(1, inlineConstraints.size());
            compareCustomModelConstraints(inlineRegExConstraint, inlineConstraints.get(0), "prefixedName");
        }

        // Create inline and referenced constraint
        {
            // Create RegEx constraint
            CustomModelConstraint regExConstraint = new CustomModelConstraint();
            regExConstraint.setName(regExConstraintName); // duplicate name
            regExConstraint.setType("REGEX");
            regExConstraint.setTitle("test RegEx title");
            // Create the RegEx constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("expression", "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)"));
            parameters.add(buildNamedValue("requiresMatch", "false"));
            // Add the parameters into the constraint
            regExConstraint.setParameters(parameters);

            // Try to create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(regExConstraint), 409); // duplicate name

            String newRegExConstraintName = "testFileNameRegEx" + System.currentTimeMillis();
            regExConstraint.setName(newRegExConstraintName);
            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(regExConstraint), 201);
            // Retrieve the created RegEx constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, newRegExConstraintName, 200);
            CustomModelConstraint returnedRegExConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);

            // Create inline anonymous LENGTH constraint
            CustomModelConstraint inlineAnonymousLengthConstraint = new CustomModelConstraint();
            inlineAnonymousLengthConstraint.setType("LENGTH");
            inlineAnonymousLengthConstraint.setTitle("test Length title");
            // Create the Length constraint's parameters
            parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxLength", "256"));
            parameters.add(buildNamedValue("minLength", "0"));
            // Add the parameters into the constraint
            inlineAnonymousLengthConstraint.setParameters(parameters);

            // Create type
            String typeName = "testType1" + System.currentTimeMillis();
            CustomType type = createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", "test type1 Desc", "cm:content");

            // Update the Type by adding property
            CustomType typePayload = new CustomType();
            typePayload.setName(typeName);
            String typePropName = "testType1Prop1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setTitle("property title");
            typeProp.setDataType("d:int");
            typeProp.setConstraintRefs(Arrays.asList(returnedRegExConstraint.getPrefixedName())); // Constraint Ref
            typeProp.setConstraints(Arrays.asList(inlineAnonymousLengthConstraint)); // inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            typePayload.setProperties(props);

            // Try to create the property - LENGTH constraint can only be used with textual data type
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 400);

            typeProp.setDataType("d:double");
            // CTry to create the property - LENGTH constraint can only be used with textual data type
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 400);

            typeProp.setDataType("d:text");
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 200);

            // Retrieve the updated type
            response = getSingle("cmm/" + modelName + "/types", customModelAdmin, type.getName(), 200);
            CustomType returnedType = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomType.class);

            // Check the type's added property
            assertEquals(1, returnedType.getProperties().size());
            CustomModelProperty customModelProperty = returnedType.getProperties().get(0);
            assertEquals(typePropName, customModelProperty.getName());

            assertEquals(1, customModelProperty.getConstraintRefs().size());
            assertEquals(returnedRegExConstraint.getPrefixedName(), customModelProperty.getConstraintRefs().get(0));

            assertEquals(1, customModelProperty.getConstraints().size());
            assertNotNull(customModelProperty.getConstraints().get(0).getName()); // M2PropertyDefinition will add a name
            compareCustomModelConstraints(inlineAnonymousLengthConstraint, customModelProperty.getConstraints().get(0), "prefixedName", "name");
        }
    }

    @Test
    public void testCreateListConstraintInvalid() throws Exception
    {
        String modelName = "testModelConstraintInvalid" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Create aspect
        String aspectName = "testAspect" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

        // Update the Aspect by adding property
        CustomAspect aspectPayload = new CustomAspect();
        aspectPayload.setName(aspectName);
        final String aspectPropName = "testAspect1Prop" + System.currentTimeMillis();
        CustomModelProperty aspectProp = new CustomModelProperty();
        aspectProp.setName(aspectPropName);
        aspectProp.setTitle("property title");
        aspectProp.setDataType("d:int");

        //Create LIST constraint
        String inlineListConstraintName = "testListConstraint" + System.currentTimeMillis();
        CustomModelConstraint inlineListConstraint = new CustomModelConstraint();
        inlineListConstraint.setName(inlineListConstraintName);
        inlineListConstraint.setType("LIST");
        inlineListConstraint.setTitle("test List title");
        inlineListConstraint.setDescription("test List desc");
        // Create the List constraint's parameters
        List<CustomModelNamedValue> parameters = new ArrayList<>(3);
        parameters.add(buildNamedValue("allowedValues", null, "a", "b", "c"));// text list value, but the the property data type is d:int
        parameters.add(buildNamedValue("sorted", "false"));
        // Add the parameters into the constraint
        inlineListConstraint.setParameters(parameters);
        aspectProp.setConstraints(Arrays.asList(inlineListConstraint));// Add inline constraint
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(aspectProp);
        aspectPayload.setProperties(props);

        // Try to create the property - Invalid LIST values
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

        // Test d:double LIST values with d:int property data type
        parameters = new ArrayList<>(3);
        parameters.add(buildNamedValue("allowedValues", null, "1.0", "2.0", "3.0"));// double list value, but the the property data type is d:int
        parameters.add(buildNamedValue("sorted", "false"));
        // Add the parameters into the constraint
        inlineListConstraint.setParameters(parameters);
        aspectProp.setConstraints(Arrays.asList(inlineListConstraint));// Add inline constraint
        props = new ArrayList<>(1);
        props.add(aspectProp);
        aspectPayload.setProperties(props);

        // Try to create the property - Invalid LIST values
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);
    }

    @Test
    public void testCreateMinMaxConstraintInvalid() throws Exception
    {
        String modelName = "testModelMinMaxInvalid" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        // Create aspect
        String aspectName = "testAspect" + System.currentTimeMillis();
        createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

        // Update the Aspect by adding property
        CustomAspect aspectPayload = new CustomAspect();
        aspectPayload.setName(aspectName);
        final String aspectPropName = "testAspect1Prop" + System.currentTimeMillis();
        CustomModelProperty aspectProp = new CustomModelProperty();
        aspectProp.setName(aspectPropName);
        aspectProp.setTitle("property title");
        aspectProp.setDataType("d:text");

        String minMaxConstraintName = "testMinMaxConstraint" + System.currentTimeMillis();
        CustomModelConstraint minMaxConstraint = new CustomModelConstraint();
        minMaxConstraint.setType("MINMAX");
        minMaxConstraint.setName(minMaxConstraintName);
        minMaxConstraint.setTitle("test MinMax title");
        minMaxConstraint.setDescription("test MinMax desc");
        // Create the MinMax constraint's parameters
        List<CustomModelNamedValue> parameters = new ArrayList<>(2);
        parameters.add(buildNamedValue("maxValue", "100.0"));
        parameters.add(buildNamedValue("minValue", "0.0"));
        // Add the parameters into the constraint
        minMaxConstraint.setParameters(parameters);

        aspectProp.setConstraints(Arrays.asList(minMaxConstraint));// Add inline constraint
        List<CustomModelProperty> props = new ArrayList<>(1);
        props.add(aspectProp);
        aspectPayload.setProperties(props);

        // Try to create constraint as a Model Administrator
        // MINMAX constraint can only be used with numeric data type.
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

        // Change type
        aspectProp.setDataType("d:datetime");
        // MINMAX constraint can only be used with numeric data type.
        put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);

        // SHA-1126
        { 
            //Change type
            aspectProp.setDataType("d:double");
            parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxValue", "0.0"));
            parameters.add(buildNamedValue("minValue", "-5.0"));
            // Add the parameters into the constraint
            minMaxConstraint.setParameters(parameters);

            aspectProp.setConstraints(Arrays.asList(minMaxConstraint));// Add inline constraint
            props = new ArrayList<>(1);
            props.add(aspectProp);
            aspectPayload.setProperties(props);
            // Maximum value of the MINMAX constraint must be a positive nonzero value.
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 400);
        }
    }

    @Test
    public void testPropDefaultValueWithInlineConstraint() throws Exception
    {
        String modelName = "testModelInlineConstraint" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        {
            // Create RegEx constraint
            String regExConstraintName = "testInlineFileNameRegEx" + System.currentTimeMillis();
            CustomModelConstraint inlineRegExConstraint = new CustomModelConstraint();
            inlineRegExConstraint.setName(regExConstraintName);
            inlineRegExConstraint.setType("REGEX");
            // Create the inline RegEx constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("expression", "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)"));
            parameters.add(buildNamedValue("requiresMatch", "false"));
            // Add the parameters into the constraint
            inlineRegExConstraint.setParameters(parameters);

            // Create aspect
            String aspectName = "testAspect1" + System.currentTimeMillis();
            createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

            // Update the Aspect by adding property
            CustomAspect aspectPayload = new CustomAspect();
            aspectPayload.setName(aspectName);
            final String aspectPropName = "testAspect1Prop1" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property with REGEX constraint");
            aspectProp.setDataType("d:text");
            aspectProp.setDefaultValue("invalid<defaultValue"); // invalid value
            aspectProp.setConstraints(Arrays.asList(inlineRegExConstraint));// Add inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            aspectPayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 409);
        }

        {
            // Create inline anonymous LENGTH constraint
            CustomModelConstraint inlineAnonymousLengthConstraint = new CustomModelConstraint();
            inlineAnonymousLengthConstraint.setType("LENGTH");
            // Create the Length constraint's parameters
            List<CustomModelNamedValue>parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxLength", "4"));
            parameters.add(buildNamedValue("minLength", "0"));
            // Add the parameters into the constraint
            inlineAnonymousLengthConstraint.setParameters(parameters);

            // Create type
            String typeName = "testType1" + System.currentTimeMillis();
            createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", "test type1 Desc", "cm:content");

            // Update the Type by adding property
            CustomType typePayload = new CustomType();
            typePayload.setName(typeName);
            String typePropName = "testType1Prop1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setTitle("property with LENGTH constraint");
            typeProp.setDataType("d:text");
            typeProp.setDefaultValue("abcdef"); // Invalid length
            typeProp.setConstraints(Arrays.asList(inlineAnonymousLengthConstraint)); // inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            typePayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 409);
        }

        {
            // Create inline anonymous MINMAX constraint
            CustomModelConstraint inlineAnonymousMinMaxConstraint = new CustomModelConstraint();
            inlineAnonymousMinMaxConstraint.setType("MINMAX");
            // Create the MinMax constraint's parameters
            List<CustomModelNamedValue>parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxValue", "10"));
            parameters.add(buildNamedValue("minValue", "0"));
            // Add the parameters into the constraint
            inlineAnonymousMinMaxConstraint.setParameters(parameters);

            // Create type
            String typeName = "testType1" + System.currentTimeMillis();
            createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", "test type1 Desc", "cm:content");

            // Update the Type by adding property
            CustomType typePayload = new CustomType();
            typePayload.setName(typeName);
            String typePropName = "testType1Prop1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setTitle("property with MINMAX constraint");
            typeProp.setDataType("d:int");
            typeProp.setDefaultValue("20"); // Not in the defined range [0,10]
            typeProp.setConstraints(Arrays.asList(inlineAnonymousMinMaxConstraint)); // inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            typePayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 409);
        }

        {
            // Create LIST constraint
            String listConstraintName = "testListConstraint" + System.currentTimeMillis();
            CustomModelConstraint inlineListConstraint = new CustomModelConstraint();
            inlineListConstraint.setName(listConstraintName);
            inlineListConstraint.setType("LIST");
            // Create the List constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(3);
            parameters.add(buildNamedValue("allowedValues", null, "one", "two", "three"));
            parameters.add(buildNamedValue("sorted", "false"));
            // Add the parameters into the constraint
            inlineListConstraint.setParameters(parameters);

            // Create aspect
            String aspectName = "testAspect" + System.currentTimeMillis();
            createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

            // Update the Aspect by adding property
            CustomAspect aspectPayload = new CustomAspect();
            aspectPayload.setName(aspectName);
            final String aspectPropName = "testAspect1Prop" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property with LIST constraint");
            aspectProp.setDataType("d:text");
            aspectProp.setDefaultValue("four"); // Not in the list
            aspectProp.setConstraints(Arrays.asList(inlineListConstraint));// Add inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            aspectPayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 409);
        }

        {
            // Create Java Class constraint
            String inlineJavaClassConstraintName = "testJavaClassConstraint" + System.currentTimeMillis();
            CustomModelConstraint inlineListConstraint = new CustomModelConstraint();
            inlineListConstraint.setName(inlineJavaClassConstraintName);
            inlineListConstraint.setType("org.alfresco.rest.api.tests.TestCustomConstraint$DummyJavaClassConstraint");

            // Create aspect
            String aspectName = "testAspect" + System.currentTimeMillis();
            createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

            // Update the Aspect by adding property
            CustomAspect aspectPayload = new CustomAspect();
            aspectPayload.setName(aspectName);
            final String aspectPropName = "testAspect1Prop" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property with Java Class constraint");
            aspectProp.setDataType("d:text");
            aspectProp.setDefaultValue("invalid#value"); // Invalid default value
            aspectProp.setConstraints(Arrays.asList(inlineListConstraint));// Add inline constraint
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            aspectPayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 409);
        }
    }

    @Test
    public void testPropDefaultValueWithConstraintRef() throws Exception
    {
        String modelName = "testModelConstraintRef" + System.currentTimeMillis();
        final Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT);

        {
            // Create List constraint
            String listConstraintName = "testListConstraint" + System.currentTimeMillis();
            CustomModelConstraint listConstraint = new CustomModelConstraint();
            listConstraint.setName(listConstraintName);
            listConstraint.setType("LIST");
            // Create the List constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(3);
            parameters.add(buildNamedValue("allowedValues", null, "London", "Paris", "New York"));// list value
            parameters.add(buildNamedValue("sorted", "false"));
            // Add the parameters into the constraint
            listConstraint.setParameters(parameters);

            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(listConstraint), 201);
            // Retrieve the created List constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, listConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);

            // Create aspect
            String aspectName = "testAspect" + System.currentTimeMillis();
            createTypeAspect(CustomAspect.class, modelName, aspectName, "title", "desc", null);

            // Update the Aspect by adding property
            CustomAspect aspectPayload = new CustomAspect();
            aspectPayload.setName(aspectName);
            final String aspectPropName = "testAspect1Prop" + System.currentTimeMillis();
            CustomModelProperty aspectProp = new CustomModelProperty();
            aspectProp.setName(aspectPropName);
            aspectProp.setTitle("property with LIST constraint ref");
            aspectProp.setDataType("d:text");
            aspectProp.setDefaultValue("Berlin"); // Not in the list
            aspectProp.setConstraintRefs(Arrays.asList(returnedConstraint.getPrefixedName())); // constrain ref
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(aspectProp);
            aspectPayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/aspects", customModelAdmin, aspectName, RestApiUtil.toJsonAsString(aspectPayload), SELECT_PROPS_QS, 409);
        }

        {
            // Create MINMAX constraint
            String minMaxConstraintName = "testMinMaxConstraint" + System.currentTimeMillis();
            CustomModelConstraint minMaxConstraint = new CustomModelConstraint();
            minMaxConstraint.setName(minMaxConstraintName);
            minMaxConstraint.setType("MINMAX");
            // Create the MinMax constraint's parameters
            List<CustomModelNamedValue> parameters = new ArrayList<>(2);
            parameters.add(buildNamedValue("maxValue", "100"));
            parameters.add(buildNamedValue("minValue", "50"));
            // Add the parameters into the constraint
            minMaxConstraint.setParameters(parameters);

            // Create constraint as a Model Administrator
            post("cmm/" + modelName + "/constraints", customModelAdmin, RestApiUtil.toJsonAsString(minMaxConstraint), 201);
            // Retrieve the created MinMax constraint
            HttpResponse response = getSingle("cmm/" + modelName + "/constraints", customModelAdmin, minMaxConstraintName, 200);
            CustomModelConstraint returnedConstraint = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelConstraint.class);

            // Create type
            String typeName = "testType1" + System.currentTimeMillis();
            createTypeAspect(CustomType.class, modelName, typeName, "test type1 title", "test type1 Desc", "cm:content");

            // Update the Type by adding property
            CustomType typePayload = new CustomType();
            typePayload.setName(typeName);
            String typePropName = "testType1Prop1" + System.currentTimeMillis();
            CustomModelProperty typeProp = new CustomModelProperty();
            typeProp.setName(typePropName);
            typeProp.setTitle("property with MINMAX constraint ref");
            typeProp.setDataType("d:int");
            typeProp.setDefaultValue("35"); // Not in the defined range [50,100]
            typeProp.setConstraintRefs(Arrays.asList(returnedConstraint.getPrefixedName())); // constrain ref
            List<CustomModelProperty> props = new ArrayList<>(1);
            props.add(typeProp);
            typePayload.setProperties(props);

            // Try to create the property - constraint violation
            put("cmm/" + modelName + "/types", customModelAdmin, typeName, RestApiUtil.toJsonAsString(typePayload), SELECT_PROPS_QS, 409);
        }
    }

    public static class DummyJavaClassConstraint extends AbstractConstraint
    {
        @Override
        protected void evaluateSingleValue(Object value)
        {
            String checkValue = DefaultTypeConverter.INSTANCE.convert(String.class, value);

            if (checkValue.contains("#"))
            {
                throw new ConstraintException("The value must not contain '#'");
            }
        }
    }
}
