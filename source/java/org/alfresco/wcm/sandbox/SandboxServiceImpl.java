/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.util.VirtServerUtils;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetInfoImpl;
import org.alfresco.wcm.asset.AssetService;
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
public class SandboxServiceImpl implements SandboxService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(SandboxServiceImpl.class);
    
    private static final String WORKFLOW_SUBMITDIRECT = "jbpm$"+WCMUtil.WORKFLOW_SUBMITDIRECT_NAME;
    
    private WebProjectService wpService;
    private SandboxFactory sandboxFactory;
    private AVMService avmService;
    private AVMSyncService avmSyncService;
    private NameMatcher nameMatcher;
    private VirtServerRegistry virtServerRegistry;
    private WorkflowService workflowService;
    private AssetService assetService;
    private TransactionService transactionService;
    private AVMLockingService avmLockingService;
    
    
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
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    public void setAssetService(AssetService assetService)
    {
        this.assetService = assetService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
 
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#createAuthorSandbox(java.lang.String)
     */
    public SandboxInfo createAuthorSandbox(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
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
        long start = System.currentTimeMillis();
        
        WebProjectInfo wpInfo = wpService.getWebProject(wpStoreId);
        
        final NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        String role = wpService.getWebUserRole(wpNodeRef, userName);
        SandboxInfo sbInfo = sandboxFactory.createUserSandbox(wpStoreId, userName, role);
        
        List<SandboxInfo> sandboxInfoList = new LinkedList<SandboxInfo>();
        sandboxInfoList.add(sbInfo);
        
        // Bind the post-commit transaction listener with data required for virtualization server notification
        CreateSandboxTransactionListener tl = new CreateSandboxTransactionListener(sandboxInfoList, wpService.listWebApps(wpNodeRef));
        AlfrescoTransactionSupport.bindListener(tl);
        
        if (logger.isDebugEnabled())
        {
           logger.debug("createAuthorSandboxImpl: " + sbInfo.getSandboxId() + " in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
        }
        
        return sbInfo;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSandboxes(java.lang.String)
     */
    public List<SandboxInfo> listSandboxes(String wpStoreId)
    {
        long start = System.currentTimeMillis();
        
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        List<SandboxInfo> sbInfos = null;
        
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        String userRole = wpService.getWebUserRole(wpStoreId, currentUser);
        
        if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userRole) || WCMUtil.ROLE_CONTENT_PUBLISHER.equals(userRole))
        {
            sbInfos = sandboxFactory.listAllSandboxes(wpStoreId);
        }
        else
        {
            sbInfos = new ArrayList<SandboxInfo>(1);
            
            if (userRole != null)
            {
                SandboxInfo authorSandbox = getAuthorSandbox(wpStoreId, currentUser);
                
                if (authorSandbox != null)
                {
                    sbInfos.add(authorSandbox);
                }
                
                sbInfos.add(getSandbox(WCMUtil.buildStagingStoreName(wpStoreId))); // get staging sandbox
            }
        }
        
        if (logger.isDebugEnabled())
        {
           logger.debug("listSandboxes: " + wpStoreId + " in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
        }
        
        return sbInfos;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSandboxes(java.lang.String, java.lang.String)
     */
    public List<SandboxInfo> listSandboxes(final String wpStoreId, String userName)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        ParameterCheck.mandatoryString("userName", userName);
        
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        String userRole = wpService.getWebUserRole(wpStoreId, currentUser);
        
        if (WCMUtil.ROLE_CONTENT_MANAGER.equals(userRole) || WCMUtil.ROLE_CONTENT_PUBLISHER.equals(userRole))
        {
            throw new AccessDeniedException("Only content managers or content publishers may list sandboxes for '"+userName+"' (web project id: "+wpStoreId+")");
        }
       
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<SandboxInfo>>()
        {
             public List<SandboxInfo> doWork() throws Exception
             {
                 return listSandboxes(wpStoreId);
             }
        }, userName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#isSandboxType(java.lang.String, org.alfresco.service.namespace.QName)
     */
    public boolean isSandboxType(String sbStoreId, QName sandboxType)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatory("sandboxType", sandboxType);
        
        SandboxInfo sbInfo = getSandbox(sbStoreId);
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
        long start = System.currentTimeMillis();
        
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        
        // check user has read access to web project (ie. is a web user)
        if (! wpService.isWebUser(wpStoreId))
        {
            return null;
        }
        
        if (! WCMUtil.isStagingStore(sbStoreId))
        {
            String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
            
            if (! (WCMUtil.getUserName(sbStoreId).equals(currentUser)))
            {
                String userRole = wpService.getWebUserRole(wpStoreId, currentUser);
                if (! (WCMUtil.ROLE_CONTENT_MANAGER.equals(userRole) || WCMUtil.ROLE_CONTENT_PUBLISHER.equals(userRole)))
                {
                    throw new AccessDeniedException("Only content managers or content publishers may get sandbox '"+sbStoreId+"' (web project id: "+wpStoreId+")");
                }
            }
        }
        
        SandboxInfo sbInfo = sandboxFactory.getSandbox(sbStoreId);
        
        if (logger.isTraceEnabled())
        {
           if (sbInfo != null)
           {
               logger.trace("getSandbox: " + sbInfo.getSandboxId() + " in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
           }
           else
           {
               logger.trace("getSandbox: " + sbStoreId +" (does not exist)  in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
           }
        }
        
        return sbInfo;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#getAuthorSandbox(java.lang.String)
     */
    public SandboxInfo getAuthorSandbox(String wpStoreId)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        return getSandbox(WCMUtil.buildUserMainStoreName(WCMUtil.buildStagingStoreName(wpStoreId), currentUserName));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#getUserSandbox(java.lang.String, java.lang.String)
     */
    public SandboxInfo getAuthorSandbox(String wpStoreId, String userName)
    {
        ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
        ParameterCheck.mandatoryString("userName", userName);
        
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
        long start = System.currentTimeMillis();
        
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        
        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            // system may delete (eg. workflow) sandbox
            sandboxFactory.deleteSandbox(sbStoreId);
        }
        else
        {
            String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
            if (sbStoreId.equals(WCMUtil.buildUserMainStoreName(wpStoreId, currentUserName)))
            {
                // author may delete their own sandbox
                sandboxFactory.deleteSandbox(sbStoreId);
            }
            else
            {
                if (! wpService.isContentManager(wpStoreId, currentUserName))
                {
                    throw new AccessDeniedException("Only content managers may delete sandbox '"+sbStoreId+"' (web project id: "+wpStoreId+")");
                }
                
                if (sbStoreId.equals(wpStoreId))
                {
                    throw new AccessDeniedException("Cannot delete staging sandbox '"+sbStoreId+"' (web project id: "+wpStoreId+")");
                }
                
                // content manager may delete sandboxes, except staging sandbox
                sandboxFactory.deleteSandbox(sbStoreId);
            }
        }
        
        if (logger.isDebugEnabled())
        {
           logger.debug("deleteSandbox: " + sbStoreId + " in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChangedAll(java.lang.String, boolean)
     */
    public List<AssetInfo> listChangedAll(String sbStoreId, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String avmDirectoryPath = WCMUtil.buildSandboxRootPath(sbStoreId); // currently <sbStoreId>:/www/avm_webapps
        return listChanged(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), includeDeleted);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChangedWebApp(java.lang.String, java.lang.String, boolean)
     */
    public List<AssetInfo> listChangedWebApp(String sbStoreId, String webApp, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        
        // filter by current webapp
        String avmDirectoryPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp);
        return listChanged(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), includeDeleted);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChanged(java.lang.String, java.lang.String, boolean)
     */
    public List<AssetInfo> listChanged(String sbStoreId, String relativePath, boolean includeDeleted)
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
        
        return listChanged(sbStoreId, relativePath, stagingSandboxId, relativePath, includeDeleted);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listChanged(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public List<AssetInfo> listChanged(String srcSandboxStoreId, String srcRelativePath, String dstSandboxStoreId, String dstRelativePath, boolean includeDeleted)
    {
        ParameterCheck.mandatoryString("srcSandboxStoreId", srcSandboxStoreId);
        ParameterCheck.mandatoryString("srcRelativePath", srcRelativePath);
        
        ParameterCheck.mandatoryString("dstSandboxStoreId", dstSandboxStoreId);
        ParameterCheck.mandatoryString("dstRelativePath", dstRelativePath);
        
        // checks sandbox access (TODO review)
        getSandbox(srcSandboxStoreId); // ignore result
        getSandbox(dstSandboxStoreId); // ignore result
        
        String avmSrcPath = AVMUtil.buildAVMPath(srcSandboxStoreId, srcRelativePath);
        String avmDstPath = AVMUtil.buildAVMPath(dstSandboxStoreId, dstRelativePath);
        
        return listChanged(-1, avmSrcPath, -1, avmDstPath, includeDeleted);
    }
    
    private List<AssetInfo> listChanged(int srcVersion, String srcPath, int dstVersion, String dstPath, boolean includeDeleted)
    {
        long start = System.currentTimeMillis();
        
        List<AVMDifference> diffs = avmSyncService.compare(srcVersion, srcPath, dstVersion, dstPath, nameMatcher);
        
        List<AssetInfo> assets = new ArrayList<AssetInfo>(diffs.size());
        
        for (AVMDifference diff : diffs)
        {
            // convert each diff record into an AVM node descriptor
            String sourcePath = diff.getSourcePath();
            
            String[] parts = WCMUtil.splitPath(sourcePath);
            AssetInfo asset = assetService.getAsset(parts[0], -1, parts[1], includeDeleted);
            if (asset != null)
            {
                // TODO refactor
                ((AssetInfoImpl)asset).setDiffCode(diff.getDifferenceCode());
                assets.add(asset);
            }
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("listChanged: "+assets.size()+" assets in "+(System.currentTimeMillis()-start)+" ms (between "+srcVersion+","+srcPath+" and "+dstVersion+","+dstPath);
        }
        
        return assets;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitAll(java.lang.String, java.lang.String, java.lang.String)
     */
    public void submitAll(String sbStoreId, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String avmDirectoryPath = WCMUtil.buildSandboxRootPath(sbStoreId); // currently <sbStoreId>:/www/avm_webapps
        submit(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitWebApp(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void submitWebApp(String sbStoreId, String webApp, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        
        String avmDirectoryPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp);
        submit(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath), submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submit(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void submit(String sbStoreId, String relativePath, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("relativePath", relativePath);
        
        List<AssetInfo> assets = listChanged(sbStoreId, relativePath, true);
        
        submitListAssets(sbStoreId, assets, submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitList(java.lang.String, java.util.List, java.lang.String, java.lang.String)
     */
    public void submitList(String sbStoreId, List<String> relativePaths, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        List<AssetInfo> assets = new ArrayList<AssetInfo>(relativePaths.size());
        
        for (String relativePath : relativePaths)
        {
            // convert each path into an asset
            AssetInfo asset = assetService.getAsset(sbStoreId, -1, relativePath, true);
            if (asset != null)
            {
                assets.add(asset);
            }
        }
        
        submitListAssets(sbStoreId, assets, submitLabel, submitComment);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitListAssets(java.lang.String, java.util.List, java.lang.String, java.lang.String)
     */
    public void submitListAssets(String sbStoreId, List<AssetInfo> assets, String submitLabel, String submitComment)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("submitLabel", submitLabel);
        
        // TODO - consider submit to higher-level sandbox, not just to staging
        if (! WCMUtil.isUserStore(sbStoreId))
        {
            throw new AlfrescoRuntimeException("Not an author sandbox: "+sbStoreId);
        }
        
        List<String> relativePaths = new ArrayList<String>(assets.size());
        for (AssetInfo asset : assets)
        {
            relativePaths.add(asset.getPath());
        }
        
        // via submit direct workflow
        submitViaWorkflow(sbStoreId, relativePaths, null, null, submitLabel, submitComment, null, null, false);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#submitListAssets(java.lang.String, java.util.List, java.lang.String, java.util.Map, java.lang.String, java.lang.String, java.util.Map, java.util.Date, boolean, boolean)
     */
    public void submitListAssets(String sbStoreId, List<String> relativePaths,
                                 String workflowName, Map<QName, Serializable> workflowParams, 
                                 String submitLabel, String submitComment,
                                 Map<String, Date> expirationDates, Date launchDate, boolean autoDeploy)
    {
        // via selected workflow
        submitViaWorkflow(sbStoreId, relativePaths, workflowName, workflowParams, submitLabel, submitComment,
                          expirationDates, launchDate, autoDeploy);
    }
    
    /**
     * Submits the selected items via the configured workflow.
     * <p>
     * This method uses 2 separate transactions to perform the submit.
     * The first one creates the workflow sandbox. The virtualisation
     * server is then informed of the new stores. The second
     * transaction then starts the appropriate workflow. This approach
     * is needed to allow link validation to be performed on the
     * workflow sandbox.
     */
    private void submitViaWorkflow(final String sbStoreId, final List<String> relativePaths, String workflowName, Map<QName, Serializable> workflowParams, 
                                   final String submitLabel, final String submitComment,
                                   final Map<String, Date> expirationDates, final Date launchDate, final boolean autoDeploy)
    {
        long start = System.currentTimeMillis();
        
        // checks sandbox access (TODO review)
        getSandbox(sbStoreId); // ignore result
        
        final String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        final String stagingSandboxId = WCMUtil.buildStagingStoreName(wpStoreId);
        
        final String finalWorkflowName;
        final Map<QName, Serializable> finalWorkflowParams;
        
        boolean isSubmitDirectWorkflowSandbox = false;
        
        if ((workflowName == null) || (workflowName.equals("")))
        {
            finalWorkflowName = WORKFLOW_SUBMITDIRECT;
            finalWorkflowParams = new HashMap<QName, Serializable>();
            isSubmitDirectWorkflowSandbox = true;
        }
        else
        {
            finalWorkflowName = workflowName;
            finalWorkflowParams = workflowParams;
        }
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        final List<String> srcPaths = new ArrayList<String>(relativePaths.size());
        for (String relativePath : relativePaths)
        {
            srcPaths.add(AVMUtil.buildAVMPath(sbStoreId, relativePath));
        }
        
        final String webApp = WCMUtil.getCommonWebApp(sbStoreId, relativePaths);
        
        RetryingTransactionCallback<Pair<SandboxInfo, String>> sandboxCallback = new RetryingTransactionCallback<Pair<SandboxInfo, String>>()
        {
            public Pair<SandboxInfo, String> execute() throws Throwable
            {
                // call the actual implementation
                return createWorkflowSandbox(finalWorkflowName, finalWorkflowParams, stagingSandboxId, srcPaths, expirationDates);
            }
        };
        
        // create the workflow sandbox firstly
        final Pair<SandboxInfo, String> workflowInfo = txnHelper.doInTransaction(sandboxCallback, false, true);
        
        if (workflowInfo != null)
        {
            final SandboxInfo wfSandboxInfo = workflowInfo.getFirst();
            String virtUpdatePath = workflowInfo.getSecond();
            
            // inform the virtualisation server if the workflow sandbox was created
            if (virtUpdatePath != null)
            {
                // optimization: direct submits no longer virtualize the workflow sandbox
                if (! isSubmitDirectWorkflowSandbox)
                {
                    WCMUtil.updateVServerWebapp(virtServerRegistry, virtUpdatePath, true);
                }
            }
            
            try
            {
                RetryingTransactionCallback<String> workflowCallback = new RetryingTransactionCallback<String>()
                {
                    public String execute() throws Throwable
                    {
                        // call the actual implementation
                        startWorkflow(wpStoreId, sbStoreId, wfSandboxInfo, webApp, finalWorkflowName, finalWorkflowParams, submitLabel, submitComment, launchDate, autoDeploy);
                        return null;
                    }
                };
                
                // start the workflow
                txnHelper.doInTransaction(workflowCallback, false, true);
            }
            catch (Throwable err)
            {
                cleanupWorkflowSandbox(wfSandboxInfo);
                throw new AlfrescoRuntimeException("Failed to submit to workflow", err);
            }
        }
        
        if (logger.isDebugEnabled())
        {
           logger.debug("submitViaWorkflow: " + sbStoreId + " ["+submitLabel+", "+finalWorkflowName+"] in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
        }
    }
    
    /**
     * Creates a workflow sandbox for all the submitted items
     *
     * @param context Faces context
     */
    protected Pair<SandboxInfo, String> createWorkflowSandbox(String workflowName, Map<QName, Serializable> workflowParams, String stagingSandboxId, final List<String> srcPaths, Map<String, Date> expirationDates)
    {
        // The virtualization server might need to be notified
        // because one or more of the files submitted could alter
        // the behavior the virtual webapp in the target of the submit.
        // For example, the user might be submitting a new jar or web.xml file.
        //
        // This must take place after the transaction has been completed;
        // therefore, a variable is needed to store the path to the
        // updated webapp so it can happen in doPostCommitProcessing.
        String virtUpdatePath = null;
        SandboxInfo sandboxInfo = null;
       
        // create container for our avm workflow package
       
        if (! workflowName.equals(WORKFLOW_SUBMITDIRECT))
        {
            // Create workflow sandbox for workflow package
            sandboxInfo = sandboxFactory.createWorkflowSandbox(stagingSandboxId);
        }
        else
        {
            // default to direct submit workflow

            // NOTE: read only workflow sandbox is lighter to construct than full workflow sandbox
            sandboxInfo = sandboxFactory.createReadOnlyWorkflowSandbox(stagingSandboxId);
        }
       
        // Example workflow main store name:
        //     mysite--workflow-9161f640-b020-11db-8015-130bf9b5b652
        String workflowMainStoreName = sandboxInfo.getMainStoreName();
       
        final List<AVMDifference> diffs = new ArrayList<AVMDifference>(srcPaths.size());
       
        // get diff list - also process expiration dates, if any, and set virt svr update path
       
        for (String srcPath : srcPaths)
        {
            // We *always* want to update virtualization server
            // when a workflow sandbox is given data in the
            // context of a submit workflow.  Without this,
            // it would be impossible to see workflow data
            // in context.  The raw operation to create a
            // workflow sandbox does not notify the virtualization
            // server that it exists because it's useful to
            // defer this operation until everything is already
            // in place; this allows pointlessly fine-grained
            // notifications to be suppressed (they're expensive).
            //
            // Therefore, just derive the name of the webapp
            // in the workflow sandbox from the 1st item in
            // the submit list (even if it's not in WEB-INF),
            // and force the virt server notification after the
            // transaction has completed via doPostCommitProcessing.
            if (virtUpdatePath  == null)
            {
                // The virtUpdatePath looks just like the srcPath
                // except that it belongs to a the main store of
                // the workflow sandbox instead of the sandbox
                // that originated the submit.
                virtUpdatePath = WCMUtil.getCorrespondingPath(srcPath, workflowMainStoreName);
            }

            if ((expirationDates != null) && (! expirationDates.isEmpty()))
            {
                // process the expiration date (if any)
                processExpirationDate(srcPath, expirationDates);
            }
          
            diffs.add(new AVMDifference(-1, srcPath, -1, WCMUtil.getCorrespondingPath(srcPath, workflowMainStoreName), AVMDifference.NEWER));
        }

        // write changes to layer so files are marked as modified
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {

            public Object doWork() throws Exception
            {
                avmSyncService.update(diffs, null, false, false, false, false, null, null);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
       
        return new Pair<SandboxInfo, String>(sandboxInfo, virtUpdatePath);
    }
    
    /**
     * Starts the configured workflow to allow the submitted items to be link
     * checked and reviewed.
     */
    protected void startWorkflow(String wpStoreId, String sbStoreId, SandboxInfo wfSandboxInfo, String webApp, String workflowName, Map<QName, Serializable> workflowParams, 
                                 String submitLabel, String submitComment, Date launchDate, boolean autoDeploy)
    {
        ParameterCheck.mandatoryString("workflowName", workflowName);
        ParameterCheck.mandatory("workflowParams", workflowParams);
        
        // start the workflow to get access to the start task
        WorkflowDefinition wfDef = workflowService.getDefinitionByName(workflowName);
        WorkflowPath path = workflowService.startWorkflow(wfDef.id, null);
        
        if (path != null)
        {
            // extract the start task
            List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
                WorkflowTask startTask = tasks.get(0);

                if (startTask.state == WorkflowTaskState.IN_PROGRESS)
                {
                    final NodeRef workflowPackage = WCMWorkflowUtil.createWorkflowPackage(workflowService, avmService, wfSandboxInfo);

                    workflowParams.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

                    // add submission parameters
                    workflowParams.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, submitComment);
                    workflowParams.put(WCMWorkflowModel.PROP_LABEL, submitLabel);
                    workflowParams.put(WCMWorkflowModel.PROP_FROM_PATH,
                            WCMUtil.buildStoreRootPath(sbStoreId));
                    workflowParams.put(WCMWorkflowModel.PROP_LAUNCH_DATE, launchDate);
                    workflowParams.put(WCMWorkflowModel.PROP_AUTO_DEPLOY,
                            new Boolean(autoDeploy));
                    workflowParams.put(WCMWorkflowModel.PROP_WEBAPP,
                            webApp);
                    workflowParams.put(WCMWorkflowModel.ASSOC_WEBPROJECT,
                           wpService.getWebProjectNodeFromStore(wpStoreId));

                    // update start task with submit parameters
                    workflowService.updateTask(startTask.id, workflowParams, null, null);

                    // end the start task to trigger the first 'proper' task in the workflow
                    workflowService.endTask(startTask.id, null);
                }
            }
        }
    }

    /**
     * Cleans up the workflow sandbox created by the first transaction. This
     * action is itself performed in a separate transaction.
     */
    private void cleanupWorkflowSandbox(final SandboxInfo sandboxInfo)
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
       
        RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                // delete AVM stores in the workflow sandbox
                sandboxFactory.deleteSandbox(sandboxInfo.getSandboxId());
                return null;
            }
        };
        
        try
        {
            // Execute the cleanup handler
            txnHelper.doInTransaction(callback);
        }
        catch (Throwable e)
        {
            // not much we can do now, just log the error to inform admins
            logger.error("Failed to cleanup workflow sandbox after workflow failure", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertAll(java.lang.String)
     */  
    public void revertAll(String sbStoreId)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String avmDirectoryPath = WCMUtil.buildSandboxRootPath(sbStoreId); // currently <sbStoreId>:/www/avm_webapps
        revert(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertWebApp(java.lang.String, java.lang.String)
     */
    public void revertWebApp(String sbStoreId, String webApp)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("webApp", webApp);
        
        String avmDirectoryPath = WCMUtil.buildStoreWebappPath(sbStoreId, webApp);
        revert(sbStoreId, WCMUtil.getStoreRelativePath(avmDirectoryPath));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertAllDir(java.lang.String, java.lang.String)
     */
    public void revert(String sbStoreId, String relativePath)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatoryString("relativePath", relativePath);
        
        List<AssetInfo> assets = listChanged(sbStoreId, relativePath, true);
        
        revertListAssets(sbStoreId, assets);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertList(java.lang.String, java.util.List)
     */
    public void revertList(String sbStoreId, List<String> relativePaths)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        List<AssetInfo> assets = new ArrayList<AssetInfo>(relativePaths.size());
        
        for (String relativePath : relativePaths)
        {
            // convert each path into an asset
            AssetInfo asset = assetService.getAsset(sbStoreId, -1, relativePath, true);
            if (asset != null)
            {
                assets.add(asset);
            }
        }
        
        revertListAssets(sbStoreId, assets);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertListAssets(java.lang.String, java.util.List)
     */
    public void revertListAssets(String sbStoreId, List<AssetInfo> assets)
    {
        long start = System.currentTimeMillis();
        
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        // checks sandbox access (TODO review)
        getSandbox(sbStoreId); // ignore result
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        
        List<AssetInfo> assetsToRevert = new ArrayList<AssetInfo>(assets.size());
        
        List<String> wfRelativePaths = WCMWorkflowUtil.getAssociatedPathsForSandbox(avmSyncService, workflowService, sbStoreId);
        
        for (AssetInfo asset : assets)
        {
           if (! asset.getSandboxId().equals(sbStoreId))
           {
               // belts-and-braces
               logger.warn("revertListAssets: Skip assert "+asset.getPath()+" (was "+asset.getSandboxId()+", expected "+sbStoreId+")");
               continue;
           }
           
           // check if in workflow
           if (! wfRelativePaths.contains(asset.getPath()))
           {
              assetsToRevert.add(asset);
              
              if (VirtServerUtils.requiresUpdateNotification(asset.getAvmPath()))
              {
                  // Bind the post-commit transaction listener with data required for virtualization server notification
                  UpdateSandboxTransactionListener tl = new UpdateSandboxTransactionListener(asset.getAvmPath());
                  AlfrescoTransactionSupport.bindListener(tl);
              }
           }
        }
        
        for (AssetInfo asset : assetsToRevert)
        {
            String [] parentChild = AVMNodeConverter.SplitBase(asset.getAvmPath());
            if (parentChild.length != 2)
            {
                continue;
            }
            
            AVMNodeDescriptor parent = avmService.lookup(-1, parentChild[0], true);
            
            if (parent.isLayeredDirectory())
            {
                if (logger.isTraceEnabled())
                {
                   logger.trace("reverting " + parentChild[1] + " in " + parentChild[0]);
                }
                
                avmService.makeTransparent(parentChild[0], parentChild[1]);
            }
            
            if (asset.isFile())
            {
                // is file or deleted file
                String relativePath = asset.getPath();
                
                if (logger.isTraceEnabled())
                {
                    logger.trace("unlocking file " + relativePath + " in web project " + wpStoreId);
                }
                
                if (avmLockingService.getLockOwner(wpStoreId, relativePath) != null)
                {
                    avmLockingService.removeLock(wpStoreId, relativePath);
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("expected file " + relativePath + " in " + wpStoreId + " to be locked");
                    }
                }
            }
        }
        
        if (logger.isDebugEnabled())
        {
           logger.debug("revertListAssets: " + sbStoreId + " ["+assets.size()+", "+assetsToRevert.size()+"] in "+(System.currentTimeMillis()-start)+" ms (web project id: " + wpStoreId + ")");
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSnapshots(java.lang.String, boolean)
     */
    public List<SandboxVersion> listSnapshots(String sbStoreId, boolean includeSystemGenerated)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may list snapshots '"+sbStoreId+"' (web project id: "+wpStoreId+")");
        }
        
        return listSnapshots(sbStoreId, null, null, includeSystemGenerated);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#listSnapshots(java.lang.String, java.util.Date, java.util.Date, boolean)
     */
    public List<SandboxVersion> listSnapshots(String sbStoreId, Date from, Date to, boolean includeSystemGenerated)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        if (! wpService.isContentManager(wpStoreId))
        {
            throw new AccessDeniedException("Only content managers may list snapshots '"+sbStoreId+"' (web project id: "+wpStoreId+")");
        }
        
        return listSnapshotsImpl(sbStoreId, from, to, includeSystemGenerated);
    }
        
    private List<SandboxVersion> listSnapshotsImpl(String sbStoreId, Date from, Date to, boolean includeSystemGenerated)
    {
        long start = System.currentTimeMillis();
        
        List<VersionDescriptor> versionsToFilter = null;
        
        if ((from != null) && (to != null))
        {
            versionsToFilter = avmService.getStoreVersions(sbStoreId, from, to);
        }
        else
        {
            versionsToFilter = avmService.getStoreVersions(sbStoreId);
        }
        
        List<SandboxVersion> versions = new ArrayList<SandboxVersion>(versionsToFilter.size());
        
        for (int i = versionsToFilter.size() - 1; i >= 0; i--) // reverse order
        {
            VersionDescriptor item = versionsToFilter.get(i);
            
            // only display snapshots with a valid tag - others are system generated snapshots
            if ((includeSystemGenerated == true) || ((item.getTag() != null) && (item.getVersionID() != 0)))
            {
                versions.add(new SandboxVersionImpl(item));
            }
        }
        
        if (logger.isDebugEnabled())
        {
           logger.debug("listSnapshotsImpl: " + sbStoreId + " ["+from+", "+to+", "+versions.size()+"] in "+(System.currentTimeMillis()-start)+" ms (web project id: "+WCMUtil.getWebProjectStoreId(sbStoreId)+")");
        }
        
        return versions;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.sandbox.SandboxService#revertSnapshot(java.lang.String, int)
     */
    public void revertSnapshot(final String sbStoreId, final int revertVersion)
    {
        long start = System.currentTimeMillis();
        
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
                String sandboxPath = AVMUtil.buildAVMPath(sbStoreId, AVMUtil.AVM_PATH_SEPARATOR); // root
                List<AVMDifference> diffs = avmSyncService.compare(revertVersion, sandboxPath, -1, sandboxPath, null);
                
                String message = "Reverted to Version " + revertVersion + ".";
                avmSyncService.update(diffs, null, false, false, true, true, message, message);
                
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
        
        if (logger.isDebugEnabled())
        {
           logger.debug("revertSnapshot: " + sbStoreId + " ["+revertVersion+"] in "+(System.currentTimeMillis()-start)+" ms (web project id: "+WCMUtil.getWebProjectStoreId(sbStoreId)+")");
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
       
       if (logger.isTraceEnabled())
       {
           logger.trace("Set expiration date of " + expirationDate + " for " + srcPath);
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
