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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;


/**
 * Workflow Service Implementation Tests
 * 
 * @author davidc
 */
public class WorkflowServiceImplTest extends BaseSpringTest
{
    WorkflowService workflowService;
    NodeService nodeService;

    //@Override
    protected void onSetUpInTransaction() throws Exception
    {
        workflowService = (WorkflowService)applicationContext.getBean(ServiceRegistry.WORKFLOW_SERVICE.getLocalName());
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
                
        // authenticate
        AuthenticationComponent auth = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        auth.setSystemUserAsCurrentUser();
    }

    public void testGetWorkflowDefinitions()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
    }
    
    public void testStartWorkflow()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        WorkflowDefinition workflowDef = workflowDefs.get(0);
        WorkflowPath path = workflowService.startWorkflow(workflowDef.id, null);
        assertNotNull(path);
        assertTrue(path.active);
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
    }
    
    public void testWorkflowPackage()
    {
        NodeRef nodeRef = workflowService.createPackage(null);
        assertNotNull(nodeRef);
        assertTrue(nodeService.hasAspect(nodeRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
    }
    
    public void testAssociateWorkflowPackage()
    {
        // create workflow package
        NodeRef rootRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "spacesStore"));
        NodeRef nodeRef = workflowService.createPackage(null);
        assertNotNull(nodeRef);
        assertTrue(nodeService.hasAspect(nodeRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
        ChildAssociationRef childAssoc = nodeService.createNode(rootRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"), ContentModel.TYPE_CONTENT, null);
        nodeService.addChild(nodeRef, childAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"));
        
        // start workflow
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        WorkflowDefinition workflowDef = workflowDefs.get(0);
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(WorkflowModel.ASSOC_PACKAGE, nodeRef);
        WorkflowPath path = workflowService.startWorkflow(workflowDef.id, parameters);
        assertNotNull(path);
        assertTrue(path.active);
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
        String workflowDefId = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_ID);
        assertEquals(workflowDefId, workflowDef.id);
        String workflowDefName = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME);
        assertEquals(workflowDefName, workflowDef.name);
        String workflowInstanceId = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
        assertEquals(workflowInstanceId, path.instance.id);

        // get workflows for content
        List<WorkflowInstance> instances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), true);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(instances.get(0).id, path.instance.id);
        List<WorkflowInstance> completedInstances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), false);
        assertNotNull(completedInstances);
        assertEquals(0, completedInstances.size());
    }
    
}
