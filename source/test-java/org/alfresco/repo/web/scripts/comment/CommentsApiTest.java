/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.comment;

import java.text.MessageFormat;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
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
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * TODO: Fix the loose transaction handling.
 */
public class CommentsApiTest extends BaseWebScriptTest
{
    private static final String URL_POST_COMMENT = "api/node/{0}/{1}/{2}/comments";
    private static final String JSON = "application/json";
    private static final String SITE_SHORT_NAME = "SomeTestSiteShortName";
    private static final String USER_ONE = "SomeTestUserOne";
    private static final String USER_TWO = "SomeTestUserTwo";
    
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
    
    private NodeRef rootNodeRef;
    private NodeRef companyHomeNodeRef; 
    private NodeRef nodeRef;
    private NodeRef sitePage;
    
    private static final String USER_TEST = "UserTest";
    
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

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        txn = transactionService.getUserTransaction();
        txn.begin();

        rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(rootNodeRef, "/app:company_home", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home");
        }
        
        companyHomeNodeRef = results.get(0);
        
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

        txn = transactionService.getUserTransaction();
        txn.begin();

        // Create users
        createUser(USER_ONE, SiteModel.SITE_CONSUMER);
        createUser(USER_TWO, SiteModel.SITE_CONTRIBUTOR);

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
    }
    
    private void addComment(NodeRef nodeRef, String user, int status) throws Exception
    {
        Response response = null;

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(user);

        StringBuilder body = new StringBuilder("{");
        body.append("\"title\" : \"Test Title\", ");
        body.append("\"content\" : \"Test Comment\"");
        body.append("}");

        response = sendRequest(new PostRequest(MessageFormat.format(URL_POST_COMMENT, new Object[] {nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId()}), body.toString(), JSON), status);
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
}
