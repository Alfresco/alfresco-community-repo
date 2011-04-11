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
package org.alfresco.web.bean.wcm;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.wcm.WebResources;
import org.alfresco.web.ui.wcm.component.UISandboxSnapshots;
import org.alfresco.web.ui.wcm.component.UIUserSandboxes;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;

/**
 * Bean backing up the AVM specific browse screens
 * 
 * @author Kevin Roast
 */
public class AVMBrowseBean implements IContextListener
{
   private static final long serialVersionUID = -2310105113473561134L;

   public static final String BEAN_NAME = "AVMBrowseBean";
   
   private static final Log logger = LogFactory.getLog(AVMBrowseBean.class);
   
   public static final String REQUEST_BEEN_DEPLOYED_RESULT = "_alfBeenDeployedResult";
   
   private static final String MSG_REVERT_SUCCESS = "revert_success";
   private static final String MSG_REVERT_SANDBOX = "revert_sandbox_success";
   private static final String MSG_SANDBOXTITLE = "sandbox_title";
   private static final String MSG_SANDBOXSTAGING = "sandbox_staging";
   private static final String MSG_CREATED_ON = "store_created_on";
   private static final String MSG_CREATED_BY = "store_created_by";
   private static final String MSG_WORKING_USERS = "store_working_users";
   private static final String MSG_SEARCH_FORM_CONTENT = "search_form_content";
   private static final String MSG_TARGET_IS_DELETED ="target_is_deleted";

   /** Component id the status messages are tied too */
   static final String COMPONENT_SANDBOXESPANEL = "sandboxes-panel";
   
   /** Top-level JSF form ID */
   static final String FORM_ID = "website";
   
   /** Snapshot date filter selection */
   private String snapshotDateFilter = UISandboxSnapshots.FILTER_DATE_TODAY;
   
   /** Current sandbox store context for actions and sandbox view */
   private String sandbox;
   
   /** Current username context for actions and sandbox view */
   private String username;
   
   /** Current webapp context for actions and sandbox view */
   private String webapp;
   
   /** List of top-level webapp directories for the current web project */
   private List<SelectItem> webapps;
   
   /** Sandbox title message */
   private String sandboxTitle = null;
   
   /** Current AVM path and node representing the current path */
   private String currentPath = null;
   private AVMNode currentPathNode = null;
   
   /** flag to indicate that all items in the sandbox are involved in the current action */
   private boolean allItemsAction = false;
   
   /** list of the deployment monitor ids currently executing */
   private List<String> deploymentMonitorIds = new ArrayList<String>();
   
   /** List of expired paths to submit */
   private List<AVMNodeDescriptor> nodesForSubmit = Collections.<AVMNodeDescriptor>emptyList();
   
   /* component references */
   private UIRichList foldersRichList;
   private UIRichList filesRichList;
   private UIUserSandboxes userSandboxes;
   
   /** transient lists of files/folders for a directory */
   private List<Map> files = null;
   private List<Map> folders = null;
   
   /** Current AVM Node action context */
   private AVMNode avmNode = null;
   
   /** The last displayed website node id */
   private String lastWebsiteId = null;
   
   /** The current webProject */
   private WebProject webProject = null;

   /** breadcrumb location */
   private List<IBreadcrumbHandler> location = null;
   
   /** Show all user sandboxes flag */
   private boolean showAllSandboxes = false;
   
   /** The current view page sizes */
   private int pageSizeFolders;
   private int pageSizeFiles;
   private String pageSizeFoldersStr;
   private String pageSizeFilesStr;
   
   /** search query string */
   private String websiteQuery;
   
   private SearchContext searchContext = null;
   
   private String searchOrigin;
   
   /** The NodeService to be used by the bean */
   transient private NodeService nodeService;
   
   /** The WorkflowService bean reference. */
   transient private WorkflowService workflowService;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** WebProjectService bean reference */
   transient protected WebProjectService wpService;
   
   /** SandboxService bean reference */
   transient protected SandboxService sbService;
   
   /** AVM service bean reference */
   transient protected AVMService avmService;
   
   /** AVM sync service bean reference */
   transient protected AVMSyncService avmSyncService;
   
   /** Action service bean reference */
   transient protected ActionService actionService;

   /** The FormsService reference */
   transient protected FormsService formsService;
   
   /** The SearchService reference */
   transient private SearchService searchService;
   
   /** The PermissionService reference */
   transient protected PermissionService permissionService;
   
   /**
    * Default Constructor
    */
   public AVMBrowseBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
      
