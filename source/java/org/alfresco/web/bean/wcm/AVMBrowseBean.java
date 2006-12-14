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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMRevertStoreAction;
import org.alfresco.repo.avm.actions.AVMUndoSandboxListAction;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
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
import org.alfresco.web.bean.wizard.WizardManager;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.wcm.WebResources;
import org.alfresco.web.ui.wcm.component.UISandboxSnapshots;
import org.alfresco.web.ui.wcm.component.UIUserSandboxes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing up the AVM specific browse screens
 * 
 * @author Kevin Roast
 */
public class AVMBrowseBean implements IContextListener
{
   private static Log logger = LogFactory.getLog(AVMBrowseBean.class);
   
   private static final String MSG_REVERT_SUCCESS = "revert_success";
   private static final String MSG_REVERTALL_SUCCESS = "revertall_success";
   private static final String MSG_REVERTSELECTED_SUCCESS = "revertselected_success";
   private static final String MSG_REVERT_SANDBOX = "revert_sandbox_success";
   private static final String MSG_SANDBOXTITLE = "sandbox_title";
   private static final String MSG_SANDBOXSTAGING = "sandbox_staging";
   private static final String MSG_CREATED_ON = "store_created_on";
   private static final String MSG_CREATED_BY = "store_created_by";
   private static final String MSG_WORKING_USERS = "store_working_users";
   private static final String MSG_SUBMIT_SUCCESS = "submit_success";
   private static final String MSG_SUBMITALL_SUCCESS = "submitall_success";
   private static final String MSG_SUBMITSELECTED_SUCCESS = "submitselected_success";
   
   /** Component id the status messages are tied too */
   private static final String COMPONENT_SANDBOXESPANEL = "sandboxes-panel";
   
   /** Top-level JSF form ID */
   private static final String FORM_ID = "browse-website";
   
   /** Content Manager role name */
   private static final String ROLE_CONTENT_MANAGER = "ContentManager";
   
   /** Snapshot date filter selection */
   private String snapshotDateFilter = UISandboxSnapshots.FILTER_DATE_TODAY;
   
   /** Current sandbox store context for actions and sandbox view */
   private String sandbox;
   
   /** Current username context for actions and sandbox view */
   private String username;
   
   /** Current webapp context for actions and sandbox view */
   private String webapp;
   
   /** Sandbox title message */
   private String sandboxTitle = null;
   
   /** Current AVM path and node representing the current path */
   private String currentPath = null;
   private AVMNode currentPathNode = null;
   
   private boolean submitAll = false;
   
   /* component references */
   private UIRichList foldersRichList;
   private UIRichList filesRichList;
   private UIUserSandboxes userSandboxes;
   
   /** transient lists of files/folders for a directory */
   private List<Map> files = null;
   private List<Map> folders = null;
   
   /** Current AVM Node action context */
   private AVMNode avmNode = null;
   
   /** breadcrumb location */
   private List<IBreadcrumbHandler> location = null;
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
      /** The DictionaryService bean reference */
   protected DictionaryService dictionaryService;
   
   /** The SearchService bean reference. */
   protected SearchService searchService;
   
   /** The NamespaceService bean reference. */
   protected NamespaceService namespaceService;
   
   /** The WorkflowService bean reference. */
   protected WorkflowService workflowService;
   
   /** The browse bean */
   protected BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** AVM service bean reference */
   protected AVMService avmService;
   
   /** AVM Sync service bean reference */
   protected AVMSyncService avmSyncService;
   
   /** Action service bean reference */
   protected ActionService actionService;
   
