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
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.wcm.WebResources;
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
   
   private static final String MSG_SANDBOXTITLE = "sandbox_title";
   private static final String MSG_SANDBOXSTAGING = "sandbox_staging";
   private static final String MSG_CREATED_ON = "store_created_on";
   private static final String MSG_CREATED_BY = "store_created_by";
   private static final String MSG_WORKING_USERS = "store_working_users";
   private static final String MSG_SUBMIT_SUCCESS = "submit_success";
   private static final String MSG_SUBMITALL_SUCCESS = "submitall_success";
   
   /** Component id the status messages are tied too */
   private static final String COMPONENT_SANDBOXESPANEL = "sandboxes-panel";
   
   /** Action bean Id for the AVM Submit action*/
   private static final String ACTION_AVM_SUBMIT = "simple-avm-submit";
   
   private String sandbox;
   private String username;
   private String sandboxTitle = null;
   private String currentPath = null;
   
   /* component references */
   private UIRichList foldersRichList;
   private UIRichList filesRichList;
   private UIUserSandboxes userSandboxes;
   
   /* transient lists of files/folders for a directory */
   private List<Map> files = null;
   private List<Map> folders = null;
   
   /** Current AVM Node context*/
   private AVMNode avmNode = null;
   
   private String wcmDomain;
   private String wcmPort;
   
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
   
   /** The browse bean */
   protected BrowseBean browseBean;
   
   /** The NavigationBean bean reference */
   protected NavigationBean navigator;
   
   /** AVM service bean reference */
   protected AVMService avmService;
   
   /** Action service bean reference */
   protected ActionService actionService; 
   
   
   /**
    * Default Constructor
    */
   public AVMBrowseBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
      
      ClientConfigElement config = Application.getClientConfig(FacesContext.getCurrentInstance());
      this.wcmDomain = config.getWCMDomain();
      this.wcmPort = config.getWCMPort();
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
      String storeRoot = (String)websiteNode.getProperties().get(ContentModel.PROP_AVMSTORE);
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
      String storeRoot = (String)websiteNode.getProperties().get(ContentModel.PROP_AVMSTORE);
      return AVMConstants.buildAVMStagingStoreName(storeRoot);
   }
   
   /**
    * @return Preview URL for the current Staging store
    */
   public String getStagingPreviewUrl()
   {
      return AVMConstants.buildAVMStoreUrl(getStagingStore());
   }
   
   /**
    * @return Preview URL for the current User Sandbox store
    */
   public String getSandboxPreviewUrl()
   {
      return AVMConstants.buildAVMStoreUrl(getSandbox());
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
    * @return Returns the sandbox.
    */
   public String getSandbox()
   {
      return this.sandbox;
   }

   /**
    * @param sandbox The sandbox to set.
    */
   public void setSandbox(String sandbox)
   {
      this.sandbox = sandbox;
   }
   
   /**
    * @return Returns the username.
    */
   public String getUsername()
   {
      return this.username;
   }

   /**
    * @param username The username to set.
    */
   public void setUsername(String username)
   {
      this.username = username;
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
    * @return Returns the current AVM node context.
    */
   public AVMNode getAvmNode()
   {
      return this.avmNode;
   }

   /**
    * @param avmNode       The AVM node context to set.
    */
   public void setAvmNode(AVMNode avmNode)
   {
      this.avmNode = avmNode;
   }
   
   /**
    * @param avmRef        The AVMNodeDescriptor context to set.
    */
   public void setAVMNodeDescriptor(AVMNodeDescriptor avmRef)
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
         this.currentPath = AVMConstants.buildAVMStoreRootPath(getSandbox());
      }
      return this.currentPath;
   }
   
   /**
    * @param path       the internal AVM path to the current folder for browsing
    */
   public void setCurrentPath(String path)
   {
      this.currentPath = path;
      
      // update UI state ready for screen refresh
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
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
            String previewUrl = MessageFormat.format(
                  AVMConstants.PREVIEW_ASSET_URL, dns, this.wcmDomain, this.wcmPort, assetPath);
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
    * Setup the context for a sandbox browse action
    */
   public void setupSandboxAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String store = params.get("store");
      String username = params.get("username");
      
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
               (String)getWebsite().getProperties().get(ContentModel.PROP_AVMSTORE)));
      }
      
      this.sandboxTitle = null;
      
      // update UI state ready for return to the previous screen
      this.location = null;
      setCurrentPath(null);
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
         AVMNodeDescriptor node = avmService.lookup(-1, path, true);
         setAVMNodeDescriptor(node);
      }
      else
      {
         setAvmNode(null);
      }
      
      // update UI state ready for return after dialog close
      if (refresh)
      {
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
   }
   
   /**
    * Submit a node from a user sandbox into the staging area sandbox
    */
   public void submitNode(ActionEvent event)
   {
      setupContentAction(event);
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         Action action = this.actionService.createAction(ACTION_AVM_SUBMIT);
         this.actionService.executeAction(action, getAvmNode().getNodeRef());
         
         // commit the transaction
         tx.commit();
         
         // if we get here, all was well - output friendly status message to the user
         String msg = MessageFormat.format(Application.getMessage(
               context, MSG_SUBMIT_SUCCESS), getAvmNode().getName());
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         String formId = Utils.getParentForm(context, event.getComponent()).getClientId(context);
         context.addMessage(formId + ':' + COMPONENT_SANDBOXESPANEL, facesMsg);
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Submit an entire user sandbox
    */
   public void submitAll(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String store = params.get("store");
      String username = params.get("username");
      
      String rootPath = AVMConstants.buildAVMStoreRootPath(store);
      NodeRef rootRef = AVMNodeConverter.ToNodeRef(-1, rootPath);
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         Action action = this.actionService.createAction(ACTION_AVM_SUBMIT);
         this.actionService.executeAction(action, rootRef);
         
         // commit the transaction
         tx.commit();
         
         // if we get here, all was well - output friendly status message to the user
         String msg = MessageFormat.format(Application.getMessage(
               context, MSG_SUBMITALL_SUCCESS), username);
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         String formId = Utils.getParentForm(context, event.getComponent()).getClientId(context);
         context.addMessage(formId + ':' + COMPONENT_SANDBOXESPANEL, facesMsg);
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
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
