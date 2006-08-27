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

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
    
}
