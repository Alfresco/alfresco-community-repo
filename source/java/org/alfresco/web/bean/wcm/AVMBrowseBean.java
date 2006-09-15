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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.UIActionLink;
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
   
   private UIRichList foldersRichList;
   private UIRichList filesRichList;
   
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
      return Collections.emptyList();
   }
   
   public List<Map> getFiles()
   {
      return Collections.emptyList();
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
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
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
      /*
      // clear the value for the list components - will cause re-bind to it's data and refresh
      if (this.forumsRichList != null)
      {
         this.forumsRichList.setValue(null);
         if (this.forumsRichList.getInitialSortColumn() == null)
         {
            // set the initial sort column and direction
            this.forumsRichList.setInitialSortColumn(
                  this.viewsConfig.getDefaultSortColumn(PAGE_NAME_FORUMS));
            this.forumsRichList.setInitialSortDescending(
                  this.viewsConfig.hasDescendingSort(PAGE_NAME_FORUMS));
         }
      }
      */
   }
}
