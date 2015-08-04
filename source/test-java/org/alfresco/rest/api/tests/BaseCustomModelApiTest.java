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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.CustomModelServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.model.AbstractClassModel;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModelConstraint;
import org.alfresco.rest.api.model.CustomModelNamedValue;
import org.alfresco.rest.api.model.CustomModelProperty;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for CMM API tests
 *
 * @author Jamal Kaabi-Mofrad
 */
public class BaseCustomModelApiTest extends EnterpriseTestApi
{
    public static final String CMM_SCOPE = "private";
    public static final String SELECT_PROPS_QS = "?select=props";
    public static final String SELECT_STATUS_QS = "?select=status";
    public static final String SELECT_ALL = "?select=all";
    public static final String SELECT_ALL_PROPS = "?select=allProps";

    protected String nonAdminUserName;
    protected String customModelAdmin;

    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;
    protected CustomModelService customModelService;

    private List<String> users = new ArrayList<>();

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);
        customModelService = applicationContext.getBean("customModelService", CustomModelService.class);

        final AuthorityService authorityService = applicationContext.getBean("authorityService", AuthorityService.class);

        this.nonAdminUserName = createUser("nonAdminUser" + System.currentTimeMillis());
        this.customModelAdmin = createUser("customModelAdmin" + System.currentTimeMillis());
        users.add(nonAdminUserName);
        users.add(customModelAdmin);
        // Add 'customModelAdmin' user into 'ALFRESCO_MODEL_ADMINISTRATORS' group
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                authorityService.addAuthority(CustomModelServiceImpl.GROUP_ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY, customModelAdmin);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        for (final String user : users)
        {
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    authenticationService.deleteAuthentication(user);
                    personService.deletePerson(user);
                    return null;
                }
            });
        }
        users.clear();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    protected String createUser(String username)
    {
        PersonInfo personInfo = new PersonInfo(username, username, username, "password", null, null, null, null, null, null, null);
        TestPerson person = repoService.createUser(personInfo, username, null);
        return person.getId();
    }

    protected HttpResponse post(String url, String runAsUser, String body, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.post(CMM_SCOPE, url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, String body,  String queryString, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(CMM_SCOPE, url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(String url, String runAsUser, Paging paging, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        Map<String, String> params = (paging == null) ? null : createParams(paging, null);

        HttpResponse response = publicApiClient.get(CMM_SCOPE, url, null, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(String url, String runAsUser, String entityId, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(CMM_SCOPE, url, entityId, null, null, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse put(String url, String runAsUser, String entityId, String body, String queryString, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            entityId += queryString;
        }
        HttpResponse response = publicApiClient.put(CMM_SCOPE, url, entityId, null, null, body, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse delete(String url, String runAsUser, String entityId, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.delete(CMM_SCOPE, url, entityId, null, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected void checkStatus(int expectedStatus, int actualStatus)
    {
        if (expectedStatus > 0 && expectedStatus != actualStatus)
        {
            fail("Status code " + actualStatus + " returned, but expected " + expectedStatus);
        }
    }

    protected CustomModel createCustomModel(String modelName, Pair<String, String> namespacePair, ModelStatus status) throws Exception
    {
        return createCustomModel(modelName, namespacePair, status, "Test model description", null);
    }

    protected CustomModel createCustomModel(String modelName, Pair<String, String> namespacePair, ModelStatus status, String desc, String author)
                throws Exception
    {
        CustomModel customModel = new CustomModel();
        customModel.setName(modelName);
        customModel.setNamespaceUri(namespacePair.getFirst());
        customModel.setNamespacePrefix(namespacePair.getSecond());
        customModel.setDescription(desc);
        customModel.setStatus(status);
        customModel.setAuthor(author);

        // Create the model as a Model Administrator
        HttpResponse response = post("cmm", customModelAdmin, RestApiUtil.toJsonAsString(customModel), 201);
        CustomModel returnedModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModel.class);
        if (author == null)
        {
            // ignore 'author' in the comparison
            compareCustomModels(customModel, returnedModel, "author");
        }
        else
        {
            compareCustomModels(customModel, returnedModel);
        }

        return customModel;
    }

    protected <T extends AbstractClassModel> T createTypeAspect(Class<T> glazz, String modelName, String typeAspectName, String title, String desc,
                String parent) throws Exception
    {
        AbstractClassModel classModel = null;
        String uri = "cmm/" + modelName;
        if (glazz.equals(CustomType.class))
        {
            classModel = new CustomType();
            uri += "/types";
        }
        else
        {
            classModel = new CustomAspect();
            uri += "/aspects";
        }

        classModel.setName(typeAspectName);
        classModel.setDescription(desc);
        classModel.setTitle(title);
        classModel.setParentName(parent);

        // Create type as a Model Administrator
        HttpResponse response = post(uri, customModelAdmin, RestApiUtil.toJsonAsString(classModel), 201);
        T returnedClassModel = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), glazz);

        compareCustomTypesAspects(classModel, returnedClassModel, "prefixedName");

        return returnedClassModel;
    }

    protected void compareCustomModels(CustomModel expectedModel, CustomModel actualModel, String... excludeFields)
    {
        boolean result = EqualsBuilder.reflectionEquals(expectedModel, actualModel, excludeFields);
        assertTrue("Two models are not equal. Expected:<" + expectedModel.toString() + "> but was:<" + actualModel.toString() + ">", result);
    }

    protected void compareCustomTypesAspects(AbstractClassModel expectedDetails, AbstractClassModel actualDetails, String... excludeFields)
    {
        List<CustomModelProperty> expectedProps = expectedDetails.getProperties();
        List<CustomModelProperty> actualProps = actualDetails.getProperties();
        // Sort them
        sortIfnotNull(expectedProps);
        sortIfnotNull(actualProps);

        boolean propEqualResult = true;
        if (expectedProps.size() == actualProps.size())
        {
            for (int i = 0, size = expectedProps.size(); i < size; i++)
            {
                boolean equalProp = EqualsBuilder.reflectionEquals(expectedProps.get(i), actualProps.get(i), excludeFields);
                if (!equalProp)
                {
                    propEqualResult = false;
                    break;
                }
            }
        }
        else
        {
            propEqualResult = false;
        }

        if (excludeFields.length > 0)
        {
            int size = excludeFields.length;
            excludeFields = Arrays.copyOf(excludeFields, size + 1);
            excludeFields[size] = "properties";
        }
        boolean result = EqualsBuilder.reflectionEquals(expectedDetails, actualDetails, excludeFields);

        String typesAspects = (expectedDetails instanceof CustomAspect) ? "aspects" : "types";
        assertTrue("Two " + typesAspects + " are not equal. Expected:<" + expectedDetails.toString() + "> but was:<" + actualDetails.toString() + ">",
                    (result && propEqualResult));
    }

    protected void compareCustomModelConstraints(CustomModelConstraint expectedConstraint, CustomModelConstraint actualConstraint, String... excludeFields)
    {
        boolean result = EqualsBuilder.reflectionEquals(expectedConstraint, actualConstraint, excludeFields);
        assertTrue("Two constraints are not equal. Expected:<" + expectedConstraint.toString() + "> but was:<" + actualConstraint.toString() + ">", result);
    }

    protected void compareCustomModelProperties(CustomModelProperty expectedProperty, CustomModelProperty actualProperty, String... excludeFields)
    {
        boolean result = EqualsBuilder.reflectionEquals(expectedProperty, actualProperty, excludeFields);
        assertTrue("Two constraints are not equal. Expected:<" + expectedProperty.toString() + "> but was:<" + actualProperty.toString() + ">", result);
    }

    protected Pair<String, String> getTestNamespaceUriPrefixPair()
    {
        long timeMillis = System.currentTimeMillis();
        String uri = "http://www.alfresco.org/model/testcmmnamespace" + timeMillis + "/1.0";
        String prefix = "testcmm" + timeMillis;

        return new Pair<String, String>(uri, prefix);
    }

    protected CustomModelDefinition getModelDefinition(final String modelName)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            @Override
            public CustomModelDefinition execute() throws Throwable
            {
                return customModelService.getCustomModel(modelName);
            }
        });
    }

    protected void sortIfnotNull(List<CustomModelProperty> list)
    {
        if (list != null && list.size() > 0)
        {
            Collections.sort(list);
        }
    }

    protected boolean hasNamespaceUri(Collection<NamespaceDefinition> namespaces, String expectedNamespaceUri)
    {
        for (NamespaceDefinition ns : namespaces)
        {
            if (ns.getUri().equals(expectedNamespaceUri))
            {
                return true;
            }
        }

        return false;
    }

    protected boolean hasNamespacePrefix(Collection<NamespaceDefinition> namespaces, String expectedNamespacePrefix)
    {
        for (NamespaceDefinition ns : namespaces)
        {
            if (ns.getPrefix().equals(expectedNamespacePrefix))
            {
                return true;
            }
        }

        return false;
    }

    protected CustomModelProperty getProperty(List<CustomModelProperty> properties, String propName)
    {
        for (CustomModelProperty prop : properties)
        {
            if (prop.getName().equals(propName))
            {
                return prop;
            }
        }
        return null;
    }

    protected CustomModelNamedValue buildNamedValue(String name, String simpleValue, String... listValue)
    {
        CustomModelNamedValue namedValue = new CustomModelNamedValue();
        namedValue.setName(name);
        namedValue.setSimpleValue(simpleValue);
        if (listValue.length > 0)
        {
            namedValue.setListValue(Arrays.asList(listValue));
        }

        return namedValue;
    }

    protected String getParameterSimpleValue(List<CustomModelNamedValue> params, String paramName)
    {
        for (CustomModelNamedValue p : params)
        {
            if (p.getName().equals(paramName))
            {
                return p.getSimpleValue();
            }
        }
        return null;
    }

    protected List<String> getParameterListValue(List<CustomModelNamedValue> params, String paramName)
    {
        for (CustomModelNamedValue p : params)
        {
            if (p.getName().equals(paramName))
            {
                return p.getListValue();
            }
        }
        return null;
    }
}
