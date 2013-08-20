/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneIndexerAndSearcherFactory.LuceneIndexBackupComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationContext;

/**
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
        
        ChildApplicationContextFactory luceneSubSystem = (ChildApplicationContextFactory) ctx.getBean("lucene");
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
