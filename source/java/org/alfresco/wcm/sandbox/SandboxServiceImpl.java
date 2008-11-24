/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.wcm.sandbox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMRevertStoreAction;
import org.alfresco.repo.avm.actions.AVMUndoSandboxListAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.VirtServerUtils;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.util.WCMWorkflowUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Sandbox Service fundamental API.
 * <p>
 * This service API is designed to support the public facing Sandbox APIs. 
 * 
 * @author janv
 */
public class SandboxServiceImpl extends WCMUtil implements SandboxService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(SandboxServiceImpl.class);
    
    private WebProjectService wpService;
    private SandboxFactory sandboxFactory;
    private AVMService avmService;
    private AVMLockingService avmLockingService;
    private AVMSyncService avmSyncService;
    private NameMatcher nameMatcher;
    private VirtServerRegistry virtServerRegistry;
    private ActionService actionService;
    private WorkflowService workflowService;
    
    public void setWebProjectService(WebProjectService wpService)
    {
        this.wpService = wpService;
    }

    public void setSandboxFactory(SandboxFactory sandboxFactory)
    {
        this.sandboxFactory = sandboxFactory;
    }
    
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    public void setAvmLockingService(AVMLockingService avmLockingService)
    {
        this.avmLockingService = avmLockingService;
    }
    
    public void setAvmSyncService(AVMSyncService avmSyncService)
    {
        this.avmSyncService = avmSyncService;
    }
    
    public void setNameMatcher(NameMatcher nameMatcher)
    {
       this.nameMatcher = nameMatcher;
    }
    
    public void setVirtServerRegistry(VirtServerRegistry virtServerRegistry)
    {
        this.virtServerRegistry = virtServerRegistry;
    }
    
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
 
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#createAuthorSandbox(java.lang.String)
     */
    public SandboxInfo createAuthorSandbox(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        String currentUserName = AuthenticationUtil.getCurrentEffectiveUserName();
        SandboxInfo sbInfo = null;
        
        if (! wpService.isWebUser(wpStoreId, currentUserName))
        {
            throw new AccessDeniedException("Only web project users may create their own (author) sandbox for '"+currentUserName+"' (store id: "+wpStoreId+")");
        }
        else
        {
            sbInfo = createAuthorSandboxImpl(wpStoreId, currentUserName);
        }
        
        return sbInfo;
    }

    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#createAuthorSandbox(java.lang.String, java.lang.String)
     */
    public SandboxInfo createAuthorSandbox(String wpStoreId, String userName)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        ParameterCheck.mandatoryString("userName", userName);
        
        // is the current user a content manager for this web project ?
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may create author sandbox for '"+userName+"' (store id: "+wpStoreId+")");
        }
        
        return createAuthorSandboxImpl(wpStoreId, userName);
    }
    
    private SandboxInfo createAuthorSandboxImpl(String wpStoreId, String userName)
    {
        WebProjectInfo wpInfo = wpService.getWebProject(wpStoreId);
        
        final NodeRef wpNodeRef = wpInfo.getNodeRef();
        final List<String> managers = new ArrayList<String>(4);
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // retrieve the list of managers from the existing users
                Map<String, String> existingUserRoles = wpService.listWebUsers(wpNodeRef);
                for (Map.Entry<String, String> userRole : existingUserRoles.entrySet())
                {
                    String username = userRole.getKey();
                    String userrole = userRole.getValue();
                      
                    if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userrole) && managers.contains(username) == false)
                    {
                        managers.add(username);
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        String role = wpService.getWebUserRole(wpNodeRef, userName);
        SandboxInfo sbInfo = sandboxFactory.createUserSandbox(wpStoreId, managers, userName, role);
        
        List<SandboxInfo> sandboxInfoList = new LinkedList<SandboxInfo>();
        sandboxInfoList.add(sbInfo);
        
        // Bind the post-commit transaction listener with data required for virtualization server notification
        CreateSandboxTransactionListener tl = new CreateSandboxTransactionListener(sandboxInfoList, wpService.listWebApps(wpNodeRef));
        AlfrescoTransactionSupport.bindListener(tl);
        
        if (logger.isInfoEnabled())
        {
           logger.info("Created author sandbox: " + sbInfo.getSandboxId() + " (web project id: " + wpStoreId + ")");
        }
        
        return sbInfo;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSandboxes(java.lang.String)
     */
    public List<SandboxInfo> listSandboxes(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        return sandboxFactory.listSandboxes(wpStoreId, AuthenticationUtil.getCurrentEffectiveUserName());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSandboxes(java.lang.String, java.lang.String)
     */
    public List<SandboxInfo> listSandboxes(final String wpStoreId, String userName)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        ParameterCheck.mandatoryString("userName", userName);
        
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may list sandboxes for '"+userName+"' (web project id: "+wpStoreId+")");
        }
       
        return sandboxFactory.listSandboxes(wpStoreId, userName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#isSandboxType(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public boolean isSandboxType(String sbStoreId, QName sandboxType)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatory("sandboxType", sandboxType);
        
        SandboxInfo sbInfo = sandboxFactory.getSandbox(sbStoreId);
        if (sbInfo != null)
        {
            return sbInfo.getSandboxType().equals(sandboxType);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#getSandbox(java.lang.String)
     */
    public SandboxInfo getSandbox(String sbStoreId)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        return sandboxFactory.getSandbox(sbStoreId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#getAuthorSandbox(java.lang.String)
     */
    public SandboxInfo getAuthorSandbox(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        String currentUserName = AuthenticationUtil.getCurrentEffectiveUserName();
        return getSandbox(WCMUtil.buildUserMainStoreName(WCMUtil.buildStagingStoreName(wpStoreId), currentUserName));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#getUserSandbox(java.lang.String, java.lang.String)
     */
    public SandboxInfo getAuthorSandbox(String wpStoreId, String userName)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        ParameterCheck.mandatoryString("userName", userName);
        
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may get author sandbox for '"+userName+"' (web project id: "+wpStoreId+")");
        }
        
        return getSandbox(WCMUtil.buildUserMainStoreName(WCMUtil.buildStagingStoreName(wpStoreId), userName));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#getStagingSandbox(java.lang.String)
     */
    public SandboxInfo getStagingSandbox(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        return getSandbox(WCMUtil.buildStagingStoreName(wpStoreId));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#deleteSandbox(java.lang.String)
     */
    public void deleteSandbox(String sbStoreId)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        
        String currentUserName = AuthenticationUtil.getCurrentEffectiveUserName();
        if (sbStoreId.equals(WCMUtil.buildUserMainStoreName(wpStoreId, currentUserName)))
        {
            // author may delete their own sandbox
            sandboxFactory.deleteSandbox(sbStoreId);
        }
        else
        {       
            if (! wpService.isContentManager(wpStoreId))
            {
                throw new AccessDeniedException("Only content managers may delete sandbox '"+sbStoreId+"' (web project id: "+wpStoreId+")");
            }
            
            if (sbStoreId.equals(wpStoreId))
            {
                throw new AlfrescoRuntimeException("Cannot delete staging sandbox '"+sbStoreId+"' (web project id: "+wpStoreId+")");
            }
            
            // content manager may delete sandboxes, except staging sandbox
            sandboxFactory.deleteSandbox(sbStoreId);
        }
        
        if (logger.isInfoEnabled())
        {
           logger.info("Deleted sandbox: " + sbStoreId + " (web project id: " + wpStoreId + ")");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChangedItems(java.lang.String, boolean)
     */
    public List<AVMNodeDescriptor> listChangedItems(String sbStoreId, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String avmDirectoryPath = WCMUtil.buildStoreRootPath(sbStoreId); // currently <sbStoreId>:/www
        return listChangedItemsDir(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), includeDeleted);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChangedItemsWebApp(java.lang.String, java.lang.String, boolean)
     */
    public List<AVMNodeDescriptor> listChangedItemsWebApp(String sbStoreId, String webApp, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        
        // filter by current webapp
        String avmDirectoryPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp);
        return listChangedItemsDir(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), includeDeleted);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChangedItemsDir(java.lang.String, java.lang.String, boolean)
     */
    public List<AVMNodeDescriptor> listChangedItemsDir(String sbStoreId, String relativePath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("relativePath", relativePath);
        
        // TODO - allow list for any sandbox
        if (! WCMUtil.isUserStore(sbStoreId))
        {
            throw new AlfrescoRuntimeException("Not an author sandbox: "+sbStoreId);
        }
        
        // build the paths to the stores to compare - filter by given directory path
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        String stagingSandboxId = WCMUtil.buildStagingStoreName(wpStoreId);
        
        return listChangedItems(sbStoreId, relativePath, stagingSandboxId, relativePath, includeDeleted);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChangedItems(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public List<AVMNodeDescriptor> listChangedItems(String srcSandboxStoreId, String srcRelativePath, String dstSandboxStoreId, String dstRelativePath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("srcSandboxStoreId", srcSandboxStoreId);
        ParameterCheck.mandatoryString("srcRelativePath", srcRelativePath);
        
        ParameterCheck.mandatoryString("dstSandboxStoreId", dstSandboxStoreId);
        ParameterCheck.mandatoryString("dstRelativePath", dstRelativePath);
        
        String avmSrcPath = srcSandboxStoreId + AVM_STORE_SEPARATOR + srcRelativePath;
        String avmDstPath = dstSandboxStoreId + AVM_STORE_SEPARATOR + dstRelativePath;
        
        return listChangedItems(-1, avmSrcPath, -1, avmDstPath, includeDeleted);
    }
    
    private List<AVMNodeDescriptor> listChangedItems(int srcVersion, String srcPath, int dstVersion, String dstPath, boolean includeDeleted)
    {
        long start = System.currentTimeMillis();
        
        List<AVMDifference> diffs = avmSyncService.compare(srcVersion, srcPath, dstVersion, dstPath, nameMatcher);
        
        List<AVMNodeDescriptor> items = new ArrayList<AVMNodeDescriptor>(diffs.size());
        
        for (AVMDifference diff : diffs)
        {
            // convert each diff record into an AVM node descriptor
            String sourcePath = diff.getSourcePath();
            AVMNodeDescriptor node = avmService.lookup(-1, sourcePath, includeDeleted);
            if (node != null)
            {
                items.add(node);
            }
        }
        
        if (logger.isTraceEnabled())
        {
            logger.trace("listModifiedItems: "+items.size()+" items in "+(System.currentTimeMillis()-start)+" ms (between "+srcVersion+","+srcPath+" and "+dstVersion+","+dstPath);
        }

        return items;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitAll(java.lang.String, java.lang.String, java.lang.String)
     */
    public void submitAll(String sbStoreId, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String avmDirectoryPath = WCMUtil.buildStoreRootPath(sbStoreId); // currently <sbStoreId>:/www
        submitAllDir(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitAllWebApp(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void submitAllWebApp(String sbStoreId, String webApp, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        
        String avmDirectoryPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp);
        submitAllDir(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitAllDir(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void submitAllDir(String sbStoreId, String relativePath, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("relativePath", relativePath);
        
        List<AVMNodeDescriptor> items = listChangedItemsDir(sbStoreId, relativePath, true);
        
        submitListNodes(sbStoreId, items, submitLabel, submitComment);
    }
    
    public void submitList(String sbStoreId, List<String> relativePaths, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        List<AVMNodeDescriptor> items = new ArrayList<AVMNodeDescriptor>(relativePaths.size());
        
        for (String relativePath : relativePaths)
        {
            // convert each path into an AVM node descriptor
            AVMNodeDescriptor node = avmService.lookup(-1, sbStoreId + WCMUtil.AVM_STORE_SEPARATOR + relativePath, true);
            if (node != null)
            {
                items.add(node);
            }
        }
        
        submitListNodes(sbStoreId, items, null, submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitListNodes(java.lang.String, java.util.List, java.lang.String, java.lang.String)
     */
    public void submitListNodes(String sbStoreId, List<AVMNodeDescriptor> items, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        submitListNodes(sbStoreId, items, null, submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitListNodes(java.lang.String, java.util.List, java.util.Map, java.lang.String, java.lang.String)
     */
    public void submitListNodes(String sbStoreId, List<AVMNodeDescriptor> items, Map<String, Date> expirationDates, final String submitLabel, final String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        // direct submit to the staging area (without workflow)
        
        // TODO - consider submit to higher-level sandbox, not just to staging
        if (! WCMUtil.isUserStore(sbStoreId))
        {
            throw new AlfrescoRuntimeException("Not an author sandbox: "+sbStoreId);
        }
        
        // construct diffs for selected items for submission
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        String stagingSandboxId = WCMUtil.buildStagingStoreName(wpStoreId);
        
        final List<AVMDifference> diffs = new ArrayList<AVMDifference>(items.size());
        
        for (AVMNodeDescriptor item : items)
        {
            String relativePath = WCMUtil.getStoreRelativePath(item.getPath());
            
            String srcPath = sbStoreId + AVM_STORE_SEPARATOR + relativePath;
            String dstPath = stagingSandboxId + AVM_STORE_SEPARATOR + relativePath;         
 
            AVMDifference diff = new AVMDifference(-1, srcPath, -1, dstPath, AVMDifference.NEWER);
            diffs.add(diff);

            if (expirationDates != null)
            {
                // process the expiration date (if any)
                processExpirationDate(srcPath, expirationDates);
            }

            // recursively remove locks from this item
            recursivelyRemoveLocks(wpStoreId, -1, avmService.lookup(-1, srcPath, true), srcPath);

            // check to see if destPath forces a notification of the virtualization server
            // (e.g.:  it might be a path to a jar file within WEB-INF/lib).
            if (VirtServerUtils.requiresUpdateNotification(dstPath))
            {
                // Bind the post-commit transaction listener with data required for virtualization server notification
                UpdateSandboxTransactionListener tl = new UpdateSandboxTransactionListener(dstPath);
                AlfrescoTransactionSupport.bindListener(tl);
            }
        }
        
        // write changes to layer so files are marked as modified
        
        // Submit is done as system as the staging store is read only
        // We could add support to runIgnoringStoreACls
        
        // TODO review flatten - assumes webapps, hence currently flattens at /www/avm_webapps level
        // also review flatten for SimpleAVMSubmitAction and AVMSubmitAction
        final String sandboxPath = WCMUtil.buildSandboxRootPath(sbStoreId);
        final String stagingPath = WCMUtil.buildSandboxRootPath(stagingSandboxId);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
             public Object doWork() throws Exception
             {
                 avmSyncService.update(diffs, null, true, true, false, false, submitLabel, submitComment);
                 AVMDAOs.Instance().fAVMNodeDAO.flush();
                 avmSyncService.flatten(sandboxPath, stagingPath);
                 return null;
             }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertAll(java.lang.String)
     */  
    public void revertAll(String sbStoreId)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String avmDirectoryPath = WCMUtil.buildStoreRootPath(sbStoreId); // currently <sbStoreId>:/www
        revertAllDir(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertAll(java.lang.String, java.lang.String)
     */
    public void revertAllWebApp(String sbStoreId, String webApp)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        
        String avmDirectoryPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp);
        revertAllDir(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertAllDir(java.lang.String, java.lang.String)
     */
    public void revertAllDir(String sbStoreId, String relativePath)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("relativePath", relativePath);
        
        List<AVMNodeDescriptor> items = listChangedItemsDir(sbStoreId, relativePath, true);
        
        revertListNodes(sbStoreId, items);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertListNodes(java.lang.String, java.util.List)
     */
    public void revertListNodes(String sbStoreId, List<AVMNodeDescriptor> items)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>(items.size());
        
        List<WorkflowTask> tasks = null;
        for (AVMNodeDescriptor node : items)
        {
           if (tasks == null)
           {
              tasks = WCMWorkflowUtil.getAssociatedTasksForSandbox(workflowService, WCMUtil.getSandboxStoreId(node.getPath()));
           }
           if (WCMWorkflowUtil.getAssociatedTasksForNode(avmService, node, tasks).size() == 0)
           {
              String revertPath = node.getPath();
              versionPaths.add(new Pair<Integer, String>(-1, revertPath));
              
              if (VirtServerUtils.requiresUpdateNotification(revertPath))
              {
                  // Bind the post-commit transaction listener with data required for virtualization server notification
                  UpdateSandboxTransactionListener tl = new UpdateSandboxTransactionListener(revertPath);
                  AlfrescoTransactionSupport.bindListener(tl);
              }
           }
        }
        
        Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
        args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
        Action action = actionService.createAction(AVMUndoSandboxListAction.NAME, args);
        actionService.executeAction(action, null);    // dummy action ref, list passed as action arg
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSnapshots(java.lang.String, boolean)
     */
    public List<VersionDescriptor> listSnapshots(String sbStoreId, boolean includeSystemGenerated)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may list snapshots '"+sbStoreId+"' (web project id: "+wpStoreId+")");
        }
        
        List<VersionDescriptor> allVersions = avmService.getStoreVersions(sbStoreId);
        return listSnapshots(allVersions, includeSystemGenerated);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSnapshots(java.lang.String, java.util.Date, java.util.Date, boolean)
     */
    public List<VersionDescriptor> listSnapshots(String sbStoreId, Date from, Date to, boolean includeSystemGenerated)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may list snapshots '"+sbStoreId+"' (web project id: "+wpStoreId+")");
        }
        
        List<VersionDescriptor> versionsToFilter = avmService.getStoreVersions(sbStoreId, from, to);
        return listSnapshots(versionsToFilter, includeSystemGenerated);
    }
        
    private List<VersionDescriptor> listSnapshots(List<VersionDescriptor> versionsToFilter, boolean includeSystemGenerated)
    {
        List<VersionDescriptor> versions = new ArrayList<VersionDescriptor>(versionsToFilter.size());
        
        for (int i = versionsToFilter.size() - 1; i >= 0; i--) // reverse order
        {
            VersionDescriptor item = versionsToFilter.get(i);

            // only display snapshots with a valid tag - others are system generated snapshots
            if ((includeSystemGenerated == true) || ((item.getTag() != null) && (item.getVersionID() != 0)))
            {
                versions.add(item);
            }
        }
        
        return versions;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertSnapshot(java.lang.String, int)
     */
    public void revertSnapshot(final String sbStoreId, final int version)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
                
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may revert staging sandbox '"+sbStoreId+"' (web project id: "+wpStoreId+")");
        }

        // do this as system as the staging area has restricted access (and content manager may not have permission to delete children, for example)
        List<AVMDifference> diffs = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<AVMDifference>>()
        {
            public List<AVMDifference> doWork() throws Exception
            {
                String sandboxPath = WCMUtil.buildSandboxRootPath(sbStoreId);

                List<AVMDifference> diffs = avmSyncService.compare(-1, sandboxPath, version, sandboxPath, null);

                Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
                args.put(AVMRevertStoreAction.PARAM_VERSION, version);
                Action action = actionService.createAction(AVMRevertStoreAction.NAME, args);
                actionService.executeAction(action, AVMNodeConverter.ToNodeRef(-1, sbStoreId + AVM_STORE_SEPARATOR + "/"));
                return diffs;
            }
        }, AuthenticationUtil.getSystemUserName());
         
        // See if any of the files being reverted require notification of the virt server, to update the webapp
        for (AVMDifference diff : diffs)
        {
            if (VirtServerUtils.requiresUpdateNotification(diff.getSourcePath()))
            {
                // Bind the post-commit transaction listener with data required for virtualization server notification
                UpdateSandboxTransactionListener tl = new UpdateSandboxTransactionListener(diff.getSourcePath());
                AlfrescoTransactionSupport.bindListener(tl);
                break;
            }
        }
    }
    
    /**
     * Sets up the expiration date for the given source path
     *
     * @param srcPath The path to set the expiration date for
     */
    private void processExpirationDate(String srcPath, Map<String, Date> expirationDates)
    {
       // if an expiration date has been set for this item we need to
       // add the expires aspect and the date supplied
       Date expirationDate = expirationDates.get(srcPath);
       if (expirationDate == null)
       {
          return;
       }
       
       // make sure the aspect is present
       if (avmService.hasAspect(-1, srcPath, WCMAppModel.ASPECT_EXPIRES) == false)
       {
           avmService.addAspect(srcPath, WCMAppModel.ASPECT_EXPIRES);
       }

       // set the expiration date
       avmService.setNodeProperty(srcPath, WCMAppModel.PROP_EXPIRATIONDATE, 
                                       new PropertyValue(DataTypeDefinition.DATETIME, expirationDate));

       if (logger.isDebugEnabled())
       {
           logger.debug("Set expiration date of " + expirationDate + " for " + srcPath);
       }
    }
    
    /**
     * Recursively remove locks from a path. Walking child folders looking for files
     * to remove locks from.
     */
    private void recursivelyRemoveLocks(String wpStoreId, int version, AVMNodeDescriptor desc, String absoluteAVMPath)
    {
       if (desc.isFile() || desc.isDeletedFile())
       {
          avmLockingService.removeLock(wpStoreId, WCMUtil.getStoreRelativePath(absoluteAVMPath));
       }
       else
       {
          if (desc.isDeletedDirectory())
          {
             // lookup the previous child and get its contents
             final List<AVMNodeDescriptor> history = avmService.getHistory(desc, 2);
             if (history.size() <= 1)
             {
                return;
             }
             desc = history.get(1);
          }

          Map<String, AVMNodeDescriptor> list = avmService.getDirectoryListingDirect(desc, true);
          for (Map.Entry<String, AVMNodeDescriptor> child : list.entrySet())
          {
             String name = child.getKey();
             AVMNodeDescriptor childDesc = child.getValue();
             recursivelyRemoveLocks(wpStoreId, version, childDesc, absoluteAVMPath + "/" + name);
          }
       }
    }

    /**
     * Create Sandbox Transaction listener - invoked after commit
     */
    private class CreateSandboxTransactionListener extends TransactionListenerAdapter
    {
        private List<SandboxInfo> sandboxInfoList;
        private List<String> webAppNames;
        
        public CreateSandboxTransactionListener(List<SandboxInfo> sandboxInfoList, List<String> webAppNames)
        {
            this.sandboxInfoList = sandboxInfoList;
            this.webAppNames = webAppNames;
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
         */
        @Override
        public void afterCommit()
        {
            // Handle notification to the virtualization server 
            // (this needs to occur after the sandboxes are created in the main txn)
            
            // reload virtualisation server for webapp(s) in this web project
            for (SandboxInfo sandboxInfo : this.sandboxInfoList)
            {
                String newlyInvitedStoreName = WCMUtil.buildStagingStoreName(sandboxInfo.getMainStoreName());
                
                for (String webAppName : webAppNames)
                {
                    String path = WCMUtil.buildStoreWebappPath(newlyInvitedStoreName, webAppName);
                    WCMUtil.updateVServerWebapp(virtServerRegistry, path, true);
                }
            }
        }
    }
    
    /**
     * Update Sandbox Transaction listener - invoked after submit or revert
     */
    private class UpdateSandboxTransactionListener extends TransactionListenerAdapter
    {
        private String virtUpdatePath;
        
        public UpdateSandboxTransactionListener(String virtUpdatePath)
        {
            this.virtUpdatePath = virtUpdatePath;
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
         */
        @Override
        public void afterCommit()
        {
            // The virtualization server might need to be notified
            // because one or more of the files submitted / reverted could alter
            // the behavior the virtual webapp in the target of the submit.
            // For example, the user might be submitting a new jar or web.xml file.
            //
            // This must take place after the transaction has been completed;
            
            // force an update of the virt server if necessary
            if (this.virtUpdatePath != null)
            {
               WCMUtil.updateVServerWebapp(virtServerRegistry, this.virtUpdatePath, true);
            }
        }
    }
}
