package org.alfresco.repo.web.scripts.comment;

import java.text.MessageFormat;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
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
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class CommentsApiTest extends BaseWebScriptTest
{
    private static final String URL_POST_COMMENT = "api/node/{0}/{1}/{2}/comments";
    
    private static final String JSON = "application/json";
    
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
    
    private static final String USER_TEST = "UserTest";
    
    private UserTransaction txn;

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

        createUser(USER_TEST);
        
        txn.commit();

        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // delete the discussions users
        if(personService.personExists(USER_TEST))
        {
           personService.deletePerson(USER_TEST);
        }
        if (authenticationService.authenticationExists(USER_TEST))
        {
           authenticationService.deleteAuthentication(USER_TEST);
        }
    }
    
    private void addComment(NodeRef nodeRef, String user, int status) throws Exception
    {
        Response response = null;

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(user);

        StringBuilder body = new StringBuilder("{");
        body.append("'title' : 'Test Title', ");
        body.append("'content' : 'Test Comment'");
        body.append("}");

        response = sendRequest(new PostRequest(MessageFormat.format(URL_POST_COMMENT, new Object[] {nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId()}), body.toString(), JSON), status);
        assertEquals(status, response.getStatus());

        txn.commit();
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
        //Consumer shouldn't be able to add comments see MNT-9883
        addComment(contentForUserConsumer, USER_TEST, 500);
        
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
}
