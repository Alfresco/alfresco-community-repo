/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.api.tests.client;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl;
import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.api.tests.TestPeople;
import org.alfresco.rest.api.tests.TestSites;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.RequestBuilder;
import org.alfresco.rest.api.tests.client.data.Action;
import org.alfresco.rest.api.tests.client.data.Aspect;
import org.alfresco.rest.api.tests.client.data.Activities;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.AuditApp;
import org.alfresco.rest.api.tests.client.data.AuditEntry;
import org.alfresco.rest.api.tests.client.data.CMISNode;
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.ContentData;
import org.alfresco.rest.api.tests.client.data.Favourite;
import org.alfresco.rest.api.tests.client.data.FavouriteSite;
import org.alfresco.rest.api.tests.client.data.FolderNode;
import org.alfresco.rest.api.tests.client.data.Group;
import org.alfresco.rest.api.tests.client.data.GroupMember;
import org.alfresco.rest.api.tests.client.data.JSONAble;
import org.alfresco.rest.api.tests.client.data.MemberOfSite;
import org.alfresco.rest.api.tests.client.data.NodeRating;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.rest.api.tests.client.data.PersonNetwork;
import org.alfresco.rest.api.tests.client.data.Preference;
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteContainer;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteMember;
import org.alfresco.rest.api.tests.client.data.SiteGroup;
import org.alfresco.rest.api.tests.client.data.SiteMembershipRequest;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.rest.api.tests.client.data.Type;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.FolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A client for interacting with the public api and returning Java objects.
 * 
 * @author steveglover
 *
 */
public class PublicApiClient
{
    private static final Log logger = LogFactory.getLog(PublicApiClient.class);

    private UserDataService userDataService;
    private PublicApiHttpClient client;

    private Sites sites;
    private Tags tags;
    private Comments comments;
    private Nodes nodes;
    private People people;
    private Favourites favourites;
    private SiteMembershipRequests siteMembershipRequests;
    private Groups groups;
    private RawProxy rawProxy;
    private AuditApps auditApps;
    private Actions actions;
    private Aspects aspects;
    private Types types;

    private ThreadLocal<RequestContext> rc = new ThreadLocal<RequestContext>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public PublicApiClient(PublicApiHttpClient client, UserDataService userDataService)
    {
        this.client = client;
        this.userDataService = userDataService;

        init();
    }

    public void init()
    {
        sites = new Sites();
        tags = new Tags();
        comments = new Comments();
        nodes = new Nodes();
        people = new People();
        favourites = new Favourites();
        siteMembershipRequests = new SiteMembershipRequests();
        groups = new Groups();
        rawProxy = new RawProxy();
        auditApps = new AuditApps();
        actions = new Actions();
        aspects = new Aspects();
        types = new Types();
    }

    public void setRequestContext(RequestContext rc)
    {
        this.rc.set(rc);
    }

    public RequestContext getRequestContext()
    {
        RequestContext context = rc.get();
        if(context == null)
        {
            throw new RuntimeException("Must set a request context");
        }
        return context;
    }

    protected UserData findUser(String userName)
    {
        return userDataService.findUserByUserName(userName);
    }

    public RawProxy rawProxy()
    {
        return rawProxy;
    }

    public People people()
    {
        return people;
    }

    public Nodes nodes()
    {
        return nodes;
    }

    public Sites sites()
    {
        return sites;
    }

    public Favourites favourites()
    {
        return favourites;
    }

    public SiteMembershipRequests siteMembershipRequests()
    {
        return siteMembershipRequests;
    }

    public Tags tags()
    {
        return tags;
    }

    public Comments comments()
    {
        return comments;
    }

    public Groups groups()
    {
        return groups;
    }
    
    public AuditApps auditApps(){
        return auditApps;
    }

    public Actions actions()
    {
        return actions;
    }

    public Aspects aspects()
    {
        return aspects;
    }

    public Types types()
    {
        return types;
    }

    public CmisSession createPublicApiCMISSession(Binding binding, String version)
    {
       return createPublicApiCMISSession(binding, version, null);
    }

    public CmisSession createPublicApiCMISSession(Binding binding, String version, String objectFactoryName)
    {
        CmisSession cmisSession = null;

        RequestContext rc = getRequestContext();
        if(rc == null)
        {
            throw new RuntimeException("Must set a request context");
        }

        String networkId = rc.getNetworkId();
        String username = rc.getRunAsUser();
        UserData userData = findUser(rc.getRunAsUser());
        if(userData != null)
        {
            String password = userData.getPassword();

            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameters = new HashMap<String, String>();

            // user credentials
            parameters.put(SessionParameter.USER, username);
            parameters.put(SessionParameter.PASSWORD, password);

            // connection settings
            if(binding == Binding.atom)
            {
                parameters.put(SessionParameter.ATOMPUB_URL, client.getPublicApiCmisUrl(networkId, binding, version, null));
                parameters.put(SessionParameter.BINDING_TYPE, binding.getOpenCmisBinding().value());
            }
            else if(binding == Binding.browser)
            {
                parameters.put(SessionParameter.BROWSER_URL, client.getPublicApiCmisUrl(networkId, binding, version, null));
                parameters.put(SessionParameter.BINDING_TYPE, binding.getOpenCmisBinding().value());
            }
            if(networkId != null)
            {
                parameters.put(SessionParameter.REPOSITORY_ID, networkId);
            }
            if(objectFactoryName != null)
            {
                parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, objectFactoryName);
            }

            // create session
            Session session = factory.createSession(parameters);

            cmisSession = new CmisSession(session);
        }

