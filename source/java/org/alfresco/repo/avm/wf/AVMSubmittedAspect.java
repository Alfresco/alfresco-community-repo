/*
 * Copyright (C) 2006 Alfresco, Inc.
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
