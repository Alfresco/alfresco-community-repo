/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.content.directurl.SystemWideDirectUrlConfig;
import org.alfresco.rest.api.impl.directurl.RestApiDirectUrlConfig;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsString;
import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.RequestBuilder;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.client.data.SiteMember;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.util.JacksonUtil;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.testing.category.LuceneTests;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Generic methods for calling the Api (originally taken and adapted from BaseCustomModelApiTest)
 * 
 * @author Jamal Kaabi-Mofrad
 * @author janv
 * @author gethin
 */
@Category(LuceneTests.class)
public abstract class AbstractBaseApiTest extends EnterpriseTestApi
{
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";
    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    private static final String RESOURCE_PREFIX = "publicapi/upload/";

    protected static final String URL_NODES = "nodes";
    protected static final String URL_DELETED_NODES = "deleted-nodes";

    protected static final String URL_RENDITIONS = "renditions";
    protected static final String URL_VERSIONS = "versions";

    private static final String URL_CHILDREN = "children";
    private static final String URL_CONTENT = "content";

    protected static final String TYPE_CM_FOLDER = "cm:folder";
    protected static final String TYPE_CM_CONTENT = "cm:content";
    protected static final String TYPE_CM_OBJECT = "cm:cmobject";

    protected static final String ASPECT_CM_PREFERENCES = "cm:preferences";
    protected static final String ASSOC_TYPE_CM_PREFERENCE_IMAGE = "cm:preferenceImage";

    protected static final String ASSOC_TYPE_CM_CONTAINS = "cm:contains";
    
    // TODO improve admin-related tests, including ability to override default admin un/pw
    protected static final String DEFAULT_ADMIN = "admin";
    protected static final String DEFAULT_ADMIN_PWD = "admin";

    // network1 with user1, user2 and a testsite1
    protected static TestNetwork networkOne;
    
    protected static String user1; // user1 from network1
    protected static String user2; // user2 from network1

    // network admin (or default super admin, if not running within a tenant/network)
    protected static String networkAdmin = DEFAULT_ADMIN;

    protected static String tSiteId;
    protected static String tDocLibNodeId;
    
    
    protected static List<String> users = new ArrayList<>();

    protected static JacksonUtil jacksonUtil;
    protected static MutableAuthenticationService authenticationService;
    protected static PersonService personService;

    protected final String RUNID = System.currentTimeMillis()+"";

    private static final String REQUEST_DIRECT_ACCESS_URL = "request-direct-access-url";
    
    @Override
    @Before
    public void setup() throws Exception
    {
        jacksonUtil = new JacksonUtil(applicationContext.getBean("jsonHelper", JacksonHelper.class));

        if (networkOne == null)
        {
            // note: populateTestData/createTestData will be called (which currently creates 2 tenants, 9 users per tenant, 10 sites per tenant, ...)
            networkOne = getTestFixture().getRandomNetwork();
        }
        
        //userOneN1 = networkN1.createUser();
        //userTwoN1 = networkN1.createUser();

        String tenantDomain = networkOne.getId();
        
        if (! TenantService.DEFAULT_DOMAIN.equals(tenantDomain))
        {
            networkAdmin = DEFAULT_ADMIN+"@"+tenantDomain;
        }

        // to enable admin access via test calls - eg. via PublicApiClient -> AbstractTestApi -> findUserByUserName
        getOrCreateUser(networkAdmin, "admin", networkOne);
        
        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);
        
        // note: createUser currently relies on repoService
        user1 = createUser("user1-" + RUNID, "user1Password", networkOne);
        user2 = createUser("user2-" + RUNID, "user2Password", networkOne);

