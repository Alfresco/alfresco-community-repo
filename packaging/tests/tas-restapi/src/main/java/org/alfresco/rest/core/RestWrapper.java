/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.core;

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.oauth2;

import static org.alfresco.utility.report.log.Step.STEP;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestHtmlResponse;
import org.alfresco.rest.model.RestSiteContainerModelsCollection;
import org.alfresco.rest.model.RestSiteMemberModelsCollection;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.rest.model.RestSyncSetRequestModel;
import org.alfresco.rest.model.RestTextResponse;
import org.alfresco.rest.requests.AdminConsole;
import org.alfresco.rest.requests.Tenant;
import org.alfresco.rest.requests.aosAPI.RestAosAPI;
import org.alfresco.rest.requests.authAPI.RestAuthAPI;
import org.alfresco.rest.requests.cmisAPI.RestCmisAPI;
import org.alfresco.rest.requests.coreAPI.RestCoreAPI;
import org.alfresco.rest.requests.discoveryAPI.RestDiscoveryAPI;
import org.alfresco.rest.requests.modelAPI.RestModelAPI;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.alfresco.rest.requests.search.SearchAPI;
import org.alfresco.rest.requests.search.SearchSQLAPI;
import org.alfresco.rest.requests.search.SearchSQLJDBC;
import org.alfresco.rest.requests.search.ShardInfoAPI;
import org.alfresco.rest.requests.search.SolrAPI;
import org.alfresco.rest.requests.search.SolrAdminAPI;
import org.alfresco.rest.requests.workflowAPI.RestWorkflowAPI;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.Utility;
import org.alfresco.utility.dsl.DSLWrapper;
import org.alfresco.utility.model.StatusModel;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

@SuppressWarnings("deprecation")
@Service
@Scope(value = "prototype")
public class RestWrapper extends DSLWrapper<RestWrapper>
{
    private static final Integer IGNORE_CONTENT_LIMIT_BYTES = 4 * 1024 * 1024;

    @Autowired
    protected RestProperties restProperties;

    private Logger LOG = LogFactory.getLogger();

    private RequestSpecification request;
    private RestErrorModel lastError;
    private StatusModel lastStatusModel;
    private Object lastException = ""; // handle values of last exception thrown
    private UserModel currentUser;
    private String statusCode;
    private String parameters = "";
    private ContentType defaultContentType = ContentType.JSON;
    private RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
    private Headers responseHeaders;
    private RestResponse response;
    private String serverURI;
    private int serverPort;

    /**
     * After configuring {@link #setServerURI(String)} and {@link #setServerPort(int)} call {@link #configureServerEndpoint()}
     *
     * @param serverURI
     *            in format of "http://localhost", without port. Set port via {@link #setServerPort(int)}
     */
    public void setServerURI(String serverURI)
    {
        this.serverURI = serverURI;
    }

    public void setServerPort(int serverPort)
    {
        this.serverPort = serverPort;
    }

    @Autowired
    private RestAisAuthentication aisAuthentication;

    public void setResponseHeaders(Headers responseHeaders)
    {
        this.responseHeaders = responseHeaders;
    }

    public Headers getResponseHeaders()
    {
        return responseHeaders;
    }

    /**
     * Verify response header contains a specific value Example: assertHeaderValueContains("Content-Disposition", "filename=\"myfile.txt\"");
     * 
     * @param headerName
     *            the header name from response
     * @param expectedHeaderValue
     *            the header property value to be checked
     * @return
     */
    public RestWrapper assertHeaderValueContains(String headerName, String expectedHeaderValue)
    {
        STEP(String.format("REST API: Assert that header value contains %s", expectedHeaderValue));
        String actualHeaderValue = getResponseHeaders().getValue(headerName);
        Assert.assertTrue(getResponseHeaders().getValue(headerName).contains(expectedHeaderValue),
                String.format("Header %s is %s", headerName, actualHeaderValue));
        return this;
    }

