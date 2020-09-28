/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.comment;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * TODO: Fix the loose transaction handling.
 * TODO: Rationalise with other v0 Comment REST API tests (eg. see BlogServiceTest ... etc). See also ACE-5437.
 */
public class CommentsApiTest extends BaseWebScriptTest
{
    // V0 Comments REST API
    private static final String URL_POST_COMMENT = "api/node/{0}/{1}/{2}/comments";
    private static final String URL_DELETE_COMMENT = "api/comment/node/{0}/{1}/{2}?site={3}&itemtitle={4}&page={5}&pageParams={6}";
    private static final String URL_PUT_COMMENT = "api/comment/node/{0}/{1}/{2}";
    
    private static final String JSON = "application/json";
    private static final String SITE_SHORT_NAME = "SomeTestSiteShortName-"+System.currentTimeMillis();
    
    private static final String USER_ONE = "SomeTestUserOne";
    private static final String USER_TWO = "SomeTestUserTwo";
    private static final String USER_THREE = "SomeTestUserThree";
    private static final String USER_FOUR = "SomeTestUserFour";
    
    private static final String JSON_KEY_NODEREF = "nodeRef";
    private static final String JSON_KEY_ITEM = "item";
    
    private String requestBodyJson = "{\"title\" : \"Test Title\", \"content\" : \"Test Comment\"}";
    
    private FileFolderService fileFolderService;
    private TransactionService transactionService;
    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private VersionService versionService;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    protected PermissionServiceSPI permissionService;
    protected ModelDAO permissionModelDAO;
    private ActivityService activityService;
    private FeedGenerator feedGenerator;
    private PostLookup postLookup;

    private NodeRef rootNodeRef;
    private NodeRef companyHomeNodeRef;
    private NodeRef sharedHomeNodeRef;
    private NodeRef nodeRef;
    private NodeRef sitePage;

    private static final String USER_TEST = "UserTest";

    private static final String DOCLIB_CONTAINER = "documentLibrary";
    
    private UserTransaction txn;

