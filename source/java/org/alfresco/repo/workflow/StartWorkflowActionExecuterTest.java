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
 * Add features action execution test
 * 
 * @author Roy Wetherall
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
        action.setParameterValue(WorkflowModel.PROP_REVIEW_DUE_DATE.toPrefixString(namespaceService), new Date());
        NodeRef reviewer = personService.getPerson("admin");
        action.setParameterValue(WorkflowModel.ASSOC_REVIEWER.toPrefixString(namespaceService), reviewer);
        executer.execute(action, this.nodeRef);
    }
}
