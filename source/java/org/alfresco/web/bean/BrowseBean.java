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
package org.alfresco.web.bean;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.content.DocumentDetailsDialog;
import org.alfresco.web.bean.ml.MultilingualManageDialog;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.QNameNodeMap;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.config.ViewsConfigElement;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.UIPanel.ExpandedEvent;
import org.alfresco.web.ui.common.component.UIStatusMessage;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;
import org.alfresco.web.ui.repo.component.UINodeDescendants;
import org.alfresco.web.ui.repo.component.UINodePath;
import org.alfresco.web.ui.repo.component.UISimpleSearch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;


/**
 * Bean providing properties and behaviour for the main folder/document browse screen and
 * search results screens.
 *
 * @author Kevin Roast
 */
public class BrowseBean implements IContextListener, Serializable
{
   private static final long serialVersionUID = -3234262484615161360L;

   /** Public JSF Bean name */
   public static final String BEAN_NAME = "BrowseBean";


   // ------------------------------------------------------------------------------
   // Construction

   /**
    * Default Constructor
    */
   public BrowseBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);

      initFromClientConfig();
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

   protected NodeService getNodeService()
   {
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return nodeService;
   }

   /**
    * @param checkOutCheckInService The service for check-in and check-out.
    */
   public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
   {
      this.checkOutCheckInService = checkOutCheckInService;
   }

   protected CheckOutCheckInService getCheckOutCheckInService()
   {
      if (checkOutCheckInService == null)
      {
          checkOutCheckInService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCheckOutCheckInService();
      }
      return checkOutCheckInService;
   }

   /**
    * @param searchService The Searcher to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }

   protected SearchService getSearchService()
   {
      if (searchService == null)
      {
         searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
      }
      return searchService;
   }

   /**
    * @param userPreferencesBean The UserPreferencesBean to set.
    */
   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean)
   {
      this.userPreferencesBean = userPreferencesBean;
   }

   /**
    * @param lockService The Lock Service to set.
    */
   public void setLockService(LockService lockService)
   {
      this.lockService = lockService;
   }

   protected LockService getLockService()
   {
      if (lockService == null)
      {
         lockService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getLockService();
      }
      return lockService;
   }

   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }

   /**
    * @param dictionaryService The DictionaryService to set.
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }

   protected DictionaryService getDictionaryService()
   {
      if (dictionaryService == null)
      {
         dictionaryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
      }
      return dictionaryService;
   }

   /**
    * @param multilingualContentService The Multilingual Content Service to set.
    */
   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }

   protected MultilingualContentService getMultilingualContentService()
   {
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      return multilingualContentService;
   }

   /**
    * @param fileFolderService The FileFolderService to set.
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }

   protected FileFolderService getFileFolderService()
   {
      if (fileFolderService == null)
      {
         fileFolderService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getFileFolderService();
      }
      return fileFolderService;
   }

   /**
    * @return Returns the browse View mode. See UIRichList
    */
   public String getBrowseViewMode()
   {
      return this.browseViewMode;
   }

   /**
    * @param browseViewMode      The browse View mode to set. See UIRichList.
    */
   public void setBrowseViewMode(String browseViewMode)
   {
      this.browseViewMode = browseViewMode;
   }

   /**
    * @return Returns true if dashboard view is available for the current node.
    */
   public boolean isDashboardView()
   {
      return this.dashboardView;
   }

   /**
    * @param dashboardView The dashboard view mode to set.
    */
   public void setDashboardView(boolean dashboardView)
   {
      this.dashboardView = dashboardView;
      if (dashboardView == true)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dashboard");
      }
   }

   public int getPageSizeContent()
   {
      return this.pageSizeContent;
   }

   public void setPageSizeContent(int pageSizeContent)
   {
      this.pageSizeContent = pageSizeContent;
      this.pageSizeContentStr = Integer.toString(pageSizeContent);
   }

   public int getPageSizeSpaces()
   {
      return this.pageSizeSpaces;
   }

   public void setPageSizeSpaces(int pageSizeSpaces)
   {
      this.pageSizeSpaces = pageSizeSpaces;
      this.pageSizeSpacesStr = Integer.toString(pageSizeSpaces);
   }

   public String getPageSizeContentStr()
   {
      return this.pageSizeContentStr;
   }

   public void setPageSizeContentStr(String pageSizeContentStr)
   {
      this.pageSizeContentStr = pageSizeContentStr;
   }

   public String getPageSizeSpacesStr()
   {
      return this.pageSizeSpacesStr;
   }

   public void setPageSizeSpacesStr(String pageSizeSpacesStr)
   {
      this.pageSizeSpacesStr = pageSizeSpacesStr;
   }

   /**
    * @return Returns the minimum length of a valid search string.
    */
   public static int getMinimumSearchLength()
   {
      return Application.getClientConfig(FacesContext.getCurrentInstance()).getSearchMinimum();
   }

   /**
    * @return Returns the panels expanded state map.
    */
   public Map<String, Boolean> getPanels()
   {
      return this.panels;
   }

   /**
    * @param panels The panels expanded state map.
    */
   public void setPanels(Map<String, Boolean> panels)
   {
      this.panels = panels;
   }

   /**
    * @return Returns the Space Node being used for the current browse screen action.
    */
   public Node getActionSpace()
   {
      return this.actionSpace;
   }

   /**
    * @param actionSpace     Set the Space Node to be used for the current browse screen action.
    */
   public void setActionSpace(Node actionSpace)
   {
      if (actionSpace != null)
      {
         for (NodeEventListener listener : getNodeEventListeners())
         {
            listener.created(actionSpace, actionSpace.getType());
         }
      }
      this.actionSpace = actionSpace;
   }

   /**
    * @return The document node being used for the current operation
    */
   public Node getDocument()
   {
      return this.document;
   }

   /**
    * @param document The document node to be used for the current operation
    */
   public void setDocument(Node document)
   {
      if (document != null)
      {
         for (NodeEventListener listener : getNodeEventListeners())
         {
            listener.created(document, document.getType());
         }
      }
      this.document = document;
   }

   /**
    * @param contentRichList The contentRichList to set.
    */
   public void setContentRichList(UIRichList contentRichList)
   {
      this.contentRichList = contentRichList;
      if (this.contentRichList != null)
      {
         this.contentRichList.setInitialSortColumn(
               this.viewsConfig.getDefaultSortColumn(PAGE_NAME_BROWSE));
         this.contentRichList.setInitialSortDescending(
               this.viewsConfig.hasDescendingSort(PAGE_NAME_BROWSE));
      }
      // special case to handle an External Access URL
      // these URLs restart the JSF lifecycle but an old UIRichList is restored from
      // the component tree - which needs clearing "late" in the lifecycle process
      if (externalForceRefresh)
      {
         this.contentRichList.setValue(null);
         externalForceRefresh = false;
      }
   }

   /**
    * @return Returns the contentRichList.
    */
   public UIRichList getContentRichList()
   {
      return this.contentRichList;
   }

   /**
    * @param spacesRichList The spacesRichList to set.
    */
   public void setSpacesRichList(UIRichList spacesRichList)
   {
      this.spacesRichList = spacesRichList;
      if (this.spacesRichList != null)
      {
         // set the initial sort column and direction
         this.spacesRichList.setInitialSortColumn(
               this.viewsConfig.getDefaultSortColumn(PAGE_NAME_BROWSE));
         this.spacesRichList.setInitialSortDescending(
               this.viewsConfig.hasDescendingSort(PAGE_NAME_BROWSE));
      }
      if (externalForceRefresh)
      {
         this.spacesRichList.setValue(null);
      }
   }

   /**
    * @return Returns the spacesRichList.
    */
   public UIRichList getSpacesRichList()
   {
      return this.spacesRichList;
   }

   /**
    * @return Returns the statusMessage component.
    */
   public UIStatusMessage getStatusMessage()
   {
      return this.statusMessage;
   }

   /**
    * @param statusMessage The statusMessage component to set.
    */
   public void setStatusMessage(UIStatusMessage statusMessage)
   {
      this.statusMessage = statusMessage;
   }

   /**
    * @return Returns the deleteMessage.
    */
   public String getDeleteMessage()
   {
      return this.deleteMessage;
   }

   /**
    * @param deleteMessage The deleteMessage to set.
    */
   public void setDeleteMessage(String deleteMessage)
   {
      this.deleteMessage = deleteMessage;
   }

   /**
    * Page accessed bean method to get the container nodes currently being browsed
    *
    * @return List of container Node objects for the current browse location
    */
   public List<Node> getNodes()
   {
      // the references to container nodes and content nodes are transient for one use only
      // we do this so we only query/search once - as we cannot distinguish between node types
      // until after the query. The logic is a bit confusing but otherwise we would need to
      // perform the same query or search twice for every screen refresh.
      if (this.containerNodes == null)
      {
         if (this.navigator.getSearchContext() == null)
         {
            queryBrowseNodes(this.navigator.getCurrentNodeId());
         }
         else
         {
            searchBrowseNodes(this.navigator.getSearchContext());
         }
      }
      List<Node> result = this.containerNodes;

      // we clear the member variable during invalidateComponents()

      return result;
   }

   /**
    * Page accessed bean method to get the content nodes currently being browsed
    *
    * @return List of content Node objects for the current browse location
    */
   public List<Node> getContent()
   {
      // see comment in getNodes() above for reasoning here
      if (this.contentNodes == null)
      {
         if (this.navigator.getSearchContext() == null)
         {
            queryBrowseNodes(this.navigator.getCurrentNodeId());
         }
         else
         {
            searchBrowseNodes(this.navigator.getSearchContext());
         }
      }
      List<Node> result = this.contentNodes;

      // we clear the member variable during invalidateComponents()

      return result;
   }
   
   /**
    * Page accessed bean method to get the parent container nodes currently being browsed
    *
    * @return List of parent container Node objects for the current browse location
    */
   public List<Node> getParentNodes(NodeRef currNodeRef)
   {
	  // As per AWC-1507 there are two scenarios for navigating to the space details. First
	  // scenario is to show space details of the current space. Second scenario is to show 
	  // space details of a child space of the current space. For now, added an extra query
	  // so that existing context remains unaffected for second scenario, although it does 
	  // mean that in first scenario there will be an extra query even though parentContainerNodes
	  // and containerNodes will contain the same list.
	   
	  if (this.parentContainerNodes == null)
	  {
	      long startTime = 0;
	      if (logger.isDebugEnabled())
	         startTime = System.currentTimeMillis();
  
	      UserTransaction tx = null;
	      try
	      {
	         FacesContext context = FacesContext.getCurrentInstance();
	         tx = Repository.getUserTransaction(context, true);
	         tx.begin();

	         NodeRef parentRef = getNodeService().getPrimaryParent(currNodeRef).getParentRef();
	         
	         List<FileInfo> children = this.getFileFolderService().list(parentRef);
	         this.parentContainerNodes = new ArrayList<Node>(children.size());
	         for (FileInfo fileInfo : children)
	         {
	            // create our Node representation from the NodeRef
	            NodeRef nodeRef = fileInfo.getNodeRef();

	            // find it's type so we can see if it's a node we are interested in
	            QName type = this.getNodeService().getType(nodeRef);

	            // make sure the type is defined in the data dictionary
	            TypeDefinition typeDef = this.getDictionaryService().getType(type);

	            if (typeDef != null)
	            {
	               MapNode node = null;

	               // look for Space folder node
	               if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER) == true &&
	                   this.getDictionaryService().isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
	               {
	                  // create our Node representation
	                  node = new MapNode(nodeRef, this.getNodeService(), fileInfo.getProperties());
	                  node.addPropertyResolver("icon", this.resolverSpaceIcon);
	                  node.addPropertyResolver("smallIcon", this.resolverSmallIcon);

	                  this.parentContainerNodes.add(node);
	               }
	               else if (ApplicationModel.TYPE_FOLDERLINK.equals(type))
	               {
	                  // create our Folder Link Node representation
	                  node = new MapNode(nodeRef, this.getNodeService(), fileInfo.getProperties());
	                  node.addPropertyResolver("icon", this.resolverSpaceIcon);
	                  node.addPropertyResolver("smallIcon", this.resolverSmallIcon);

	                  this.parentContainerNodes.add(node);
	               }
	            }
	            else
	            {
	               if (logger.isWarnEnabled())
	                  logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
	            }
	         }

	         // commit the transaction
	         tx.commit();
	      }
	      catch (InvalidNodeRefException refErr)
	      {
	         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
	               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}), refErr );
	         this.parentContainerNodes = Collections.<Node>emptyList();
	         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
	      }
	      catch (Throwable err)
	      {
	         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
	               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
	         this.parentContainerNodes = Collections.<Node>emptyList();
	         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
	      }

	      if (logger.isDebugEnabled())
	      {
	         long endTime = System.currentTimeMillis();
	         logger.debug("Time to query and build map parent nodes: " + (endTime - startTime) + "ms");
	      }	   
	  }
	      
      List<Node> result = this.parentContainerNodes;

      // we clear the member variable during invalidateComponents()

      return result;
   }

   /**
    * Determines whether the current space is a 'Sites' space
    * 
    * @return true if the current space is a 'Sites' space
    */
   public boolean isSitesSpace()
   {
      boolean siteSpace = false;
      
      Node currentNode = this.navigator.getCurrentNode();
      if (currentNode != null)
      {
         // check the type of the node to see if it is a 'site' related space
         QName currentNodeType = currentNode.getType();
         
         if (SiteModel.TYPE_SITES.isMatch(currentNodeType) ||
             SiteModel.TYPE_SITE.isMatch(currentNodeType) ||
             getDictionaryService().isSubClass(currentNodeType, SiteModel.TYPE_SITE))
         {
            siteSpace = true;
         }
      }
      
      return siteSpace;
   }
   
   /**
    * Returns the HTML to display if a space is a 'Sites' space
    * 
    * @return The HTML to display
    */
   public String getSitesSpaceWarningHTML()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String contextPath = context.getExternalContext().getRequestContextPath();
      StringBuilder html = new StringBuilder();
      
      try
      {
         html.append("<tr valign='top'>");
         html.append("<td style='background-image: url(");
         html.append(contextPath);
         html.append("/images/parts/whitepanel_4.gif)' ");
         html.append("width='4'></td><td style='padding:4px'>");
         
         StringWriter writer = new StringWriter();
         PanelGenerator.generatePanelStart(writer, contextPath, "yellowInner", "#ffffcc");
         html.append(writer.toString());
         
         html.append("<table cellpadding='0' cellspacing='0' border='0' width='100%'>");
         html.append("<tr><td valign='top' style='padding-top: 2px' width='20'>");
         html.append("<img src='");
         html.append(contextPath);
         html.append("/images/icons/warning.gif' width='16' height='16' /></td>");
         html.append("<td class='mainSubText'>");
         html.append(Application.getMessage(context, "sites_space_warning"));
         html.append("</td></tr></table>");
         
         writer = new StringWriter();
         PanelGenerator.generatePanelEnd(writer, contextPath, "yellowInner");
         html.append(writer.toString());
            
         html.append("</td><td style='background-image: url(");
         html.append(contextPath);
         html.append("/images/parts/whitepanel_6.gif)'"); 
         html.append("width='4'></td></tr>");
      }
      catch (IOException ioe)
      {
         logger.error(ioe);
      }
      
      return html.toString();
   }

   /**
    * Setup the common properties required at data-binding time.
    * <p>
    * These are properties used by components on the page when iterating over the nodes.
    * The properties are available as the Node is a Map so they can be accessed directly
    * by name. Information such as download URL, size and filetype are provided etc.
    * <p>
    * We use a set of anonymous inner classes to provide the implemention for the property
    * getters. The interfaces are only called when the properties are first requested.
    *
    * @param node       Node to add the properties too
    */
   public void setupCommonBindingProperties(Node node)
   {
      // special properties to be used by the value binding components on the page
      node.addPropertyResolver("url", this.resolverUrl);

      if (ApplicationModel.TYPE_FILELINK.equals(node.getType()))
      {
         node.addPropertyResolver("downloadUrl", this.resolverLinkDownload);
      }
      else
      {
         node.addPropertyResolver("downloadUrl", this.resolverDownload);
      }

      node.addPropertyResolver("webdavUrl", this.resolverWebdavUrl);
      node.addPropertyResolver("cifsPath", this.resolverCifsPath);
      node.addPropertyResolver("fileType16", this.resolverFileType16);
      node.addPropertyResolver("fileType32", this.resolverFileType32);
      node.addPropertyResolver("size", this.resolverSize);
      node.addPropertyResolver("lang", this.resolverLang);
   }


   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      invalidateComponents();
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
   // NodeEventListener listeners

   /**
    * Add a listener to those called by the BrowseBean when nodes are created
    */
   public void addNodeEventListener(NodeEventListener listener)
   {
      if (this.nodeEventListeners == null)
      {
          this.nodeEventListeners = new HashSet<NodeEventListener>();
      }
      this.nodeEventListeners.add(listener);
   }

   /**
    * Remove a listener from the list of those called by BrowseBean
    */
   public void removeNodeEventListener(NodeEventListener listener)
   {
      if (this.nodeEventListeners != null)
      {
          this.nodeEventListeners.remove(listener);
      }
   }


   // ------------------------------------------------------------------------------
   // Navigation action event handlers

   /**
    * Change the current view mode based on user selection
    *
    * @param event      ActionEvent
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();

      // get the view mode ID
      String viewMode = viewList.getValue().toString();

      if (VIEWMODE_DASHBOARD.equals(viewMode) == false)
      {
         // set the page size based on the style of display
         int pageSize = this.viewsConfig.getDefaultPageSize(PAGE_NAME_BROWSE, viewMode);
         setPageSizeContent(pageSize);
         setPageSizeSpaces(pageSize);

         if (logger.isDebugEnabled())
            logger.debug("Browse view page size set to: " + pageSize);

         setDashboardView(false);

         // push the view mode into the lists
         setBrowseViewMode(viewMode);

         // setup dispatch context for custom views
         this.navigator.setupDispatchContext(this.navigator.getCurrentNode());
         
         // browse to appropriate view
         FacesContext fc = FacesContext.getCurrentInstance();
         String outcome = null;
         String viewId = fc.getViewRoot().getViewId();
         if (viewId.equals(BROWSE_VIEW_ID) == false && viewId.equals(CATEGORY_VIEW_ID) == false)
         {
            outcome = "browse";
         }
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, outcome);
      }
      else
      {
         // special case for Dashboard view
         setDashboardView(true);
      }
   }


   // ------------------------------------------------------------------------------
   // Helper methods

   /**
    * Query a list of nodes for the specified parent node Id
    *
    * @param parentNodeId     Id of the parent node or null for the root node
    */
   private void queryBrowseNodes(String parentNodeId)
   {
      long startTime = 0;
      if (logger.isDebugEnabled())
         startTime = System.currentTimeMillis();

      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();

         NodeRef parentRef;
         if (parentNodeId == null)
         {
            // no specific parent node specified - use the root node
            parentRef = this.getNodeService().getRootNode(Repository.getStoreRef());
         }
         else
         {
            // build a NodeRef for the specified Id and our store
            parentRef = new NodeRef(Repository.getStoreRef(), parentNodeId);
         }

         List<FileInfo> children = this.getFileFolderService().removeHiddenFiles(this.getFileFolderService().list(parentRef));
         this.containerNodes = new ArrayList<Node>(children.size());
         this.contentNodes = new ArrayList<Node>(children.size());
         
         // in case of dynamic config, only lookup once
         Set<NodeEventListener> nodeEventListeners = getNodeEventListeners();
         
         for (FileInfo fileInfo : children)
         {
            // create our Node representation from the NodeRef
            NodeRef nodeRef = fileInfo.getNodeRef();

            // find it's type so we can see if it's a node we are interested in
            QName type = this.getNodeService().getType(nodeRef);

            // make sure the type is defined in the data dictionary
            TypeDefinition typeDef = this.getDictionaryService().getType(type);

            if (typeDef != null)
            {
               MapNode node = null;

               // look for File content node
               if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT))
               {
                  // create our Node representation
                  node = new MapNode(nodeRef, this.getNodeService(), fileInfo.getProperties());
                  setupCommonBindingProperties(node);

                  this.contentNodes.add(node);
               }
               // look for Space folder node
               else if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER) == true &&
                        this.getDictionaryService().isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
               {
                  // create our Node representation
                  node = new MapNode(nodeRef, this.getNodeService(), fileInfo.getProperties());
                  node.addPropertyResolver("icon", this.resolverSpaceIcon);
                  node.addPropertyResolver("smallIcon", this.resolverSmallIcon);

                  this.containerNodes.add(node);
               }
               // look for File Link object node
               else if (ApplicationModel.TYPE_FILELINK.equals(type))
               {
                  // create our File Link Node representation
                  node = new MapNode(nodeRef, this.getNodeService(), fileInfo.getProperties());
                  // only display the user has the permissions to navigate to the target of the link
                  NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
                  if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true)
                  {
                     node.addPropertyResolver("url", this.resolverLinkUrl);
                     node.addPropertyResolver("downloadUrl", this.resolverLinkDownload);
                     node.addPropertyResolver("webdavUrl", this.resolverLinkWebdavUrl);
                     node.addPropertyResolver("cifsPath", this.resolverLinkCifsPath);
                     node.addPropertyResolver("fileType16", this.resolverFileType16);
                     node.addPropertyResolver("fileType32", this.resolverFileType32);
                     node.addPropertyResolver("lang", this.resolverLang);
   
                     this.contentNodes.add(node);
                  }
               }
               else if (ApplicationModel.TYPE_FOLDERLINK.equals(type))
               {
                  // create our Folder Link Node representation
                  node = new MapNode(nodeRef, this.getNodeService(), fileInfo.getProperties());
                  // only display the user has the permissions to navigate to the target of the link
                  NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
                  if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true)
                  {
                     node.addPropertyResolver("icon", this.resolverSpaceIcon);
                     node.addPropertyResolver("smallIcon", this.resolverSmallIcon);
   
                     this.containerNodes.add(node);
                  }
               }

               // inform any listeners that a Node wrapper has been created
               if (node != null)
               {
                  for (NodeEventListener listener : nodeEventListeners)
                  {
                     listener.created(node, type);
                  }
               }
            }
            else
            {
               if (logger.isWarnEnabled())
                  logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
            }
         }

         // commit the transaction
         tx.commit();
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}), refErr );
         this.containerNodes = Collections.<Node>emptyList();
         this.contentNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         this.containerNodes = Collections.<Node>emptyList();
         this.contentNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }

      if (logger.isDebugEnabled())
      {
         long endTime = System.currentTimeMillis();
         logger.debug("Time to query and build map nodes: " + (endTime - startTime) + "ms");
      }
   }

   /**
    * Search for a list of nodes using the specific search context
    *
    * @param searchContext    To use to perform the search
    */
   private void searchBrowseNodes(SearchContext searchContext)
   {
      long startTime = 0;
      if (logger.isDebugEnabled())
         startTime = System.currentTimeMillis();

      // get the searcher object to build the query
      String query = searchContext.buildQuery(getMinimumSearchLength());
      if (query == null)
      {
         // failed to build a valid query, the user probably did not enter the
         // minimum text required to construct a valid search
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), MSG_SEARCH_MINIMUM),
               new Object[] {getMinimumSearchLength()}));
         this.containerNodes = Collections.<Node>emptyList();
         this.contentNodes = Collections.<Node>emptyList();
         return;
      }

      // perform the search against the repo
      UserTransaction tx = null;
      ResultSet results = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();

         // build up the search parameters
         SearchParameters sp = new SearchParameters();
         sp.setLanguage(SearchService.LANGUAGE_LUCENE);
         sp.setQuery(query);
         sp.addStore(Repository.getStoreRef());

         // limit search results size as configured
         int searchLimit = Application.getClientConfig(FacesContext.getCurrentInstance()).getSearchMaxResults();
         if (searchLimit > 0)
         {
            sp.setLimitBy(LimitBy.FINAL_SIZE);
            sp.setLimit(searchLimit);
         }

         results = this.getSearchService().query(sp);
         if (logger.isDebugEnabled())
            logger.debug("Search results returned: " + results.length());

         // create a list of items from the results
         this.containerNodes = new ArrayList<Node>(results.length());
         this.contentNodes = new ArrayList<Node>(results.length());
         if (results.length() != 0)
         {
            // in case of dynamic config, only lookup once
            Set<NodeEventListener> nodeEventListeners = getNodeEventListeners();
             
            for (ResultSetRow row: results)
            {
               NodeRef nodeRef = row.getNodeRef();

               if (this.getNodeService().exists(nodeRef))
               {
                  // find it's type so we can see if it's a node we are interested in
                  QName type = this.getNodeService().getType(nodeRef);

                  // make sure the type is defined in the data dictionary
                  TypeDefinition typeDef = this.getDictionaryService().getType(type);

                  if (typeDef != null)
                  {
                     MapNode node = null;

                     // look for Space or File nodes
                     if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER) &&
                         this.getDictionaryService().isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
                     {
                        // create our Node representation
                        node = new MapNode(nodeRef, this.getNodeService(), false);

                        node.addPropertyResolver("path", this.resolverPath);
                        node.addPropertyResolver("displayPath", this.resolverDisplayPath);
                        node.addPropertyResolver("icon", this.resolverSpaceIcon);
                        node.addPropertyResolver("smallIcon", this.resolverSmallIcon);

                        this.containerNodes.add(node);
                     }
                     else if (this.getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT))
                     {
                        // create our Node representation
                        node = new MapNode(nodeRef, this.getNodeService(), false);

                        setupCommonBindingProperties(node);

                        node.addPropertyResolver("path", this.resolverPath);
                        node.addPropertyResolver("displayPath", this.resolverDisplayPath);

                        this.contentNodes.add(node);
                     }
                     // look for File Link object node
                     else if (ApplicationModel.TYPE_FILELINK.equals(type))
                     {
                        // create our File Link Node representation
                        node = new MapNode(nodeRef, this.getNodeService(), false);
                        // only display the user has the permissions to navigate to the target of the link
                        NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
                        if (new Node(destRef).hasPermission(PermissionService.READ) == true)
                        {
                           node.addPropertyResolver("url", this.resolverLinkUrl);
                           node.addPropertyResolver("downloadUrl", this.resolverLinkDownload);
                           node.addPropertyResolver("webdavUrl", this.resolverLinkWebdavUrl);
                           node.addPropertyResolver("cifsPath", this.resolverLinkCifsPath);
                           node.addPropertyResolver("fileType16", this.resolverFileType16);
                           node.addPropertyResolver("fileType32", this.resolverFileType32);
                           node.addPropertyResolver("lang", this.resolverLang);
                           node.addPropertyResolver("path", this.resolverPath);
                           node.addPropertyResolver("displayPath", this.resolverDisplayPath);

                           this.contentNodes.add(node);
                        }
                     }
                     else if (ApplicationModel.TYPE_FOLDERLINK.equals(type))
                     {
                        // create our Folder Link Node representation
                        node = new MapNode(nodeRef, this.getNodeService(), false);
                        // only display the user has the permissions to navigate to the target of the link
                        NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
                        if (new Node(destRef).hasPermission(PermissionService.READ) == true)
                        {
                           node.addPropertyResolver("icon", this.resolverSpaceIcon);
                           node.addPropertyResolver("smallIcon", this.resolverSmallIcon);
                           node.addPropertyResolver("path", this.resolverPath);
                           node.addPropertyResolver("displayPath", this.resolverDisplayPath);

                           this.containerNodes.add(node);
                        }
                     }

                     // inform any listeners that a Node wrapper has been created
                     if (node != null)
                     {
                        for (NodeEventListener listener : nodeEventListeners)
                        {
                           listener.created(node, type);
                        }
                     }
                  }
                  else
                  {
                     if (logger.isWarnEnabled())
                        logger.warn("Found invalid object in database: id = " + nodeRef + ", type = " + type);
                  }
               }
               else
               {
                  if (logger.isWarnEnabled())
                     logger.warn("Missing object returned from search indexes: id = " + nodeRef + " search query: " + query);
               }
            }
         }

         // commit the transaction
         tx.commit();
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}), refErr );
         this.containerNodes = Collections.<Node>emptyList();
         this.contentNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (SearcherException serr)
      {
         logger.info("Search failed for: " + query, serr);
         Utils.addErrorMessage(Application.getMessage(
              FacesContext.getCurrentInstance(), Repository.ERROR_QUERY));
         this.containerNodes = Collections.<Node>emptyList();
         this.contentNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_SEARCH), new Object[] {err.getMessage()}), err );
         this.containerNodes = Collections.<Node>emptyList();
         this.contentNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }

      if (logger.isDebugEnabled())
      {
         long endTime = System.currentTimeMillis();
         logger.debug("Time to query and build map nodes: " + (endTime - startTime) + "ms");
      }
   }


   // ------------------------------------------------------------------------------
   // Property Resolvers

   public NodePropertyResolver resolverDownload = new NodePropertyResolver() {
      private static final long serialVersionUID = 4048859853585650378L;

      public Object get(Node node) {
         return DownloadContentServlet.generateDownloadURL(node.getNodeRef(), node.getName());
      }
   };

   public NodePropertyResolver resolverUrl = new NodePropertyResolver() {
      private static final long serialVersionUID = -5264085143622470386L;

      public Object get(Node node) {
         return DownloadContentServlet.generateBrowserURL(node.getNodeRef(), node.getName());
      }
   };

   public NodePropertyResolver resolverWebdavUrl = new NodePropertyResolver() {
      private static final long serialVersionUID = 9127234483419089006L;

      public Object get(Node node) {
         return Utils.generateURL(FacesContext.getCurrentInstance(), node, URLMode.WEBDAV);
      }
   };

   public NodePropertyResolver resolverCifsPath = new NodePropertyResolver() {
      private static final long serialVersionUID = -5804924617772163104L;

      public Object get(Node node) {
         return Utils.generateURL(FacesContext.getCurrentInstance(), node, URLMode.CIFS);
      }
   };

   public NodePropertyResolver resolverLinkDownload = new NodePropertyResolver() {
      private static final long serialVersionUID = 7208696954599958859L;

      public Object get(Node node) {
         NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (getNodeService().exists(destRef) == true)
         {
            String destName = Repository.getNameForNode(getNodeService(), destRef);
            return DownloadContentServlet.generateDownloadURL(destRef, destName);
         }
         else
         {
            // TODO: link object is missing - navigate to a page with appropriate message
            return "#";
         }
      }
   };

   public NodePropertyResolver resolverLinkUrl = new NodePropertyResolver() {
      private static final long serialVersionUID = -1280702397805414147L;

      public Object get(Node node) {
         NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (getNodeService().exists(destRef) == true)
         {
            String destName = Repository.getNameForNode(getNodeService(), destRef);
            return DownloadContentServlet.generateBrowserURL(destRef, destName);
         }
         else
         {
            // TODO: link object is missing - navigate to a page with appropriate message
            return "#";
         }
      }
   };

   public NodePropertyResolver resolverLinkWebdavUrl = new NodePropertyResolver() {
      private static final long serialVersionUID = -3097558079118837397L;

      public Object get(Node node) {
         NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (getNodeService().exists(destRef) == true)
         {
            return Utils.generateURL(FacesContext.getCurrentInstance(), new Node(destRef), URLMode.WEBDAV);
         }
         else
         {
            // TODO: link object is missing - navigate to a page with appropriate message
            return "#";
         }
      }
   };

   public NodePropertyResolver resolverLinkCifsPath = new NodePropertyResolver() {
      private static final long serialVersionUID = 673020173327603487L;

      public Object get(Node node) {
         NodeRef destRef = (NodeRef)node.getProperties().get(ContentModel.PROP_LINK_DESTINATION);
         if (getNodeService().exists(destRef) == true)
         {
            return Utils.generateURL(FacesContext.getCurrentInstance(), new Node(destRef), URLMode.CIFS);
         }
         else
         {
            // TODO: link object is missing - navigate to a page with appropriate message
            return "#";
         }
      }
   };

   public NodePropertyResolver resolverFileType16 = new NodePropertyResolver() {
      private static final long serialVersionUID = -2690520488415178029L;

      public Object get(Node node) {
         return FileTypeImageUtils.getFileTypeImage(node.getName(), true);
      }
   };

   public NodePropertyResolver resolverFileType32 = new NodePropertyResolver() {
      private static final long serialVersionUID = 1991254398502584389L;

      public Object get(Node node) {
         return FileTypeImageUtils.getFileTypeImage(node.getName(), false);
      }
   };

   public NodePropertyResolver resolverPath = new NodePropertyResolver() {
      private static final long serialVersionUID = 8008094870888545035L;

      public Object get(Node node) {
         return node.getNodePath();
      }
   };

   public NodePropertyResolver resolverDisplayPath = new NodePropertyResolver() {
      private static final long serialVersionUID = -918422848579179425L;

      public Object get(Node node) {
         // TODO: replace this with a method that shows the full display name - not QNames?
         return Repository.getDisplayPath(node.getNodePath());
      }
   };

   public NodePropertyResolver resolverSpaceIcon = new NodePropertyResolver() {
      private static final long serialVersionUID = -5644418026591098018L;

      public Object get(Node node) {
         QNameNodeMap props = (QNameNodeMap)node.getProperties();
         String icon = (String)props.getRaw("app:icon");
         return (icon != null ? icon : CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME);
      }
   };

   public NodePropertyResolver resolverSmallIcon = new NodePropertyResolver() {
      private static final long serialVersionUID = -150483121767183580L;

      public Object get(Node node) {
         QNameNodeMap props = (QNameNodeMap)node.getProperties();
         String icon = (String)props.getRaw("app:icon");
         return (icon != null ? icon + "-16" : SPACE_SMALL_DEFAULT);
      }
   };

   public NodePropertyResolver resolverMimetype = new NodePropertyResolver() {
      private static final long serialVersionUID = -8864267975247235172L;

      public Object get(Node node) {
         ContentData content = (ContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
         return (content != null ? content.getMimetype() : null);
      }
   };

   public NodePropertyResolver resolverEncoding = new NodePropertyResolver() {
      private static final long serialVersionUID = -1130974681844152101L;

      public Object get(Node node) {
         ContentData content = (ContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
         return (content != null ? content.getEncoding() : null);
      }
   };
   
   public NodePropertyResolver resolverSize = new NodePropertyResolver() {
      private static final long serialVersionUID = 1273541660444385276L;

      public Object get(Node node) {
         ContentData content = (ContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
         return (content != null ? new Long(content.getSize()) : 0L);
      }
   };

   public NodePropertyResolver resolverLang = new NodePropertyResolver() {
      
    private static final long serialVersionUID = 5412446489528560367L;

      public Object get(Node node) {

         String lang = null;

         if (node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
         {
            Locale locale = null;

            if(node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                // if the translation is empty, the lang of the content is the lang of it's pivot.
                NodeRef pivot = getMultilingualContentService().getPivotTranslation(node.getNodeRef());
                locale = (Locale) getNodeService().getProperty(pivot, ContentModel.PROP_LOCALE);
            }
            else
            {
                locale = (Locale) node.getProperties().get(ContentModel.PROP_LOCALE);
            }
            // the content filter lang defined by the user
            String userLang = userPreferencesBean.getContentFilterLanguage();
            // the node lang
            String nodeLang = locale.getLanguage();

            // if filter equals all languages : display the lang for each translation
            if (nodeLang == null)
            {
               lang = nodeLang;
            }

            // if filter is different : display the lang
            else if (!nodeLang.equalsIgnoreCase(userLang))
            {
               lang = nodeLang;
            }

            // else if the filter is equal to the lang node : nothing to do [lang = null]
         }

         return lang;
      }
   };


   // ------------------------------------------------------------------------------
   // Navigation action event handlers

   /**
    * Action called from the Simple Search component.
    * Sets up the SearchContext object with the values from the simple search menu.
    */
   public void search(ActionEvent event)
   {
      // setup the search text string on the top-level navigation handler
      UISimpleSearch search = (UISimpleSearch)event.getComponent();
      this.navigator.setSearchContext(search.getSearchContext());

      navigateBrowseScreen();
   }

   /**
    * Action called to Close the search dialog by returning to the last view node Id
    */
   public void closeSearch(ActionEvent event)
   {
      // set the current node Id ready for page refresh
      String currentNodeId = this.navigator.getCurrentNodeId();
      this.navigator.setCurrentNodeId(currentNodeId);
      
      // setup dispatch context so we go back to the right place
      NodeRef currentNodeRef = new NodeRef(Repository.getStoreRef(), currentNodeId);
      Node currentNode = new Node(currentNodeRef);
      this.navigator.setupDispatchContext(currentNode);
   }

   /**
    * Update page size based on user selection
    */
   public void updateSpacesPageSize(ActionEvent event)
   {
      try
      {
         int size = Integer.parseInt(this.pageSizeSpacesStr);
         if (size >= 0)
         {
            this.pageSizeSpaces = size;
         }
         else
         {
            // reset to known value if this occurs
            this.pageSizeSpacesStr = Integer.toString(this.pageSizeSpaces);
         }
      }
      catch (NumberFormatException err)
      {
         // reset to known value if this occurs
         this.pageSizeSpacesStr = Integer.toString(this.pageSizeSpaces);
      }
   }

   /**
    * Update page size based on user selection
    */
   public void updateContentPageSize(ActionEvent event)
   {
      try
      {
         int size = Integer.parseInt(this.pageSizeContentStr);
         if (size >= 0)
         {
            this.pageSizeContent = size;
         }
         else
         {
            // reset to known value if this occurs
            this.pageSizeContentStr = Integer.toString(this.pageSizeContent);
         }
      }
      catch (NumberFormatException err)
      {
         // reset to known value if this occurs
         this.pageSizeContentStr = Integer.toString(this.pageSizeContent);
      }
   }

   /**
    * Action called when a folder space is clicked.
    * Navigate into the space.
    */
   public void clickSpace(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         try
         {
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);

            // handle special folder link node case
            if (ApplicationModel.TYPE_FOLDERLINK.equals(this.getNodeService().getType(ref)))
            {
               ref = (NodeRef)this.getNodeService().getProperty(ref, ContentModel.PROP_LINK_DESTINATION);
            }

            clickSpace(ref);
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         }
      }
   }

   /**
    * Action called when a folder space is clicked.
    *
    * @param nodeRef The node being clicked
    */
   public void clickSpace(NodeRef nodeRef)
   {
      // refresh UI based on node selection
      updateUILocation(nodeRef);
   }

   /**
    * Handler called when a path element is clicked - navigate to the appropriate Space
    */
   public void clickSpacePath(ActionEvent event)
   {
      UINodePath.PathElementEvent pathEvent = (UINodePath.PathElementEvent)event;
      NodeRef ref = pathEvent.NodeReference;
      try
      {
         // refresh UI based on node selection
         this.updateUILocation(ref);
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {ref.getId()}) );
      }
   }

   /**
    * Action called when a folders direct descendant (in the 'list' browse mode) is clicked.
    * Navigate into the the descendant space.
    */
   public void clickDescendantSpace(ActionEvent event)
   {
      UINodeDescendants.NodeSelectedEvent nodeEvent = (UINodeDescendants.NodeSelectedEvent)event;
      NodeRef nodeRef = nodeEvent.NodeReference;
      if (nodeRef == null)
      {
         throw new IllegalStateException("NodeRef returned from UINodeDescendants.NodeSelectedEvent cannot be null!");
      }

      if (logger.isDebugEnabled())
         logger.debug("Selected noderef Id: " + nodeRef.getId());

      try
      {
         // user can either select a descendant of a node display on the page which means we
         // must add the it's parent and itself to the breadcrumb
         ChildAssociationRef parentAssocRef = getNodeService().getPrimaryParent(nodeRef);

         if (logger.isDebugEnabled())
         {
            logger.debug("Selected item getPrimaryParent().getChildRef() noderef Id:  " + parentAssocRef.getChildRef().getId());
            logger.debug("Selected item getPrimaryParent().getParentRef() noderef Id: " + parentAssocRef.getParentRef().getId());
            logger.debug("Current value getNavigator().getCurrentNodeId() noderef Id: " + this.navigator.getCurrentNodeId());
         }

         if (nodeEvent.IsParent == false)
         {
            // a descendant of the displayed node was selected
            // first refresh based on the parent and add to the breadcrumb
            updateUILocation(parentAssocRef.getParentRef());

            // now add our selected node
            updateUILocation(nodeRef);
         }
         else
         {
            // else the parent ellipses i.e. the displayed node was selected
            updateUILocation(nodeRef);
         }
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {nodeRef.getId()}) );
      }
   }

   /**
    * Action event called by all Browse actions that need to setup a Space context
    * before an action page/wizard is called. The context will be a Node in setActionSpace() which
    * can be retrieved on the action page from BrowseBean.getActionSpace().
    *
    * @param event   ActionEvent
    */
   public void setupSpaceAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      setupSpaceAction(id, true);
   }

   /**
    * Public helper to setup action pages with Space context
    *
    * @param id     of the Space node to setup context for
    */
   public void setupSpaceAction(String id, boolean invalidate)
   {
      if (id != null && id.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup for action, setting current space to: " + id);

         try
         {
            // create the node ref, then our node representation
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            Node node = new Node(ref);

            // resolve icon in-case one has not been set
            node.addPropertyResolver("icon", this.resolverSpaceIcon);

            // prepare a node for the action context
            setActionSpace(node);

            // setup the dispatch context in case it is required
            this.navigator.setupDispatchContext(node);
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         }
      }
      else
      {
         setActionSpace(null);
      }

      // clear the UI state in preparation for finishing the next action
      if (invalidate == true)
      {
         // use the context service to notify all registered beans
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
   }

   /**
    * Acrtion event called by Delete Space actions. We setup the action space as normal, then prepare
    * any special case message string to be shown to the user if they are trying to delete specific spaces.
    */
   public void setupDeleteAction(ActionEvent event)
   {
      String message = null;

      setupSpaceAction(event);

      Node node = getActionSpace();
      if (node != null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         NodeRef companyRootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId(fc));
         if (node.getNodeRef().equals(companyRootRef))
         {
            message = Application.getMessage(fc, MSG_DELETE_COMPANYROOT);
         }
      }

      setDeleteMessage(message);
   }

   /**
    * Action event called by all actions that need to setup a Content Document context on the
    * BrowseBean before an action page/wizard is called. The context will be a Node in
    * setDocument() which can be retrieved on the action page from BrowseBean.getDocument().
    */
   public void setupContentAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      setupContentAction(params.get("id"), true);
   }

   /**
    * Action event called by all actions that need to setup a <b>Multilingual</b> Content Document context on the
    * BrowseBean before an action page/wizard is called. The context will be a Node in
    * setDocument() which can be retrieved on the action page from BrowseBean.getDocument().
    */
   public void setupMLContainerContentAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();

      String id = params.get("id");

      NodeRef translation =  new NodeRef(Repository.getStoreRef(), id);

      // remember the bean from which the action comes
      FacesContext fc = FacesContext.getCurrentInstance();
      DocumentDetailsDialog docDetails = (DocumentDetailsDialog)FacesHelper.getManagedBean(fc, "DocumentDetailsDialog");
      docDetails.setTranslationDocument(new MapNode(translation));
      MultilingualManageDialog mmDialog = (MultilingualManageDialog)FacesHelper.getManagedBean(fc, "MultilingualManageDialog");
      mmDialog.setTranslationDocument(docDetails.getTranslationDocument());

      // set the ml container as the current document
      NodeRef mlContainer = getMultilingualContentService().getTranslationContainer(translation);

      setupContentAction(mlContainer.getId(), true);
   }

   /**
    * Public helper to setup action pages with content context
    *
    * @param id     of the content node to setup context for
    */
   public void setupContentAction(String id, boolean invalidate)
   {
      if (id != null && id.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup for action, setting current document to: " + id);

         try
         {
            // create the node ref, then our node representation
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            Node node = new MapNode(ref);

            // store the URL to for downloading the content
            if (ApplicationModel.TYPE_FILELINK.equals(node.getType()))
            {
               node.addPropertyResolver("url", this.resolverLinkDownload);
               node.addPropertyResolver("downloadUrl", this.resolverLinkDownload);
            }
            else
            {
               node.addPropertyResolver("url", this.resolverDownload);
               node.addPropertyResolver("downloadUrl", this.resolverDownload);
            }
            node.addPropertyResolver("webdavUrl", this.resolverWebdavUrl);
            node.addPropertyResolver("cifsPath", this.resolverCifsPath);
            node.addPropertyResolver("fileType32", this.resolverFileType32);
            node.addPropertyResolver("mimetype", this.resolverMimetype);
            node.addPropertyResolver("encoding", this.resolverEncoding);
            node.addPropertyResolver("size", this.resolverSize);
            node.addPropertyResolver("lang", this.resolverLang);

            for (NodeEventListener listener : getNodeEventListeners())
            {
               listener.created(node, node.getType());
            }

            // get hold of the DocumentDetailsDialog and reset it
            DocumentDetailsDialog docDetails = (DocumentDetailsDialog)FacesContext.getCurrentInstance().
               getExternalContext().getSessionMap().get("DocumentDetailsDialog");
            if (docDetails != null)
            {
               docDetails.reset();
            }

            // remember the document
            setDocument(node);

            // setup the dispatch context in case it is required
            this.navigator.setupDispatchContext(node);
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
            throw new AbortProcessingException("Invalid node reference");
         }
      }
      else
      {
         setDocument(null);
      }

      // clear the UI state in preparation for finishing the next action
      if (invalidate == true)
      {
         // use the context service to notify all registered beans
         UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      }
   }

   /**
    * Removes the given node from the breadcrumb i.e. following a delete
    *
    * @param node The space to remove from the breadcrumb
    */
   public void removeSpaceFromBreadcrumb(Node node)
   {
      List<IBreadcrumbHandler> location = navigator.getLocation();
      IBreadcrumbHandler handler = location.get(location.size() - 1);
      if (handler instanceof IRepoBreadcrumbHandler)
      {
         // see if the current breadcrumb location is our node
         if ( ((IRepoBreadcrumbHandler)handler).getNodeRef().equals(node.getNodeRef()) == true )
         {
            location.remove(location.size() - 1);

            // now work out which node to set the list to refresh against
            if (location.size() != 0)
            {
               handler = location.get(location.size() - 1);
               
               if (handler instanceof IRepoBreadcrumbHandler)
               {
                  // change the current node Id
                  navigator.setCurrentNodeId(((IRepoBreadcrumbHandler)handler).getNodeRef().getId());
               }
               else
               {
                  // if we don't have access to the NodeRef to go to next then go to the home space
                  navigator.processToolbarLocation(NavigationBean.LOCATION_HOME, false);
               }
            }
            else
            {
               // if there is no breadcrumb left go to the user's home space
               navigator.processToolbarLocation(NavigationBean.LOCATION_HOME, false);
            }
         }
      }
   }

   /**
    * Support for refresh of lists via special case for an External Access URL.
    * these URLs restart the JSF lifecycle but an old UIRichList is restored from
    * the component tree - which needs clearing "late" in the lifecycle process.
    */
   public void externalAccessRefresh()
   {
      this.externalForceRefresh = true;
   }

   /**
    * Save the state of the panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      if (event instanceof ExpandedEvent)
      {
         String id = event.getComponent().getId();
         this.panels.put(id, ((ExpandedEvent)event).State);
      }
   }


   // ------------------------------------------------------------------------------
   // Private helpers

   /**
    * Initialise default values from client configuration
    */
   private void initFromClientConfig()
   {
      // TODO - review implications of these default values on dynamic/MT client: viewsConfig & browseViewMode, as well as page size content/spaces ...
      ConfigService config = Application.getConfigService(FacesContext.getCurrentInstance());

      this.viewsConfig = (ViewsConfigElement)config.getConfig("Views").
            getConfigElement(ViewsConfigElement.CONFIG_ELEMENT_ID);

      this.browseViewMode = this.viewsConfig.getDefaultView(PAGE_NAME_BROWSE);
      int pageSize = this.viewsConfig.getDefaultPageSize(PAGE_NAME_BROWSE, this.browseViewMode);
      setPageSizeContent(pageSize);
      setPageSizeSpaces(pageSize);
   }

   /**
    * @return the Set of NodeEventListeners registered against this bean
    */
   private Set<NodeEventListener> getNodeEventListeners()
   {
      if ((this.nodeEventListeners == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      { 
         Set<NodeEventListener> allNodeEventListeners = new HashSet<NodeEventListener>();		     

         if (Application.isDynamicConfig(FacesContext.getCurrentInstance()) && (this.nodeEventListeners != null))
         {	 
            // for dynamic config, can add/remove node event listeners dynamically ...
            // however, in case anyone is using public methods (add/removeNodeEventListener)
            // we merge list here with list returned from the config
            allNodeEventListeners.addAll(this.nodeEventListeners);
         }

         FacesContext fc = FacesContext.getCurrentInstance();
         Config listenerConfig = Application.getConfigService(fc).getConfig("Node Event Listeners");
         if (listenerConfig != null)
         {
            ConfigElement listenerElement = listenerConfig.getConfigElement("node-event-listeners");
            if (listenerElement != null)
            {
               for (ConfigElement child : listenerElement.getChildren())
               {
                  if (child.getName().equals("listener"))
                  {
                     // retrieved the JSF Managed Bean identified in the config
                     String listenerName = child.getValue().trim();
                     Object bean = FacesHelper.getManagedBean(fc, listenerName);
                     if (bean instanceof NodeEventListener)
                     {
                        allNodeEventListeners.add((NodeEventListener)bean);
                     }
                  }
               }
            }
         }

         if (Application.isDynamicConfig(FacesContext.getCurrentInstance()))
         {	     
            return allNodeEventListeners;
         }
         else
         {
            this.nodeEventListeners = allNodeEventListeners;
         }
      }
      return this.nodeEventListeners;
   }

   /**
    * Refresh the UI after a Space selection change. Adds the selected space to the breadcrumb
    * location path and also updates the list components in the UI.
    *
    * @param ref     NodeRef of the selected space
    */
   public void updateUILocation(NodeRef ref)
   {
      // get the current breadcrumb location and append a new handler to it
      // our handler know the ID of the selected node and the display label for it
      List<IBreadcrumbHandler> location = this.navigator.getLocation();
      if (location.size() != 0)
      {
         // attempt to find the ID - if it's already in the breadcrumb then we
         // navigate directly to that node - rather than add duplication to the breadcrumb path
         boolean foundNode = false;
         for (int i=0; i<location.size(); i++)
         {
            IBreadcrumbHandler element = location.get(i);
            if (element instanceof IRepoBreadcrumbHandler)
            {
               NodeRef nodeRef = ((IRepoBreadcrumbHandler)element).getNodeRef();
               if (ref.equals(nodeRef) == true)
               {
                  // TODO: we should be able to do this - but the UIBreadcrumb component modifies
                  //       it's own internal value when clicked - then uses that from then on!
                  //       the other ops are using the same List object and modding it directly.
                  //List<IBreadcrumbHandler> newLocation = new ArrayList<IBreadcrumbHandler>(i+1);
                  //newLocation.addAll(location.subList(0, i + 1));
                  //this.navigator.setLocation(newLocation);
                  // TODO: but instead for now we do this:
                  int count = location.size();
                  for (int n=i+1; n<count; n++)
                  {
                     location.remove(i+1);
                  }

                  foundNode = true;
                  break;
               }
            }
         }

         // add new node to the end of the existing breadcrumb
         if (foundNode == false)
         {
            FacesContext context = FacesContext.getCurrentInstance(); 
            String breadcrumbMode = Application.getClientConfig(context).getBreadcrumbMode();
            
            if (ClientConfigElement.BREADCRUMB_LOCATION.equals(breadcrumbMode))
            {
               // if the breadcrumb is in "location" mode set the breadcrumb
               // to the full path to the node
               
               // TODO: check the end of the current breadcrumb, if the given
               //       node is a child then we can shortcut the build of the
               //       whole path.
               
               Repository.setupBreadcrumbLocation(context, this.navigator, location, ref);
            }
            else
            {
               // if the breadcrum is in "path" mode just add the given item to the end
               String name = Repository.getNameForNode(this.getNodeService(), ref);
               location.add(new BrowseBreadcrumbHandler(ref, name));
            }
         }
      }
      else
      {
         // special case to add first item to the location
         String name = Repository.getNameForNode(this.getNodeService(), ref);
         location.add(new BrowseBreadcrumbHandler(ref, name));
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Updated breadcrumb: " + location);

      // set the current node Id ready for page refresh
      this.navigator.setCurrentNodeId(ref.getId());

      // set up the dispatch context for the navigation handler
      this.navigator.setupDispatchContext(new Node(ref));

      // inform any listeners that the current space has changed
      UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();

      navigateBrowseScreen();
   }

   /**
    * Invalidate list component state after an action which changes the UI context
    */
   private void invalidateComponents()
   {
      if (logger.isDebugEnabled())
         logger.debug("Invalidating browse components...");

      // clear the value for the list components - will cause re-bind to it's data and refresh
      if (this.contentRichList != null)
      {
         this.contentRichList.setValue(null);
         if (this.navigator.getSearchContext() != null)
         {
            // clear the sorting mode so the search results are displayed in default 'score' order
            this.contentRichList.clearSort();
         }
      }
      if (this.spacesRichList != null)
      {
         this.spacesRichList.setValue(null);
         if (this.navigator.getSearchContext() != null)
         {
            // clear the sorting mode so the search results are displayed in default 'score' order
            this.spacesRichList.clearSort();
         }
      }

      // clear the storage of the last set of nodes
      this.containerNodes = null;
      this.contentNodes = null;
      this.parentContainerNodes = null;
   }

   /**
    * @return whether the current View ID is the "browse" screen
    */
   private boolean isViewCurrent()
   {
      return (FacesContext.getCurrentInstance().getViewRoot().getViewId().equals(BROWSE_VIEW_ID));
   }

   /**
    * Perform navigation to the browse screen if it is not already the current View
    */
   private void navigateBrowseScreen()
   {
      String outcome = null;

      if (isViewCurrent() == false)
      {
         outcome = "browse";
      }

      FacesContext fc = FacesContext.getCurrentInstance();
      fc.getApplication().getNavigationHandler().handleNavigation(fc, null, outcome);
   }

   /**
    * Event handler used when a file is being deleted, checks that the node
    * does not have an associated working copy.
    * 
    * @param event The event
    */
   public void deleteFile(ActionEvent event)
   {
      setupContentAction(event);
      
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();

      String ref = params.get("ref");
      if (ref != null && ref.length() > 0)
      {
         NodeRef nodeRef = new NodeRef(ref);
         
         NodeRef workingCopyNodeRef = getCheckOutCheckInService().getWorkingCopy(nodeRef);
         
         if (workingCopyNodeRef != null)
         {
            // if node has a working copy setup error message and return
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                     FacesContext.getCurrentInstance(), MSG_CANNOT_DELETE_NODE_HAS_WORKING_COPY),
                     new Object[] {getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME)}));
            return;
         }
         
         // if there isn't a working copy go to normal delete dialog
         boolean hasMultipleParents = false;
         boolean showDeleteAssocDialog = false;
         
         // get type of node being deleted
         Node node = this.getDocument();
         QName type = node.getType();
         TypeDefinition typeDef = this.dictionaryService.getType(type);
         
         // determine if the node being delete has multiple parents
         if (!type.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER) &&
             !node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION) &&
             !node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) &&
             !type.equals(ContentModel.TYPE_LINK) &&
             !this.dictionaryService.isSubClass(typeDef.getName(), ContentModel.TYPE_LINK))
         {
            List<ChildAssociationRef> parents = this.nodeService.getParentAssocs(node.getNodeRef(), 
                     ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            if (parents != null && parents.size() > 1)
            {
               hasMultipleParents = true;
            }
         }
         
         // determine which delete dialog to display
         if (this.navigator.getSearchContext() == null && hasMultipleParents)
         {
            // if we are not in a search and the node has multiple parents
            // see if the current node has the primary parent association
            NodeRef parentSpace = this.navigator.getCurrentNode().getNodeRef();
            ChildAssociationRef assoc = this.nodeService.getPrimaryParent(node.getNodeRef());
            
            // show delete assoc dialog if the current space is not the primary parent for the node
            showDeleteAssocDialog = !parentSpace.equals(assoc.getParentRef());
         }
         
         // show the appropriate dialog
         FacesContext fc = FacesContext.getCurrentInstance();
         if (showDeleteAssocDialog)
         {
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:deleteFileAssoc");
         }
         else
         {
            final Map<String, String> dialogParams = new HashMap<String, String>(1);
            dialogParams.put("hasMultipleParents", Boolean.toString(hasMultipleParents));
            Application.getDialogManager().setupParameters(dialogParams);
            fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:deleteFile");
         }
      }
   }

   /**
    * Handles the deleteSpace action by deciding which delete dialog to display
    */
   public void deleteSpace(ActionEvent event)
   {
      setupDeleteAction(event);
      
      boolean hasMultipleParents = false;
      boolean showDeleteAssocDialog = false;
      
      // determine if the node being delete has multiple parents
      Node node = this.getActionSpace();
      List<ChildAssociationRef> parents = this.nodeService.getParentAssocs(node.getNodeRef(), 
               ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
      if (parents != null && parents.size() > 1)
      {
         hasMultipleParents = true;
      }
      
      // determine which delete dialog to display
      if (this.navigator.getSearchContext() == null && hasMultipleParents)
      {
         // if we are not in a search and the node has multiple parents
         // see if the current node has the primary parent association
         NodeRef parentSpace = this.navigator.getCurrentNode().getNodeRef();
         ChildAssociationRef assoc = this.nodeService.getPrimaryParent(node.getNodeRef());
         
         // show delete assoc dialog if the current space is not the primary parent for the node
         showDeleteAssocDialog = !parentSpace.equals(assoc.getParentRef());
      }
      
      // show the appropriate dialog
      FacesContext fc = FacesContext.getCurrentInstance();
      if (showDeleteAssocDialog)
      {
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:deleteSpaceAssoc");
      }
      else
      {
         final Map<String, String> dialogParams = new HashMap<String, String>(1);
         dialogParams.put("hasMultipleParents", Boolean.toString(hasMultipleParents));
         Application.getDialogManager().setupParameters(dialogParams);
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "dialog:deleteSpace");
      }
   }
   
   // ------------------------------------------------------------------------------
   // Inner classes

   /**
    * Class to handle breadcrumb interaction for Browse pages
    */
   private class BrowseBreadcrumbHandler implements IRepoBreadcrumbHandler
   {
      private static final long serialVersionUID = 3833183653173016630L;

      /**
       * Constructor
       *
       * @param NodeRef    The NodeRef for this browse navigation element
       * @param label      Element label
       */
      public BrowseBreadcrumbHandler(NodeRef nodeRef, String label)
      {
         this.label = label;
         this.nodeRef = nodeRef;
      }

      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         return this.label;
      }

      /**
       * @see org.alfresco.web.ui.common.component.IBreadcrumbHandler#navigationOutcome(org.alfresco.web.ui.common.component.UIBreadcrumb)
       */
      @SuppressWarnings("unchecked")
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         // All browse breadcrumb element relate to a Node Id - when selected we
         // set the current node id
         navigator.setCurrentNodeId(this.nodeRef.getId());
         navigator.setLocation( (List)breadcrumb.getValue() );

         // setup the dispatch context
         navigator.setupDispatchContext(new Node(this.nodeRef));

         // inform any listeners that the current space has changed
         UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();

         // return to browse page if required
         return (isViewCurrent() ? null : "browse");
      }

      public NodeRef getNodeRef()
      {
         return this.nodeRef;
      }

      private NodeRef nodeRef;
      private String label;
   }


   // ------------------------------------------------------------------------------
   // Private data

   private static Log logger = LogFactory.getLog(BrowseBean.class);
   
   /** Browse screen view ID */
   public static final String BROWSE_VIEW_ID    = "/jsp/browse/browse.jsp";
   public static final String CATEGORY_VIEW_ID  = "/jsp/browse/category-browse.jsp";

   /** Small icon default name */
   public static final String SPACE_SMALL_DEFAULT = "space_small";

   private static final String VIEWMODE_DASHBOARD = "dashboard";
   private static final String PAGE_NAME_BROWSE = "browse";

   /** I18N messages */
   private static final String MSG_DELETE_COMPANYROOT = "delete_companyroot_confirm";
   public static final String MSG_SEARCH_MINIMUM      = "search_minimum";
   private static final String MSG_CANNOT_DELETE_NODE_HAS_WORKING_COPY = "cannot_delete_node_has_working_copy";

   /** The NodeService to be used by the bean */
   private transient NodeService nodeService;

   /** The CheckOutCheckInService to be used by the bean */
   private transient CheckOutCheckInService checkOutCheckInService;

   /** The SearchService to be used by the bean */
   private transient SearchService searchService;

   /** The LockService to be used by the bean */
   private transient LockService lockService;

   /** The NavigationBean bean reference */
   protected NavigationBean navigator;

   /** The UserPreferencesBean to be used by the bean */
   protected UserPreferencesBean userPreferencesBean;

   /** The DictionaryService bean reference */
   private transient DictionaryService dictionaryService;

   /** The file folder service */
   private transient FileFolderService fileFolderService;

   /** The Multilingual Content Service */
   private transient MultilingualContentService multilingualContentService;

   /** Views configuration object */
   protected ViewsConfigElement viewsConfig = null;

   /** Listeners for Node events */
   protected Set<NodeEventListener> nodeEventListeners = null;

   /** Collapsable Panel state */
   private Map<String, Boolean> panels = new HashMap<String, Boolean>(4, 1.0f);

   /** Component references */
   protected UIRichList spacesRichList;
   protected UIRichList contentRichList;
   private UIStatusMessage statusMessage;

   /** Transient lists of container and content nodes for display */
   protected List<Node> containerNodes = null;
   protected List<Node> contentNodes = null;
   protected List<Node> parentContainerNodes = null;

   /** The current space and it's properties - if any */
   protected Node actionSpace;

   /** The current document */
   protected Node document;

   /** Special message to display when user deleting certain folders e.g. Company Home */
   private String deleteMessage;

   /** The current browse view mode - set to a well known IRichListRenderer identifier */
   private String browseViewMode;

   /** The current browse view page sizes */
   private int pageSizeSpaces;
   private int pageSizeContent;
   private String pageSizeSpacesStr;
   private String pageSizeContentStr;

   /** True if current space has a dashboard (template) view available */
   private boolean dashboardView;

   private boolean externalForceRefresh = false;
}