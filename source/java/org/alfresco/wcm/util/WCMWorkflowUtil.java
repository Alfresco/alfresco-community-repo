/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.wcm.util;

import java.util.ArrayList;
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
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
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
        long start = System.currentTimeMillis();
        
        String fromPath = WCMUtil.buildStoreRootPath(storeName);
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        
        HashMap<QName, Object> props = new HashMap<QName, Object>(1, 1.0f);
        
        props.put(WCMWorkflowModel.PROP_FROM_PATH, fromPath);
        query.setProcessCustomProps(props);
        query.setActive(true);
        
        List<WorkflowTask> tasks = workflowService.queryTasks(query);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("getAssociatedTasksForSandbox: "+storeName+" (found "+tasks.size()+" tasks originating user sandbox "+fromPath+") in "+(System.currentTimeMillis()-start)+" msecs");
        }
      
       return tasks;
    }
    
    /**
     * @deprecated since 3.2
     */
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
    
    /**
     * @deprecated since 3.2
     */
    public static List<WorkflowTask> getAssociatedTasksForNode(WorkflowService workflowService, AVMService avmService, AVMNodeDescriptor node)
    {
        final List<WorkflowTask> tasks = getAssociatedTasksForSandbox(workflowService, WCMUtil.getSandboxStoreId(node.getPath()));
        return getAssociatedTasksForNode(avmService, node, tasks);
    }
    
    public static List<String> getAssociatedPathsForSandbox(AVMSyncService avmSyncService, WorkflowService workflowService, String sandboxName)
    {
        long start = System.currentTimeMillis();
        
        List<WorkflowTask> tasks = getAssociatedTasksForSandbox(workflowService, sandboxName);
        List<String> storeRelativePaths = getAssociatedPathsForSandboxTasks(avmSyncService, sandboxName, tasks);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("getAssociatedPathsForSandbox: "+sandboxName+" (tasks="+tasks.size()+", paths="+storeRelativePaths.size()+") in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return storeRelativePaths;
    }
    
    private static List<String> getAssociatedPathsForSandboxTasks(AVMSyncService avmSyncService, String sandboxName, List<WorkflowTask> tasks)
    {
        long start = System.currentTimeMillis();
        
        String stagingSandboxName = WCMUtil.buildStagingStoreName(WCMUtil.getWebProjectStoreId(sandboxName));
        List<String> storeRelativePaths = new ArrayList<String>(tasks.size());
        
        for (WorkflowTask task : tasks)
        {
            final NodeRef ref = task.path.instance.workflowPackage;
            
            String wfPath = AVMNodeConverter.ToAVMVersionPath(ref).getSecond();
            String stagingSandboxPath = WCMUtil.getCorrespondingPath(wfPath, stagingSandboxName);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, wfPath, -1, stagingSandboxPath, null, true);
            
            for (AVMDifference diff : diffs)
            {
                storeRelativePaths.add(WCMUtil.getStoreRelativePath(diff.getSourcePath()));
            }
        }
        
        if (logger.isTraceEnabled())
        {
            logger.trace("getAssociatedPathsForSandboxTasks: "+sandboxName+" (tasks="+tasks.size()+", paths="+storeRelativePaths.size()+") in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return storeRelativePaths;
    }
}
