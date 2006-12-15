/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.avm.wf;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;


/**
 * Aspect to represent a node is currently taking part in a workflow.
 * 
 * @author davidc
 */
public class AVMSubmittedAspect
{
    
    private final static String NAMESPACE_URI = "http://www.alfresco.org/model/wcmworkflow/1.0";
    
    public final static QName ASPECT = QName.createQName(NAMESPACE_URI, "submitted"); 
    public final static QName PROP_WORKFLOW_INSTANCE_ID = QName.createQName(NAMESPACE_URI, "workflowInstanceId");
    
    
    // Dependencies
    private AVMService avmService;


    /**
     * Sets the AVM Service
     * 
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    
    /**
     * Mark an item as submitted via a workflow
     * 
     * @param version
     * @param path
     * @param workflowInstanceId
     */
    public void markSubmitted(int version, String path, String workflowInstanceId)
    {
        String existingWorkflowInstanceId = getWorkflowInstance(version, path);
        if (existingWorkflowInstanceId != null)
        {
            throw new WorkflowException("Node " + path + "[" + version + "] already submitted in workflow " + existingWorkflowInstanceId);
        }

        ParameterCheck.mandatoryString("workflowInstanceId", workflowInstanceId);
        avmService.addAspect(path, ASPECT);
        avmService.setNodeProperty(path, PROP_WORKFLOW_INSTANCE_ID, new PropertyValue(DataTypeDefinition.TEXT, workflowInstanceId));
    }
    
    
    /**
     * Unmark an submitted item
     * 
     * @param version
     * @param path
     */
    public void clearSubmitted(int version, String path)
    {
        if (avmService.hasAspect(version, path, ASPECT))
        {
            avmService.removeAspect(path, ASPECT);
        }
    }

    
    /**
     * Gets the submitted workflow instances for the specified item
     * 
     * @param version
     * @param path
     * @return  workflow instance (or null, if not submitted)
     */
    public String getWorkflowInstance(int version, String path)
    {
        String workflowInstanceId = null;
        PropertyValue value = avmService.getNodeProperty(version, path, PROP_WORKFLOW_INSTANCE_ID);
        if (value != null)
        {
            workflowInstanceId = value.getStringValue();
        }
        return workflowInstanceId;
    }
    
}