      initFromClientConfig();
   }

   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param wpService The WebProjectService to set.
    */
   public void setWebProjectService(WebProjectService wpService)
   {
      this.wpService = wpService;
   }
   
   protected WebProjectService getWebProjectService()
   {
      if (wpService == null)
      {
          wpService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWebProjectService();
      }
      return wpService;
   }
   
   /**
    * @param sbService The SandboxService to set.
    */
   public void setSandboxService(SandboxService sbService)
   {
      this.sbService = sbService;
   }
   
   protected SandboxService getSandboxService()
   {
      if (sbService == null)
      {
          sbService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSandboxService();
      }
      return sbService;
   }
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return avmService;
   }
   
   /**
    * @param avmSyncService   The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   protected AVMSyncService getAvmSyncService()
   {
      if (avmSyncService == null)
      {
         avmSyncService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMSyncService();
      }
      return avmSyncService;
   }
   
   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * Getter used by the Inline Edit XML JSP
    * 
    * @return The NodeService
    */
   public NodeService getNodeService()
   {
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return nodeService;
   }
   
   /**
    * Set the workflow service
    * @param service The workflow service instance.
    */
   public void setWorkflowService(WorkflowService service)
   {
      workflowService = service;
   }
   
   protected WorkflowService getWorkflowService()
   {
      if (workflowService == null)
      {
         workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWorkflowService();
      }
      return workflowService;
   }
   
   /**
    * @param searchService The Searcher to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @return searchService
    */
   public SearchService getSearchService()
   {
      //check for null for cluster environment
      if (searchService == null)
      {
         searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
      }
      return searchService;
   }

   /**
    * @param permissionService The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   protected PermissionService getPermissionService()
   {
      if (permissionService == null)
      {
         permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
      }
      return permissionService;
   }

   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigationBean(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }

   protected ActionService getActionService()
   {
      if (actionService == null)
      {
         actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
   }
   
   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   protected FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }
   
   public int getPageSizeFiles()
   {
      return this.pageSizeFiles;
   }

   public void setPageSizeFiles(int pageSizeContent)
   {
      this.pageSizeFiles = pageSizeContent;
      this.pageSizeFilesStr = Integer.toString(pageSizeContent);
   }

   public int getPageSizeFolders()
   {
      return this.pageSizeFolders;
   }

   public void setPageSizeFolders(int pageSizeSpaces)
   {
      this.pageSizeFolders = pageSizeSpaces;
      this.pageSizeFoldersStr = Integer.toString(pageSizeSpaces);
   }

   public String getPageSizeFilesStr()
   {
      return this.pageSizeFilesStr;
   }

   public void setPageSizeFilesStr(String pageSizeContentStr)
   {
      this.pageSizeFilesStr = pageSizeContentStr;
   }

   public String getPageSizeFoldersStr()
   {
      return this.pageSizeFoldersStr;
   }

   public void setPageSizeFoldersStr(String pageSizeSpacesStr)
   {
      this.pageSizeFoldersStr = pageSizeSpacesStr;
   }

   /**
    * Summary text for the staging store:
    *    Created On: xx/yy/zz
    *    Created By: username
    *    There are N user(s) working on this website.
    * 
    * @return summary text
    */
   public String getStagingSummary()
   {
      StringBuilder summary = new StringBuilder(128);
      FacesContext fc = FacesContext.getCurrentInstance();
      ResourceBundle msg = Application.getBundle(fc);
      
      NodeRef wpNodeRef = getWebsite().getNodeRef();
      WebProjectInfo wpInfo = getWebProjectService().getWebProject(wpNodeRef);
      SandboxInfo sbInfo = getSandboxService().getStagingSandbox(wpInfo.getStoreId());
      
      if (sbInfo != null)
      {
         summary.append(msg.getString(MSG_CREATED_ON)).append(": ")
                .append(Utils.getDateFormat(fc).format(sbInfo.getCreatedDate()))
                .append("<p>");
         summary.append(msg.getString(MSG_CREATED_BY)).append(": ")
                .append(sbInfo.getCreator())
                .append("<p>");
         final int numUsers = getWebProjectService().getWebUserCount(wpNodeRef);
         summary.append(MessageFormat.format(msg.getString(MSG_WORKING_USERS), numUsers));
      }
      
      // reset the current path so the context for the Modified File list actions is cleared
      this.currentPath = null;
      
      return summary.toString();
   }
   
   /**
    * @return the current staging store name
    */
   public String getStagingStore()
   {
      return this.getWebProject().getStagingStore();
   }
   
   /**
    * @return Preview URL for the current Staging store and current webapp
    */
   public String getStagingPreviewUrl()
   {
       return(AVMUtil.getPreviewURI(getStagingStore(), '/' + JNDIConstants.DIR_DEFAULT_WWW + '/' + JNDIConstants.DIR_DEFAULT_APPBASE + '/' + getWebapp()));
   }
   
   /**
    * @return Preview URL for the current User Sandbox store and current webapp
    */
   public String getSandboxPreviewUrl()
   {
       return(AVMUtil.getPreviewURI(getSandbox(), '/' + JNDIConstants.DIR_DEFAULT_WWW + '/' + JNDIConstants.DIR_DEFAULT_APPBASE + '/' + getWebapp()));
   }
   
   /**
    * @param foldersRichList      The foldersRichList to set.
    */
   public void setFoldersRichList(UIRichList foldersRichList)
   {
      this.foldersRichList = foldersRichList;
   }
   
   /**
    * @return Returns the foldersRichList.
    */
   public UIRichList getFoldersRichList()
   {
      return this.foldersRichList;
   }
   
   /**
    * @return Returns the filesRichList.
    */
   public UIRichList getFilesRichList()
   {
      return this.filesRichList;
   }

   /**
    * @param filesRichList       The filesRichList to set.
    */
   public void setFilesRichList(UIRichList filesRichList)
   {
      this.filesRichList = filesRichList;
   }
   
   /**
    * @return Returns the userSandboxes.
    */
   public UIUserSandboxes getUserSandboxes()
   {
      return this.userSandboxes;
   }
   
   /**
    * @param userSandboxes       The userSandboxes to set.
    */
   public void setUserSandboxes(UIUserSandboxes userSandboxes)
   {
      this.userSandboxes = userSandboxes;
   }
   
   /**
    * @return Returns the current sandbox context.
    */
   public String getSandbox()
   {
      return this.sandbox;
   }

   /**
    * @param sandbox    The sandbox to set.
    */
   public void setSandbox(String sandbox)
   {
      this.sandbox = sandbox;
   }
   
   /**
    * @return Returns the current username context.
    */
   public String getUsername()
   {
      return this.username;
   }

   /**
    * @param username   The username to set.
    */
   public void setUsername(String username)
   {
      this.username = username;
   }
   
   /**
    * @return current webapp context
    */
   public String getWebapp()
   {
      if (this.webapp == null)
      {
         // TODO - temporary, should only be called for WCM forms (not ECM forms)
         Node wpNode = getWebsite();
         if (wpNode != null)
         {
            WebProjectInfo wpInfo = getWebProjectService().getWebProject(wpNode.getNodeRef());
            if (wpInfo != null)
            {
               this.webapp = wpInfo.getDefaultWebApp();
            }
         }
      }
      return this.webapp;
   }

   /**
    * @param webapp  Webapp folder context
    */
   public void setWebapp(String webapp)
   {
      this.webapp = webapp;
   }
   
   /**
    * @return Returns the list of deployment monitor ids currently executing
    */
   public List<String> getDeploymentMonitorIds()
   {
      return this.deploymentMonitorIds;
   }

   /**
    * @param deploymentMonitorIds Sets the list of deployment monitor ids
    */
   public void setDeploymentMonitorIds(List<String> deploymentMonitorIds)
   {
      this.deploymentMonitorIds = deploymentMonitorIds;
   }
   
   public List<AVMNodeDescriptor> getNodesForSubmit()
   {
      return this.nodesForSubmit;
   }

   public void setNodesForSubmit(final List<AVMNodeDescriptor> nodesForSubmit)
   {
      this.nodesForSubmit = nodesForSubmit;
   }

   /**
    * @return list of available root webapp folders for this Web project
    */
   public List<SelectItem> getWebapps()
   {
      if (this.webapps == null)
      {
         List<String> webAppNames = getWebProjectService().listWebApps(getWebsite().getNodeRef());
         List<SelectItem> webAppItems = new ArrayList<SelectItem>(webAppNames.size());
         for (String webAppName : webAppNames)
         {
             webAppItems.add(new SelectItem(webAppName, webAppName));
         }
         this.webapps = webAppItems;
      }
      return this.webapps;
   }
   
   /**
    * @return count of the root webapps in the current web project
    */
   public int getWebappsSize()
   {
      return getWebapps().size();
   }

   /**
    * @return Returns the sandboxTitle.
    */
   public String getSandboxTitle()
   {
      if (this.sandboxTitle == null)
      {
         String forUser = username;
         if (forUser == null)
         {
            forUser = Application.getMessage(FacesContext.getCurrentInstance(), MSG_SANDBOXSTAGING);
         }
         this.sandboxTitle = MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), MSG_SANDBOXTITLE),
               getWebsite().getName(),
               forUser);
      }
      return this.sandboxTitle;
   }

   /**
    * @param sandboxTitle The sandboxTitle to set.
    */
   public void setSandboxTitle(String sandboxTitle)
   {
      this.sandboxTitle = sandboxTitle;
   }
   
   /**
    * @return Returns the Snapshot Date Filter.
    */
   public String getSnapshotDateFilter()
   {
      return this.snapshotDateFilter;
   }

   /**
    * @param snapshotDateFilter The Snapshot Date Filter to set.
    */
   public void setSnapshotDateFilter(String snapshotDateFilter)
   {
      this.snapshotDateFilter = snapshotDateFilter;
   }
   
   /**
    * @return the website search query string
    */
   public String getWebsiteQuery()
   {
       return this.websiteQuery;
   }

   /**
    * @param websiteQuery   The website search query string
    */
   public void setWebsiteQuery(String websiteQuery)
   {
       this.websiteQuery = websiteQuery;
   }

   /**
    * @return icon image for the appropriate sandbox type
    */
   public String getIcon()
   {
      return this.username == null ? WebResources.IMAGE_SANDBOX_32 : WebResources.IMAGE_USERSANDBOX_32;
   }
   
   /**
    * @return website node the view is currently within
    */
   public Node getWebsite()
   {
      // check to see if the website we are browsing has changed since the last time
      final Node currentNode = this.navigator.getCurrentNode();
      if (!this.navigator.getCurrentNodeId().equals(this.lastWebsiteId) ||
          !WCMAppModel.TYPE_AVMWEBFOLDER.equals(currentNode.getType()))
      {
         // clear context when we are browsing a new website
         this.lastWebsiteId = this.navigator.getCurrentNodeId();
         this.webapp = null;
         this.webapps = null;
         this.webProject = null;
      }
      return WCMAppModel.TYPE_AVMWEBFOLDER.equals(currentNode.getType()) ? currentNode : null;
   }

   /**
    * @return the web project the view is currently within
    */
   public WebProject getWebProject()
   {
      Node website = this.getWebsite();
      if (this.webProject == null && website != null)
      {
         this.webProject = new WebProject(website.getNodeRef());
      }
      return this.webProject;
   }
   
   /**
    * @return Returns the current AVM node action context.
    */
   public AVMNode getAvmActionNode()
   {
      return this.avmNode;
   }

   /**
    * @param avmNode       The AVM node action context to set.
    */
   public void setAvmActionNode(AVMNode avmNode)
   {
      this.avmNode = avmNode;
   }
   
   /**
    * @param avmRef        The AVMNodeDescriptor action context to set.
    */
   public void setAVMActionNodeDescriptor(AVMNodeDescriptor avmRef)
   {
      if (avmRef == null)
      {
         throw new NullPointerException();
      }
      AVMNode avmNode = new AVMNode(avmRef);
      this.avmNode = avmNode;
   }
   
   /**
    * @return the internal AVM path to the current folder for browsing
    */
   public String getCurrentPath()
   {
      if (this.currentPath == null)
      {
         // TODO - temporary, should only be called for WCM forms (not ECM forms)
         String webApp = getWebapp();
         if (webApp != null)
         {
            this.currentPath = AVMUtil.buildStoreWebappPath(getSandbox(), webApp);
         }     
      }
      return this.currentPath;
   }
   
   /**
    * @param path       the internal AVM path to the current folder for browsing
    */
   public void setCurrentPath(String path)
   {
      this.currentPath = path;
      if (path == null)
      {
         // clear dependant objects (recreated when the path is reinitialised) 
         this.currentPathNode = null; 
      }
      
      // clear the search context as we have navigated to a folder path
      this.searchContext = null;
      
      // update UI state ready for screen refresh
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }
   
   /**
    * @return the AVMNode that represents the current browsing path
    */
   public AVMNode getCurrentPathNode()
   {
      if (this.currentPathNode == null)
      {
         AVMNodeDescriptor node = getAvmService().lookup(-1, getCurrentPath(), true);
         this.currentPathNode = new AVMNode(node);
      }
      return this.currentPathNode;
   }
   
   /**
    * @return Breadcrumb location list
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (this.location == null)
      {
         List<IBreadcrumbHandler> loc = new ArrayList<IBreadcrumbHandler>(8);
         loc.add(new AVMBreadcrumbHandler(getCurrentPath()));
         
         this.location = loc;
      }
      return this.location;
   }
   
   /**
    * @param location Breadcrumb location list
    */
   public void setLocation(List<IBreadcrumbHandler> location)
   {
      this.location = location;
   }
   
      /**
    * @return true if the current node has a custom view available
    */
   public boolean getHasCustomView()
   {
      return getHasWebscriptView() || getHasTemplateView();
   }
   
   /**
    * @return true if the current node has a Template based custom view available
    */
   public boolean getHasTemplateView()
   {
      AVMNode node = getCurrentPathNode();
      if (node.hasAspect(ContentModel.ASPECT_TEMPLATABLE))
      {
         NodeRef templateRef = (NodeRef)node.getProperties().get(ContentModel.PROP_TEMPLATE);
         return (templateRef != null && this.getNodeService().exists(templateRef) &&
                 getPermissionService().hasPermission(templateRef, PermissionService.READ) == AccessStatus.ALLOWED);
      }
      return false;
   }
   
   /**
    * @return true if the current node has a Webscript based custom view available
    */
   public boolean getHasWebscriptView()
   {
      AVMNode node = getCurrentPathNode();
      if (node.hasAspect(ContentModel.ASPECT_WEBSCRIPTABLE))
      {
         return (node.getProperties().get(ContentModel.PROP_WEBSCRIPT) != null);
      }
      return false;
   }
   
   /**
    * @return the NodeRef.toString() for the current node Template custom view if it has one 
    */
   public String getCurrentNodeTemplate()
   {
      NodeRef ref = (NodeRef)getCurrentPathNode().getProperties().get(ContentModel.PROP_TEMPLATE);
      return ref != null ? ref.toString() : null;
   }
   
   /**
    * @return the service url for the current node Webscript custom view if it has one 
    */
   public String getCurrentNodeWebscript()
   {
      return (String)getCurrentPathNode().getProperties().get(ContentModel.PROP_WEBSCRIPT);
   }
   
   /**
    * Returns a model for use by a template on a space Dashboard page.
    * 
    * @return model containing current current space info.
    */
   @SuppressWarnings("unchecked")
   public Map getTemplateModel()
   {
      HashMap model = new HashMap(4, 1.0f);
      
      model.put("space", getCurrentPathNode().getNodeRef());
      model.put("path", getCurrentPathNode().getPath());
      model.put(TemplateService.KEY_IMAGE_RESOLVER, 
            new TemplateImageResolver() 
            {
               public String resolveImagePathForName(String filename, FileTypeImageSize size)
               {
                  return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
               }
            });
      
      return model;
   }
   
   public Map getCustomWebscriptContext()
   {
       HashMap model = new HashMap(2, 1.0f);
       model.put("path", getCurrentPathNode().getPath());
       return model;
   }
   
   /**
    * @return true if the current user has the manager role in the current website
    */
   public boolean getIsManagerRole()
   {
      Node wpNode = getWebsite();
      if (wpNode != null && nodeService.exists(wpNode.getNodeRef()))
      {
         return getWebProjectService().isContentManager(wpNode.getNodeRef());
      }
      return false;
   }
   
   public boolean getIsManagerOrPublisherRole()
   {
      Node wpNode = getWebsite();
      if (wpNode != null && nodeService.exists(wpNode.getNodeRef()))
      {
         User user = Application.getCurrentUser(FacesContext.getCurrentInstance());
         String userRole = getWebProjectService().getWebUserRole(wpNode.getNodeRef(), user.getUserName());
         return (WCMUtil.ROLE_CONTENT_MANAGER.equals(userRole) || WCMUtil.ROLE_CONTENT_PUBLISHER.equals(userRole));
      }
      return false;
   }
   
   /**
    * @return true to show all sandboxes visible to this user, false to only show the current user sandbox
    */
   public boolean getShowAllSandboxes()
   {
      return this.showAllSandboxes;
   }
   
   /**
    * @param value  true to show all sandboxes visible to this user, false to only show the current user sandbox
    */
   public void setShowAllSandboxes(boolean value)
   {
      this.showAllSandboxes = value;
   }
   
   /**
    * @return true if the website has had a deployment attempt
    */
   @SuppressWarnings("unchecked")
   public boolean getHasDeployBeenAttempted()
   {
      // NOTE: This method is called a lot as it is referenced as a value binding
      //       expression in a 'rendered' attribute, we therefore cache the result
      //       on a per request basis
      
      Boolean result = null;
      
      FacesContext context = FacesContext.getCurrentInstance();
      Map request = context.getExternalContext().getRequestMap();
      if (request.get(REQUEST_BEEN_DEPLOYED_RESULT) == null)
      {
         if (!nodeService.exists(this.getWebsite().getNodeRef()))
         {
            result = false;
         }
         else
         {
            // see if there are any deployment attempts for the staging area
            NodeRef webProjectRef = this.getWebsite().getNodeRef();
            String store = (String)getNodeService().getProperty(webProjectRef, 
                  WCMAppModel.PROP_AVMSTORE);
            List<NodeRef> deployAttempts = DeploymentUtil.findDeploymentAttempts(store);
            
            // add a placeholder object in the request so we don't evaluate this again for this request
            result = new Boolean(deployAttempts != null && deployAttempts.size() > 0);
         }
         request.put(REQUEST_BEEN_DEPLOYED_RESULT, result);
      }
      else
      {
         result = (Boolean)request.get(REQUEST_BEEN_DEPLOYED_RESULT);
      }
      
      return result.booleanValue();
   }
   
   /**
    * @return the Search Context object for the current website project
    */
   public SearchContext getSearchContext()
   {
      return this.searchContext;
   }

   /**
    * @return Map of avm node objects representing the folders with the current website space
    */
   public List<Map> getFolders()
   {
      if (this.folders == null)
      {
         if (this.searchContext == null)
         {
            buildDirectoryNodes();
         }
         else
         {
            buildSearchNodes();
         }
      }
      return this.folders;
   }
   
   /**
    * @return Map of avm node objects representing the files with the current website space
    */
   public List<Map> getFiles()
   {
      if (this.files == null)
      {
         if (this.searchContext == null)
         {
            buildDirectoryNodes();
         }
         else
         {
            buildSearchNodes();
         }
      }
      return this.files;
   }
   
   /**
    * Build the lists of files and folders within the current browsing path in a website space
    */
   private void buildDirectoryNodes()
   {
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         Map<String, AVMNodeDescriptor> nodes = getAvmService().getDirectoryListing(-1, getCurrentPath());
         this.files = new ArrayList<Map>(nodes.size());
         this.folders = new ArrayList<Map>(nodes.size());
         for (String name : nodes.keySet())
         {
            AVMNodeDescriptor avmRef = nodes.get(name);
            
            // build and add the client representation of the AVM node
            addAVMNodeResult(avmRef);
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         this.folders = Collections.<Map>emptyList();
         this.files = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Build the lists of files and folders from the current search context in a website space
    */
   private void buildSearchNodes()
   {
      String query = this.searchContext.buildQuery(getMinimumSearchLength());
      if (query == null)
      {
         this.folders = Collections.<Map>emptyList();
         this.files = Collections.<Map>emptyList();
         return;
      }

      UserTransaction tx = null;
      ResultSet results = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         // build up the search parameters
         SearchParameters sp = new SearchParameters();
         sp.setLanguage(SearchService.LANGUAGE_LUCENE);
         sp.setQuery(query);
         // add the Staging Store for this website - it is the only searchable store for now
         sp.addStore(new StoreRef(StoreRef.PROTOCOL_AVM, getStagingStore()));
         
         // limit search results size as configured
         int searchLimit = Application.getClientConfig(context).getSearchMaxResults();
         if (searchLimit > 0)
         {
            sp.setLimitBy(LimitBy.FINAL_SIZE);
            sp.setLimit(searchLimit);
         }
         
         results = getSearchService().query(sp);
         
         if (logger.isDebugEnabled())
         {
            logger.debug("Search results returned: " + results.length());
         }
         
         // filter hidden folders above the web app
         boolean isStagingStore = getIsStagingStore();
         int sandboxPathLength = AVMUtil.getSandboxPath(getCurrentPath()).length();
         
         this.files = new ArrayList<Map>(results.length());
         this.folders = new ArrayList<Map>(results.length());
         for (ResultSetRow row : results)
         {
            NodeRef nodeRef = row.getNodeRef();
            
            // Modify the path to point to the current user sandbox - this change is performed so
            // that any action permission evaluators will correctly resolve for the current user.
            // Therefore deleted node will be filtered out by the lookup() call, but some text based
            // results may be incorrect - however a note is provided in the search UI to indicate this.
            String path = AVMNodeConverter.ToAVMVersionPath(nodeRef).getSecond();
            if (isStagingStore == false)
            {
               path = getSandbox() + ':' + AVMUtil.getStoreRelativePath(path);
            }
            if (path.length() > sandboxPathLength)
            {
               AVMNodeDescriptor avmRef = getAvmService().lookup(-1, path);
               if (avmRef != null)
               {
                  AVMNode node = addAVMNodeResult(avmRef);
                  
                  // add extra properties for search results lists
                  node.addPropertyResolver("displayPath", AVMNode.RESOLVER_DISPLAY_PATH);
                  node.addPropertyResolver("parentPath", AVMNode.RESOLVER_PARENT_PATH);
               }
            }
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         this.folders = Collections.<Map>emptyList();
         this.files = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
   }
   
   /**
    * Add an AVM node result to the list of files/folders to display. Applies various
    * client pseudo properties resolver objects used for list display columns.
    */
   private AVMNode addAVMNodeResult(AVMNodeDescriptor avmRef)
   {
      // build the client representation of the AVM node
      AVMNode node = new AVMNode(avmRef);
      
      // properties specific to folders or files
      if (avmRef.isDirectory())
      {
         node.getProperties().put("smallIcon", BrowseBean.SPACE_SMALL_DEFAULT);
         
         String type = "";
         if (avmRef.getType() == AVMNodeType.LAYERED_DIRECTORY && avmRef.isPrimary())
         {
            if ((getAvmService().lookup(avmRef.getIndirectionVersion(), avmRef.getIndirection()) != null) || (avmRef.getOpacity()))
            {
               type = Application.getMessage(FacesContext.getCurrentInstance(), "shared_folder");
            }
            else
            {
               type = Application.getMessage(FacesContext.getCurrentInstance(), "stale_shared_folder");
            }
         }
         else
         {
            type = Application.getMessage(FacesContext.getCurrentInstance(), "folder");
         }
         node.getProperties().put("folderType", type);
         
         this.folders.add(node);
      }
      else
      {
         String type = "";
         if (avmRef.isLayeredFile())
         {
            if (getAvmService().lookup(avmRef.getIndirectionVersion(), avmRef.getIndirection()) != null)
            {
               type = Application.getMessage(FacesContext.getCurrentInstance(), "shared_file");
            }
            else
            {
               type = Application.getMessage(FacesContext.getCurrentInstance(), "stale_shared_file");
            } 
         }
         else
         {
            type = Application.getMessage(FacesContext.getCurrentInstance(), "file");
         }
         
         node.getProperties().put("fileType", type);
         node.getProperties().put("fileType16", FileTypeImageUtils.getFileTypeImage(avmRef.getName(), true));
         node.getProperties().put("url", DownloadContentServlet.generateBrowserURL(
               AVMNodeConverter.ToNodeRef(-1, avmRef.getPath()), avmRef.getName()));
         
         this.files.add(node);
      }
      
      // common properties
      node.addPropertyResolver("previewUrl", AVMNode.RESOLVER_PREVIEW_URL);
      
      return node;
   }
   
   /**
    * Return the list of selected items for the current user sandbox view
    * 
    * @return List of AVMNodeDescriptor objects representing selected items
    */
   public List<AVMNodeDescriptor> getSelectedSandboxItems()
   {
      return this.userSandboxes.getSelectedNodes(this.username);
   }
   
   /**
    * @return true if a special All Items action has been initialised
    */
   public boolean getAllItemsAction()
   {
      return this.allItemsAction;
   }
   
   /**
    * @return true if the current sandbox is a Staging store, false otherwise
    */
   public boolean getIsStagingStore()
   {
      return AVMUtil.isMainStore(this.sandbox);
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Update the UI after a folder click action in the website browsing screens
    */
   public void clickFolder(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String path = params.get("id");
      AVMNodeDescriptor avmNode = getAvmService().lookup(-1, path);

      if (avmNode.isLayeredDirectory() && avmNode.isPrimary() && 
          (getAvmService().lookup(avmNode.getIndirectionVersion(), avmNode.getIndirection()) == null) &&
          (! avmNode.getOpacity()) &&
          getAvmService().getDirectoryListingDirect(avmNode, false).isEmpty())
      {
         String pattern = Application.getMessage(FacesContext.getCurrentInstance(), MSG_TARGET_IS_DELETED);
         String folderName = path.substring(path.lastIndexOf("/") + 1);
         Utils.addErrorMessage(MessageFormat.format(pattern, folderName));
      }
      else
      {
         updateUILocation(path);
      }
   }

   /**
    * Action handler called when the Snapshot Date filter is changed by the user
    */
   public void snapshotDateFilterChanged(ActionEvent event)
   {
      UIModeList filterComponent = (UIModeList)event.getComponent();
      setSnapshotDateFilter(filterComponent.getValue().toString());
   }
   
   /**
    * Setup the context for a sandbox browse action
    */
   public void setupSandboxAction(final ActionEvent event)
   {
      final UIActionLink link = (UIActionLink)event.getComponent();
      final Map<String, String> params = link.getParameterMap();
      this.setupSandboxAction(params.get("store"), params.get("username"));
   }
   
   /**
    * Setup the context for a sandbox browse action
    * 
    * @param store The store name for the action
    * @param username The authority pertinent to the action (null for staging store actions) 
    */
   public void setupSandboxAction(String store, String username)
   {
      this.setupSandboxActionImpl(store, null, username, true);
   }

   /**
    * Setup the context for a sandbox browse action
    * 
    * @param store      The store name for the action
    * @param username   The authority pertinent to the action (null for staging store actions)
    * @param reset      True to reset the current path and AVM action node context
    */
   private void setupSandboxActionImpl(final String store,
                                       final String webapp,
                                       final String username, 
                                       final boolean reset)
   {
      // can be null if it's the staging store - i.e. not a user specific store
      setUsername(username);
      
      // the store can be either a user store or the staging store if this is null
      // get the staging store from the current website node
      this.setSandbox(store != null ? store : this.getStagingStore());

      if (webapp != null)
      {
         this.setWebapp(webapp);
      }
      
      // update UI state ready for return to the previous screen
      if (reset == true)
      {
         this.sandboxTitle = null;
         this.location = null;
         setCurrentPath(null);
         setAvmActionNode(null);
         this.allItemsAction = false;
         this.searchOrigin = null;
      }
      
      this.websiteQuery = null;
   }
   
   /**
    * Action event called by all actions that need to setup a Content node context on the 
    * before an action page/wizard is called. The context will be an AVMNodeDescriptor in
    * setAVMNode() which can be retrieved on action pages via getAVMNode().
    * 
    * @param event   ActionEvent
    */
   public void setupContentAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      this.setupContentAction(params.get("id"), true);
   }
   
   /*package*/ void setupContentAction(String path, boolean refresh)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Setup content action for path: " + path);
      }
      
      if (path == null || path.length() == 0)
      {
         setAvmActionNode(null);
      }
      else
      {
         // calculate username and store name from specified path
         String storeName = AVMUtil.getStoreName(path);
         String storeId = AVMUtil.getStoreId(storeName);
         String webapp = AVMUtil.getWebapp(path);
         String username = AVMUtil.getUserName(storeName);
         if (username == null)
         {
            storeName = (AVMUtil.isPreviewStore(storeName)
                         ? AVMUtil.buildStagingPreviewStoreName(storeId)
                         : AVMUtil.buildStagingStoreName(storeId));
            this.setupSandboxActionImpl(storeName, webapp, null, false);
         }
         else
         {
            storeName = (AVMUtil.isPreviewStore(storeName)
                         ? AVMUtil.buildUserPreviewStoreName(storeId, username)
                         : AVMUtil.buildUserMainStoreName(storeId, username));
            this.setupSandboxActionImpl(storeName, webapp, username, false);
         }

         if (this.webProject == null)
         {
            this.webProject = new WebProject(path);
         }
         
         // setup the action node
         AVMNodeDescriptor avmNodeDesc = getAvmService().lookup(-1, path, true);
         this.setAVMActionNodeDescriptor(avmNodeDesc);
      }
      
      // update UI state ready for return after dialog close
      if (refresh)
      {
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
   }
   
   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void setupEditAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      this.setupEditAction(params.get("id"));
   }
   
   /**
    * Action handler called to calculate which editing screen to display based on the mimetype
    * of a document. If appropriate, the in-line editing screen will be shown.
    */
   public void setupEditAction(String path)
   {
      this.setupContentAction(path, true);
      
      // retrieve the content reader for this node
      String avmPath = this.getAvmActionNode().getPath();
      if (getAvmService().hasAspect(-1, avmPath, WCMAppModel.ASPECT_RENDITION))
      {
         if (logger.isDebugEnabled())
         {
            logger.debug(avmPath + " is a rendition, editing primary rendition instead");
         }
         
         try
         {
            final FormInstanceData fid = this.getFormsService().getRendition(-1, avmPath).getPrimaryFormInstanceData();
            avmPath = fid.getPath();
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Editing primary form instance data " + avmPath);
            }
            
            this.setAvmActionNode(new AVMNode(getAvmService().lookup(-1, avmPath)));
         }
         catch (IllegalArgumentException iae)
         {
            //Utils.addErrorMessage(iae.getMessage(), iae);
            logger.warn(iae);
         }
         catch (FileNotFoundException fnfe)
         {
            //Utils.addErrorMessage(fnfe.getMessage(), fnfe);
            logger.warn(fnfe);
         }
      }
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Editing AVM node: " + avmPath);
      }
      
      String outcome = null;
      // calculate which editor screen to display
      if (getAvmService().hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         // make content available to the editing screen
         try
         {
            // make sure the form association works before proceeding to the
            // edit web content wizard
            this.getFormsService().getFormInstanceData(-1, avmPath).getForm();
            // navigate to appropriate screen
            outcome = "wizard:editWebContent";
         }
         catch (FormNotFoundException fnfe)
         {
            //Utils.addErrorMessage(fnfe.getMessage(), fnfe);
            logger.warn(fnfe);
            
            final Map<String, String> params = new HashMap<String, String>(2, 1.0f);
            params.put("finishOutcome", "wizard:editWebContent");
            params.put("cancelOutcome", "dialog:editAvmFile");
            Application.getDialogManager().setupParameters(params);
            
            outcome = "dialog:promptForWebForm";
         }
      }
      else
      {
         // normal downloadable document
         outcome = "dialog:editAvmFile";
         
         // we need to mark the file as modified so it gets a lock
         this.getAvmService().forceCopy(avmPath);
      }
      
      if (logger.isDebugEnabled())
      {
          logger.debug("outcome " + outcome + " for path " + path);
      }
      
      FacesContext fc = FacesContext.getCurrentInstance();
      fc.getApplication().getNavigationHandler().handleNavigation(fc, null, outcome);
   }
   
   /**
    * Action handler for all nodes from user sandbox
    */
   public void setupAllItemsAction(ActionEvent event)
   {
      setupSandboxAction(event);
      this.allItemsAction = true;
   }
   
   /**
    * Refresh Sandbox in the virtualisation server
    */
   public void refreshSandbox(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String store = params.get("store");
      
      if (store == null)
      {
         store = getStagingStore();
      }
      
      // update the specified webapp in the store
      String webappPath = AVMUtil.buildStoreWebappPath(store, getWebapp());
      AVMUtil.updateVServerWebapp(webappPath, true);
   }
   
   /**
    * Undo changes to a single node
    */
   public void revertNode(ActionEvent event)
   {
      String avmPath = getPathFromEventArgs(event);
      String sbStoreId = WCMUtil.getSandboxStoreId(avmPath);
      List<String> namesForDisplayMsg = new LinkedList<String>();
      UserTransaction tx = null;
      final FacesContext context = FacesContext.getCurrentInstance();
      try
      {
         tx = Repository.getUserTransaction(context, false);
         tx.begin();
         
         AVMNodeDescriptor node = getAvmService().lookup(-1, avmPath, true);
         if (node != null)
         {
            FormInstanceData fid = null;
            if (getAvmService().hasAspect(-1, avmPath, WCMAppModel.ASPECT_RENDITION))
            {
               fid = this.getFormsService().getRendition(-1, avmPath).getPrimaryFormInstanceData();
            }
            else if (getAvmService().hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
            {
               fid = this.getFormsService().getFormInstanceData(-1, avmPath);
            }
            List<String> paths = new ArrayList<String>();
            if (fid != null)
            {
               paths.add(WCMUtil.getStoreRelativePath(fid.getPath()));
               namesForDisplayMsg.add(fid.getName());
               for (Rendition r : fid.getRenditions())
               {
                  paths.add(WCMUtil.getStoreRelativePath(r.getPath()));
                  namesForDisplayMsg.add(r.getName());
               }
            }
            else
            {
               paths.add(WCMUtil.getStoreRelativePath(avmPath));
               namesForDisplayMsg.add(node.getName());
            }
            
            getSandboxService().revertList(sbStoreId, paths);
         }
         
         // commit the transaction
         tx.commit();
         
         // if we get here, all was well - output friendly status message to the user
         this.displayStatusMessage(context,
                                   MessageFormat.format(Application.getMessage(context, MSG_REVERT_SUCCESS), 
                                                        StringUtils.join(namesForDisplayMsg.toArray(), ", "),
                                                        namesForDisplayMsg.size()));
      }
      catch (Throwable err)
      {
         err.printStackTrace(System.err);
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_GENERIC), 
                                                    err.getMessage()), 
                               err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Revert a sandbox to a specific snapshot version ID
    */
   public void revertSnapshot(Map<String, String> params)
   {
      String sandbox = params.get("sandbox");
      String strVersion = params.get("version");
      if (sandbox != null && strVersion != null && strVersion.length() != 0)
      {
         try
         {
            getSandboxService().revertSnapshot(sandbox, Integer.valueOf(strVersion));
            FacesContext context = FacesContext.getCurrentInstance();
            
            // if we get here, all was well - output friendly status message to the user
            this.displayStatusMessage(context, 
                                      MessageFormat.format(Application.getMessage(context, MSG_REVERT_SANDBOX), 
                                                           sandbox, 
                                                           strVersion));
         }
         catch (Throwable err)
         {
            err.printStackTrace(System.err);
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         }
      }
   }
   
   /**
    * Create web content from a specific Form via the User Sandbox 'Available Forms' panel
    */
   public void createFormContent(ActionEvent event)
   {
      // setup the correct sandbox for the create action
      this.setupSandboxAction(event);
      
      // pass form ID to the wizard - to be picked up in init()
      Application.getWizardManager().setupParameters(event);
      final FacesContext fc = FacesContext.getCurrentInstance();
      fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "wizard:createWebContent");
   }
   
   /**
    * Perform a canned search for previously generated Form content 
    */
   public void searchFormContent(ActionEvent event)
   {
      // setup the correct sandbox for the canned search
      this.setupSandboxAction(event);
      
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String formName = params.get(UIUserSandboxes.PARAM_FORM_NAME);
      
      StringBuilder query = new StringBuilder(256);
      query.append("+ASPECT:\"").append(WCMAppModel.ASPECT_FORM_INSTANCE_DATA).append("\"");
      query.append(" -ASPECT:\"").append(WCMAppModel.ASPECT_RENDITION).append("\"");
      query.append(" +@").append(Repository.escapeQName(WCMAppModel.PROP_PARENT_FORM_NAME))
           .append(":\"").append(formName).append("\"");
      FormSearchContext searchContext = new FormSearchContext();
      searchContext.setCannedQuery(query.toString(), formName);
      
      // set the search context - when the view is refreshed, this will be detected and
      // the search results mode of the AVM Browse screen will be displayed 
      this.searchContext = searchContext;
      
      // set the search origin so that when the search is closed we know
      // to go back to the website view and not the browse view (WCM-1007)
      this.searchOrigin = "formContent";
   }
   
   /**
    * Event handler that transitions a 'submitpending' task to effectively
    * bypass the lauch date and immediately submit the items.
    * 
    * @param event The event
    */
   public void promotePendingSubmission(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String taskId = params.get("taskId");
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, false);
         tx.begin();

         // transition the task
         this.getWorkflowService().endTask(taskId, "launch");
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Event handler that cancels a pending submission.
    * 
    * @param event The event
    */
   public void cancelPendingSubmission(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String workflowId = params.get("workflowInstanceId");
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, false);
         tx.begin();

         // cancel the workflow
         this.getWorkflowService().cancelWorkflow(workflowId);
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Update page size based on user selection
    */
   public void updateFoldersPageSize(ActionEvent event)
   {
      try
      {
         int size = Integer.parseInt(this.pageSizeFoldersStr);
         if (size >= 0)
         {
            this.pageSizeFolders = size;
         }
         else
         {
            // reset to known value if this occurs
            this.pageSizeFoldersStr = Integer.toString(this.pageSizeFolders);
         }
      }
      catch (NumberFormatException err)
      {
         // reset to known value if this occurs
         this.pageSizeFoldersStr = Integer.toString(this.pageSizeFolders);
      }
   }

   /**
    * Update page size based on user selection
    */
   public void updateFilesPageSize(ActionEvent event)
   {
      try
      {
         int size = Integer.parseInt(this.pageSizeFilesStr);
         if (size >= 0)
         {
            this.pageSizeFiles = size;
         }
         else
         {
            // reset to known value if this occurs
            this.pageSizeFilesStr = Integer.toString(this.pageSizeFiles);
         }
      }
      catch (NumberFormatException err)
      {
         // reset to known value if this occurs
         this.pageSizeFilesStr = Integer.toString(this.pageSizeFiles);
      }
   }
   
   /**
    * Perform a lucene search of the website
    */
   public void searchWebsite(ActionEvent event)
   {
      if (this.websiteQuery != null && this.websiteQuery.length() != 0)
      {
         SearchContext searchContext = new SearchContext();
         
         searchContext.setText(this.websiteQuery);
         
         // set the search context - when the view is refreshed, this will be detected and
         // the search results mode of the AVM Browse screen will be displayed 
         this.searchContext = searchContext;
         
         resetFileFolderLists();

         if (searchContext.buildQuery(getMinimumSearchLength()) == null)
         {
            // failed to build a valid query, the user probably did not enter the
            // minimum text required to construct a valid search
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                  BrowseBean.MSG_SEARCH_MINIMUM), new Object[] {getMinimumSearchLength()}));
         }
      }
   }
   
   /**
    * Action called to Close the search dialog by returning to the last viewed path
    */
   public void closeSearch(ActionEvent event)
   {
      this.searchContext = null;
      resetFileFolderLists();
      
      if (this.searchOrigin != null)
      {
         // if the search was from elsewhere navigate back to the website view
         this.searchOrigin = null;
         
         FacesContext fc = FacesContext.getCurrentInstance();
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "browseWebsite");
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Display a status message to the user
    * 
    * @param context
    * @param msg        Text message to display
    */
   /*package*/ void displayStatusMessage(FacesContext context, String msg)
   {
      FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
      context.addMessage(FORM_ID + ':' + COMPONENT_SANDBOXESPANEL, facesMsg);
   }
   
   /*package*/ boolean isCurrentPathNull()
   {
      return (this.currentPath == null);
   }
   
   /**
    * Initialise default values from client configuration
    */
   private void initFromClientConfig()
   {
      ConfigService config = Application.getConfigService(FacesContext.getCurrentInstance());
      ConfigElement wcmConfig = config.getGlobalConfig().getConfigElement("wcm");
      if (wcmConfig != null)
      {
         ConfigElement viewsConfig = wcmConfig.getChild("views");
         if (viewsConfig != null)
         {
            ConfigElement pageConfig = viewsConfig.getChild("browse-page-size");
            if (pageConfig != null)
            {
               String strPageSize = pageConfig.getValue();
               if (strPageSize != null)
               {
                  int pageSize = Integer.valueOf(strPageSize.trim());
                  setPageSizeFiles(pageSize);
                  setPageSizeFolders(pageSize);
               }
            }
         }
      }
   }
   
   /**
    * Update the breadcrumb with the clicked Folder path location
    */
   private void updateUILocation(String path)
   {
      // fully update the entire breadcrumb path - i.e. do not append as may
      // have navigated from deeper sub-folder (search results)
      int sandboxPathLength = AVMUtil.getSandboxPath(path).length();
      String currentPath = path;
      this.location.clear();
      while (currentPath.length() != sandboxPathLength)
      {
         this.location.add(new AVMBreadcrumbHandler(currentPath));
         currentPath = AVMNodeConverter.SplitBase(currentPath)[0];
      }
      Collections.reverse(this.location);
      
      setCurrentPath(path);
   }
   
   /**
    * @return the path from the 'id' argument in the specified UIActionLink event
    */
   private String getPathFromEventArgs(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      return params.get("id");
   }
   
   /**
    * @return Returns the minimum length of a valid search string.
    */
   public static int getMinimumSearchLength()
   {
      return Application.getClientConfig(FacesContext.getCurrentInstance()).getSearchMinimum();
   }
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation 
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      resetFileFolderLists();
      
      // clear webapp listing as we may have returned from the Create Webapp dialog
      this.webapps = null;
   }

   private void resetFileFolderLists()
   {
      if (this.foldersRichList != null)
      {
         this.foldersRichList.setValue(null);
      }
      if (this.filesRichList != null)
      {
         this.filesRichList.setValue(null);
      }
      
      this.files = null;
      this.folders = null;
      
      // reset the WebProject instance - as values may be cached that have now changed
      this.webProject = null;
   }
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#areaChanged()
    */
   public void areaChanged()
   {
      // nothing to do
   }

   /**
    * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
    */
   public void spaceChanged()
   {
      // nothing to do
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class to handle breadcrumb interaction for AVM page
    */
   @SuppressWarnings("serial")
   private class AVMBreadcrumbHandler implements IBreadcrumbHandler
   {
      private String path;
      
      AVMBreadcrumbHandler(String path)
      {
         this.path = path;
      }
      
      @SuppressWarnings("unchecked")
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         setCurrentPath(path);
         setLocation((List)breadcrumb.getValue());
         return null;
      }
      
      @Override
      public String toString()
      {
         if (AVMUtil.buildSandboxRootPath(getSandbox()).equals(path))
         {
            // don't display the 'root' webapps path as this will confuse users
            // instead display which sandbox we are in
            String label = username;
            if (label == null)
            {
               label = Application.getMessage(FacesContext.getCurrentInstance(), MSG_SANDBOXSTAGING);
            }
            return label;
         }
         else
         {
            return path.substring(path.lastIndexOf('/') + 1);
         }
      }
   }
   
   /**
    * Wrap SearchContext to allow prebuilt canned Form query to be apply as search context
    */
   public class FormSearchContext extends SearchContext
   {
      private String cannedQuery = null;
      private String formName;
      
      public void setCannedQuery(String q, String formName)
      {
         this.cannedQuery = q;
         this.formName = formName;
      }
      
      @Override
      public String buildQuery(int minimum)
      {
         return (this.cannedQuery == null ? super.buildQuery(minimum) : this.cannedQuery);
      }

      @Override
      public String getText()
      {
         return (this.cannedQuery == null ?
                     super.getText() :
                     MessageFormat.format(
                           Application.getMessage(FacesContext.getCurrentInstance(), MSG_SEARCH_FORM_CONTENT),
                           this.formName));
      }
   }
   
   /**
    * Revert All Conflicts
    * 
    * @param event
    */
   public void revertAllConflict(ActionEvent event)
   {
      final HtmlCommandButton button = (HtmlCommandButton) event.getComponent();

      List<Object> params = button.getChildren();
      String userStorePath = null;
      //String stagingStorePath = null;
      for (Object obj : params)
      {
         UIParameter uip = (UIParameter) obj;
         if (uip.getName().equals("userStorePath"))
         {
            userStorePath = (String) uip.getValue();
         }
         /*
         if (uip.getName().equals("stagingStorePath"))
         {
            stagingStore = (String) uip.getValue();
         }
         */
      }
      String[] storePath = WCMUtil.splitPath(userStorePath);
      
      List<AssetInfo> assets = sbService.listChanged(storePath[0], storePath[1], true);
      
      String sbStoreId = storePath[0];
      
      List<String> paths = new ArrayList<String>();
      for (AssetInfo asset : assets)
      {
         if (asset.getDiffCode() == AVMDifference.CONFLICT)
         {
            if (! AVMWorkflowUtil.isInActiveWorkflow(sbStoreId, asset.getPath()))
            {
               paths.add(asset.getPath());
            }
         }
      }
      
      sbService.revertList(sbStoreId, paths);
   }
}