    @PostConstruct
    public void initializeRequestSpecBuilder()
    {
        this.serverURI = restProperties.envProperty().getTestServerUrl();
        this.serverPort = restProperties.envProperty().getPort();
        configureServerEndpoint();
    }

    /**
     * Authenticate specific user to Alfresco REST API
     * 
     * @param userModel
     * @return
     */
    public RestWrapper authenticateUser(UserModel userModel)
    {
        STEP(String.format("REST API: Basic Authentication using user {%s}", userModel.toString()));
        currentUser = userModel;
        setTestUser(userModel);
        return this;
    }

    public RestWrapper noAuthentication()
    {
        STEP("REST API: No Authentication");
        currentUser = null;
        setTestUser(null);
        return this;
    }

    /**
     * Request sent to server
     */
    protected RequestSpecification onRequest()
    {
        if (currentUser != null)
        {
            if (aisAuthentication.isAisAuthenticationEnabled())
            {
                configureRequestSpec().setAuth(oauth2(aisAuthentication.getAisAuthenticationToken(currentUser)));
            }
            else
            {
                configureRequestSpec().setAuth(basic(currentUser.getUsername(), currentUser.getPassword()));
            }
        }

        request = given().spec(configureRequestSpec().build());

        // reset to default as JSON
        usingContentType(ContentType.JSON);
        return request;
    }

    /**
     * @return the last error model thrown if any
     */
    private RestErrorModel getLastError()
    {
        if (lastError == null)
            return new RestErrorModel();
        else
            return lastError;
    }

    public void setLastError(RestErrorModel errorModel)
    {
        lastError = errorModel;
    }

    public RestErrorModel assertLastError()
    {

        return getLastError();
    }

    public StatusModel assertLastStatus()
    {
        return getLastStatus();
    }

    public RestWrapper assertLastExceptionContains(String exception)
    {
        if (!lastException.toString().contains(exception))
            Assert.fail(String.format("Expected exception {%s} but found {%s}", exception, lastException));

        return this;
    }

    /**
     * Process responses for a collection of models as {@link RestSiteModelsCollection}
     *
     * @throws JsonToModelConversionException
     *             If the response cannot be converted to the given model.
     * @throws EmptyJsonResponseException
     *             If there is no response from the server.
     */

