/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.wcm.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.LayeringDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WCM Specific workflow related helper methods.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 * @author janv
 */
public class WCMWorkflowUtil
{
    private static final Log logger = LogFactory.getLog(WCMWorkflowUtil.class);

    public static NodeRef createWorkflowPackage(WorkflowService workflowService, AVMService avmService, SandboxInfo sandboxInfo)
    {
        // create package paths (layered to user sandbox area as target)
        final String workflowMainStoreName = sandboxInfo.getMainStoreName();
        final String packagesPath = WCMUtil.buildStoreRootPath(workflowMainStoreName);
        
        // convert package to workflow package
        final AVMNodeDescriptor packageDesc = avmService.lookup(-1, packagesPath);
        final NodeRef packageNodeRef = workflowService.createPackage(AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
        
        avmService.setNodeProperty(packagesPath, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, new PropertyValue(DataTypeDefinition.BOOLEAN, true));
        
        // NOTE: WCM-1019: As permissions are now implemented for AVM nodes we no longer need to set permisssions here
        //                 as they will be inherited from the store the workflow store is layered over.
        
        //final ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
        //final PermissionService permissionService = services.getPermissionService();
        //permissionService.setPermission(packageNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
        
        return packageNodeRef;
    }
    
    public static List<WorkflowTask> getAssociatedTasksForSandbox(WorkflowService workflowService, final String storeName)
    {
        String fromPath = WCMUtil.buildStoreRootPath(storeName);
        WorkflowTaskQuery query = new WorkflowTaskQuery();
      
        HashMap<QName, Object> props = new HashMap<QName, Object>(1, 1.0f);
      
        props.put(WCMWorkflowModel.PROP_FROM_PATH, fromPath);
        query.setProcessCustomProps(props);
        query.setActive(true);
      
        List<WorkflowTask> tasks = workflowService.queryTasks(query);
      
        if (logger.isDebugEnabled())
        {
            logger.debug("found " + tasks.size() + " tasks originating user sandbox " + fromPath);
        }
      
       return tasks;
    }
   
    public static List<WorkflowTask> getAssociatedTasksForNode(AVMService avmService, AVMNodeDescriptor node, List<WorkflowTask> tasks)
    {
        List<WorkflowTask> result = new LinkedList<WorkflowTask>();
      
        for (WorkflowTask task : tasks)
        {
            final NodeRef ref = task.path.instance.workflowPackage;
            final String path = WCMUtil.getCorrespondingPath(node.getPath(), ref.getStoreRef().getIdentifier());
            
            if (logger.isDebugEnabled())
            {
                logger.debug("checking store " + ref.getStoreRef().getIdentifier() +
                             " for " + node.getPath() + " (" + path + ")");
            }
            
            try
            {
                final LayeringDescriptor ld = avmService.getLayeringInfo(-1, path);
                if (!ld.isBackground())
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(path + " is in the foreground.  workflow active");
                    }
                    
                    result.add(task);
                }
            }
            catch (final AVMNotFoundException avmnfe)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(path + " not found");
                }
            }
        }
      
        return result;
    }
   
    public static List<WorkflowTask> getAssociatedTasksForNode(WorkflowService workflowService, AVMService avmService, AVMNodeDescriptor node)
    {
        final List<WorkflowTask> tasks = WCMWorkflowUtil.getAssociatedTasksForSandbox(workflowService, WCMUtil.getSandboxStoreId(node.getPath()));
        return getAssociatedTasksForNode(avmService, node, tasks);
    }
}
