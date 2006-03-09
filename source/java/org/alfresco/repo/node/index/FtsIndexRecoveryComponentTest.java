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
package org.alfresco.repo.node.index;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Checks that the FTS index recovery component is working
 * 
 * @author Derek Hulley
 */
public class FtsIndexRecoveryComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private IndexRecovery indexRecoverer;
    private TransactionService txnService;
    
    public void setUp() throws Exception
    {
        indexRecoverer = (IndexRecovery) ctx.getBean("indexRecoveryComponent");
        txnService = (TransactionService) ctx.getBean("transactionComponent");
    }
    
    public void testReindexing() throws Exception
    {
        // performs a reindex
        TransactionWork<Object> reindexWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                indexRecoverer.reindex();
                return null;
            }
        };
        
        // reindex
        TransactionUtil.executeInNonPropagatingUserTransaction(txnService, reindexWork);
    }
}
