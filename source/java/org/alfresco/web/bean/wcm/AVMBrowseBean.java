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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.wcm.WebResources;
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
   
   private String sandbox;
   private String username;
   private String sandboxTitle = null;
   private String currentPath = null;
   
   private UIRichList foldersRichList;
   private UIRichList filesRichList;
   
   private List<Map> files = null;
   private List<Map> folders = null;
   
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
   
   
   /**
    * Default Constructor
    */
   public AVMBrowseBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
      
      //initFromClientConfig();
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
    * @param foldersRichList      The foldersRichList to set.
    */
   public void setFoldersRichList(UIRichList foldersRichList)
   {
      this.foldersRichList = foldersRichList;
      /*if (this.foldersRichList != null)
      {
         // set the initial sort column and direction
         this.foldersRichList.setInitialSortColumn(
               this.viewsConfig.getDefaultSortColumn(PAGE_NAME_FORUMS));
         this.foldersRichList.setInitialSortDescending(
               this.viewsConfig.hasDescendingSort(PAGE_NAME_FORUMS));
      }*/
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
   
   public Node getWebsite()
   {
      return this.navigator.getCurrentNode();
   }
   
   public List<Map> getFolders()
   {
      if (this.folders == null)
      {
         getNodes();
      }
      return this.folders;
   }
   
   public List<Map> getFiles()
   {
      if (this.files == null)
      {
         getNodes();
      }
      return this.files;
   }
   
   private void getNodes()
   {
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         Map<String, AVMNodeDescriptor> nodes = this.avmService.getDirectoryListing(-1, getCurrentPath());
         this.files = new ArrayList<Map>(nodes.size());
         this.folders = new ArrayList<Map>(nodes.size());
         for (String name : nodes.keySet())
         {
            AVMNodeDescriptor avmRef = nodes.get(name);
            
            // build the client representation of the AVM node
            AVMNode node = new AVMNode(avmRef);
            
            // add any common properties
            
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
                     AVMNodeConverter.ToNodeRef(-1, avmRef.getPath()), name));
               this.files.add(node);
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
   }
   
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
    * @return the internal AVM path to the current folder for browsing
    */
   private String getCurrentPath()
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
   private void setCurrentPath(String path)
   {
      this.currentPath = path;
      
      // update UI state ready for screen refresh
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
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