    public <T> T processModels(Class<T> classz, RestRequest restRequest)
            throws EmptyJsonResponseException, JsonToModelConversionException
    {
        T models = callAPIAndCreateModel(classz, restRequest, "list");

        if (models == null)
        {
            try
            {
                return classz.getDeclaredConstructor().newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return models;
    }

    /**
     * Process responses for a single model as {@link RestSiteModel}
     *
     * @throws JsonToModelConversionException
     *             If the response cannot be converted to the given model.
     * @throws EmptyJsonResponseException
     *             If there is no response from the server.
     */
    public <T> T processModel(Class<T> classz, RestRequest restRequest)
            throws EmptyJsonResponseException, JsonToModelConversionException
    {
        T model = callAPIAndCreateModel(classz, restRequest, "entry");

        if (model == null)
        {
            try
            {
                return classz.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return model;
    }

    /**
     * Send the request and convert the response to the appropriate model.
     *
     * @param classz
     *            The class of the model to create.
     * @param restRequest
     *            The request to send.
     * @param path
     *            The path to the part of the response from which the model should be populated.
     * @return The populated model object.
     */
    private <T> T callAPIAndCreateModel(Class<T> classz, RestRequest restRequest, String path)
    {
        Response returnedResponse = sendRequest(restRequest);

        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        boolean responseHasErrors = checkForJsonError(returnedResponse);
        boolean responseHasExceptions = checkForJsonStatusException(returnedResponse);

        T models = null;

        if (!responseHasExceptions && !responseHasErrors)
        {
            try
            {
                models = returnedResponse.jsonPath().getObject(path, classz);
                validateJsonModelSchema(classz, models);
            }
            catch (Exception processError)
            {
                processError.printStackTrace();
                throw new JsonToModelConversionException(classz, processError);
            }
        }
        return models;
    }

    /**
     * Process responses for a single model as {@link RestSiteModel}
     *
     * @throws JsonToModelConversionException
     *             If the response cannot be converted to the given model.
     * @throws EmptyJsonResponseException
     *             If there is no response from the server.
     */
    public JSONObject processJson(RestRequest restRequest)
            throws EmptyJsonResponseException, JsonToModelConversionException
    {
        Response returnedResponse = sendRequest(restRequest);

        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        boolean responseHasErrors = checkForJsonError(returnedResponse);
        boolean responseHasExceptions = checkForJsonStatusException(returnedResponse);

        JSONObject response = null;

        try
        {
            if (!responseHasExceptions && !responseHasErrors)
            {
                JSONObject jsonObject = new JSONObject(returnedResponse.getBody().asString());
                response = jsonObject.getJSONObject("entry");
            }
        }
        catch (Exception processError)
        {
            throw new EmptyJsonResponseException(processError.getMessage());
        }

        return response;
    }

    /**
     * Process responses for site relations models, such as {@link RestSiteModel, RestSiteContainerModelsCollection, RestSiteMemberModelsCollection}
     */
    public List<Object> processRelationsJson(RestRequest restRequest)
    {
        List<Object> jsonObjects = new ArrayList<Object>();
        Response returnedResponse = sendRequest(restRequest);

        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        boolean responseHasErrors = checkForJsonError(returnedResponse);
        boolean responseHasExceptions = checkForJsonStatusException(returnedResponse);

        ObjectMapper mapper = new ObjectMapper();
        JSONObject response = null;

        try
        {
            if (!responseHasExceptions && !responseHasErrors)
            {
                JSONObject jsonObject = new JSONObject(returnedResponse.getBody().asString());

                response = jsonObject.getJSONObject("entry");
                RestSiteModel site = mapper.readValue(response.toString(), RestSiteModel.class);
                jsonObjects.add(site);

                if (!jsonObject.getJSONObject("relations").isNull("containers"))
                {
                    response = jsonObject.getJSONObject("relations").getJSONObject("containers").getJSONObject("list");
                    RestSiteContainerModelsCollection containers = mapper.readValue(response.toString(),
                            RestSiteContainerModelsCollection.class);
                    jsonObjects.add(containers);
                }

                if (!jsonObject.getJSONObject("relations").isNull("members"))
                {
                    response = jsonObject.getJSONObject("relations").getJSONObject("members").getJSONObject("list");
                    RestSiteMemberModelsCollection members = mapper.readValue(response.toString(),
                            RestSiteMemberModelsCollection.class);
                    jsonObjects.add(members);
                }
            }
        }
        catch (Exception processError)
        {
            throw new EmptyJsonResponseException(processError.getMessage());
        }

        return jsonObjects;
    }

    /**
     * Process responses for site relations models, such as {@link RestSiteModel, RestSiteContainerModelsCollection, RestSiteMemberModelsCollection}
     */
    public List<List<Object>> processSitesRelationsJson(RestRequest restRequest)
    {
        List<List<Object>> allObjects = new ArrayList<List<Object>>();
        List<Object> sitesList = new ArrayList<Object>();
        List<Object> containersList = new ArrayList<Object>();
        List<Object> membersList = new ArrayList<Object>();

        Response returnedResponse = sendRequest(restRequest);

        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        boolean responseHasErrors = checkForJsonError(returnedResponse);
        boolean responseHasExceptions = checkForJsonStatusException(returnedResponse);

        ObjectMapper mapper = new ObjectMapper();
        JSONObject response = null;

        try
        {
            if (!responseHasExceptions && !responseHasErrors)
            {
                JSONObject jsonObject = new JSONObject(returnedResponse.getBody().asString());

                if (jsonObject.getJSONObject("list").getJSONArray("entries").length() != 0)
                {
                    {
                        for (int i = 0; i < jsonObject.getJSONObject("list").getJSONArray("entries").length(); i++)
                        {
                            response = jsonObject.getJSONObject("list").getJSONArray("entries").getJSONObject(i)
                                    .getJSONObject("entry");
                            RestSiteModel site = mapper.readValue(response.toString(), RestSiteModel.class);
                            sitesList.add(site);
                        }

                        allObjects.add(sitesList);
                    }

                    if (jsonObject.toString().contains("containers"))
                    {
                        for (int i = 0; i < jsonObject.getJSONObject("list").getJSONArray("entries").length(); i++)
                        {
                            response = jsonObject.getJSONObject("list").getJSONArray("entries").getJSONObject(i)
                                    .getJSONObject("relations").getJSONObject("containers").getJSONObject("list");
                            RestSiteContainerModelsCollection containers = mapper.readValue(response.toString(),
                                    RestSiteContainerModelsCollection.class);
                            containersList.add(containers);
                        }

                        allObjects.add(containersList);
                    }

                    if (jsonObject.toString().contains("members"))
                    {
                        for (int i = 0; i < jsonObject.getJSONObject("list").getJSONArray("entries").length(); i++)
                        {
                            response = jsonObject.getJSONObject("list").getJSONArray("entries").getJSONObject(i)
                                    .getJSONObject("relations").getJSONObject("members").getJSONObject("list");
                            RestSiteMemberModelsCollection members = mapper.readValue(response.toString(),
                                    RestSiteMemberModelsCollection.class);
                            membersList.add(members);
                        }

                        allObjects.add(membersList);
                    }
                }
            }
        }
        catch (Exception processError)
        {
            throw new EmptyJsonResponseException(processError.getMessage());
        }

        return allObjects;
    }

    /**
     * Process a response that returns a html
     *
     * @param restRequest
     * @return
     * @throws EmptyJsonResponseException
     *             If there is no response from the server.
     */
    public RestHtmlResponse processHtmlResponse(RestRequest restRequest) throws EmptyJsonResponseException
    {
        Response returnedResponse = sendRequest(restRequest);

        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        if (returnedResponse.contentType().contains("json"))
        {
            checkForJsonError(returnedResponse);
            checkForJsonStatusException(returnedResponse);
        }

        return new RestHtmlResponse(returnedResponse.getHeaders(), returnedResponse.getBody());
    }

    /**
     * Generic REST API call on a {@link RestRequest}
     * 
     * @param restRequest
     * @return
     */
    public RestResponse process(RestRequest restRequest)
    {
        Response returnedResponse = sendRequest(restRequest);
        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));
        RestResponse response = new RestResponse(returnedResponse);
        setResponseHeaders(response.getResponse().getHeaders());
        return response;
    }

    public RestTextResponse processTextResponse(RestRequest restRequest)
    {
        Response returnedResponse = sendRequest(restRequest);
        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));
        if (returnedResponse.contentType().contains("text/plain"))
        {
            RestAssured.registerParser("text/plain", Parser.TEXT);

            RestTextResponse testResponse = new RestTextResponse(returnedResponse);
            return testResponse;
        }

        RestResponse response = new RestResponse(returnedResponse);
        setResponseHeaders(response.getResponse().getHeaders());
        RestTextResponse testResponse = new RestTextResponse(returnedResponse);
        return testResponse;
    }

