package org.alfresco.repo.model.ml.tools;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Sergey Scherbovich
 */
public class LanguagesTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    
    private ServiceRegistry serviceRegistry;
    
    private UserTransaction txn;
    private NodeRef rootNodeRef;
    private NodeRef workingRootNodeRef;

    @Override
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        
        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        // downgrade integrity
        IntegrityChecker.setWarnInTransaction();

        // authenticate
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // create a test store
        StoreRef storeRef = nodeService
                .createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        // create a folder to import into
        workingRootNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "working root"),
                ContentModel.TYPE_FOLDER).getChildRef();
    }

    public void tearDown() throws Exception
    {
        try
        {
            if (txn.getStatus() != Status.STATUS_ROLLEDBACK && txn.getStatus() != Status.STATUS_COMMITTED)
            {
                txn.rollback();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    /* MNT-10388 test */
    public void testCreateNodeInUTF8() throws Exception
    {
        String Czech1 = "Vytvořit složku " + System.currentTimeMillis();
        String Czech2 = "Nová složka " + System.currentTimeMillis();
        String Russian = "На русском " + System.currentTimeMillis();
        String Japaneese = "日本語でのタイトル " + System.currentTimeMillis();
        
        testNodesCreating(new String[]{Czech1, Czech2});
        testNodesCreating(new String[]{Russian});
        testNodesCreating(new String[]{Japaneese});
    }
    
    private void testNodesCreating(String[] nodeNames)
    {
        for (String node : nodeNames)
        {
            fileFolderService.create(workingRootNodeRef, node, ContentModel.TYPE_CONTENT);
        }
    }
}