        return cmisSession;
    }

    public CmisSession createCMISSession(String repositoryId, RequestContext rc)
    {
        CmisSession session = null;

        UserData userData = findUser(rc.getRunAsUser());
        if(userData != null)
        {
            session = createCMISSession(repositoryId, rc.getRunAsUser(), userData.getPassword());
        }

        return session;
    }

    /**
     * Get CMIS repositories (Enterprise AtomPub CMIS binding)
     *
     * @return List<Repository>
     */
    public List<Repository> getCMISRepositories()
    {
        List<Repository> repositories = null;

        RequestContext rc = getRequestContext();
        if(rc == null)
        {
            throw new RuntimeException("Must set a request context");
        }

        UserData userData = findUser(rc.getRunAsUser());
        if(userData != null)
        {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameters = new HashMap<String, String>();

            // user credentials
            parameters.put(SessionParameter.USER, rc.getRunAsUser());
            parameters.put(SessionParameter.PASSWORD, userData.getPassword());

            // connection settings
            parameters.put(SessionParameter.ATOMPUB_URL, client.getCmisUrl(null, null));
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

            repositories = factory.getRepositories(parameters);
        }

        return repositories;
    }

    /**
     * Create a CMIS session using Enterprise AtomPub binding.
     *
     * @param repositoryId String
     * @param username String
     * @param password String
     * @return CmisSession
     */
    public CmisSession createCMISSession(String repositoryId, String username, String password)
    {
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameters = new HashMap<String, String>();

        // user credentials
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);

        // connection settings
        parameters.put(SessionParameter.ATOMPUB_URL, client.getCmisUrl(repositoryId, null));
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        if(repositoryId != null)
        {
            parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);
        }
        parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, AlfrescoObjectFactoryImpl.class.getName());

        // create session
        Session session = factory.createSession(parameters);

        CmisSession cmisSession = new CmisSession(session);
        return cmisSession;
    }

    public CmisSession getCMISSession(Repository respository)
    {
        RequestContext rc = getRequestContext();
        if(rc == null)
        {
            throw new RuntimeException("Must set a request context");
        }

        CmisSession session = createCMISSession(respository.getId(), rc);
        return session;
    }

    public HttpResponse post(Binding cmisBinding, String version, String cmisOperation, String body) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.post(getRequestContext(), cmisBinding, version, cmisOperation, body);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse put(Binding cmisBinding, String version, String cmisOperation, String body) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.put(getRequestContext(), cmisBinding, version, cmisOperation, body);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse get(Binding cmisBinding, String version, String cmisOperation, Map<String, String> parameters) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.get(getRequestContext(), cmisBinding, version, cmisOperation, parameters);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse patch(Binding cmisBinding, String version, String cmisOperation, Map<String, String> parameters) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.patch(getRequestContext(), cmisBinding, version, cmisOperation);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse trace(Binding cmisBinding, String version, String cmisOperation, Map<String, String> parameters) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.trace(getRequestContext(), cmisBinding, version, cmisOperation);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse options(Binding cmisBinding, String version, String cmisOperation, Map<String, String> parameters) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.options(getRequestContext(), cmisBinding, version, cmisOperation);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse head(Binding cmisBinding, String version, String cmisOperation, Map<String, String> parameters) throws PublicApiException
    {
        try
        {
            HttpResponse response = client.head(getRequestContext(), cmisBinding, version, cmisOperation);

            logger.debug(response.toString());

            return response;
        }
        catch(IOException e)
        {
            throw new PublicApiException(e);
        }
    }

    public HttpResponse post(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, String body) throws IOException
    {
        HttpResponse response = client.post(getRequestContext(), scope, entityCollectionName, entityId, relationCollectionName, relationshipEntityId != null ? relationshipEntityId.toString() : null, body);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse post(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, String body,
            final Map<String, String> params) throws IOException
    {
        HttpResponse response = client.post(getRequestContext(), scope, entityCollectionName, entityId, relationCollectionName,
                relationshipEntityId != null ? relationshipEntityId.toString() : null, body, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse post(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId,
                String body, String contentType) throws IOException
    {
        HttpResponse response = client.post(getRequestContext(), scope, entityCollectionName, entityId, relationCollectionName,
                    relationshipEntityId != null ? relationshipEntityId.toString() : null, body, contentType);

        logger.debug(response.toString());

        return response;
    }


    public HttpResponse post(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId,
                             byte[] body, String contentType) throws IOException
    {
        HttpResponse response = client.post(getRequestContext(), scope, entityCollectionName, entityId, relationCollectionName,
                relationshipEntityId != null ? relationshipEntityId.toString() : null, body, contentType);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse post(String urlSuffix, String body) throws IOException
    {
        HttpResponse response = client.post(getRequestContext(), urlSuffix, body);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse post(final Class<?> c, Object entityId, Object relationshipEntityId, String body) throws IOException
    {
        HttpResponse response = client.post(c, getRequestContext(), entityId, relationshipEntityId, body);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse get(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, Map<String, String> params, Map<String, String> headers) throws IOException
    {
        HttpResponse response = client.get(getRequestContext(), scope, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params, headers);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse get(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        return get(scope, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params, null);
    }

    public HttpResponse getWithPassword(String scope, String password, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        HttpResponse response = client.get(getRequestContext(), scope, password, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse get(String url, Map<String, String> params) throws IOException
    {
        RequestContext rc = getRequestContext();
        HttpResponse response = client.get(url, rc, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse get(final Class<?> c, final Object entityId, final Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        HttpResponse response = client.get(c, getRequestContext(), entityId, relationshipEntityId, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse put(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, String body, Map<String, String> params) throws IOException
    {
        HttpResponse response = client.put(getRequestContext(), scope, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, body, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse put(final Class<?> c, Object entityId, Object relationshipEntityId, String body) throws IOException
    {
        HttpResponse response = client.put(c, getRequestContext(), entityId, relationshipEntityId, body);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse putBinary(String scope, int version, String entityCollectionName, Object entityId, String relationCollectionName,
                Object relationshipEntityId, BinaryPayload payload, Map<String, String> params) throws IOException
    {
        HttpResponse response = client.putBinary(getRequestContext(), scope, version, entityCollectionName, entityId, relationCollectionName, relationshipEntityId,
                                payload, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse delete(String scope, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId) throws IOException
    {
        return delete(scope, 1, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, null);
    }

    public HttpResponse delete(String scope, int version, String entityCollectionName, Object entityId, String relationCollectionName, Object relationshipEntityId, Map<String, String> params) throws IOException
    {
        HttpResponse response = client.delete(getRequestContext(), scope, version, entityCollectionName, entityId, relationCollectionName, relationshipEntityId, params);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse put(final Class<?> c, Object entityId, Object relationshipEntityId) throws IOException
    {
        HttpResponse response = client.delete(c, getRequestContext(), entityId, relationshipEntityId);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse execute(RequestBuilder requestBuilder) throws IOException
    {
        if (requestBuilder.getRequestContext() == null)
        {
            throw new RuntimeException("Must set a request context");
        }
        HttpResponse response = client.execute(requestBuilder);

        logger.debug(response.toString());

        return response;
    }

    public HttpResponse index(Map<String, String> params) throws IOException
    {
        HttpResponse response = get("", params);
        return response;
    }

    public List<String> getNetworkIds(String version)
    {
        RequestContext rc = getRequestContext();

        UserData userData = findUser(rc.getRunAsUser());
        if(userData == null)
        {
            throw new RuntimeException("Must provide a valid username");
        }

        SessionFactory factory = SessionFactoryImpl.newInstance();

        Map<String, String> parameters = new HashMap<String, String>();

        // connection settings
        parameters.put(SessionParameter.ATOMPUB_URL, client.getPublicApiCmisUrl(null, Binding.atom, version, null));
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        // user credentials
        parameters.put(SessionParameter.USER, rc.getRunAsUser());
        parameters.put(SessionParameter.PASSWORD, userData.getPassword());
        List<Repository> repositories = factory.getRepositories(parameters);

        List<String> repositoryIds = new ArrayList<String>(repositories.size());
        for(Repository repository : repositories)
        {
            repositoryIds.add(repository.getId());
        }

        return repositoryIds;
    }

    public class AbstractProxy
    {
        public HttpResponse getAll(String entityCollectionName, String entityId, String relationCollectionName, String relationId, Map<String, String> params, String errorMessage)
                throws PublicApiException
        {
            return getAll(entityCollectionName, entityId, relationCollectionName, relationId, params, errorMessage, HttpServletResponse.SC_OK);
        }

        public HttpResponse getAll(String entityCollectionName, String entityId, String relationCollectionName, String relationId, Map<String, String> params, String errorMessage,
                int expectedStatus) throws PublicApiException
        {
            try
            {
                HttpResponse response = get("public", entityCollectionName, entityId, relationCollectionName, relationId, params);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch (IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public HttpResponse getSingle(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String errorMessage) throws PublicApiException
        {
            return getSingle(entityCollectionName, entityId, relationCollectionName, relationId, errorMessage, HttpServletResponse.SC_OK);
        }

        public HttpResponse getSingle(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String errorMessage, int expectedStatus) throws PublicApiException
        {
            try
            {
                HttpResponse response = get("public", entityCollectionName, entityId, relationCollectionName, relationId, null);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch(IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public HttpResponse getSingle(String entityCollectionName, String entityId, String relationCollectionName, String relationId, Map<String, String> params,
                                      Map<String, String> headers, String errorMessage, int expectedStatus) throws PublicApiException
        {
            try
            {
                HttpResponse response = get("public", entityCollectionName, entityId, relationCollectionName, relationId, params, headers);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch (IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public HttpResponse getSingle(String entityCollectionName, String entityId, String relationCollectionName, String relationId, Map<String, String> params,
                String errorMessage, int expectedStatus) throws PublicApiException
        {
            try
            {
                HttpResponse response = get("public", entityCollectionName, entityId, relationCollectionName, relationId, params);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch (IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public HttpResponse update(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String body, String errorMessage) throws PublicApiException
        {
            return update(entityCollectionName, entityId, relationCollectionName, relationId, body, null, errorMessage, 200);
        }

        public HttpResponse update(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String body, Map<String, String> params, String errorMessage, int expectedStatus) throws PublicApiException
        {
            try
            {
                HttpResponse response = put("public", entityCollectionName, entityId, relationCollectionName, relationId, body, params);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch(IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public HttpResponse create(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String body, String errorMessage) throws PublicApiException
        {
            return create(entityCollectionName, entityId, relationCollectionName, relationId, body, errorMessage, HttpServletResponse.SC_CREATED);
        }

        public HttpResponse create(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String body, String errorMessage,
                int expectedStatus) throws PublicApiException
        {
            return create(entityCollectionName, entityId, relationCollectionName, relationId, body, errorMessage, expectedStatus, null);
        }

        public HttpResponse create(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String body, String errorMessage,
                int expectedStatus, Map<String, String> params) throws PublicApiException
        {
            try
            {
                HttpResponse response = post("public", entityCollectionName, entityId, relationCollectionName, relationId, body, params);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch (IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public HttpResponse remove(String entityCollectionName, String entityId, String relationCollectionName, String relationId, String errorMessage) throws PublicApiException
        {
            return remove(entityCollectionName, entityId, relationCollectionName, relationId, null, errorMessage, HttpServletResponse.SC_NO_CONTENT);
        }

        public HttpResponse remove(String entityCollectionName, String entityId, String relationCollectionName, String relationId, Map<String,String> params, String errorMessage, int expectedStatus) throws PublicApiException
        {
            try
            {
                HttpResponse response = delete("public", 1, entityCollectionName, entityId, relationCollectionName, relationId, params);
                checkStatus(errorMessage, expectedStatus, response);
                return response;
            }
            catch(IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public JSONObject parseListSource(JSONObject jsonResponse)
        {
            JSONObject jsonList = (JSONObject)jsonResponse.get("list");
            assertNotNull(jsonList);

            JSONObject source = (JSONObject)jsonList.get("source");
            assertNotNull(source);
            return source;
        }
        
        /**
         * Used for validation of status code on rest response
         */
        public void checkStatus(String errorMessage, int expectedStatus, HttpResponse response) throws PublicApiException
        {
            int actualStatus = response.getStatusCode();
            if ((expectedStatus > 0) && (expectedStatus != actualStatus))
            {
                String msg = "Status code " + actualStatus + " returned, but expected " + expectedStatus + ": \n" + errorMessage + ": \n"
                        + "   Response: " + response;
                throw new PublicApiException(msg, response);
            }
            /// in case of Status.SC_UNAUTHORIZED no response is returned
            if (expectedStatus >= 400 && expectedStatus != 401)
            {
                checkErrorKeyResponse(errorMessage, expectedStatus, response);
            }
        }

        /**
         * Used for validation on field errorKey on Error rest response
         */
        public void checkErrorKeyResponse(String errorMessage, int expectedStatus, HttpResponse response) throws PublicApiException
        {
            PublicApiClient.ExpectedErrorResponse error;
            // the error response must have a valid format in conformity with
            // the
            // ErrorResponse model
            try
            {
                error = RestApiUtil.parseErrorResponse(response.getJsonResponse());
            }
            catch (Exception e)
            {
                throw new PublicApiException(e.getMessage(), response);
            }

            String errorKey = error.errorKey;
            // validate the errorKey which have default message exception
            if (errorKey != null && errorKey.contains("exception"))
            {
                boolean validErrorKey = false;

                List<RestErrorResponseDefault> httpErrorsFilteredByStatusCode = Arrays.asList(RestErrorResponseDefault.values()).stream()
                        .filter(t -> t.getStatusCode() == expectedStatus && t.getDefaultMessage() != null).collect(Collectors.toList());

                httpErrorsFilteredByStatusCode.add(RestErrorResponseDefault.DEFAULT_API_EXCEPTION);

                for (RestErrorResponseDefault errorResponseDefault : httpErrorsFilteredByStatusCode)
                {
                    if (errorResponseDefault.getDefaultMessage().equals(errorKey))
                    {
                        validErrorKey = true;
                    }
                }

                if (validErrorKey == false)
                {
                    String msg = "Error key " + errorKey + " returned is not valid " + "\n" + errorMessage + ": \n" + "   Response: " + response;
                    throw new PublicApiException(msg, response);
                }

            }
        }

    }

    public static class ListResponse<T>
    {
        private ExpectedPaging paging;
        private List<T> list;

        public ListResponse(ExpectedPaging paging, List<T> list)
        {
            super();
            this.paging = paging;
            this.list = list;
        }

        public ExpectedPaging getPaging()
        {
            return paging;
        }

        public List<T> getList()
        {
            return list;
        }
    }

    public class Sites extends AbstractProxy
    {
        public ListResponse<Site> getSites(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("sites", null, null, null, params, "Failed to get sites");
            return SiteImpl.parseSites(response.getJsonResponse());
        }

        public Site getSite(String siteId) throws PublicApiException
        {
            return getSite(siteId, 200);
        }

        public Site getSite(String siteId, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = getSingle("sites", siteId, null, null, "Failed to get site " + siteId, expectedStatus);
            if ((response != null) && (response.getJsonResponse() != null))
            {
                return SiteImpl.parseSite((JSONObject)response.getJsonResponse().get("entry"));
            }
            else
            {
                return null;
            }
        }

        public Site createSite(Site site) throws PublicApiException
        {
            return createSite(site, 201);
        }

        public Site createSite(Site site, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = create("sites", null, null, null, site.toJSON().toString(), "Failed to create site "+site.getTitle(), expectedStatus);
            return SiteImpl.parseSite((JSONObject)response.getJsonResponse().get("entry"));
        }

        public void removeSite(String siteId) throws PublicApiException
        {
            removeSite(siteId, false, 204);
        }

        public void removeSite(String siteId, boolean permanent, int expectedStatus) throws PublicApiException
        {
            Map<String, String> params = null;
            if (permanent)
            {
                params = Collections.singletonMap("permanent", "true");
            }
            remove("sites", siteId, null, null, params, "Failed to remove site", expectedStatus);
        }

        public Site updateSite(String siteId, SiteUpdate update, int expectedStatus) throws PublicApiException
        {
            JSONAble jsonizer = new TestSites.SiteUpdateJSONSerializer(update);
            HttpResponse response = update("sites", siteId, null, null, jsonizer.toJSON().toString(), null, "Failed to update site " + update.getTitle(), expectedStatus);
            if (response.getJsonResponse() != null)
            {
                return SiteImpl.parseSite((JSONObject) response.getJsonResponse().get("entry"));
            }
            // No JSON response to parse.
            return null;
        }

        public ListResponse<SiteContainer> getSiteContainers(String siteId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("sites", siteId, "containers", null, params, "Failed to get site containers");
            return SiteContainer.parseSiteContainers(response.getJsonResponse());
        }

        public SiteContainer getSingleSiteContainer(String siteId, String containerId) throws PublicApiException
        {
            HttpResponse response = getSingle("sites", siteId, "containers", containerId, "Failed to get site container");
            SiteContainer siteContainer = SiteContainer.parseSiteContainer(siteId, (JSONObject)response.getJsonResponse().get("entry"));
            return siteContainer;
        }

        public SiteContainer updateSiteContainer(SiteContainer siteContainer) throws PublicApiException
        {
            HttpResponse response = update("sites", siteContainer.getSiteId(), "containers", siteContainer.getId(), siteContainer.toJSON().toString(), "Failed to update site container");
            SiteContainer retSiteContainer = SiteContainer.parseSiteContainer(siteContainer.getSiteId(), (JSONObject)response.getJsonResponse().get("entry"));
            return retSiteContainer;
        }

        public SiteContainer createSiteContainer(SiteContainer siteContainer) throws PublicApiException
        {
            HttpResponse response = create("sites", siteContainer.getSiteId(), "containers", null, siteContainer.toJSON().toString(), "Failed to create site container");
            SiteContainer retSiteContainer = SiteContainer.parseSiteContainer(siteContainer.getSiteId(), (JSONObject)response.getJsonResponse().get("entry"));
            return retSiteContainer;
        }

        public void removeSiteContainer(SiteContainer siteContainer) throws PublicApiException
        {
            remove("sites", siteContainer.getSiteId(), "containers", siteContainer.getId(), "Failed to remove site container");
        }

        public ListResponse<SiteMember> getSiteMembers(String siteId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("sites", siteId, "members", null, params, "Failed to get all site members");
            return SiteMember.parseSiteMembers(siteId, response.getJsonResponse());
        }

        public SiteMember getSingleSiteMember(String siteId, String personId) throws PublicApiException
        {
            HttpResponse response = getSingle("sites", siteId, "members", personId, "Failed to get site member");
            SiteMember retSiteMember = SiteMember.parseSiteMember(siteId, (JSONObject)response.getJsonResponse().get("entry"));
            return retSiteMember;
        }

        public SiteMember updateSiteMember(String siteId, SiteMember siteMember) throws PublicApiException
        {
            HttpResponse response = update("sites", siteId, "members", siteMember.getMemberId(), siteMember.toJSON().toString(), "Failed to update site member");
            SiteMember retSiteMember = SiteMember.parseSiteMember(siteMember.getSiteId(), (JSONObject)response.getJsonResponse().get("entry"));
            return retSiteMember;
        }

        public SiteMember createSiteMember(String siteId, SiteMember siteMember) throws PublicApiException
        {
            HttpResponse response = create("sites", siteId, "members", null, siteMember.toJSON().toString(), "Failed to create site member");
            SiteMember retSiteMember = SiteMember.parseSiteMember(siteMember.getSiteId(), (JSONObject)response.getJsonResponse().get("entry"));
            return retSiteMember;
        }

        public void removeSiteMember(String siteId, SiteMember siteMember) throws PublicApiException
        {
            remove("sites", siteId, "members", siteMember.getMemberId(), "Failed to remove site member");
        }

        public ListResponse<MemberOfSite> getPersonSites(String personId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("people", personId, "sites", null, params, "Failed to get person sites");
            return MemberOfSite.parseMemberOfSites(response.getJsonResponse());
        }

        public MemberOfSite getPersonSite(String personId, String siteId) throws PublicApiException
        {
            HttpResponse response = getSingle("people", personId, "sites", siteId, "Failed to get person site" + siteId);
            return MemberOfSite.parseMemberOfSite((JSONObject)response.getJsonResponse().get("entry"));
        }

        public MemberOfSite updatePersonSite(String personId, SiteMember siteMember) throws PublicApiException
        {
            HttpResponse response = update("people", personId, "sites", siteMember.getSiteId(), siteMember.toJSON().toString(), "Failed to update person site");
            MemberOfSite retSiteMember = MemberOfSite.parseMemberOfSite((JSONObject)response.getJsonResponse().get("entry"));
            return retSiteMember;
        }

        public MemberOfSite createPersonSite(String personId, SiteMember siteMember) throws PublicApiException
        {
            HttpResponse response = create("people", personId, "sites", null, siteMember.toJSON().toString(), "Failed to create person site");
            MemberOfSite retSiteMember = MemberOfSite.parseMemberOfSite((JSONObject)response.getJsonResponse().get("entry"));
            return retSiteMember;
        }

        public void removePersonSite(String personId, SiteMember siteMember) throws PublicApiException
        {
            remove("people", personId, "sites", siteMember.getSiteId(), "Failed to remove person site");
        }

        public ListResponse<FavouriteSite> getFavouriteSites(String personId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("people", personId, "favorite-sites", null, params, "Failed to get favourite sites");
            return FavouriteSite.parseFavouriteSites(response.getJsonResponse());
        }

        public FavouriteSite getSingleFavouriteSite(String personId, String siteId) throws PublicApiException
        {
            HttpResponse response = getSingle("people", personId, "favorite-sites", siteId, "Failed to get favourite site");
            FavouriteSite favouriteSite = FavouriteSite.parseFavouriteSite((JSONObject)response.getJsonResponse().get("entry"));
            return favouriteSite;
        }

        public FavouriteSite updateFavouriteSite(String personId, FavouriteSite site) throws PublicApiException
        {
            HttpResponse response = update("people", personId, "favorite-sites", site.getSiteId(), site.toJSON().toString(), "Failed to update favourite site");
            FavouriteSite favouriteSite = FavouriteSite.parseFavouriteSite((JSONObject)response.getJsonResponse().get("entry"));
            return favouriteSite;
        }

        public FavouriteSite createFavouriteSite(String personId, FavouriteSite site) throws PublicApiException
        {
            HttpResponse response = create("people", personId, "favorite-sites", null, site.toJSON().toString(), "Failed to create favourite site");
            FavouriteSite favouriteSite = FavouriteSite.parseFavouriteSite((JSONObject)response.getJsonResponse().get("entry"));
            return favouriteSite;
        }

        public void removeFavouriteSite(String personId, FavouriteSite site) throws PublicApiException
        {
            remove("people", personId, "favorite-sites", site.getSiteId(), "Failed to remove favourite site");
        }

        public SiteGroup addGroup(String siteId, SiteGroup group) throws PublicApiException
        {
            HttpResponse response = create("sites", siteId, "group-members", null, group.toJSON().toString() , "Failed to add site groups");
            return SiteGroup.parseSiteGroup(siteId, (JSONObject)response.getJsonResponse().get("entry"));
        }

        public ListResponse<SiteGroup> getGroups(String siteId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("sites", siteId, "group-members", null, params, "Failed to fetch all site groups");
            return SiteGroup.parseGroupMemberOfSites(siteId, response.getJsonResponse());
        }

        public SiteGroup getGroup(String siteId, String groupId) throws PublicApiException
        {
            HttpResponse response = getSingle("sites", siteId, "group-members", groupId.toString() , "Failed to get a site group");
            return SiteGroup.parseSiteGroup(siteId, (JSONObject)response.getJsonResponse().get("entry"));
        }

        public SiteGroup updateGroup(String siteId, SiteGroup group) throws PublicApiException
        {
            HttpResponse response = update("sites", siteId, "group-members", group.getId(), group.toJSON().toString() , "Failed to update a site group");
            return SiteGroup.parseSiteGroup(siteId, (JSONObject)response.getJsonResponse().get("entry"));
        }

        public void deleteGroup(String siteId, String groupId) throws PublicApiException
        {
            remove("sites", siteId, "group-members", groupId , "Failed to delete site group");
        }
    }

    public class SiteMembershipRequests extends AbstractProxy
    {
        public SiteMembershipRequest getSiteMembershipRequest(String personId, String siteId) throws PublicApiException, ParseException
        {
            HttpResponse response = getSingle("people", personId, "site-membership-requests", siteId, "Failed to get siteMembershipRequest");
            return SiteMembershipRequest.parseSiteMembershipRequest((JSONObject) response.getJsonResponse().get("entry"));
        }

        public ListResponse<SiteMembershipRequest> getSiteMembershipRequests(String personId, Map<String, String> params) throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("people", personId, "site-membership-requests", null, params, "Failed to get siteMembershipRequests");
            return SiteMembershipRequest.parseSiteMembershipRequests(response.getJsonResponse());
        }

        public SiteMembershipRequest createSiteMembershipRequest(String personId, SiteMembershipRequest siteMembershipRequest) throws PublicApiException, ParseException
        {
            HttpResponse response = create("people", personId, "site-membership-requests", null, siteMembershipRequest.toJSON().toString(), "Failed to create siteMembershipRequest");
            SiteMembershipRequest ret = SiteMembershipRequest.parseSiteMembershipRequest((JSONObject) response.getJsonResponse().get("entry"));
            return ret;
        }

        public SiteMembershipRequest updateSiteMembershipRequest(String personId, SiteMembershipRequest siteMembershipRequest) throws PublicApiException, ParseException
        {
            HttpResponse response = update("people", personId, "site-membership-requests", siteMembershipRequest.getId(), siteMembershipRequest.toJSON().toString(), "Failed to update siteMembershipRequest");
            SiteMembershipRequest ret = SiteMembershipRequest.parseSiteMembershipRequest((JSONObject) response.getJsonResponse().get("entry"));
            return ret;
        }

        public void cancelSiteMembershipRequest(String personId, String siteMembershipRequestId) throws PublicApiException
        {
            remove("people", personId, "site-membership-requests", siteMembershipRequestId, "Failed to cancel siteMembershipRequest");
        }

        public ListResponse<SiteMembershipRequest> getSiteMembershipRequests(Map<String, String> params, String errorMessage, int expectedStatus) throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("site-membership-requests", null, null, null, params, errorMessage, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return SiteMembershipRequest.parseSiteMembershipRequests(response.getJsonResponse());
                }
            }
            return null;
        }
    }

    public class RawProxy extends AbstractProxy
    {
    }

    public class Favourites extends AbstractProxy
    {
        public ListResponse<Favourite> getFavourites(String personId, Map<String, String> params) throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("people", personId, "favorites", null, params, "Failed to get favourites");
            return Favourite.parseFavourites(response.getJsonResponse());
        }

        public Favourite getFavourite(String personId, String favouriteId) throws PublicApiException, ParseException
        {
            HttpResponse response = getSingle("people", personId, "favorites", favouriteId, "Failed to get favourite " + favouriteId);
            return Favourite.parseFavourite((JSONObject)response.getJsonResponse().get("entry"));
        }

        public Favourite getFavourite(String personId, String favouriteId, Map<String, String> params) throws PublicApiException, ParseException
        {
            HttpResponse response = getSingle("people", personId, "favorites", favouriteId, params, "Failed to get favourite " + favouriteId, 200);
            return Favourite.parseFavourite((JSONObject) response.getJsonResponse().get("entry"));
        }

        public Favourite createFavourite(String personId, Favourite favourite) throws PublicApiException, ParseException
        {
            HttpResponse response = create("people", personId, "favorites", null, favourite.toJSON().toString(), "Failed to create favourite");
            Favourite ret = Favourite.parseFavourite((JSONObject)response.getJsonResponse().get("entry"));
            return ret;
        }

        public Favourite createFavourite(String personId, Favourite favourite, Map<String, String> params) throws PublicApiException, ParseException
        {
            HttpResponse response = create("people", personId, "favorites", null, favourite.toJSON().toString(), "Failed to create favourite", 201, params);
            Favourite ret = Favourite.parseFavourite((JSONObject)response.getJsonResponse().get("entry"));
            return ret;
        }

        public void removeFavourite(String personId, String favouriteId) throws PublicApiException
        {
            remove("people", personId, "favorites", favouriteId, "Failed to remove favourite");
        }
    }

    public class People extends AbstractProxy
    {
        public ListResponse<Person> getPeople(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("people", null, null, null, params, "Failed to get people");
            return Person.parsePeople(response.getJsonResponse());
        }

        public Person getPerson(String personId) throws PublicApiException
        {
            return getPerson(personId, 200);
        }

        public Person getPerson(String personId, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = getSingle("people", personId, null, null, "Failed to get person", expectedStatus);

            if(logger.isDebugEnabled())
            {
                logger.debug(response);
            }
            System.out.println(response);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
                if (entry != null)
                {
                    return Person.parsePerson(entry);
                }
            }
            return null;
        }

        public Person update(String personId, Person person) throws PublicApiException
        {
            return update(personId, person, 200);
        }

        public Person update(String personId, Person person, int expectedStatus) throws PublicApiException
        {
            return update(personId, person.toJSON(true).toString(), expectedStatus);
        }

        public Person update(String personId, String json, int expectedStatus) throws PublicApiException
        {
            return update(personId, json, null, expectedStatus);
        }

        public Person update(String personId, String json, Map<String,String> params, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = update("people", personId, null, null, json, params, "Failed to update person", expectedStatus);
            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
                if (entry != null)
                {
                    return Person.parsePerson(entry);
                }
            }
            return null;
        }

        public Person create(Person person) throws PublicApiException
        {
            return create(person, 201);
        }

        public Person create(Person person, int expectedStatus) throws PublicApiException
        {
            TestPeople.PersonJSONSerializer jsonizer = new TestPeople.PersonJSONSerializer(person) ;
            HttpResponse response = create("people", null, null, null, jsonizer.toJSON().toString(), "Failed to create person", expectedStatus);
            if ((response != null) && (response.getJsonResponse() != null))
            {
                JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
                if (entry != null)
                {
                    return Person.parsePerson(entry);
                }
            }
            return null;
        }

        public void remove(Person person) throws PublicApiException
        {
            remove("people", person.getId(), null, null, "Failed to remove person");
        }

        public ListResponse<Preference> getPreferences(String personId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("people", personId, "preferences", null, params, "Failed to get person preferences");
            return Preference.parsePreferences(response.getJsonResponse());
        }

        public Preference getPreference(String personId, String preferenceId) throws PublicApiException
        {
            HttpResponse response = getSingle("people", personId, "preferences", preferenceId, "Failed to get person preference");
            Preference pref = Preference.parsePreference((JSONObject)response.getJsonResponse().get("entry"));
            return pref;
        }

        public Person updatePreference(String personId, Preference preference) throws PublicApiException
        {
            HttpResponse response = update("people", personId, "preferences", preference.getId(), preference.toJSON().toString(), "Failed to update person preference");
            Person retSite = Person.parsePerson((JSONObject)response.getJsonResponse().get("entry"));
            return retSite;
        }

        public Person createPreference(String personId, Preference preference) throws PublicApiException
        {
            HttpResponse response = create("people", personId, "preferences", null, preference.toJSON().toString(), "Failed to create person preference");
            Person retSite = Person.parsePerson((JSONObject)response.getJsonResponse().get("entry"));
            return retSite;
        }

        public void removePreference(String personId, Preference preference) throws PublicApiException
        {
            remove("people", personId, "preferences", preference.getId(), "Failed to remove person preference");
        }

        public ListResponse<PersonNetwork> getNetworkMemberships(String personId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("people", personId, "networks", null, params, "Failed to get network members");
            return PersonNetwork.parseNetworkMembers(response.getJsonResponse());
        }

        public PersonNetwork getNetworkMembership(String personId, String networkId) throws PublicApiException
        {
            HttpResponse response = getSingle("people", personId, "networks", networkId, "Failed to get network member");
            PersonNetwork networkMember = PersonNetwork.parseNetworkMember((JSONObject)response.getJsonResponse().get("entry"));
            return networkMember;
        }

        public PersonNetwork updateNetworkMembership(String personId, PersonNetwork networkMember) throws PublicApiException
        {
            HttpResponse response = update("people", personId, "networks", networkMember.getId(), networkMember.toJSON().toString(), "Failed to update network member");
            PersonNetwork retNetwork = PersonNetwork.parseNetworkMember((JSONObject)response.getJsonResponse().get("entry"));
            return retNetwork;
        }

        public PersonNetwork createNetworkMembership(String personId, PersonNetwork network) throws PublicApiException
        {
            HttpResponse response = create("people", personId, "networks", null, network.toJSON().toString(), "Failed to create network member");
            PersonNetwork retNetwork = PersonNetwork.parseNetworkMember((JSONObject)response.getJsonResponse().get("entry"));
            return retNetwork;
        }

        public void removeNetworkMembership(String personId, PersonNetwork networkMember) throws PublicApiException
        {
            remove("people", personId, "networks", networkMember.getId(), "Failed to remove network member");
        }

        public ListResponse<Activity> getActivities(String personId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("people", personId, "activities", null, params, "Failed to get activities");
            return Activities.parseActivities(response.getJsonResponse());
        }

        public Activity getSingleActivity(String personId, String activityId) throws PublicApiException
        {
            HttpResponse response = getSingle("people", personId, "activities", activityId, "Failed to get activities");
            Activity activity = Activity.parseActivity((JSONObject)response.getJsonResponse().get("entry"));
            return activity;
        }

        public Activity update(String personId, Activity activity) throws PublicApiException
        {
            HttpResponse response = update("people", personId, "activities", String.valueOf(activity.getId()), activity.toJSON().toString(), "Failed to update activity");
            Activity retActivity = Activity.parseActivity((JSONObject)response.getJsonResponse().get("entry"));
            return retActivity;
        }

        public Activity create(String personId, Activity activity) throws PublicApiException
        {
            HttpResponse response = create("people", personId, "activities", String.valueOf(activity.getId()), activity.toJSON().toString(), "Failed to create activity");
            Activity retActivity = Activity.parseActivity((JSONObject)response.getJsonResponse().get("entry"));
            return retActivity;
        }

        public void remove(String personId, Activity activity) throws PublicApiException
        {
            remove("people", personId, "activities", String.valueOf(activity.getId()), "Failed to remove activity");
        }

        public HttpResponse getAvatar(String personId, boolean placeholder, int expectedStatus) throws PublicApiException, InterruptedException
        {
            return getAvatar(personId, null, placeholder, null, expectedStatus);
        }

        public HttpResponse getAvatar(String personId, String ifModifiedSince, int expectedStatus) throws PublicApiException, InterruptedException
        {
            return getAvatar(personId, null, false, ifModifiedSince, expectedStatus);
        }

        public HttpResponse getAvatar(String personId, Boolean attachment, boolean placeholder, String ifModifiedSince, int expectedStatus) throws PublicApiException, InterruptedException
        {
            // Binary response expected
            Map<String, String> params = new HashMap<>();
            params.put("placeholder", Boolean.toString(placeholder));
            // Optional attachment parameter
            if (attachment != null)
            {
                params.put("attachment", attachment.toString());
            }

            Map<String, String> headers = new HashMap<>();
            if (ifModifiedSince != null)
            {
                headers.put("If-Modified-Since", ifModifiedSince);
            }

            // As renditions are now done async, we generally need to wait.
            HttpResponse response = getSingleWithDelayRetry("people", personId, "avatar",
                    null, params, headers, "Failed to get avatar", 40, 2500, expectedStatus);
            checkStatus("Unexpected response", expectedStatus, response);

            return response;
        }

        private HttpResponse getSingleWithDelayRetry(String entityCollectionName, String entityId, String relationCollectionName, String relationId, Map<String, String> params,
                                      Map<String, String> headers, String errorMessage, int repeat, long pauseInMillisecond, int expectedStatus) throws PublicApiException, InterruptedException
        {
            int retryCount = 0;
            while (retryCount < repeat)
            {
                try
                {
                    return getSingle(entityCollectionName, entityId, relationCollectionName, relationId, params, headers, errorMessage, expectedStatus);
                }
                catch (PublicApiException ex)
                {
                    retryCount++;
                    Thread.sleep(pauseInMillisecond);
                }
            }
            return null;
        }

        public HttpResponse updateAvatar(String personId, File avatar, int expectedStatus) throws PublicApiException
        {
            try
            {
                Map<String, String> params = new HashMap<>();
                BinaryPayload payload = new BinaryPayload(avatar);
                HttpResponse response = client.putBinary(getRequestContext(), "public", 1, "people", personId, "avatar", null, payload, params);
                checkStatus("Unexpected status", expectedStatus, response);
                return response;
            }
            catch(IOException e)
            {
                throw new PublicApiException(e);
            }
        }

        public void deleteAvatarImage(String personId, int expectedStatus) throws PublicApiException{
            remove("people", personId, "avatar", null, null, "Failed to remove avatar image", expectedStatus);
        }
    }

    public class Comments extends AbstractProxy
    {
        public ListResponse<Comment> getTenantComments(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("comments", null, null, null, params, "Failed to get comments");
            return Comment.parseComments(null, response.getJsonResponse());
        }

        public Comment getTenantComment(String commentId) throws PublicApiException
        {
            HttpResponse response = getSingle("comments", commentId, null, null, "Failed to get comment");
            Comment comment = Comment.parseComment(null, (JSONObject)response.getJsonResponse().get("entry"));
            return comment;
        }

        public Comment updateTenantComment(Comment comment) throws PublicApiException
        {
            HttpResponse response = update("comments", comment.getId(), null, null, comment.toJSON(true).toString(), "Failed to update comment");
            Comment retComment = Comment.parseComment(null, (JSONObject)response.getJsonResponse().get("entry"));
            return retComment;
        }

        public Comment createTenantComment(Comment comment) throws PublicApiException
        {
            HttpResponse response = create("comments", null, null, null, comment.toJSON(true).toString(), "Failed to create comment");
            Comment retComment = Comment.parseComment(null, (JSONObject)response.getJsonResponse().get("entry"));
            return retComment;
        }

        public void removeTenantComment(Comment comment) throws PublicApiException
        {
            remove("comments", comment.getId(), null, null, "Failed to remove comment");
        }

        public ListResponse<Comment> getNodeComments(String nodeId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("nodes", nodeId, "comments", null, params, "Failed to get comments");
            return Comment.parseComments(nodeId, response.getJsonResponse());
        }

        public Comment getNodeComment(String nodeId) throws PublicApiException
        {
            HttpResponse response = getSingle("nodes", nodeId, null, null, "Failed to get comment");
            Comment comment = Comment.parseComment(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
            return comment;
        }

        public Comment updateNodeComment(String nodeId, String commentId, Comment comment) throws PublicApiException
        {
            HttpResponse response = update("nodes", nodeId, "comments", commentId, comment.toJSON(true).toString(), "Failed to update comment");
            Comment retComment = Comment.parseComment(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
            return retComment;
        }

        public Comment createNodeComment(String nodeId, Comment comment) throws PublicApiException
        {
            HttpResponse response = create("nodes", nodeId, "comments", null, comment.toJSON(true).toString(), "Failed to create comment");
            Comment retComment = Comment.parseComment(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
            return retComment;
        }

        public void removeNodeComment(String nodeId, String commentId) throws PublicApiException
        {
            remove("nodes", nodeId, "comments", commentId, "Failed to remove comment");
        }
    }

    public class Tags extends AbstractProxy
    {
        public ListResponse<Tag> getTags(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("tags", null, null, null, params, "Failed to get tags");
            return Tag.parseTags(null, response.getJsonResponse());
        }

        public Tag getSingle(String tagId) throws PublicApiException
        {
            HttpResponse response = getSingle("tags", tagId, null, null, "Failed to get tag");
            Tag tag = Tag.parseTag(null, (JSONObject)response.getJsonResponse().get("entry"));
            return tag;
        }

        public Tag update(Tag tag) throws PublicApiException
        {
            HttpResponse response = update("tags", tag.getId(), null, null, tag.toJSON().toString(), "Failed to update tag");
            Tag retTag = Tag.parseTag(null, (JSONObject)response.getJsonResponse().get("entry"));
            return retTag;
        }

//		public Tag create(Tag tag) throws PublicApiException
//		{
//			HttpResponse response = create("tags", tag.getTagId(), null, null, tag.toJSON().toString(), "Failed to create tag");
//			Tag retTag = Tag.parseTag(null, (JSONObject)response.getJsonResponse().get("entry"));
//			return retTag;
//		}

        public void remove(Tag tag) throws PublicApiException
        {
            remove("tags", tag.getId(), null, null, "Failed to remove tag");
        }
    }

    public class Nodes extends AbstractProxy
    {
        public ListResponse<Tag> getNodeTags(String nodeId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("nodes", nodeId, "tags", null, params, "Failed to get node tags");
            return Tag.parseTags(nodeId, response.getJsonResponse());
        }

        public void removeNodeTag(String nodeId, String tagId) throws PublicApiException
        {
            remove("nodes", nodeId, "tags", tagId, "Failed to remove node tag");
        }

        public Tag createNodeTag(String nodeId, Tag tag) throws PublicApiException
        {
            HttpResponse response = create("nodes", nodeId, "tags", null, tag.toJSON().toString(), "Failed to create node tag");
            Tag tagRet = Tag.parseTag(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
            return tagRet;
        }

        public NodeRating getNodeRating(String nodeId, String ratingId) throws PublicApiException
        {
            HttpResponse response = getSingle("nodes", nodeId, "ratings", ratingId, "Failed to get node ratings");
            return NodeRating.parseNodeRating(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
        }

        public ListResponse<NodeRating> getNodeRatings(String nodeId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("nodes", nodeId, "ratings", null, params, "Failed to get node ratings");
            return NodeRating.parseNodeRatings(nodeId, response.getJsonResponse());
        }

        public NodeRating updateNodeRating(String nodeId, NodeRating nodeRating) throws PublicApiException
        {
            HttpResponse response = update("nodes", nodeId, "ratings", nodeRating.getId(), nodeRating.toJSON().toString(), "Failed to update node rating");
            NodeRating nodeRatingRet = NodeRating.parseNodeRating(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
            return nodeRatingRet;
        }

        public NodeRating createNodeRating(String nodeId, NodeRating nodeRating) throws PublicApiException
        {
            HttpResponse response = create("nodes", nodeId, "ratings", null, nodeRating.toJSON().toString(), "Failed to create node rating");
            NodeRating nodeRatingRet = NodeRating.parseNodeRating(nodeId, (JSONObject)response.getJsonResponse().get("entry"));
            return nodeRatingRet;
        }

        public void removeNodeRating(String nodeId, NodeRating rating) throws PublicApiException
        {
            remove("nodes", nodeId, "ratings", rating.getId(), "Failed to remove node rating");
        }
    }

    public class Aspects extends AbstractProxy
    {
        public PublicApiClient.ListResponse<Aspect> getAspects(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("aspects", null, null, null, params, "Failed to get aspects");
            return Aspect.parseAspects(response.getJsonResponse());
        }

        public Aspect getAspect(String aspectId) throws PublicApiException
        {
            HttpResponse response = getAll("aspects", aspectId, null, null, null, "Failed to get aspect");
            return Aspect.parseAspect((JSONObject)response.getJsonResponse().get("entry"));
        }
    }

    public class Types extends AbstractProxy
    {
        public PublicApiClient.ListResponse<Type> getTypes(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("types", null, null, null, params, "Failed to get types");
            return Type.parseTypes(response.getJsonResponse());
        }

        public Type getType(String typeId) throws PublicApiException
        {
            HttpResponse response = getAll("types", typeId, null, null, null, "Failed to get type");
            return Type.parseType((JSONObject)response.getJsonResponse().get("entry"));
        }
    }

    public static class ExpectedPaging
    {
        private int skipCount;
        private int maxItems;
        private Integer totalItems;
        private boolean hasMoreItems;
        private int count;

        public ExpectedPaging()
        {
        }

        public ExpectedPaging(int skipCount, int maxItems, Integer totalItems, boolean hasMoreItems, int count)
        {
            super();
            this.skipCount = skipCount;
            this.maxItems = maxItems;
            this.totalItems = totalItems;
            this.hasMoreItems = hasMoreItems;
            this.count = count;
        }

        public Integer getSkipCount()
        {
            return skipCount;
        }

        public Integer getMaxItems()
        {
            return maxItems;
        }

        public Integer getTotalItems()
        {
            return totalItems;
        }

        public Boolean getHasMoreItems()
        {
            return hasMoreItems;
        }

        public Integer getCount()
        {
            return count;
        }

        public void setCount(Integer count)
        {
            this.count = count;
        }

        public void setHasMoreItems(Boolean hasMoreItems)
        {
            this.hasMoreItems = hasMoreItems;
        }

        public void setTotalItems(Integer totalItems)
        {
            this.totalItems = totalItems;
        }

        public void setSkipCount(Integer skipCount)
        {
            this.skipCount = skipCount;
        }

        public void setMaxItems(Integer maxItems)
        {
            this.maxItems = maxItems;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + (hasMoreItems ? 1231 : 1237);
            result = prime * result + maxItems;
            result = prime * result + skipCount;
            result = prime * result
                    + ((totalItems == null) ? 0 : totalItems.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExpectedPaging other = (ExpectedPaging) obj;
            if (count != other.count)
                return false;
            if (hasMoreItems != other.hasMoreItems)
                return false;
            if (maxItems != other.maxItems)
                return false;
            if (skipCount != other.skipCount)
                return false;
            if (totalItems == null) {
                if (other.totalItems != null)
                    return false;
            } else if (!totalItems.equals(other.totalItems))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "ExpectedPaging [skipCount="
                    + skipCount
                    + ", maxItems="
                    + maxItems
                    + ", "
                    + (totalItems != null ? "totalItems=" + totalItems + ", "
                            : "") + "hasMoreItems=" + hasMoreItems + ", count="
                    + count + "]";
        }

        public static ExpectedPaging parsePagination(JSONObject jsonList)
        {
            ExpectedPaging paging = new ExpectedPaging();
            JSONObject jsonPagination = (JSONObject)jsonList.get("pagination");
            if(jsonPagination != null)
            {
                Long count = (Long)jsonPagination.get("count");
                paging.setCount(count.intValue());

                Boolean hasMoreItems = (Boolean)jsonPagination.get("hasMoreItems");
                paging.setHasMoreItems(hasMoreItems);

                Long totalItems = (Long)jsonPagination.get("totalItems");
                if(totalItems != null)
                {
                    paging.setTotalItems(totalItems.intValue());
                }

                Long maxItems = (Long)jsonPagination.get("maxItems");
                paging.setMaxItems(maxItems.intValue());

                Long skipCount = (Long)jsonPagination.get("skipCount");
                paging.setSkipCount(skipCount.intValue());
            }
            return paging;
        }

        public static ExpectedPaging getExpectedPaging(Integer skipCount, Integer maxItems, int total, Integer expectedTotal)
        {
            int skip = skipCount != null ? skipCount : org.alfresco.rest.framework.resource.parameters.Paging.DEFAULT_SKIP_COUNT;
            int max = maxItems != null ? maxItems : org.alfresco.rest.framework.resource.parameters.Paging.DEFAULT_MAX_ITEMS;
            int expectedCount = -1;
            int end = skip + max;
            if(end < 0 || end > total)
            {
                // overflow or greater than the total
                expectedCount = total - skip;
                end = total;
            }
            else
            {
                expectedCount = max;
            }
            if(expectedCount < 0)
            {
                expectedCount = 0;
            }
            boolean hasMore = end < total;
            ExpectedPaging expectedPaging = new ExpectedPaging(skip, max, expectedTotal, hasMore, expectedCount);
            return expectedPaging;
        }
    }

    public class CmisSession
    {
        private Session session;

        public CmisSession(Session session)
        {
            this.session = session;
        }

        public Session getCMISSession()
        {
            return session;
        }

        public CmisObject getObject(String objectId)
        {
            RequestContext rc = getRequestContext();
            OperationContext ctx = rc.getCmisOperationCtxOverride();
            if(ctx == null)
            {
                 ctx = new OperationContextImpl();
            }

            CmisObject res = session.getObject(objectId, ctx);
            return res;
        }

        public CmisObject getObjectByPath(String path)
        {
            OperationContextImpl ctx = new OperationContextImpl();
            CmisObject res = session.getObjectByPath(path, ctx);
            return res;
        }

        public List<Folder> getObjectParents(String objectId)
        {
            CmisObject o = session.getObject(objectId);
            if(o instanceof FileableCmisObject)
            {
                FileableCmisObject f = (FileableCmisObject)o;

                OperationContextImpl ctx = new OperationContextImpl();
                List<Folder> res = f.getParents(ctx);
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a fileable cmis object");
            }
        }

        public void deleteContent(String objectId, boolean refresh)
        {
            CmisObject o = getObject(objectId);

            if(o instanceof Document)
            {
                Document d = (Document)o;
                d.deleteContentStream(refresh);
            }
            else
            {
                throw new IllegalArgumentException("Object does not exists or is not a document");
            }
        }

        public ContentData getContent(String objectId) throws IOException
        {
            CmisObject o = getObject(objectId);
            if(o instanceof Document)
            {
                Document d = (Document)o;
                ContentStream res = d.getContentStream();
                ContentData c = new ContentData(res);
                return c;
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a document");
            }
        }

        public void putContent(String objectId, String filename, BigInteger length, String mimetype, InputStream content, boolean overwrite)
        {
            CmisObject o = getObject(objectId);
            if(o instanceof Document)
            {
                Document d = (Document)o;
                ContentStream contentStream = new ContentStreamImpl(filename, length, mimetype, content);
                try
                {
                    d.setContentStream(contentStream, overwrite);
                }
                finally
                {
                    try
                    {
                        contentStream.getStream().close();
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a document");
            }
        }

        public void addChildren(FolderNode f, Tree<FileableCmisObject> t)
        {
            FileableCmisObject fco = t.getItem();
            CMISNode child = CMISNode.createNode(fco);

            if(child instanceof FolderNode)
            {
                f.addFolder((FolderNode)child);
                for(Tree<FileableCmisObject> c : t.getChildren())
                {
                    addChildren((FolderNode)child, c);
                }
            }
            else
            {
                f.addNode(child);
            }
        }

        public boolean objectExists(String objectId)
        {
            CmisObject o = getObject(objectId);
            return(o != null);
        }

        public FolderNode getRootFolder()
        {
            Folder rootFolder = session.getRootFolder();

            FolderNode ret = (FolderNode)CMISNode.createNode(rootFolder);
            return ret;
        }

        public FolderNode getDescendants(String folderId, int depth)
        {
            Session session = getCMISSession();

            CmisObject o = session.getObject(folderId);
            if(o instanceof Folder)
            {
                Folder f = (Folder)o;

                OperationContextImpl ctx = new OperationContextImpl();
                List<Tree<FileableCmisObject>> res = f.getDescendants(depth, ctx);
                FolderNode ret = (FolderNode)CMISNode.createNode(f);
                for(Tree<FileableCmisObject> t : res)
                {
                    addChildren(ret, t);
                }

                return ret;
            }
            else
            {
                throw new IllegalArgumentException("Folder does not exist or is not a folder");
            }
        }

        public ItemIterable<ObjectType> getTypeChildren(String typeId, boolean includePropertyDefinitions)
        {
            ItemIterable<ObjectType> res = session.getTypeChildren(typeId, includePropertyDefinitions);
            return res;
        }

        public List<Tree<ObjectType>> getTypeDescendants(String typeId, int depth, boolean includePropertyDefinitions)
        {
            List<Tree<ObjectType>> res = session.getTypeDescendants(typeId, depth, includePropertyDefinitions);
            return res;
        }

        public ObjectType getTypeDefinition(String typeId)
        {
            ObjectType res = session.getTypeDefinition(typeId);
            return res;
        }

        public void removeAllVersions(String objectId)
        {
            CmisObject o = getObject(objectId);
            if(o instanceof Document)
            {
                Document d = (Document)o;
                d.deleteAllVersions();
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a document");
            }
        }

        public List<CMISNode> query(String query, boolean searchAllVersions, long skipCount, int maxItems)
        {
            OperationContext ctx = new OperationContextImpl();
            ItemIterable<QueryResult> res = session.query(query, searchAllVersions, ctx);
            res = res.skipTo(skipCount);
            res = res.getPage(maxItems);

            List<CMISNode> results = new ArrayList<CMISNode>((int)res.getPageNumItems());

            for(QueryResult r : res)
            {
                CMISNode n = CMISNode.createNode(r);
                results.add(n);
            }

            return results;
        }

        public void removeObject(String objectId, boolean allVersions)
        {
            CmisObject o = getObject(objectId);
            if(o != null)
            {
                o.delete(allVersions);
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist");
            }
        }

        public List<String> removeTree(String objectId, boolean allVersions, UnfileObject unfile, boolean continueOnFailure)
        {
            CmisObject o = getObject(objectId);
            if(o instanceof Folder)
            {
                Folder f = (Folder)o;
                List<String> res = f.deleteTree(allVersions, unfile, continueOnFailure);
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a folder");
            }
        }

        public void updateProperties(String objectId, Map<String, ?> properties, boolean refresh)
        {
            CmisObject o = getObject(objectId);
            if(o != null)
            {
                o.updateProperties(properties, refresh);
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist");
            }
        }

        public List<Tree<FileableCmisObject>> getFolderTree(String folderId, int depth)
        {
            CmisObject o = session.getObject(folderId);
            if(o instanceof Folder)
            {
                Folder f = (Folder)o;

                OperationContextImpl ctx = new OperationContextImpl();
                List<Tree<FileableCmisObject>> res = f.getFolderTree(depth, ctx);
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a folder");
            }
        }

        public FolderNode getChildren(String folderId, int skipCount, int maxItems)
        {
            CmisObject o = session.getObject(folderId);
            if(o instanceof Folder)
            {
                Folder f = (Folder)o;
                FolderNode ret = (FolderNode)CMISNode.createNode(f);

                OperationContextImpl ctx = new OperationContextImpl();
                ItemIterable<CmisObject> res = f.getChildren(ctx);
                res.skipTo(skipCount);
                ItemIterable<CmisObject> l = res.getPage(maxItems);
                for(CmisObject c : l)
                {
                    CMISNode child = null;
                    if(c.getBaseType() instanceof FolderTypeDefinition)
                    {
                        child = (FolderNode)CMISNode.createNode(c);
                        ret.addFolder((FolderNode)child);
                    }
                    else
                    {
                        child = CMISNode.createNode(c);
                        ret.addNode(child);
                    }
                }

                return ret;
            }
            else
            {
                throw new IllegalArgumentException("Folder does not exist or is not a folder");
            }
        }

        public ItemIterable<Document> getCheckedOutDocs()
        {
            OperationContextImpl ctx = new OperationContextImpl();
            ItemIterable<Document> res = session.getCheckedOutDocs(ctx);
            return res;
        }

        public List<Document> getAllVersions(String objectId)
        {
            CmisObject o = getObject(objectId);
            if(o instanceof Document)
            {
                Document d = (Document)o;
                OperationContext ctx = new OperationContextImpl();
                List<Document> res = d.getAllVersions(ctx);
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a document");
            }
        }

        public AllowableActions getAllowableActions(String objectId)
        {
            CmisObject o = getObject(objectId);
            AllowableActions res = o.getAllowableActions();
            return res;
        }

        public Document createDocument(String parentId, String name, Map<String, Serializable> properties, ContentStream contentStream, VersioningState versioningState)
        {
            CmisObject o = getObject(parentId);

            if(o instanceof Folder)
            {
                Folder f = (Folder)o;

                if(properties == null)
                {
                    properties = new HashMap<String, Serializable>();
                }
                String objectTypeId = (String)properties.get(PropertyIds.OBJECT_TYPE_ID);
                String type = "cmis:document";
                if(objectTypeId == null)
                {
                    objectTypeId = type;
                }
                if(objectTypeId.indexOf(type) == -1)
                {
                    StringBuilder sb = new StringBuilder(objectTypeId);
                    if(sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(type);
                    objectTypeId = sb.toString();
                }

                properties.put(PropertyIds.NAME, name);
                properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

                Document res = f.createDocument(properties, contentStream, versioningState);
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Parent does not exists or is not a folder");
            }
        }

        public Folder createFolder(String folderId, String name, Map<String, Serializable> properties)
        {
            CmisObject o = getObject(folderId);

            if(o instanceof Folder)
            {
                Folder f = (Folder)o;

                if(properties == null)
                {
                    properties = new HashMap<String, Serializable>();
                }
                String objectTypeId = (String)properties.get(PropertyIds.OBJECT_TYPE_ID);
                String type = "cmis:folder";
                if(objectTypeId == null)
                {
                    objectTypeId = type;
                }
                if(objectTypeId.indexOf(type) == -1)
                {
                    StringBuilder sb = new StringBuilder(objectTypeId);
                    if(sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(type);
                    objectTypeId = sb.toString();
                }

                properties.put(PropertyIds.NAME, name);
                properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

                Folder res = f.createFolder(properties);
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Parent does not exist or is not a folder");
            }
        }

        public ObjectId createRelationship(String sourceObjectId, String targetObjectId)
        {
            Map<String, Serializable> relProps = new HashMap<String, Serializable>();
            relProps.put("cmis:sourceId", sourceObjectId);
            relProps.put("cmis:targetId", targetObjectId);
            relProps.put("cmis:objectTypeId", "cmis:relationship");
            ObjectId res = session.createRelationship(relProps);
            return res;
        }

        public ObjectId checkoutObject(String objectId)
        {
            CmisObject o = getObject(objectId);
            if(o instanceof Document)
            {
                Document d = (Document)o;
                ObjectId res = d.checkOut();
                return res;
            }
            else
            {
                throw new IllegalArgumentException("Object does not exist or is not a document");
            }
        }
    }

    public static class Paging
    {
        private Integer skipCount;
        private Integer maxItems;
        private ExpectedPaging expectedPaging;

        public Paging()
        {
        }

        public Paging(Integer skipCount, Integer maxItems, ExpectedPaging expectedPaging)
        {
            super();
            this.skipCount = skipCount;
            this.maxItems = maxItems;
            this.expectedPaging = expectedPaging;
        }

        public ExpectedPaging getExpectedPaging()
        {
            return expectedPaging;
        }

        public void setExpectedPaging(ExpectedPaging expectedPaging)
        {
            this.expectedPaging = expectedPaging;
        }

        public Integer getSkipCount()
        {
            return skipCount;
        }

        public Integer getMaxItems()
        {
            return maxItems;
        }

        public void setSkipCount(Integer skipCount)
        {
            this.skipCount = skipCount;
        }

        public void setMaxItems(Integer maxItems)
        {
            this.maxItems = maxItems;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((maxItems == null) ? 0 : maxItems.hashCode());
            result = prime * result
                    + ((skipCount == null) ? 0 : skipCount.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Paging other = (Paging) obj;
            if (maxItems == null) {
                if (other.maxItems != null)
                    return false;
            } else if (!maxItems.equals(other.maxItems))
                return false;
            if (skipCount == null) {
                if (other.skipCount != null)
                    return false;
            } else if (!skipCount.equals(other.skipCount))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "Paging ["
                    + (skipCount != null ? "skipCount=" + skipCount + ", " : "")
                    + (maxItems != null ? "maxItems=" + maxItems : "") + "]";
        }
    }

    /**
     * Representation of an error response.
     *
     * @author Jamal Kaabi-Mofrad
     */
    public static class ExpectedErrorResponse
    {
        private String errorKey;
        private int statusCode;
        private String briefSummary;
        private String stackTrace;
        private Map<String, Object> additionalState;
        private String descriptionURL;
        private String logId;

        public ExpectedErrorResponse()
        {
        }

        public ExpectedErrorResponse(String errorKey, int statusCode, String briefSummary, StackTraceElement[] stackTrace,
                    Map<String, Object> additionalState, String logId)
        {
            super();
            this.errorKey = errorKey;
            this.statusCode = statusCode;
            this.briefSummary = briefSummary;
            this.stackTrace = Arrays.toString(stackTrace);
            this.additionalState = additionalState;
            this.logId = logId;
        }

        public String getErrorKey()
        {
            return errorKey;
        }

        public ExpectedErrorResponse setErrorKey(String errorKey)
        {
            this.errorKey = errorKey;
            return this;
        }

        public int getStatusCode()
        {
            return statusCode;
        }

        public ExpectedErrorResponse setStatusCode(int statusCode)
        {
            this.statusCode = statusCode;
            return this;
        }

        public String getBriefSummary()
        {
            return briefSummary;
        }

        public ExpectedErrorResponse setBriefSummary(String briefSummary)
        {
            this.briefSummary = briefSummary;
            return this;
        }

        public String getLogId() {
            return logId;
        }

        public void setLogId(String logId) {
            this.logId = logId;
        }

        public String getStackTrace()
        {
            return stackTrace;
        }

        public ExpectedErrorResponse setStackTrace(String stackTrace)
        {
            this.stackTrace = stackTrace;
            return this;
        }

        public Map<String, Object> getAdditionalState()
        {
            return additionalState;
        }

        public ExpectedErrorResponse setAdditionalState(Map<String, Object> additionalState)
        {
            this.additionalState = additionalState;
            return this;
        }

        public String getDescriptionURL()
        {
            return descriptionURL;
        }

        public ExpectedErrorResponse setDescriptionURL(String descriptionURL)
        {
            this.descriptionURL = descriptionURL;
            return this;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("ExpectedErrorResponse [errorKey='").append(errorKey)
                        .append(", statusCode=").append(statusCode)
                        .append(", briefSummary='").append(briefSummary)
                        .append(", logId='").append(logId)
                        .append(", stackTrace='").append(stackTrace)
                        .append(", additionalState=").append(additionalState)
                        .append(", descriptionURL='").append(descriptionURL)
                        .append(']');
            return sb.toString();
        }
    }

    public class Groups extends AbstractProxy
    {

        public Group createGroup(Group group, Map<String, String> params, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = create("groups", null, null, null, group.toJSON().toString(), "Failed to create group " + group.getId(), expectedStatus, params);
            return parseGroupEntity(response);
        }

        public GroupMember createGroupMember(String groupId, GroupMember groupMember) throws PublicApiException
        {
            return createGroupMember(groupId, groupMember, HttpServletResponse.SC_OK);
        }

        public GroupMember createGroupMember(String groupId, GroupMember groupMember, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = create("groups", groupId, "members", null, groupMember.toJSON().toString(), "Failed to create group membership", expectedStatus);
            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonEntity = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonEntity != null)
                {
                    return GroupMember.parseGroupMember(response.getJsonResponse());
                }
            }
            return null;
        }

        public Group updateGroup(String groupId, Group group, Map<String, String> params, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = update("groups", groupId, null, null, group.toJSON().toString(), params, "Failed to update group " + group.getId(), expectedStatus);
            return parseGroupEntity(response);
        }

        public Group getGroup(String groupId) throws PublicApiException
        {
            return getGroup(groupId, HttpServletResponse.SC_OK);
        }

        public Group getGroup(String groupId, int expectedStatus) throws PublicApiException
        {
            return getGroup(groupId, null, expectedStatus);
        }

        public Group getGroup(String groupId, Map<String, String> params, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = getSingle("groups", groupId, null, null, params, "Failed to get group " + groupId, expectedStatus);
            return parseGroupEntity(response);
        }

        public void deleteGroup(String groupId, boolean cascade, int expectedStatus) throws PublicApiException
        {
            Map<String, String> params = null;
            if (cascade)
            {
                params = Collections.singletonMap("cascade", "true");
            }
            remove("groups", groupId, null, null, params, "Failed to remove group", expectedStatus);
        }

        public void deleteGroupMembership(String groupId, String groupMemberId, int expectedStatus) throws PublicApiException
        {
            remove("groups", groupId, "members", groupMemberId, null, "Failed to remove group member", expectedStatus);
        }

        private Group parseGroupEntity(HttpResponse response)
        {
            if ((response != null) && (response.getJsonResponse() != null))
            {
                JSONObject jsonEntity = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonEntity != null)
                {
                    return Group.parseGroup(jsonEntity);
                }
            }

            return null;
        }

        public ListResponse<Group> getGroups(Map<String, String> params, String errorMessage, int expectedStatus) throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("groups", null, null, null, params, errorMessage, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return Group.parseGroups(response.getJsonResponse());
                }
            }
            return null;
        }

        public ListResponse<GroupMember> getGroupMembers(String groupId, Map<String, String> params) throws PublicApiException, ParseException
        {
            return getGroupMembers(groupId, params, "Failed to get groups", HttpServletResponse.SC_OK);
        }

        public ListResponse<GroupMember> getGroupMembers(String groupId, Map<String, String> params, String errorMessage, int expectedStatus)
                throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("groups", groupId, "members", null, params, errorMessage, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return GroupMember.parseGroupMembers(response.getJsonResponse());
                }
            }
            return null;
        }

        public ListResponse<Group> getGroupsByPersonId(String userId, Map<String, String> params, String errorMessage, int expectedStatus)
                throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("people", userId, "groups", null, params, errorMessage, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return Group.parseGroups(response.getJsonResponse());
                }
            }
            return null;
        }

    }

    public class AuditApps extends AbstractProxy
    {

        public ListResponse<AuditApp> getAuditApps(Map<String, String> params, String errorMessage, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = getAll("audit-applications", null, null, null, params, errorMessage, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return AuditApp.parseAuditApps(response.getJsonResponse());
                }
            }
            return null;
        }

        public AuditApp getAuditApp(String applicationId) throws PublicApiException
        {
            return getAuditApp(applicationId, HttpServletResponse.SC_OK);
        }

        public AuditApp getAuditApp(String applicationId, int expectedStatus) throws PublicApiException
        {
            return getAuditApp(applicationId, null, expectedStatus);
        }

        public AuditApp getAuditApp(String applicationId, Map<String, String> params, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = getSingle("audit-applications", applicationId, null, null, params, "Failed to get Audit Application " + applicationId,
                    expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonEntry = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonEntry != null)
                {
                    return AuditApp.parseAuditApp(jsonEntry);
                }
            }
            return null;
        }

        public AuditApp updateAuditApp(String applicationId, AuditApp auditApp, Map<String, String> params,  int expectedStatus) throws PublicApiException
        {
            HttpResponse response = update("audit-applications", applicationId,null,null, auditApp.toJSON().toString(), params, "Failed to update Audit Application", expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonEntry = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonEntry != null)
                {
                    return AuditApp.parseAuditApp(jsonEntry);
                }
            }
            return null;
        }

        public ListResponse<AuditEntry> getAuditAppEntries(String applicationId, Map<String, String> params, int expectedStatus)
                throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("audit-applications", applicationId, "audit-entries", null, params,
                    "Failed to get audit entries for " + applicationId, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return AuditEntry.parseAuditEntries(response.getJsonResponse());
                }
            }
            return null;
        }

        public AuditEntry getAuditEntry(String applicationId, String entryId, Map<String, String> param, int expectedStatus)
                throws PublicApiException, ParseException
        {
            HttpResponse response = getSingle("audit-applications", applicationId, "audit-entries",entryId,
                    param, "Failed to get Audit Application " + applicationId, expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonList != null)
                {
                    return AuditEntry.parseAuditEntry(jsonList);
                }
            }
            return null;
        }

        public void deleteAuditEntry(String applicationId, String entryId, Map<String, String> param, int expectedStatus)
                throws PublicApiException
        {
            remove("audit-applications", applicationId, "audit-entries", entryId, param,"Failed to delete entry " + entryId, expectedStatus);
        }

        public void deleteAuditEntries(String applicationId, Map<String, String> param, int expectedStatus)
                throws PublicApiException
        {
            remove("audit-applications", applicationId, "audit-entries", null, param,"Failed to delete entries for audit application " + applicationId, expectedStatus);
        }
        
        public ListResponse<AuditEntry> getAuditAppEntriesByNodeRefId(String nodeId, Map<String, String> params, int expectedStatus)
                throws PublicApiException, ParseException
        {
            HttpResponse response = getAll("nodes", nodeId, "audit-entries", null, params, "Failed to get audit entries for " + nodeId,
                    expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return AuditEntry.parseAuditEntries(response.getJsonResponse());
                }
            }
            return null;
        }

    }
    
    public class Actions extends AbstractProxy
    {
        public ListResponse<ActionDefinition> getActionDefinitionsForNode(String nodeId,
                                                                          Map<String, String> params,
                                                                          int expectedStatus)
                throws PublicApiException
        {
            HttpResponse response = getAll("nodes", nodeId, "action-definitions",
                    null, params, "Unexpected response", expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return parseActionDefinitions(response.getJsonResponse());
                }
            }
            return null;
        }

        public ListResponse<ActionDefinition> getActionDefinitions(Map<String, String> params,
                                                                   int expectedStatus)
                throws PublicApiException
        {
            HttpResponse response = getAll("action-definitions", null, null,
                    null, params, "Unexpected response", expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
                if (jsonList != null)
                {
                    return parseActionDefinitions(response.getJsonResponse());
                }
            }
            return null;
        }

        public Action executeAction(Action action, Map<String, String> params, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = create("action-executions", null, null, null, action.toJSON().toString(), "Failed to create action for action def " + action.getActionDefinitionId(), expectedStatus, params);
            return parseActionEntity(response);
        }
        
        private ListResponse<ActionDefinition> parseActionDefinitions(JSONObject jsonResponse)
        {
            List<ActionDefinition> actionDefinitions = new ArrayList<>();


            JSONObject jsonList = (JSONObject) jsonResponse.get("list");
            assertNotNull(jsonList);

            JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
            assertNotNull(jsonEntries);

            for (int i = 0; i < jsonEntries.size(); i++)
            {
                JSONObject jsonEntry = (JSONObject) jsonEntries.get(i);
                JSONObject entry = (JSONObject) jsonEntry.get("entry");
                actionDefinitions.add(parseActionDefinition(entry));
            }

            ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
            return new ListResponse<>(paging, actionDefinitions);
        }

        private ActionDefinition parseActionDefinition(JSONObject entry)
        {
            ActionDefinition def = null;
            try
            {
                def = objectMapper.readValue(entry.toString(), ActionDefinition.class);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to parse ActionDefinition JSON", e);
            }
            
            return def;
        }

        private Action parseActionEntity(HttpResponse response)
        {
            if ((response != null) && (response.getJsonResponse() != null))
            {
                JSONObject jsonEntity = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonEntity != null)
                {
                    return Action.parseAction(jsonEntity);
                }
            }

            return null;
        }
        
        public ActionDefinition getActionDefinition(String actionDefinitionId, int expectedStatus) throws PublicApiException
        {
            HttpResponse response = getSingle("action-definitions", actionDefinitionId, null,
                    null, null, "Unexpected response", expectedStatus);

            if (response != null && response.getJsonResponse() != null)
            {
                JSONObject jsonEntity = (JSONObject) response.getJsonResponse().get("entry");
                if (jsonEntity != null)
                {
                    return parseActionDefinition(jsonEntity);
                }                
            }
            
            return null;
        }
    }
}
