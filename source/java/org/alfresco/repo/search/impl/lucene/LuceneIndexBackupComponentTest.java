/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.File;

import junit.framework.TestCase;

import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcherFactory2.LuceneIndexBackupComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcherFactory.LuceneIndexBackupComponent
 * 
 * @author Derek Hulley
 */
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
        LuceneIndexerAndSearcher factory = (LuceneIndexerAndSearcher) ctx.getBean("luceneIndexerAndSearcherFactory");
        
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        tempTargetDir = new File(TempFileProvider.getTempDir(), getName());
        tempTargetDir.mkdir();
        
        backupComponent = new LuceneIndexBackupComponent();
        backupComponent.setTransactionService(transactionService);
        backupComponent.setFactory(factory);
        backupComponent.setNodeService(nodeService);
        backupComponent.setTargetLocation(tempTargetDir.toString());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    public void testBackup()
    {
        backupComponent.backup();
        
        // make sure that the target directory was created
        assertTrue("Target location doesn't exist", tempTargetDir.exists());
    }
}