    /**
     * Process a response that has no body - basically will need only the status code from it
     * 
     * @param restRequest
     * @throws EmptyJsonResponseException
     */
    public void processEmptyModel(RestRequest restRequest) throws EmptyJsonResponseException
    {
        Response returnedResponse = sendRequest(restRequest);
        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        if (!returnedResponse.asString().isEmpty())
        {
            checkForJsonError(returnedResponse);
            checkForJsonStatusException(returnedResponse);
        }

    }

    public StatusModel getLastStatus()
    {
        return lastStatusModel;
    }

    /**
     * Set the status code for the latest REST call
     * 
     * @param lastStatusModel
     */
    public void setLastStatus(StatusModel lastStatusModel)
    {
        this.lastStatusModel = lastStatusModel;
    }

    public String getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(String statusCode)
    {
        this.statusCode = statusCode;
    }

    /**
     * Send REST request based on HTTP method
     * 
     * @param restRequest
     * @return
     */
    protected Response sendRequest(RestRequest restRequest)
    {
        // If there are unused parameters then include them in the request.
        String parameters = getParameters();
        if (parameters != null && !parameters.isEmpty())
        {
            restRequest.setPathParams(ArrayUtils.addAll(restRequest.getPathParams(), parameters));
        }

        STEP(restRequest.toString());

        final Response returnedResponse;
        HttpMethod httpMethod = restRequest.getHttpMethod();
        if (HttpMethod.GET.equals(httpMethod))
        {
            returnedResponse = onRequest().get(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.DELETE.equals(httpMethod))
        {
            returnedResponse = onRequest().delete(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.HEAD.equals(httpMethod))
        {
            returnedResponse = onRequest().head(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.OPTIONS.equals(httpMethod))
        {
            returnedResponse = onRequest().options(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.POST.equals(httpMethod))
        {
            returnedResponse = onRequest().body(restRequest.getBody())
                    .post(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.PUT.equals(httpMethod))
        {
            returnedResponse = onRequest().body(restRequest.getBody())
                    .contentType(ContentType.JSON.withCharset(restRequest.getContentType()))
                    .put(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.TRACE.equals(httpMethod))
        {
            returnedResponse = onRequest().get(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else if (HttpMethod.PATCH.equals(httpMethod))
        {
            returnedResponse = onRequest().body(restRequest.getBody())
                    .patch(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }
        else
        {
            returnedResponse = onRequest().get(restRequest.getPath(), restRequest.getPathParams()).andReturn();
        }

        logResponseInformation(restRequest, returnedResponse);

        configureServerEndpoint();
        response = new RestResponse(returnedResponse);
        return returnedResponse;
    }

    private void logResponseInformation(RestRequest restRequest, Response returnedResponse)
    {
        String responseSizeString = returnedResponse.getHeader("Content-Length");
        if (responseSizeString != null && Integer.valueOf(responseSizeString) > IGNORE_CONTENT_LIMIT_BYTES)
        {
            LOG.info("On {} {}, received a response size that was {} bytes.\n"
                    + "This is bigger than the limit of {} bytes so its content will not be displayed: \n", restRequest.getHttpMethod(),
                    restRequest.getPath(), Integer.valueOf(responseSizeString), IGNORE_CONTENT_LIMIT_BYTES);
        }
        else
        {
            if (returnedResponse.asString().isEmpty())
            {
                LOG.info("On {} {}, received the following response \n{}", restRequest.getHttpMethod(), restRequest.getPath(),
                        returnedResponse.getStatusCode());
            }
            else if (returnedResponse.getContentType().contains("image/png"))
            {
                LOG.info("On {} {}, received the response with an image and headers: \n{}", restRequest.getHttpMethod(), restRequest.getPath(),
                        returnedResponse.getHeaders().toString());
            }
            else if (returnedResponse.getContentType().contains("application/json"))
            {
                LOG.info("On {} {}, received the following response \n{}", restRequest.getHttpMethod(), restRequest.getPath(),
                        Utility.prettyPrintJsonString(returnedResponse.asString()));
            }
            else if (returnedResponse.getContentType().contains("application/xml"))
            {
                String response = parseXML(returnedResponse);
                LOG.info("On {} {}, received the following response \n{}", restRequest.getHttpMethod(), restRequest.getPath(), response);
            }
            else
            {
                LOG.info("On {} {}, received the following response \n{}", restRequest.getHttpMethod(), restRequest.getPath(),
                        ToStringBuilder.reflectionToString(returnedResponse.asString(), ToStringStyle.MULTI_LINE_STYLE));
            }
        }
    }

    private String parseXML(Response returnedResponse)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        String result = "";
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(returnedResponse.asString()));
            Document document = db.parse(is);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            result = out.toString();
        }
        catch (Exception e)
        {
            result = "Error Parsing XML file returned: " + e.getMessage();
            setStatusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        return result;
    }

    /**
     * Check if returned response contains an error (error node)
     * 
     * @param returnedResponse
     * @throws EmptyJsonResponseException
     */
    private boolean checkForJsonError(Response returnedResponse) throws EmptyJsonResponseException
    {
        setLastError(null);

        try
        {
            // check for empty json response
            returnedResponse.jsonPath().get();
        }
        catch (Exception e)
        {
            throw new EmptyJsonResponseException(e.getMessage());
        }
        Object error = returnedResponse.jsonPath().get("error");
        if (error != null)
        {
            setLastError(returnedResponse.jsonPath().getObject("error", RestErrorModel.class));
            return true;
        }

        return false;
    }

    /**
     * Check if returned response contains an exception (status node)
     * 
     * @param returnedResponse
     * @throws EmptyJsonResponseException
     */
    private boolean checkForJsonStatusException(Response returnedResponse) throws EmptyJsonResponseException
    {
        try
        {
            // check for empty json response
            lastException = returnedResponse.jsonPath().get("exception");
        }
        catch (Exception e)
        {
            throw new EmptyJsonResponseException(e.getMessage());
        }
        if (lastException == null)
            lastException = "";

        Object error = returnedResponse.jsonPath().get("status");
        if (error != null)
        {
            setLastStatus(returnedResponse.jsonPath().getObject("status", StatusModel.class));
            LOG.error("Exception thrown on response: {}", getLastStatus().toInfo());
            return true;
        }

        return false;
    }

    /**
     * Assert that a specific status code is returned
     * 
     * @param statusCode @return;
     */
    public RestWrapper assertStatusCodeIs(HttpStatus statusCode)
    {
        STEP(String.format("REST API: Assert that status code is %s", statusCode.toString()));
        Assert.assertEquals(getStatusCode(), String.valueOf(statusCode.value()), "Status code is not as expected.");

        return this;
    }

    /**
     * Backtrack algorithm to gather all declared fields within SuperClasses but stopping on TestModel.class
     * 
     * @param fields
     * @param classz
     * @return
     */
    private List<Field> getAllDeclaredFields(List<Field> fields, Class<?> classz)
    {
        if (classz.isAssignableFrom(TestModel.class))
        {
            return fields;
        }

        fields.addAll(Arrays.asList(classz.getDeclaredFields()));

        if (classz.getSuperclass() != null)
        {
            fields = getAllDeclaredFields(fields, classz.getSuperclass());
        }

        return fields;
    }

    /**
     * Check that REST response has returned all required fields
     * 
     * @param classz
     * @param classzInstance
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public <T> void validateJsonModelSchema(Class<T> classz, Object classzInstance)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
    {
        List<Field> allFields = getAllDeclaredFields(new LinkedList<Field>(), classz);

        for (Field field : allFields)
        {
            /* check for required fields */
            if (field.isAnnotationPresent(JsonProperty.class))
            {
                if (field.getAnnotation(JsonProperty.class).required())
                {
                    // we make it accessible to get the value
                    field.setAccessible(true);

                    // now obtaining the field value from instance
                    Object fieldValue = field.get(classzInstance);
                    String info = String.format("Checking required field [%s] from class %s - value: %s",
                            field.getName(), classzInstance.getClass().getName(), fieldValue);
                    LOG.info(info);
                    Assert.assertNotNull(fieldValue, info);

                    // continue for non-primitive objects
                    if (!field.getType().isPrimitive())
                    {
                        validateJsonModelSchema(fieldValue.getClass(), fieldValue);
                    }
                }
            }
        }
    }

    /**
     * Get and clear the stored parameters.
     *
     * @return parameters that you could pass on the request ?param=value
     */
    public String getParameters()
    {
        String localParam = parameters;
        clearParameters();
        return localParam;
    }

    /**
     * Define the entire string of parameters that will be send to request Don't forget to call {@link #getParameters()} in the request to enable this.
     * 
     * @param parameters
     */
    public void setParameters(String parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Just clear the parameters sent
     */
    public void clearParameters()
    {
        setParameters("");
    }

    /**
     * just clear the base path
     * 
     * @return
     */
    public RestWrapper clearBasePath()
    {
        this.configureRequestSpec().setBasePath("");
        return this;
    }

    /**
     * Send key=value parameters.
     * <p>
     * Note that this will replace any existing parameters.
     *
     * @param parameters
     *            A list of URL query parameters - e.g. "maxItems=10000"
     * @return The RestWrapper
     */
    public RestWrapper withParams(String... parameters)
    {
        String paramsStr = Arrays.stream(parameters).collect(Collectors.joining("&"));
        setParameters(paramsStr);
        return this;
    }

    /**
     * @return {@link RestCoreAPI} using the rest Core API as prefix: {@link /alfresco/api/-default-/public/alfresco/versions/1}
     */
    public RestCoreAPI withCoreAPI()
    {
        return new RestCoreAPI(this);
    }

    /**
     * @return {@link RestWorkflowAPI} using the rest Workflow API with prefix: {@link /alfresco/api/-default-/public/workflow/versions/1 }
     */
    public RestWorkflowAPI withWorkflowAPI()
    {
        return new RestWorkflowAPI(this);
    }

    /**
     * @return {@link RestAuthAPI} using the rest Auth API with prefix: {@link /alfresco/api/-default-/public/authentication/versions/1 }
     */
    public RestAuthAPI withAuthAPI()
    {
        return new RestAuthAPI(this);
    }

    public RestModelAPI withModelAPI()
    {
        return new RestModelAPI(this);
    }

    public SearchAPI withSearchAPI()
    {
        return new SearchAPI(this);
    }

    public SearchSQLAPI withSearchSqlAPI()
    {
        return new SearchSQLAPI(this);
    }

    public SearchSQLJDBC withSearchSqlViaJDBC()
    {
        return new SearchSQLJDBC(this);
    }

    public ShardInfoAPI withShardInfoAPI()
    {
        return new ShardInfoAPI(this);
    }

    public SolrAPI withSolrAPI()
    {
        return new SolrAPI(this);
    }

    public SolrAdminAPI withSolrAdminAPI()
    {
        return new SolrAdminAPI(this);
    }

    /**
     * @return {@link RestDiscoveryAPI} using the rest Discovery API as prefix: {@link /alfresco/api/discovery}
     */
    public RestDiscoveryAPI withDiscoveryAPI()
    {
        return new RestDiscoveryAPI(this);
    }

    /**
     * @return {@link AdminConsole} using the Admin Console API as prefix: {@link /alfresco/service/api/server}
     */
    public AdminConsole withAdminConsole()
    {
        return new AdminConsole(this);
    }

    /**
     * Provides DSL on creating Tenant users
     * 
     * @return {@link Tenant}
     */
    public Tenant usingTenant()
    {
        return new Tenant(this, restProperties);
    }

    /**
     * Construct the Where clause of any REST API call You can use the where parameter to restrict the list in the response to entries of a specific kind. The where parameter takes a value.
     */
    public RestWrapper where(String whereExpression)
    {
        String whereClause = "where=(%s)";

        return withParams(String.format(whereClause, whereExpression));
    }

    public ContentType getDefaultContentType()
    {
        return defaultContentType;
    }

    public RestWrapper usingContentType(ContentType defaultContentType)
    {
        this.defaultContentType = defaultContentType;
        return this;
    }

    /**
     * You can handle the request sent to server by calling this method. If for example you want to sent multipart form data you can use: <code>
     * restClient.configureRequestSpec() 
                    .addMultiPart("filedata", Utility.getResourceTestDataFile("restapi-resource"))
                    .addFormParam("renditions", "doclib")
                    .addFormParam("autoRename", true);
                    
       restClient.withCoreAPI().usingNode(ContentModel.my()).createNode();             
     * </code> This will create the node using the multipart data defined.
     * 
     * @return
     */
    public RequestSpecBuilder configureRequestSpec()
    {
        return this.requestSpecBuilder;
    }

    /**
     * Perform CMIS browser binding calls ("alfresco/api/-default-/public/cmis/versions/1.1/browser") with Rest API
     * 
     * @return {@link RestCmisAPI}
     */
    public RestCmisAPI withCMISApi()
    {
        return new RestCmisAPI(this);
    }

    /**
     * Perform AOS browser binding calls ("alfresco/aos") with Rest API
     * 
     * @return {@link RestAosAPI}
     */
    public RestAosAPI withAosAPI()
    {
        return new RestAosAPI(this);
    }

    /**
     * @return {@link RestPrivateAPI} using the rest Private API as prefix: {@link /alfresco/api/-default-/private/alfresco/versions/1}
     */
    public RestPrivateAPI withPrivateAPI()
    {
        return new RestPrivateAPI(this);
    }

    public RestResponse onResponse()
    {
        if (response == null)
            throw new UnsupportedOperationException("Cannot perform on a Response that wasn't yet received!");
        return response;
    }

    /**
     * Process responses for a single model as {@link RestSyncSetRequestModel} Notice that {@link RestSyncSetRequestModel} doesn't have one "entry" field as any other rest request model
     *
     * @throws JsonToModelConversionException
     *             If the response cannot be converted to the given model.
     * @throws EmptyJsonResponseException
     *             If there is no response from the server.
     */
    public <T> T processModelWithoutEntryObject(Class<T> classz, RestRequest restRequest)
            throws EmptyJsonResponseException, JsonToModelConversionException
    {
        Response returnedResponse = sendRequest(restRequest);

        setStatusCode(String.valueOf(returnedResponse.getStatusCode()));

        boolean responseHasErrors = checkForJsonError(returnedResponse);
        // Do not check checkForJsonStatusException as status object in the API response is not a standard statusModel
        // boolean responseHasExceptions = checkForJsonStatusException(returnedResponse);

        T model = null;

        try
        {
            if (!responseHasErrors)
            {
                model = returnedResponse.jsonPath().getObject("$", classz);
                validateJsonModelSchema(classz, model);
            }
        }
        catch (Exception processError)
        {
            throw new JsonToModelConversionException(classz, processError);
        }
        if (model == null)
        {
            try
            {
                return classz.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return model;
    }

    public void configureSyncServiceEndPoint()
    {
        this.serverURI = restProperties.envProperty().getSyncServerUrl();
        this.serverPort = restProperties.envProperty().getSyncPort();
        configureServerEndpoint();
    }

    public void configureSolrEndPoint()
    {
        this.serverURI = restProperties.envProperty().getSolrServerUrl();
        this.serverPort = restProperties.envProperty().getSolrPort();
        configureServerEndpoint();
        configureSecretHeader();
    }

    /**
     * Adds the secret Solr header if it has been set
     */
    private void configureSecretHeader()
    {
        String solrSecret = restProperties.envProperty().getSolrSecret();
        if (!solrSecret.isEmpty())
        {
            String solrSecretName = restProperties.envProperty().getSolrSecretName();
            configureRequestSpec().addHeader(solrSecretName, solrSecret);
        }
    }

    /**
     * Use {@link #setServerURI(String)} and {@link #setServerPort(int)}
     */
    public void configureServerEndpoint()
    {
        requestSpecBuilder = new RequestSpecBuilder();

        // use static variables for logs, etc
        // the request spec is built from data set via setters, see RestWrapper#onRequest
        RestAssured.baseURI = this.serverURI;
        RestAssured.port = this.serverPort;

        configureRequestSpec().setBaseUri(this.serverURI);
        configureRequestSpec().setPort(this.serverPort);
    }

    /**
     * Adding new method to configure Alfresco Endpoint. Reconfiguration is required when restClient is used to executed apis on different <host>:<port> e.g. solr api followed by search api
     */
    public void configureAlfrescoEndpoint()
    {
        this.serverURI = restProperties.envProperty().getTestServerUrl();
        this.serverPort = restProperties.envProperty().getPort();
        configureServerEndpoint();
    }
}