    private String USER2 = "user2";
    private SiteService siteService;
    private NodeArchiveService nodeArchiveService; 

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        fileFolderService = (FileFolderService)appContext.getBean("fileFolderService");
        transactionService = (TransactionService)appContext.getBean("transactionService");
        searchService = (SearchService)appContext.getBean("SearchService");
        nodeService = (NodeService)appContext.getBean("nodeService");
        namespaceService = (NamespaceService)appContext.getBean("namespaceService");
        versionService = (VersionService)appContext.getBean("versionService");
        personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        permissionService =  (PermissionServiceSPI) getServer().getApplicationContext().getBean("permissionService");
        permissionModelDAO = (ModelDAO) getServer().getApplicationContext().getBean("permissionsModelDAO");
        siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        nodeArchiveService = (NodeArchiveService)getServer().getApplicationContext().getBean("nodeArchiveService");
        activityService = (ActivityService)getServer().getApplicationContext().getBean("activityService");
        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory)getServer().getApplicationContext().getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        feedGenerator = (FeedGenerator)activitiesFeedCtx.getBean("feedGenerator");
        postLookup = (PostLookup)activitiesFeedCtx.getBean("postLookup");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        txn = transactionService.getUserTransaction();
        txn.begin();

        // Get Company Home
        rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(rootNodeRef, "/app:company_home", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home");
        }
        companyHomeNodeRef = results.get(0);
        
        // Get Shared
        results = searchService.selectNodes(rootNodeRef, "/app:company_home/app:shared", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home/app:shared");
        }

        sharedHomeNodeRef = results.get(0);
        
        results = searchService.selectNodes(rootNodeRef, "/app:company_home/cm:Commenty", null, namespaceService, false);
        if (results.size() > 0)
        {
        	fileFolderService.delete(results.get(0));
        }

        nodeRef = fileFolderService.create(companyHomeNodeRef, "Commenty", ContentModel.TYPE_CONTENT).getNodeRef();
        versionService.ensureVersioningEnabled(nodeRef, null);
        nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS, true);
        
        createUser(USER2);
        createUser(USER_TEST);
        
        txn.commit();

        AuthenticationUtil.clearCurrentSecurityContext();
        
        // MNT-12082
        // Authenticate as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        // Create test site
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = siteService.getSite(SITE_SHORT_NAME);
        if (siteInfo == null)
        {
            siteInfo = siteService.createSite("SomeTestSite", SITE_SHORT_NAME, "SiteTitle", "SiteDescription", SiteVisibility.PUBLIC);
        }

        NodeRef docLibContainer = siteService.getContainer(SITE_SHORT_NAME, DOCLIB_CONTAINER);
        if (docLibContainer == null)
        {
            siteService.createContainer(SITE_SHORT_NAME, DOCLIB_CONTAINER, ContentModel.TYPE_FOLDER, null);
        }

        txn = transactionService.getUserTransaction();
        txn.begin();

        // Create users
        
        createUser(USER_ONE, SiteModel.SITE_CONSUMER);
        createUser(USER_TWO, SiteModel.SITE_CONTRIBUTOR);

        createUser(USER_THREE, SiteModel.SITE_COLLABORATOR);
        createUser(USER_FOUR, SiteModel.SITE_COLLABORATOR);

        // Create site page
        sitePage = nodeService.createNode(siteInfo.getNodeRef(),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        txn.commit();
        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // delete the discussions users
        if(personService.personExists(USER_TEST))
        {
            // delete invite site
           personService.deletePerson(USER_TEST);

        // delete the users
        }
        if (authenticationService.authenticationExists(USER_TEST))
        {
           authenticationService.deleteAuthentication(USER_TEST);
        }
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME);
        if (siteInfo != null)
        {
            // delete invite site
            siteService.deleteSite(SITE_SHORT_NAME);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }

        // delete the users
        deleteUser(USER_ONE);
        deleteUser(USER_TWO);
        deleteUser(USER_THREE);
        deleteUser(USER_FOUR);
    }

    /**
     * add a comment to given node ref
     *
     * @param nodeRef
     * @param user
     * @param status
     * @return
     * @throws Exception
     */
    private Response addComment(NodeRef nodeRef, String user, int status) throws Exception
    {
        Response response = null;

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(user);

        // add comment and save response (comment nodeRef)

        StringBuilder body = new StringBuilder("{");
        body.append("\"itemTitle\" : \"Test Title\", ");
        body.append("\"content\" : \"Test Comment\", ");
        body.append("\"pageParams\" : \"{\\\"nodeRef\\\" : \\\"");
        body.append(nodeRef.getStoreRef().getProtocol());
        body.append(":\\/\\/");
        body.append(nodeRef.getStoreRef().getIdentifier());
        body.append("\\/");
        body.append(nodeRef.getId());
        body.append("\\\"}");
        if (nodeRef.equals(sitePage))
        {
            body.append("\",\"site\" : \"");
            body.append(SITE_SHORT_NAME);
        }
        body.append("\"}");
        response = sendRequest(
                new PostRequest(MessageFormat.format(URL_POST_COMMENT, new Object[] { nodeRef.getStoreRef().getProtocol(),
                        nodeRef.getStoreRef().getIdentifier(), nodeRef.getId() }), body.toString(), JSON), status);

        assertEquals(status, response.getStatus());

        // Normally, webscripts are in their own transaction.  The test infrastructure here forces us to have a transaction
        // around the calls.  if the WebScript fails, then we should rollback.
        if (response.getStatus() == 500)
        {
            txn.rollback();
        }
        else
        {
            txn.commit();
        }

        return response;
    }


    /**
     * delete comment
     *
     * @param commentNodeRef
     * @param parentNodeRef
     * @param user
     * @param status
     * @throws Exception
     */
    private void deleteComment(NodeRef commentNodeRef, NodeRef parentNodeRef, String user, int status) throws Exception
    {
        Response response = null;

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(user);

        String itemTitle = "Test Title";
        String page = "document-details";

        StringBuilder pageParamsBuilder = new StringBuilder("{");
        pageParamsBuilder.append("\"nodeRef\" : \"");
        pageParamsBuilder.append(parentNodeRef.toString());
        pageParamsBuilder.append("\", ");
        pageParamsBuilder.append("}");
        String pageParams = pageParamsBuilder.toString();

        String URL = MessageFormat.format(URL_DELETE_COMMENT, new Object[] { commentNodeRef.getStoreRef().getProtocol(),
                commentNodeRef.getStoreRef().getIdentifier(), commentNodeRef.getId(), SITE_SHORT_NAME, itemTitle, page, pageParams });
        response = sendRequest(new DeleteRequest(URL), status);
        assertEquals(status, response.getStatus());

        // Normally, webscripts are in their own transaction. The test
        // infrastructure here forces us to have a transaction
        // around the calls. if the WebScript fails, then we should rollback.
        if (response.getStatus() == 500)
        {
            txn.rollback();
        }
        else
        {
            txn.commit();
        }
    }
    
    /**
     * 
     * @param nodeRef
     * @param user
     * @param expectedStatus
     * @return
     * @throws Exception
     */
    private Response updateComment(NodeRef nodeRef, String user, int expectedStatus) throws Exception
    {
        Response response = null;
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        AuthenticationUtil.setFullyAuthenticatedUser(user);

        String now = System.currentTimeMillis()+"";

        JSONObject comment = new JSONObject();
        comment.put("title", "Test title updated "+now);
        comment.put("content", "Test comment updated "+now);

        response = sendRequest(new PutRequest(MessageFormat.format(URL_PUT_COMMENT,
                new Object[] {nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId()}), comment.toString(), JSON), expectedStatus);

        assertEquals(expectedStatus, response.getStatus());

        // Normally, webscripts are in their own transaction.  The test infrastructure here forces us to have a transaction
        // around the calls.  if the WebScript fails, then we should rollback.
        if (response.getStatus() == 500)
        {
            txn.rollback();
        }
        else
        {
            txn.commit();
        }
        
        return response;
    }
    
    private String getCurrentVersion(NodeRef nodeRef) throws Exception
    {
    	String version = versionService.getCurrentVersion(nodeRef).getVersionLabel();
        return version;
    }
    
    public void testCommentDoesNotVersion() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        String versionBefore = getCurrentVersion(nodeRef);
        addComment(nodeRef, AuthenticationUtil.getAdminUserName(), 200);
        String versionAfter = getCurrentVersion(nodeRef);
        assertEquals(versionBefore, versionAfter);
    }
    
    /**
     * MNT-9771
     * @throws Exception
     */
    public void testCommentPermissions() throws Exception
    {
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        NodeRef contentForUserContributor = fileFolderService.create(companyHomeNodeRef, "CommentyContributor" + System.currentTimeMillis(), ContentModel.TYPE_CONTENT).getNodeRef();
        permissionService.setPermission(new SimplePermissionEntry(contentForUserContributor, getPermission(PermissionService.CONTRIBUTOR), USER_TEST, AccessStatus.ALLOWED));
        
        NodeRef contentForUserConsumer = fileFolderService.create(companyHomeNodeRef, "CommentyConsumer" + System.currentTimeMillis(), ContentModel.TYPE_CONTENT).getNodeRef();
        permissionService.setPermission(new SimplePermissionEntry(contentForUserConsumer, getPermission(PermissionService.CONSUMER), USER_TEST, AccessStatus.ALLOWED));

        //Contributor should be able to add comments
        addComment(contentForUserContributor, USER_TEST, 200);
        
        txn.commit();       // Hack.  Internally, the addComment starts and rolls back the next txn.
        //Consumer shouldn't be able to add comments see MNT-9883
        addComment(contentForUserConsumer, USER_TEST, 500);
        
        txn = transactionService.getUserTransaction();
        txn.begin();
        nodeService.deleteNode(contentForUserContributor);
        nodeService.deleteNode(contentForUserConsumer);
        
        txn.commit();
    }

    /**
     * MNT-16446
     * @throws Exception
     */
    public void testCommentUpdateAndDeletePermission() throws Exception
    {
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        NodeRef sharedContent = null;
        NodeRef siteContent = null;

        {
            //
            // in Shared folder
            //
            UserTransaction txn = transactionService.getUserTransaction();
            txn.begin();
            sharedContent = fileFolderService.create(sharedHomeNodeRef, "SharedContent" + System.currentTimeMillis(), ContentModel.TYPE_CONTENT).getNodeRef();
            txn.commit();

            Response response = addComment(sharedContent, USER_THREE, 200);
            JSONObject jsonResponse = parseResponseJSON(response);
            NodeRef commentNodeRef = new NodeRef(getOrNull(jsonResponse, JSON_KEY_NODEREF));

            // MNT-16446 - now returns 403 rather than 500
            // -ve test:
            updateComment(commentNodeRef, USER_FOUR, 403);

            updateComment(commentNodeRef, USER_THREE, 200);

            // -ve test: ideally would return 403, but currently v0 REST API returns 500 :-(
            deleteComment(commentNodeRef, sharedContent, USER_FOUR, 500);

            deleteComment(commentNodeRef, sharedContent, USER_THREE, 200);
        }

        {
            //
            // in a public Site
            //
            txn = transactionService.getUserTransaction();
            txn.begin();
            NodeRef siteDocLibNodeRef = siteService.getContainer(SITE_SHORT_NAME, DOCLIB_CONTAINER);
            siteContent = fileFolderService.create(siteDocLibNodeRef, "SiteContent" + System.currentTimeMillis(), ContentModel.TYPE_CONTENT).getNodeRef();
            txn.commit();

            Response response = addComment(siteContent, USER_THREE, 200);
            JSONObject jsonResponse = parseResponseJSON(response);
            NodeRef commentNodeRef = new NodeRef(getOrNull(jsonResponse, JSON_KEY_NODEREF));

            // MNT-16446 - now returns 403 rather than 200 !!
            // -ve test:
            updateComment(commentNodeRef, USER_FOUR, 403);

            updateComment(commentNodeRef, USER_THREE, 200);

            // -ve test: ideally would return 403, but currently v0 REST API returns 500 :-(
            deleteComment(commentNodeRef, siteContent, USER_FOUR, 500);

            deleteComment(commentNodeRef, siteContent, USER_THREE, 200);
        }

        {
            // cleanup
            authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
            txn = transactionService.getUserTransaction();
            txn.begin();

            if (sharedContent != null)
            {
                nodeService.deleteNode(sharedContent);
            }

            if (siteContent != null)
            {
                nodeService.deleteNode(siteContent);
            }
            txn.commit();
        }
    }

    private void createUser(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (!authenticationService.authenticationExists(userName))
        {
            // create user
            authenticationService.createAuthentication(userName, "password".toCharArray());
        }
         
        if (!personService.personExists(userName))
        {
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstNameTest");
            personProps.put(ContentModel.PROP_LASTNAME, "LastNameTest");
            personProps.put(ContentModel.PROP_EMAIL, "FirstNameTest.LastNameTest@test.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitleTest");
            
            // create person node for user
            personService.createPerson(personProps);
        }
    }
    
    private PermissionReference getPermission(String permission)
    {
        return permissionModelDAO.getPermissionReference(null, permission);
    }
    
    /**
     * MNT-9771
     */
    public void testCommentDoesNotChangeModifier() throws Exception
    {
        permissionService.setPermission(nodeRef, USER2, PermissionService.ALL_PERMISSIONS, true);
        String modifierBefore = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        addComment(nodeRef, USER2, 200);
        String modifierAfter = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals(modifierBefore, modifierAfter);
    }
    
    /**
     * MNT-15679
     */
    public void testDeleteCommentDoesNotChangeModifiedDate() throws Exception
    {
        // in site page
        permissionService.setPermission(sitePage, USER_TWO, PermissionService.ALL_PERMISSIONS, true);
        String modifierBefore = (String) nodeService.getProperty(sitePage, ContentModel.PROP_MODIFIER);
        Date modifiedDateBefore = (Date) nodeService.getProperty(sitePage, ContentModel.PROP_MODIFIED);

        Response response1 = addComment(sitePage, USER_TWO, 200);

        JSONObject jsonResponse1 = parseResponseJSON(response1);
        String nodeRefComment1 = getOrNull(jsonResponse1, JSON_KEY_NODEREF);
        if (nodeRefComment1 != null)
        {
            NodeRef commentNodeRef1 = new NodeRef(nodeRefComment1);
            deleteComment(commentNodeRef1, sitePage, USER_TWO, 200);
        }

        Date modifiedDateAfter = (Date) nodeService.getProperty(sitePage, ContentModel.PROP_MODIFIED);
        String modifierAfter = (String) nodeService.getProperty(sitePage, ContentModel.PROP_MODIFIER);
        assertEquals(modifiedDateBefore.getTime(), modifiedDateAfter.getTime());
        assertEquals(modifierBefore, modifierAfter);

        // in repository - on nodeRef
        permissionService.setPermission(nodeRef, USER2, PermissionService.ALL_PERMISSIONS, true);
        modifierBefore = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        modifiedDateBefore = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        Response response2 = addComment(nodeRef, USER2, 200);

        JSONObject jsonResponse2 = parseResponseJSON(response2);
        String nodeRefComment2 = getOrNull(jsonResponse2, JSON_KEY_NODEREF);
        if (nodeRefComment2 != null)
        {
            NodeRef commentNodeRef2 = new NodeRef(nodeRefComment2);
            deleteComment(commentNodeRef2, nodeRef, USER2, 200);
        }

        modifiedDateAfter = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        modifierAfter = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals(modifiedDateBefore.getTime(), modifiedDateAfter.getTime());
        assertEquals(modifierBefore, modifierAfter);
    }

    /**
     * REPO-828 (MNT-16401)
     * @throws Exception
     */
    public void testDeleteCommentPostActivity() throws Exception
    {
        permissionService.setPermission(sitePage, USER_TWO, PermissionService.ALL_PERMISSIONS, true);
        postLookup.execute();
        feedGenerator.execute();
        int activityNumStart = activityService.getSiteFeedEntries(SITE_SHORT_NAME).size();
        Response response = addComment(sitePage, USER_TWO, 200);
        postLookup.execute();
        feedGenerator.execute();
        int activityNumNext = activityService.getSiteFeedEntries(SITE_SHORT_NAME).size();
        assertEquals("The activity feeds were not generated after adding a comment", activityNumStart + 1, activityNumNext);
        JSONObject jsonResponse = parseResponseJSON(response);
        String nodeRefComment = getOrNull(jsonResponse, JSON_KEY_NODEREF);
        NodeRef commentNodeRef = new NodeRef(nodeRefComment);
        deleteComment(commentNodeRef, sitePage, USER_TWO, 200);
        activityNumStart = activityNumNext;
        postLookup.execute();
        feedGenerator.execute();
        activityNumNext = activityService.getSiteFeedEntries(SITE_SHORT_NAME).size();
        assertEquals("The activity feeds were not generated after deleting a comment", activityNumStart + 1, activityNumNext);
    }

    /**
     * MNT-12082
     */
    public void testConsumerCanNotComment() throws Exception
    {
        // Authenticate as consumer
        authenticationService.authenticate(USER_ONE, USER_ONE.toCharArray());

        String uri = MessageFormat.format(URL_POST_COMMENT, new Object[] {sitePage.getStoreRef().getProtocol(), sitePage.getStoreRef().getIdentifier(), sitePage.getId()});
        Response response = sendRequest(new PostRequest(uri, requestBodyJson, JSON),
                Status.STATUS_INTERNAL_SERVER_ERROR);
        assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, response.getStatus());
    }
    
    /**
     * MNT-12082
     */
    public void testContributorCanComment() throws Exception
    {
        // Authenticate as contributor
        authenticationService.authenticate(USER_TWO, USER_TWO.toCharArray());

        String uri = MessageFormat.format(URL_POST_COMMENT, new Object[] {sitePage.getStoreRef().getProtocol(), sitePage.getStoreRef().getIdentifier(), sitePage.getId()});
        Response response = sendRequest(new PostRequest(uri, requestBodyJson, JSON), Status.STATUS_OK);
        assertEquals(Status.STATUS_OK, response.getStatus());        
    }
    
    private void createUser(String userName, String role)
    {
        // if user with given user name doesn't already exist then create user
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, userName.toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstName123");
            personProps.put(ContentModel.PROP_LASTNAME, "LastName123");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            personService.createPerson(personProps);
        }
        
        siteService.setMembership(SITE_SHORT_NAME, userName, role);
    }
    
    private void deleteUser(String user)
    {
        personService.deletePerson(user);
        if (authenticationService.authenticationExists(user))
        {
           authenticationService.deleteAuthentication(user);
        }
    }
    
    /**
     * returns value from JSON for a given key
     * @param json
     * @param key
     * @return
     */
    protected String getOrNull(JSONObject json, String key)
    {
        if (json != null && json.containsKey(key))
        {
            return (String) json.get(key);
        }
        
        JSONObject itemJsonObject = (JSONObject) json.get(JSON_KEY_ITEM);
        if (itemJsonObject != null && itemJsonObject.containsKey(key))
        {
            return (String) itemJsonObject.get(key);
        }
        return null;
    }
    
    /**
     * parse JSON
     * @param response
     * @return
     */
    protected JSONObject parseResponseJSON(Response response)
    {
        JSONObject json = null;
        String contentType = response.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1)
        {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        if (MimetypeMap.MIMETYPE_JSON.equals(contentType))
        {
            JSONParser parser = new JSONParser();
            try
            {
                json = (JSONObject) parser.parse(response.getContentAsString());
            }
            catch (IOException io)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            }
            catch (ParseException pe)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
            }
        }
        return json;
    } 
}
