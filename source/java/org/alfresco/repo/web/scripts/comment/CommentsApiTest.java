package org.alfresco.repo.web.scripts.comment;

import java.text.MessageFormat;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
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
    
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    
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

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        txn = transactionService.getUserTransaction();
        txn.begin();

        rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(rootNodeRef, "/app:company_home", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home");
        }
        
        NodeRef companyHomeNodeRef = results.get(0);
        
        results = searchService.selectNodes(rootNodeRef, "/app:company_home/cm:Commenty", null, namespaceService, false);
        if (results.size() > 0)
        {
        	fileFolderService.delete(results.get(0));
        }

        nodeRef = fileFolderService.create(companyHomeNodeRef, "Commenty", ContentModel.TYPE_CONTENT).getNodeRef();
        versionService.ensureVersioningEnabled(nodeRef, null);
        nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS, true);

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
    }
    
    private void addComment(NodeRef nodeRef) throws Exception
    {
        Response response = null;

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        StringBuilder body = new StringBuilder("{");
        body.append("'title' : 'Test Title', ");
        body.append("'content' : 'Test Comment'");
        body.append("}");

        response = sendRequest(new PostRequest(MessageFormat.format(URL_POST_COMMENT, new Object[] {nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId()}), body.toString(), JSON), Status.STATUS_OK);
        assertEquals(Status.STATUS_OK, response.getStatus());

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
        addComment(nodeRef);
        String versionAfter = getCurrentVersion(nodeRef);
        assertEquals(versionBefore, versionAfter);
    }
}
