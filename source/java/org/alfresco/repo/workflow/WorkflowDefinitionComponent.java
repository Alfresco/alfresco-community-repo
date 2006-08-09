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

import java.io.InputStream;
import java.util.List;

import org.alfresco.service.cmr.workflow.WorkflowDefinition;


/**
 * SPI to be implemented by a BPM Engine that provides Workflow Definition management.
 * 
 * @author davidc
 */
public interface WorkflowDefinitionComponent
{

    /**
     * Deploy a Workflow Definition
     * 
     * @param workflowDefinition  the content object containing the definition
     * @param mimetype (optional)  the mime type of the workflow definition
     * @return workflow definition
     */
    public WorkflowDefinition deployDefinition(InputStream workflowDefinition, String mimetype);

    /**
     * Is the specified Workflow Definition already deployed?
     * 
     * Note: the notion of "already deployed" may differ between bpm engines. For example,
     *       different versions of the same process may be considered equal.
     *       
     * @param workflowDefinition  the definition to check
     * @param mimetype  the mimetype of the definition
     * @return  true => already deployed
     */
    public boolean isDefinitionDeployed(InputStream workflowDefinition, String mimetype);
    
    /**
     * Undeploy an exisiting Workflow Definition
     * 
     * TODO: Determine behaviour when "in-flight" workflow instances exist
     *  
     * @param workflowDefinitionId  the id of the definition to undeploy
     */
    public void undeployDefinition(String workflowDefinitionId);
    
    /**
     * Gets all deployed Workflow Definitions
     * 
     * @return  the deployed workflow definitions
     */
    public List<WorkflowDefinition> getDefinitions();
    
    /**
     * Gets a Workflow Definition by unique Id
     * 
     * @param workflowDefinitionId  the workflow definition id
     * @return  the deployed workflow definition
     */
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId);
    
}

