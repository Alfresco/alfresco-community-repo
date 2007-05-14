/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.avm;

import junit.framework.TestCase;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.avm.AVMEXpiredContentProcessor
 * 
 * @author gavinc
 */
public class AVMExpiredContentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AVMExpiredContentProcessor processor;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        AVMService avmService = serviceRegistry.getAVMService();
        AVMSyncService avmSyncService = serviceRegistry.getAVMSyncService();
        NodeService nodeService = serviceRegistry.getNodeService();
        PermissionService permissionService = serviceRegistry.getPermissionService();
        PersonService personService = serviceRegistry.getPersonService();
        TransactionService transactionService = serviceRegistry.getTransactionService();
        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        
        // construct the test processor
        this.processor = new AVMExpiredContentProcessor();
        this.processor.setAvmService(avmService);
        this.processor.setAvmSyncService(avmSyncService);
        this.processor.setNodeService(nodeService);
        this.processor.setPermissionService(permissionService);
        this.processor.setPersonService(personService);
        this.processor.setTransactionService(transactionService);
        this.processor.setWorkflowService(workflowService);
    }
    
    public void testProcessor() throws Exception
    {
        this.processor.execute();
    }
}