   /** Global exclude name matcher */
   protected NameMatcher nameMatcher;
   
   
   /**
    * Default Constructor
    */
   public AVMBrowseBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }

   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param avmSyncService   The AVMSyncService to set.
    */
   public void setAvmSyncService(AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }

   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * Set the workflow service
    * @param service The workflow service instance.
    */
   public void setWorkflowService(WorkflowService service)
   {
      workflowService = service;
   }
   
   /**
    * Getter used by the Inline Edit XML JSP
    * 
    * @return The NodeService
    */
   public NodeService getNodeService()
   {
      return this.nodeService;
   }

   /**
    * @param dictionaryService The DictionaryService to set.
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }
   
   /**
    * @param searchService The SearchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * @param namespaceService The NamespaceService to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }

   /**
    * Sets the BrowseBean instance to use to retrieve the current document
    * 
    * @param browseBean BrowseBean instance
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
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

   /**
    * @param nameMatcher The nameMatcher to set.
    */
   public void setNameMatcher(NameMatcher nameMatcher)
   {
      this.nameMatcher = nameMatcher;
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
      Node websiteNode = this.navigator.getCurrentNode();
      String storeRoot = (String)websiteNode.getProperties().get(WCMAppModel.PROP_AVMSTORE);
      String stagingStore = getStagingStore();
      AVMStoreDescriptor store = this.avmService.getAVMStore(stagingStore);
      if (store != null)
      {
         // count user stores
         int users = avmService.queryStoresPropertyKeys(QName.createQName(null,
               AVMConstants.PROP_SANDBOX_STORE_PREFIX + storeRoot + "-%" + AVMConstants.STORE_MAIN)).size();
         summary.append(msg.getString(MSG_CREATED_ON)).append(": ")
                .append(Utils.getDateFormat(fc).format(new Date(store.getCreateDate())))
                .append("<p>");
         summary.append(msg.getString(MSG_CREATED_BY)).append(": ")
                .append(store.getCreator())
                .append("<p>");
         summary.append(MessageFormat.format(msg.getString(MSG_WORKING_USERS), users));
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
      Node websiteNode = this.navigator.getCurrentNode();
      String storeRoot = (String)websiteNode.getProperties().get(WCMAppModel.PROP_AVMSTORE);
      return AVMConstants.buildAVMStagingStoreName(storeRoot);
   }
   
   /**
    * @return Preview URL for the current Staging store and current webapp
    */
   public String getStagingPreviewUrl()
   {
      return AVMConstants.buildAVMWebappUrl(getStagingStore(), getWebapp());
   }
   
   /**
    * @return Preview URL for the current User Sandbox store and current webapp
    */
   public String getSandboxPreviewUrl()
   {
      return AVMConstants.buildAVMWebappUrl(getSandbox(), getWebapp());
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
         this.webapp = (String)getWebsite().getProperties().get(WCMAppModel.PROP_DEFAULTWEBAPP);
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
    * @return list of available root webapp folders for this Web project
    */
   public List<SelectItem> getWebapps()
   {
      String path = AVMConstants.buildAVMStoreRootPath(getStagingStore());
      Map<String, AVMNodeDescriptor> folders = this.avmService.getDirectoryListing(-1, path);
      List<SelectItem> webapps = new ArrayList<SelectItem>(folders.size());
      for (AVMNodeDescriptor node : folders.values())
      {
         webapps.add(new SelectItem(node.getName(), node.getName()));
      }
      return webapps;
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
               this.navigator.getCurrentNode().getName(),
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
      return this.navigator.getCurrentNode();
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
         this.currentPath = AVMConstants.buildAVMStoreWebappPath(getSandbox(), getWebapp());
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
         AVMNodeDescriptor node = this.avmService.lookup(-1, getCurrentPath(), true);
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
    * @return true if the current user has the manager role in the current website
    */
   public boolean getIsManagerRole()
   {
      boolean isManager = false;
      
      User user = Application.getCurrentUser(FacesContext.getCurrentInstance());
      if (user.isAdmin() == false)
      {
         String currentUser = user.getUserName();
         Node websiteNode = this.navigator.getCurrentNode();
         List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(
               websiteNode.getNodeRef(), WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
         for (ChildAssociationRef ref : userInfoRefs)
         {
            NodeRef userInfoRef = ref.getChildRef();
            String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
            String userrole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
            if (currentUser.equals(username) && ROLE_CONTENT_MANAGER.equals(userrole))
            {
               isManager = true;
               break;
            }
         }
      }
      else
      {
         isManager = true;
      }
      
      return isManager;
   }
   
   /**
    * @return Map of avm node objects representing the folders with the current website space
    */
   public List<Map> getFolders()
   {
      if (this.folders == null)
      {
         getNodes();
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
         getNodes();
      }
      return this.files;
   }
   
   /**
    * Build the lists of files and folders within the current browsing path in a website space
    */
   private void getNodes()
   {
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         String dns = AVMConstants.lookupStoreDNS(getSandbox());
         int rootPathIndex = AVMConstants.buildAVMStoreRootPath(getSandbox()).length();
         
         Map<String, AVMNodeDescriptor> nodes = this.avmService.getDirectoryListing(-1, getCurrentPath());
         this.files = new ArrayList<Map>(nodes.size());
         this.folders = new ArrayList<Map>(nodes.size());
         ClientConfigElement config = Application.getClientConfig(context);
         String wcmDomain = config.getWCMDomain();
         String wcmPort = config.getWCMPort();
         for (String name : nodes.keySet())
         {
            AVMNodeDescriptor avmRef = nodes.get(name);
            
            // build the client representation of the AVM node
            AVMNode node = new AVMNode(avmRef);
            String path = avmRef.getPath();
            
            // properties specific to folders or files
            if (avmRef.isDirectory())
            {
               node.getProperties().put("smallIcon", BrowseBean.SPACE_SMALL_DEFAULT);
               this.folders.add(node);
            }
            else
            {
               node.getProperties().put("fileType16", Utils.getFileTypeImage(name, true));
               node.getProperties().put("url", DownloadContentServlet.generateBrowserURL(
                     AVMNodeConverter.ToNodeRef(-1, path), name));
               this.files.add(node);
            }
            
            // common properties
            String assetPath = path.substring(rootPathIndex);
            String previewUrl = AVMConstants.buildAVMAssetUrl(assetPath, wcmDomain, wcmPort, dns);
            node.getProperties().put("previewUrl", previewUrl);
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
    * Return the list of selected items for the current user sandbox view
    * 
    * @return List of AVMNodeDescriptor objects representing selected items
    */
   public List<AVMNodeDescriptor> getSelectedSandboxItems()
   {
      return this.userSandboxes.getSelectedNodes(this.username);
   }
   
   /**
    * @return true if the special Submit All action has been initialised
    */
   public boolean getSubmitAll()
   {
      return this.submitAll;
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
      updateUILocation(path);
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
   public void setupSandboxAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String store = params.get("store");
      String username = params.get("username");
      
      setupSandboxActionImpl(store, username, true);
   }

   /**
    * Setup the context for a sandbox browse action
    * 
    * @param store      The store name for the action
    * @param username   The authority pertinent to the action (null for staging store actions)
    * @param reset      True to reset the current path and AVM action node context
    */
   private void setupSandboxActionImpl(String store, String username, boolean reset)
   {
      // can be null if it's the staging store - i.e. not a user specific store
      setUsername(username);
      
      // the store can be either a user store or the staging store if this is null
      if (store != null)
      {
         setSandbox(store);
      }
      else
      {
         // get the staging store from the current website node
         setSandbox(AVMConstants.buildAVMStagingStoreName(
               (String)getWebsite().getProperties().get(WCMAppModel.PROP_AVMSTORE)));
      }
      
      // update UI state ready for return to the previous screen
      if (reset == true)
      {
         this.sandboxTitle = null;
         this.location = null;
         setCurrentPath(null);
         setAvmActionNode(null);
         this.submitAll = false;
      }
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
      String path = params.get("id");
      setupContentAction(path, true);
   }
   
   /*package*/ void setupContentAction(String path, boolean refresh)
   {
      if (path != null && path.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup content action for path: " + path);
         
         // calculate username and store name from specified path
         String[] parts = path.split("[-:]");
         String storename = parts[0];
         String username = parts[1];
         setupSandboxActionImpl(AVMConstants.buildAVMUserMainStoreName(storename, username), username, false);
         
         // setup the action node
         AVMNodeDescriptor node = avmService.lookup(-1, path, true);
         setAVMActionNodeDescriptor(node);
      }
      else
      {
         setAvmActionNode(null);
      }
      
      // update UI state ready for return after dialog close
      if (refresh)
      {
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
   }
   
   /**
    * Submit all nodes from user sandbox into the staging area sandbox via workflow
    */
   public void setupSubmitAllAction(ActionEvent event)
   {
      setupSandboxAction(event);
      this.submitAll = true;
   }
   
   /**
    * Undo changes to a single node
    */
   public void revertNode(ActionEvent event)
   {
      String path = getPathFromEventArgs(event);
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, false);
         tx.begin();
         
         AVMNodeDescriptor node = this.avmService.lookup(-1, path, true);
         if (node != null)
         {
            Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
            List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();
            versionPaths.add(new Pair<Integer, String>(-1, path));
            args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
            Action action = this.actionService.createAction(AVMUndoSandboxListAction.NAME, args);
            this.actionService.executeAction(action, null); // dummy action ref
         }
         
         // commit the transaction
         tx.commit();
         
         // if we get here, all was well - output friendly status message to the user
         String msg = MessageFormat.format(Application.getMessage(
               context, MSG_REVERT_SUCCESS), node.getName());
         displayStatusMessage(context, msg);
      }
      catch (Throwable err)
      {
         err.printStackTrace(System.err);
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Undo changes to the entire sandbox
    */
   public void revertAll(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String store = params.get("store");
      String username = params.get("username");
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         // calcluate the list of differences between the user store and the staging area
         List<AVMDifference> diffs = this.avmSyncService.compare(
               -1, store + ":/", -1, getStagingStore() + ":/", this.nameMatcher);
         List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();
         for (AVMDifference diff : diffs)
         {
            versionPaths.add(new Pair<Integer, String>(-1, diff.getSourcePath()));
         }
         Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
         args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
         Action action = this.actionService.createAction(AVMUndoSandboxListAction.NAME, args);
         this.actionService.executeAction(action, null); // dummy action ref
         
         // commit the transaction
         tx.commit();
         
         // if we get here, all was well - output friendly status message to the user
         String msg = MessageFormat.format(Application.getMessage(
               context, MSG_REVERTALL_SUCCESS), username);
         displayStatusMessage(context, msg);
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }

   /**
    * Undo changes to items selected using multi-select
    */
   public void revertSelected(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String username = params.get("username");
      
      List<AVMNodeDescriptor> selected = this.userSandboxes.getSelectedNodes(username);
      if (selected != null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context, false);
            tx.begin();
            
            List<Pair<Integer, String>> versionPaths = new ArrayList<Pair<Integer, String>>();
            for (AVMNodeDescriptor node : selected)
            {
               versionPaths.add(new Pair<Integer, String>(-1, node.getPath()));
            }
            Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
            args.put(AVMUndoSandboxListAction.PARAM_NODE_LIST, (Serializable)versionPaths);
            for (AVMNodeDescriptor node : selected)
            {
               Action action = this.actionService.createAction(AVMUndoSandboxListAction.NAME, args);
               this.actionService.executeAction(action, AVMNodeConverter.ToNodeRef(-1, node.getPath()));
            }
            
            // commit the transaction
            tx.commit();
            
            // if we get here, all was well - output friendly status message to the user
            String msg = MessageFormat.format(Application.getMessage(
                  context, MSG_REVERTSELECTED_SUCCESS), username);
            displayStatusMessage(context, msg);
         }
         catch (Throwable err)
         {
            err.printStackTrace(System.err);
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
   }
   
   /**
    * Revert a sandbox to a specific snapshot version ID
    */
   public void revertSnapshot(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String sandbox = params.get("sandbox");
      String strVersion = params.get("version");
      if (sandbox != null && strVersion != null && strVersion.length() != 0)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context, false);
            tx.begin();
            
            Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
            args.put(AVMRevertStoreAction.PARAM_VERSION, Integer.valueOf(strVersion));
            Action action = this.actionService.createAction(AVMRevertStoreAction.NAME, args);
            this.actionService.executeAction(action, AVMNodeConverter.ToNodeRef(-1, sandbox + ":/"));
            
            // commit the transaction
            tx.commit();
            
            // if we get here, all was well - output friendly status message to the user
            String msg = MessageFormat.format(Application.getMessage(
                  context, MSG_REVERT_SANDBOX), sandbox, strVersion);
            displayStatusMessage(context, msg);
         }
         catch (Throwable err)
         {
            err.printStackTrace(System.err);
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
   }
   
   /**
    * Create web content from a specific Form via the User Sandbox 'Available Forms' panel
    */
   public void createFormContent(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get(UIUserSandboxes.PARAM_FORM_ID);
      
      // setup the correct sandbox for the create action
      setupSandboxAction(event);
      
      // pass form ID to the wizard - to be picked up in init()
      FacesContext fc = FacesContext.getCurrentInstance();
      WizardManager manager = (WizardManager)FacesHelper.getManagedBean(fc, "WizardManager");
      manager.setupParameters(event);
      fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "wizard:createWebContent");
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
    * Update the breadcrumb with the clicked Group location
    */
   private void updateUILocation(String path)
   {
      this.location.add(new AVMBreadcrumbHandler(path));
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
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation 
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
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
   private class AVMBreadcrumbHandler implements IBreadcrumbHandler
   {
      private String path;
      
      AVMBreadcrumbHandler(String path)
      {
         this.path = path;
      }
      
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         setCurrentPath(path);
         setLocation((List)breadcrumb.getValue());
         return null;
      }
      
      @Override
      public String toString()
      {
         if (AVMConstants.buildAVMStoreRootPath(getSandbox()).equals(path))
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
}
