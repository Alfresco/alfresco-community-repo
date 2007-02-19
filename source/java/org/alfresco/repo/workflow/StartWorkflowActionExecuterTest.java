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
package org.alfresco.repo.workflow;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Start Advanced Workflow action execution test
 * 
 * @author David Caruana
 */
public class StartWorkflowActionExecuterTest extends BaseSpringTest
{
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private PersonService personService;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private StartWorkflowActionExecuter executer;

    
    /**
     * Called at the begining of all tests
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("namespaceService");
        this.personService = (PersonService)this.applicationContext.getBean("personService");
        
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        
        // Create the store and get the root node
        rootNodeRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "spacesStore"));
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        // Get the executer instance 
        this.executer = (StartWorkflowActionExecuter)this.applicationContext.getBean(StartWorkflowActionExecuter.NAME);
    }
    
    /**
     * Test execution
     */
    public void testExecution()
    {
        // Execute the action
        ActionImpl action = new ActionImpl(null, GUID.generate(), StartWorkflowActionExecuter.NAME, null);
        action.setParameterValue(StartWorkflowActionExecuter.PARAM_WORKFLOW_NAME, "jbpm$wf:review");
        action.setParameterValue(WorkflowModel.PROP_WORKFLOW_DUE_DATE.toPrefixString(namespaceService), new Date());
        NodeRef reviewer = personService.getPerson("admin");
        action.setParameterValue(WorkflowModel.ASSOC_ASSIGNEE.toPrefixString(namespaceService), reviewer);
        executer.execute(action, this.nodeRef);
    }
    
}