        // used-by teardown to cleanup
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);
        users.add(user1);
        users.add(user2);

        setRequestContext(networkOne.getId(), user1, null);
        
        tSiteId = createSite("TestSite A - " + RUNID, SiteVisibility.PRIVATE).getId();
        tDocLibNodeId = getSiteContainerNodeId(tSiteId, "documentLibrary");

        setRequestContext(null, null, null);
    }

    @After
    public void tearDown() throws Exception
    {
        if ((networkOne != null) && (user1 != null) && (tSiteId != null))
        {
            setRequestContext(networkOne.getId(), user1, null);
            deleteSite(tSiteId, true, 204);
        }

        setRequestContext(networkAdmin);
        
        for (final String username : users)
        {
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    deleteUser(username, networkOne);
                    return null;
                }
            });
        }
        
        users.clear();
        AuthenticationUtil.clearCurrentSecurityContext();
        setRequestContext(null);
    }

    protected String getRequestContentDirectUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + REQUEST_DIRECT_ACCESS_URL;
    }

    protected String getRequestVersionRenditionContentDirectUrl(String nodeId, String versionId, String renditionId)
    {
        return getNodeVersionRenditionIdUrl(nodeId, versionId, renditionId) + "/" + REQUEST_DIRECT_ACCESS_URL;
    }

    protected String getRequestArchivedContentDirectUrl(String nodeId)
    {
        return URL_DELETED_NODES + "/" + nodeId + "/" + REQUEST_DIRECT_ACCESS_URL;
    }

    protected String getRequestArchivedRenditonContentDirectUrl(String nodeId, String renditionID)
    {
        return URL_DELETED_NODES + "/" + nodeId + "/" + URL_RENDITIONS + "/" + renditionID + "/" + REQUEST_DIRECT_ACCESS_URL;
    }

    protected String getRequestRenditionDirectAccessUrl(String nodeId, String renditionID)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_RENDITIONS + "/" + renditionID + "/" + REQUEST_DIRECT_ACCESS_URL;
    }

    protected String getRequestVersionDirectAccessUrl(String nodeId, String versionId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_VERSIONS + "/" + versionId + "/" + REQUEST_DIRECT_ACCESS_URL;
    }


    /**
     * The api scope. either public or private
     *
     * @return public or private
     */
    public abstract String getScope();

    protected HttpResponse post(String url, String body, int expectedStatus) throws Exception
    {
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, byte[] body, Map<String, String> params, Map<String, String> headers, String apiName, String contentType, int expectedStatus) throws Exception
    {
        RequestBuilder requestBuilder = httpClient.new PostRequestBuilder()
                .setBodyAsByteArray(body)
                .setContentType(contentType)
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName(apiName)
                .setEntityCollectionName(url)
                .setHeaders(headers)
                .setParams(params);
        HttpResponse response = publicApiClient.execute(requestBuilder);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String body, Map<String, String> params, Map<String, String> headers, String apiName, int expectedStatus) throws Exception
    {
        RequestBuilder requestBuilder = httpClient.new PostRequestBuilder()
                .setBodyAsString(body)
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName(apiName)
                .setEntityCollectionName(url)
                .setHeaders(headers)
                .setParams(params);
        HttpResponse response = publicApiClient.execute(requestBuilder);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String body, String queryString, int expectedStatus) throws Exception
    {
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, byte[] body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    // TODO unused queryString - fix-up usages and then remove
    protected HttpResponse post(String entityCollectionName, String entityId, String relationCollectionName, byte[] body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        HttpResponse response = publicApiClient.post(getScope(), entityCollectionName, entityId, relationCollectionName, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(String url, PublicApiClient.Paging paging, int expectedStatus) throws Exception
    {
        return getAll(url, paging, null, expectedStatus);
    }

    protected HttpResponse getAll(String url, PublicApiClient.Paging paging, Map<String, String> otherParams, int expectedStatus) throws Exception
    {
        Map<String, String> params = createParams(paging, otherParams);

        HttpResponse response = publicApiClient.get(getScope(), url, null, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(Class<?> entityResource, PublicApiClient.Paging paging, Map<String, String> otherParams, int expectedStatus) throws Exception
    {
        HttpResponse response = publicApiClient.get(entityResource, null, null, otherParams);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(String url, PublicApiClient.Paging paging, Map<String, String> otherParams, Map<String, String> headers, int expectedStatus) throws Exception
    {
        return getAll(url, paging, otherParams, headers, null, expectedStatus);
    }

    protected HttpResponse getAll(String url, PublicApiClient.Paging paging, Map<String, String> otherParams, Map<String, String> headers, String apiName, int expectedStatus) throws Exception
    {
        Map<String, String> params = createParams(paging, otherParams);
        RequestBuilder requestBuilder = httpClient.new GetRequestBuilder()
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName(apiName)
                .setEntityCollectionName(url)
                .setParams(params)
                .setHeaders(headers);

        HttpResponse response = publicApiClient.execute(requestBuilder);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }
    
    protected HttpResponse getSingle(String url, String entityId, int expectedStatus) throws Exception
    {
        return getSingle(url, entityId, null, expectedStatus);
    }

    public HttpResponse get(String url, Map<String, String> params, int expectedStatus) throws IOException
    {
        HttpResponse response = publicApiClient.get(url, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(String url, String entityId, Map<String, String> params, int expectedStatus) throws Exception
    {
        HttpResponse response = publicApiClient.get(getScope(), url, entityId, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(Class<?> entityResource, String entityId, Map<String, String> params, int expectedStatus) throws Exception
    {
        HttpResponse response = publicApiClient.get(entityResource, entityId, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(String url, String entityId, Map<String, String> params, Map<String, String> headers, int expectedStatus) throws Exception
    {
        return getSingle(url, entityId, params, headers, null, expectedStatus);
    }

    protected HttpResponse getSingle(String url, String entityId, Map<String, String> params, Map<String, String> headers, String apiName, int expectedStatus) throws Exception
    {
        RequestBuilder requestBuilder = httpClient.new GetRequestBuilder()
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName(apiName)
                .setEntityCollectionName(url)
                .setEntityId(entityId)
                .setParams(params)
                .setHeaders(headers);

        HttpResponse response = publicApiClient.execute(requestBuilder);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingleWithDelayRetry(String url, String entityId, Map<String, String> params,
                                                   Map<String, String> headers, int repeat, long pauseInMillisecond, int expectedStatus) throws Exception
    {
        int retryCount = 0;
        while (retryCount < repeat)
        {
            try
            {
                return getSingle(url, entityId, params, headers, expectedStatus);
            } 
            catch (AssertionError ex)
            {
                retryCount++;
                Thread.sleep(pauseInMillisecond);
            }
        }
        return null;
    }

    protected HttpResponse put(String url, String entityId, String body, String queryString, int expectedStatus) throws Exception
    {
        if (queryString != null)
        {
            entityId += queryString;
        }
        HttpResponse response = publicApiClient.put(getScope(), url, entityId, null, null, body, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse putBinary(String url, int version, BinaryPayload payload, String queryString, Map<String, String> params,
                                     int expectedStatus) throws Exception
    {
        if (queryString != null)
        {
            url += queryString;
        }

        HttpResponse response = publicApiClient.putBinary(getScope(), version, url, null, null, null, payload, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse putBinary(String url, BinaryPayload payload, String queryString, Map<String, String> params,
                                     int expectedStatus) throws Exception
    {
        return putBinary(url, 1, payload, queryString, params, expectedStatus);
    }

    protected HttpResponse delete(String url, String entityId, int expectedStatus) throws Exception
    {
        return delete(url, entityId, null, expectedStatus);
    }

    protected HttpResponse delete(String url, String entityId, Map<String, String> params, int expectedStatus) throws Exception
    {
        HttpResponse response = publicApiClient.delete(getScope(), 1, url, entityId, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }
    
    protected HttpResponse delete(String url, String entityId, Map<String, String> params, Map<String, String> headers, String apiName, int expectedStatus) throws Exception
    {
        RequestBuilder requestBuilder = httpClient.new DeleteRequestBuilder()
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName(apiName)
                .setEntityCollectionName(url)
                .setEntityId(entityId)
                .setParams(params)
                .setHeaders(headers);

        HttpResponse response = publicApiClient.execute(requestBuilder);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected String createUser(String username)
    {
        return createUser(username, "password", null);
    }

    protected String createUser(String usernameIn, String password, TestNetwork network)
    {
        return createUser(new PersonInfo(usernameIn, usernameIn, usernameIn, password, null, null, null, null, null, null, null), network);
    }

    /**
     * TODO implement as remote api call
     */
    protected String createUser(final PersonInfo personInfo, final TestNetwork network)
    {
        final String tenantDomain = (network != null ? network.getId() : TenantService.DEFAULT_DOMAIN);
        
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return TenantUtil.runAsTenant(new TenantUtil.TenantRunAsWork<String>()
                {
                    public String doWork() throws Exception
                    {
                        String username = repoService.getPublicApiContext().createUserName(personInfo.getUsername(), tenantDomain);
                        personInfo.setUsername(username);
                        RepoService.TestPerson person = repoService.createUser(personInfo, username, network);
                        return person.getId();

                    }
                }, tenantDomain);
            }
        }, networkAdmin);
    }

    /**
     * TODO implement as remote api call
     */
    protected String getOrCreateUser(String usernameIn, String password, TestNetwork network)
    {
        final String tenantDomain = (network != null ? network.getId() : TenantService.DEFAULT_DOMAIN);

        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return TenantUtil.runAsTenant(new TenantUtil.TenantRunAsWork<String>()
                {
                    public String doWork() throws Exception
                    {
                        String username = repoService.getPublicApiContext().createUserName(usernameIn, tenantDomain);
                        PersonInfo personInfo = new PersonInfo(username, username, username, password, null, null, null, null, null, null, null);
                        RepoService.TestPerson person = repoService.getOrCreateUser(personInfo, username, network);
                        return person.getId();
                    }
                }, tenantDomain);
            }
        }, networkAdmin);
    }

    /**
     * TODO implement as remote api call
     */
    protected String deleteUser(final String username, final TestNetwork network)
    {
        final String tenantDomain = (network != null ? network.getId() : TenantService.DEFAULT_DOMAIN);

        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return TenantUtil.runAsTenant(new TenantUtil.TenantRunAsWork<String>()
                {
                    public String doWork() throws Exception
                    {
                        repoService.deleteUser(username, network);
                        return null;
                    }
                }, tenantDomain);
            }
        }, networkAdmin);
    }
    
    protected SiteMember addSiteMember(String siteId, String userId, final SiteRole siteRole) throws Exception
    {
        SiteMember siteMember = new SiteMember(userId, siteRole.name());
        HttpResponse response = publicApiClient.post(getScope(), "sites", siteId, "members", null, siteMember.toJSON().toString());
        checkStatus(201, response.getStatusCode());
        return SiteMember.parseSiteMember(siteMember.getSiteId(), (JSONObject)response.getJsonResponse().get("entry"));
    }

    protected Site createSite(String siteTitle, SiteVisibility siteVisibility) throws Exception
    {
        return createSite(null, siteTitle, null, siteVisibility, 201);
    }

    protected Site createSite(String siteId, String siteTitle, String siteDescription, SiteVisibility siteVisibility, int expectedStatus) throws Exception
    {
        Site site = new Site();
        site.setId(siteId);
        site.setTitle(siteTitle);
        site.setVisibility(siteVisibility);
        site.setDescription(siteDescription);

        HttpResponse response = publicApiClient.post(getScope(), "sites", null, null, null, toJsonAsStringNonNull(site));
        checkStatus(expectedStatus, response.getStatusCode());
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Site.class);
    }

    protected HttpResponse deleteSite(String siteId, boolean permanent, int expectedStatus) throws Exception
    {
        Map params = null;
        if (permanent == true)
        {
            params = Collections.singletonMap("permanent", "true");
        }

        HttpResponse response = publicApiClient.delete(getScope(), 1, "sites", siteId, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());
        return response;
    }

    /**
     * Helper: to get site container id (see also RepoService.getContainerNodeRef -> SiteService.getContainer)
     * <p>
     * GET /nodes/-root?relativePath=/Sites/siteId/documentLibrary
     * <p>
     * alternatively:
     * <p>
     * GET /nodes/siteNodeId?relativePath=documentLibrary
     */
    protected String getSiteContainerNodeId(String siteId, String containerNameId) throws Exception
    {
        Map<String, String> params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/Sites/" + siteId + "/" + containerNameId);
        
        HttpResponse response = publicApiClient.get(NodesEntityResource.class, Nodes.PATH_ROOT, null, params);
        checkStatus(200, response.getStatusCode());

        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }
    
    protected void checkStatus(int expectedStatus, int actualStatus)
    {
        if (expectedStatus > 0 && expectedStatus != actualStatus)
        {
            fail("Status code " + actualStatus + " returned, but expected " + expectedStatus);
        }
    }

    /**
     * @deprecated
     * 
     * @param runAsUser
     */
    protected void setRequestContext(String runAsUser)
    {
        String password = null;
        if ((runAsUser != null) && runAsUser.equals(DEFAULT_ADMIN))
        {
            // TODO improve "admin" related tests
            password = DEFAULT_ADMIN_PWD;
        }

        // Assume "networkN1" if set !
        String runAsNetwork = (networkOne != null ? networkOne.getId() : null);

        setRequestContext(runAsNetwork, runAsUser, password);
    }

    /**
     * TODO implement as remote (login) api call
     */
    protected void setRequestContext(String runAsNetwork, String runAsUser, String password)
    {
        if ((runAsNetwork == null) || TenantService.DEFAULT_DOMAIN.equals(runAsNetwork))
        {
            runAsNetwork = "-default-";
        }
        else if ((runAsUser != null) && runAsUser.equals(DEFAULT_ADMIN))
        {
            runAsUser = runAsUser+"@"+runAsNetwork;
        }
        
        publicApiClient.setRequestContext(new RequestContext(runAsNetwork, runAsUser, password));
    }

    // -root- (eg. Company Home for on-prem)
    protected String getRootNodeId() throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, Nodes.PATH_ROOT, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    // -my- (eg. User's Home for on-prem)
    protected String getMyNodeId() throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    // -shared- (eg. "Shared" folder for on-prem)
    protected String getSharedNodeId() throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, Nodes.PATH_SHARED, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    protected Folder createFolder(String parentId, String folderName) throws Exception
    {
        return createFolder(parentId, folderName, null);
    }
    
    protected Folder createFolder(String parentId, String folderName, Map<String, Object> props) throws Exception
    {
        return createNode(parentId, folderName, TYPE_CM_FOLDER, props, Folder.class);
    }

    protected String createUniqueFolder(String parentId) throws Exception
    {
        return createFolder(parentId, "folder-" + System.currentTimeMillis()).getId();
    }

    protected String createUniqueContent(String folderId) throws Exception
    {
        Document documentResp = createTextFile(folderId, "file-" + System.currentTimeMillis(),
        "some text-" + System.currentTimeMillis(), "UTF-8", null);
        return documentResp.getId();
    }

    protected Node createNode(String parentId, String nodeName, String nodeType, Map<String, Object> props) throws Exception
    {
        return createNode(parentId, nodeName, nodeType, props, Node.class);
    }

    protected <T> T createNode(String parentId, String nodeName, String nodeType, Map<String, Object> props, Class<T> returnType)
                throws Exception
    {
        Node n = new Node();
        n.setName(nodeName);
        n.setNodeType(nodeType);
        n.setProperties(props);

        // create node
        HttpResponse response = post(getNodeChildrenUrl(parentId), RestApiUtil.toJsonAsStringNonNull(n), 201);

        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), returnType);
    }
    
    protected void deleteNode(String nodeId) throws Exception
    {
        deleteNode(nodeId, 204);
    }

    protected void deleteNode(String nodeId, int expectedStatus) throws Exception
    {
        deleteNode(nodeId, false, expectedStatus);
    }

    protected void deleteNode(String nodeId, boolean permanent, int expectedStatus) throws Exception
    {
        Map params = null;
        if (permanent == true)
        {
            params = Collections.singletonMap("permanent", "true");
        }
        
        delete(URL_NODES, nodeId, params, expectedStatus);
    }

    protected Document createTextFile(String parentId, String fileName, String textContent) throws IOException, Exception
    {
        return createTextFile(parentId, fileName, textContent, "UTF-8", null);
    }

    protected Document createTextFile(String parentId, String fileName, String textContent, String encoding, Map<String, String> props) throws IOException, Exception
    {
        return createTextFile(parentId, fileName, textContent, encoding, props, 201);
    }

    protected Document createTextFile(String parentId, String fileName, String textContent, String encoding, Map<String, String> props, int expectedStatus) throws IOException, Exception
    {
        if (props == null)
        {
            props = Collections.EMPTY_MAP;

        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(textContent.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");

        MultiPartBuilder.MultiPartRequest reqBody = MultiPartBuilder.create()
                .setFileData(new MultiPartBuilder.FileData(fileName, txtFile))
                .setProperties(props)
                .build();

        HttpResponse response = post(getNodeChildrenUrl(parentId), reqBody.getBody(), null, reqBody.getContentType(), expectedStatus);

        if (response.getJsonResponse().get("error") != null)
        {
            return null;
        }

        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    protected Document createEmptyTextFile(String parentFolderId, String docName) throws Exception
    {
        return createEmptyTextFile(parentFolderId, docName, null, 201);
    }
    
    protected Document createEmptyTextFile(String parentFolderId, String docName, Map<String, String> params, int expectedStatus) throws Exception
    {
        Document d1 = new Document();
        d1.setName(docName);
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        // create empty file
        HttpResponse response = post(getNodeChildrenUrl(parentFolderId), toJsonAsStringNonNull(d1), params, null, "alfresco", expectedStatus);
        if (expectedStatus != 201)
        {
            return null;
        }
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    protected Document updateTextFile(String contentId, String textContent, Map<String, String> params) throws Exception
    {
        return updateTextFile(contentId, textContent, params, 200);
    }

    protected Document updateTextFile(String contentId, String textContent, Map<String, String> params, int expectedStatus) throws Exception
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(textContent.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
        BinaryPayload payload = new BinaryPayload(txtFile);

        HttpResponse response = putBinary(getNodeContentUrl(contentId), payload, null, params, expectedStatus);
        if (expectedStatus != 200)
        {
            return null;
        }
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    protected File getResourceFile(String fileName) throws FileNotFoundException
    {
        URL url = NodeApiTest.class.getClassLoader().getResource(RESOURCE_PREFIX + fileName);
        if (url == null)
        {
            fail("Cannot get the resource: " + fileName);
        }
        return ResourceUtils.getFile(url);
    }

    protected Document lock(String nodeId, String body) throws Exception
    {
        HttpResponse response = post(getNodeOperationUrl(nodeId, "lock"), body, null, 200);
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    protected Document unlock(String nodeId) throws Exception
    {
        HttpResponse response = post(getNodeOperationUrl(nodeId, "unlock"), null, null, 200);
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    /**
     * This test helper method uses "update binary content" to create one or more new versions. The file must already exist.
     *
     * @param userId
     * @param contentNodeId
     * @param cnt
     * @param textContentPrefix
     * @param verCnt
     * @param majorVersion
     * @param currentVersionLabel
     * @return
     * @throws Exception
     */
    protected String updateFileVersions(String userId, String contentNodeId, int cnt,
                                      String textContentPrefix, int verCnt,
                                      Boolean majorVersion, String currentVersionLabel) throws Exception
    {
        String[] parts = currentVersionLabel.split("\\.");

        int majorVer = new Integer(parts[0]).intValue();
        int minorVer = new Integer(parts[1]).intValue();

        Map<String, String> params = new HashMap<>();
        params.put(Nodes.PARAM_OVERWRITE, "true");

        if (majorVersion != null)
        {
            params.put(Nodes.PARAM_VERSION_MAJOR, majorVersion.toString());
        }
        else
        {
            majorVersion = false;
        }


        if (majorVersion)
        {
            minorVer = 0;
        }

        for (int i = 1; i <= cnt; i++)
        {
            if (majorVersion)
            {
                majorVer++;
            }
            else
            {
                minorVer++;
            }

            verCnt++;

            params.put("comment", "my version " + verCnt);

            String textContent = textContentPrefix + verCnt;

            currentVersionLabel = majorVer + "." + minorVer;

            // Update
            ByteArrayInputStream inputStream = new ByteArrayInputStream(textContent.getBytes());
            File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
            PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

            HttpResponse response = putBinary(getNodeContentUrl(contentNodeId), payload, null, params, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(nodeResp.getProperties());
            assertEquals(currentVersionLabel, nodeResp.getProperties().get("cm:versionLabel"));
            assertEquals((majorVersion ? "MAJOR" : "MINOR"), nodeResp.getProperties().get("cm:versionType"));

            // double-check - get version node info
            response = getSingle(getNodeVersionsUrl(contentNodeId), currentVersionLabel, null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals(currentVersionLabel, nodeResp.getProperties().get("cm:versionLabel"));
            assertEquals((majorVersion ? "MAJOR" : "MINOR"), nodeResp.getProperties().get("cm:versionType"));
        }

        return currentVersionLabel;
    }

    protected static final long PAUSE_TIME = 5000; //millisecond
    protected static final int MAX_RETRY = 20;

    protected Rendition waitAndGetRendition(String sourceNodeId, String versionId, String renditionId) throws Exception
    {
        return waitAndGetRendition(sourceNodeId, versionId, renditionId, MAX_RETRY, PAUSE_TIME);
    }

    protected Rendition waitAndGetRendition(String sourceNodeId, String versionId, String renditionId, int maxRetry, long pauseTime) throws Exception
    {
        int retryCount = 0;
        while (retryCount < maxRetry)
        {
            try
            {
                HttpResponse response;
                if ((versionId != null) && (! versionId.isEmpty()))
                {
                    response = getSingle(getNodeVersionRenditionsUrl(sourceNodeId, versionId), renditionId, 200);
                }
                else
                {
                    response = getSingle(getNodeRenditionsUrl(sourceNodeId), renditionId, 200);
                }
                Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
                assertNotNull(rendition);
                assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());
                return rendition;
            }
            catch (AssertionError ex)
            {
                // If the asynchronous create rendition action is not finished yet,
                // wait for 'PAUSE_TIME' and try again.
                retryCount++;

                System.out.println("waitAndGetRendition: "+retryCount);
                Thread.sleep(pauseTime);
            }
        }

        return null;
    }

    protected Rendition createAndGetRendition(String sourceNodeId, String renditionId) throws Exception
    {
        return createAndGetRendition(sourceNodeId, null, renditionId);
    }

    protected Rendition createAndGetRendition(String sourceNodeId, String versionId, String renditionId) throws Exception
    {
        Rendition renditionRequest = new Rendition();
        renditionRequest.setId(renditionId);

        int retryCount = 0;
        while (retryCount < MAX_RETRY)
        {
            try
            {
                HttpResponse response;
                if ((versionId != null) && (! versionId.isEmpty()))
                {
                    response = post(getNodeVersionRenditionsUrl(sourceNodeId, versionId), toJsonAsString(renditionRequest), 202);
                }
                else
                {
                    response = post(getNodeRenditionsUrl(sourceNodeId), toJsonAsString(renditionRequest), 202);
                }
                assertNull(response.getJsonResponse());
                break;
            }
            catch (AssertionError ex)
            {
                // If no transformer is currently available,
                // wait for 'PAUSE_TIME' and try again.
                retryCount++;

                System.out.println("waitAndGetRendition: "+retryCount);
                Thread.sleep(PAUSE_TIME);
            }
        }

        return waitAndGetRendition(sourceNodeId, versionId, renditionId);
    }

    protected String getNodeRenditionsUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_RENDITIONS;
    }

    protected String getNodeRenditionIdUrl(String nodeId, String renditionID)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_RENDITIONS + "/" + renditionID;
    }

    protected String getNodeVersionRenditionIdUrl(String nodeId, String versionId, String renditionID)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_VERSIONS + "/" + versionId + "/" + URL_RENDITIONS + "/" + renditionID;
    }

    protected String getNodeVersionsUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_VERSIONS;
    }

    protected String getNodeVersionRenditionsUrl(String nodeId, String versionId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_VERSIONS + "/" + versionId + "/" + URL_RENDITIONS;
    }

    protected String getNodeChildrenUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_CHILDREN;
    }

    protected String getNodeContentUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_CONTENT;
    }

    protected String getNodeOperationUrl(String nodeId, String operation)
    {
        return URL_NODES + "/" + nodeId + "/" + operation;
    }

    protected String getNode(String nodeId)
    {
        return URL_NODES + "/" + nodeId;
    }

    protected void enableRestDirectAccessUrls()
    {
        SystemWideDirectUrlConfig systemDauConfig = (SystemWideDirectUrlConfig) applicationContext.getBean("systemWideDirectUrlConfig");
        systemDauConfig.setEnabled(true);
        RestApiDirectUrlConfig restDauConfig = (RestApiDirectUrlConfig) applicationContext.getBean("restApiDirectUrlConfig");
        restDauConfig.setEnabled(true);
    }

    protected void disableRestDirectAccessUrls()
    {
        SystemWideDirectUrlConfig systemDauConfig = (SystemWideDirectUrlConfig) applicationContext.getBean("systemWideDirectUrlConfig");
        systemDauConfig.setEnabled(false);
        RestApiDirectUrlConfig restDauConfig = (RestApiDirectUrlConfig) applicationContext.getBean("restApiDirectUrlConfig");
        restDauConfig.setEnabled(false);
    }
}

