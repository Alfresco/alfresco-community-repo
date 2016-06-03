package org.alfresco.repo.search.impl.lucene;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneIndexerAndSearcherFactory.LuceneIndexBackupComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class LuceneIndexBackupComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private LuceneIndexBackupComponent backupComponent;
    private File tempTargetDir;
    
    private AuthenticationComponent authenticationComponent;
    
    @Override
    public void setUp() throws Exception
    {
        TransactionService transactionService = (TransactionService) ctx.getBean("transactionComponent");
        NodeService nodeService = (NodeService) ctx.getBean("NodeService");
        
        ChildApplicationContextFactory luceneSubSystem = (ChildApplicationContextFactory) ctx.getBean("buildonly");
        LuceneIndexerAndSearcher factory = (LuceneIndexerAndSearcher) luceneSubSystem.getApplicationContext().getBean("search.admLuceneIndexerAndSearcherFactory");
        
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        tempTargetDir = new File(TempFileProvider.getTempDir(), getName());
        tempTargetDir.mkdir();
        
        backupComponent = new LuceneIndexBackupComponent();
        backupComponent.setTransactionService(transactionService);
        backupComponent.setFactories(Collections.singleton(factory));
        backupComponent.setNodeService(nodeService);
        backupComponent.setTargetLocation(tempTargetDir.toString());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    /**
     * Test back up
     */
    public void testBackup()
    {
        backupComponent.backup();
        
        // make sure that the target directory was created
        assertTrue("Target location doesn't exist", tempTargetDir.exists());
    }
}
