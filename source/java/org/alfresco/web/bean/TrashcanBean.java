/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.bean;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.search.impl.lucene.QueryParser;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.QNameNodeMap;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;

/**
 * Backing bean for the Manage Deleted Items (soft delete and archiving) pages.
 * 
 * @author Kevin Roast
 */
public class TrashcanBean implements IContextListener
{
   private static final String MSG_DELETED_ITEMS_FOR = "deleted_items_for";
   private static final String MSG_DELETED_ITEMS = "deleted_items";
   private static final String MSG_RECOVERED_ITEM_INTEGRITY = "recovered_item_integrity";
   private static final String MSG_RECOVERED_ITEM_PERMISSION = "recovered_item_permission";
   private static final String MSG_RECOVERED_ITEM_PARENT = "recovered_item_parent";
   private static final String MSG_RECOVERED_ITEM_FAILURE = "recovered_item_failure";
   private static final String MSG_RECOVERED_ITEM_INTEGRITY_S = "recovered_item_integrity_short";
   private static final String MSG_RECOVERED_ITEM_PERMISSION_S = "recovered_item_permission_short";
   private static final String MSG_RECOVERED_ITEM_PARENT_S = "recovered_item_parent_short";
   private static final String MSG_RECOVERED_ITEM_FAILURE_S = "recovered_item_failure_short";
   private static final String MSG_RECOVERED_ITEM_SUCCESS = "recovered_item_success";
   private static final String MSG_RECOVERY_REASON = "recovery_report_reason";
   private static final String MSG_LOCATION = "location";
   private static final String MSG_NAME = "name";
   
   private static final String PROP_RECOVERSTATUS = "recoverstatus";
   
   private static final String FILTER_DATE_ALL    = "all";
   private static final String FILTER_DATE_TODAY  = "today";
   private static final String FILTER_DATE_WEEK   = "week";
   private static final String FILTER_DATE_MONTH  = "month";
   private static final String FILTER_USER_ALL    = "all";
   private static final String FILTER_USER_USER   = "user";
   
   private static final String OUTCOME_DIALOGCLOSE = "dialog:close";
   private static final String OUTCOME_RECOVERY_REPORT = "recoveryReport";
   
   private static final String RICHLIST_ID = "trashcan-list";
   private static final String RICHLIST_MSG_ID = "trashcan" + ':' + RICHLIST_ID;
   
   private final static String NAME_ATTR = Repository.escapeQName(ContentModel.PROP_NAME);
   private final static String USER_ATTR = Repository.escapeQName(ContentModel.PROP_ARCHIVED_BY);
   private final static String DATE_ATTR = Repository.escapeQName(ContentModel.PROP_ARCHIVED_DATE);
   
   private final static String SEARCH_ALL  = "PARENT:\"%s\" AND ASPECT:\"%s\"";
   private final static String SEARCH_NAME = "PARENT:\"%s\" AND ASPECT:\"%s\" AND @" + NAME_ATTR + ":*%s*";
   private final static String SEARCH_TEXT = "PARENT:\"%s\" AND ASPECT:\"%s\" AND TEXT:%s";
   private final static String SEARCH_NAME_QUOTED = "PARENT:\"%s\" AND ASPECT:\"%s\" AND @" + NAME_ATTR + ":\"%s\"";
   private final static String SEARCH_TEXT_QUOTED = "PARENT:\"%s\" AND ASPECT:\"%s\" AND TEXT:\"%s\"";
   private final static String SEARCH_USERPREFIX  = "@" + USER_ATTR + ":%s AND ";
   
   /** NodeService bean reference */
   protected NodeService nodeService;
   
   /** NodeArchiveService bean reference */
   protected NodeArchiveService nodeArchiveService;

   /** SearchService bean reference */
   protected SearchService searchService;
   
   /** The DictionaryService bean reference */
   protected DictionaryService dictionaryService;
   
   /** Component reference for Deleted Items RichList control */
   protected UIRichList itemsRichList;
   
   /** Search text */
   private String searchText = null;
   
   /** We show an empty list until a Search or Show All is executed */
   private boolean showItems = false;
   
   private boolean fullTextSearch = false;
   
   /** Currently listed items */
   private List<Node> listedItems = Collections.<Node>emptyList();
   
   private List<Node> successItems = Collections.<Node>emptyList();
   
   private List<Node> failureItems = Collections.<Node>emptyList();
   
   /** Current action context Node */
   private Node actionNode;
   
   /** Root node to the spaces store archive store*/
   private NodeRef archiveRootRef = null;
   
   /** Alternative destination for recovered items */
   private NodeRef destination = null;
   
   /** Date filter selection */
   private String dateFilter = FILTER_DATE_ALL;
   
   /** User filter selection */
   private String userFilter = FILTER_USER_ALL;
   
   /** User filter search box text */
   private String userSearchText = null;
   
   private boolean inProgress = false;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * @param nodeService        The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param searchService      The search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @param nodeArchiveService The nodeArchiveService to set.
    */
   public void setNodeArchiveService(NodeArchiveService nodeArchiveService)
   {
      this.nodeArchiveService = nodeArchiveService;
   }
   
   /**
    * @param dictionaryService The DictionaryService to set.
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }

   /**
    * @return Returns the itemsRichList.
    */
   public UIRichList getItemsRichList()
   {
      return this.itemsRichList;
   }

   /**
    * @param itemsRichList  The itemsRichList to set.
    */
   public void setItemsRichList(UIRichList itemsRichList)
   {
      this.itemsRichList = itemsRichList;
   }
   
   /**
    * @return Returns the searchText.
    */
   public String getSearchText()
   {
      return this.searchText;
   }

   /**
    * @param searchText The searchText to set.
    */
   public void setSearchText(String searchText)
   {
      this.searchText = searchText;
   }
   
   /**
    * @return Returns the alternative destination to use if recovery fails. 
    */
   public NodeRef getDestination()
   {
      return this.destination;
   }
   
   /**
    * @param destination    The alternative destination to use if recovery fails.
    */
   public void setDestination(NodeRef destination)
   {
      this.destination = destination;
   }
   
   /**
    * @return Returns the dateFilter.
    */
   public String getDateFilter()
   {
      return this.dateFilter;
   }

   /**
    * @param dateFilter The dateFilter to set.
    */
   public void setDateFilter(String dateFilter)
   {
      this.dateFilter = dateFilter;
   }

   /**
    * @return Returns the userFilter.
    */
   public String getUserFilter()
   {
      return this.userFilter;
   }

   /**
    * @param userFilter The userFilter to set.
    */
   public void setUserFilter(String userFilter)
   {
      this.userFilter = userFilter;
   }

   /**
    * @return Returns the userSearchText.
    */
   public String getUserSearchText()
   {
      return this.userSearchText;
   }

   /**
    * @param userSearchText The userSearchText to set.
    */
   public void setUserSearchText(String userSearchText)
   {
      this.userSearchText = userSearchText;
   }

   /**
    * @return Message to display in the title of the panel area
    */
   public String getPanelMessage()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      String msg = Application.getMessage(fc, MSG_DELETED_ITEMS);
      if (isAdminUser() == false)
      {
         msg = msg + ' ' + MessageFormat.format(
               Application.getMessage(fc, MSG_DELETED_ITEMS_FOR), Application.getCurrentUser(fc).getUserName());
      }
      return msg;
   }
   
   /**
    * Returns the URL to the content for the current document item
    *  
    * @return Content url to the current document item
    */
   public String getItemBrowserUrl()
   {
      return Utils.generateURL(FacesContext.getCurrentInstance(), getItem(), URLMode.HTTP_INLINE);
   }

   /**
    * Returns the download URL to the content for the current document item
    *  
    * @return Download url to the current document item
    */
   public String getItemDownloadUrl()
   {
      return Utils.generateURL(FacesContext.getCurrentInstance(), getItem(), URLMode.HTTP_DOWNLOAD);
   }
   
   /**
    * Return the Alfresco NodeRef URL for the current item node
    * 
    * @return the Alfresco NodeRef URL
    */
   public String getItemNodeRefUrl()
   {
      return getItem().getNodeRef().toString();
   }
   
   /**
    * @return Returns the listed items.
    */
   public List<Node> getListedItems()
   {
      return this.listedItems;
   }

   /**
    * @param listedItems The listed items to set.
    */
   public void setListedItems(List<Node> listedItems)
   {
      this.listedItems = listedItems;
   }
   
   /**
    * @return HTML table of the listed items
    */
   public String getListedItemsTable()
   {
      return buildItemsTable(getListedItems(), "recoveredItemsList", false, true);
   }
   
   /**
    * @return HTML table of the items successfully recovered 
    */
   public String getSuccessItemsTable()
   {
      return buildItemsTable(this.successItems, "recoveredItemsList", false, false);
   }
   
   /**
    * @return HTML table of the items that failed to recover
    */
   public String getFailureItemsTable()
   {
      return buildItemsTable(this.failureItems, "failedItemsList", true, false);
   }
   
   /**
    * @return count of the items that failed to recover 
    */
   public int getFailureItemsCount()
   {
      return this.failureItems.size();
   }

   /**
    * @param node   The item context for the current action 
    */
   public void setItem(Node node)
   {
      this.actionNode = node;
   }
   
   /**
    * @return the item context for the current action
    */
   public Node getItem()
   {
      return this.actionNode;
   }
   
   /**
    * @return the list of deleted items to display
    */
   public List<Node> getItems()
   {
      // to get deleted items from deleted items store
      // use a search to find the items - also filters by name/username
      List<Node> itemNodes = null;
      
      UserTransaction tx = null;
      ResultSet results = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         // get the root node to the deleted items store
         if (getArchiveRootRef() != null && this.showItems == true)
         {
            String query = buildSearchQuery();
            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query);
            sp.addStore(getArchiveRootRef().getStoreRef());     // the Archived Node store
            
            results = this.searchService.query(sp);
            itemNodes = new ArrayList<Node>(results.length());
         }
         
         if (results != null && results.length() != 0)
         {
            for (ResultSetRow row : results)
            {
               NodeRef nodeRef = row.getNodeRef();
               
               if (this.nodeService.exists(nodeRef))
               {
                  QName type = this.nodeService.getType(nodeRef);
                  
                  MapNode node = new MapNode(nodeRef, this.nodeService, false);
                  
                  node.addPropertyResolver("locationPath", resolverLocationPath);
                  node.addPropertyResolver("displayPath", resolverDisplayPath);
                  node.addPropertyResolver("deletedDate", resolverDeletedDate);
                  node.addPropertyResolver("deletedBy", resolverDeletedBy);
                  node.addPropertyResolver("isFolder", resolverIsFolder);
                  
                  if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true && 
                      this.dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
                  {
                     node.addPropertyResolver("typeIcon", this.resolverSmallIcon);
                  }
                  else
                  {
                     node.addPropertyResolver("typeIcon", this.resolverFileType16);
                  }
                  itemNodes.add(node);
               }
            }
         }
         
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), new Object[] {err.getMessage()}), err );
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      this.listedItems = (itemNodes != null ? itemNodes : Collections.<Node>emptyList());
      
      return this.listedItems;
   }
   
   private NodePropertyResolver resolverLocationPath = new NodePropertyResolver() {
      public Object get(Node node) {
         ChildAssociationRef childRef =
            (ChildAssociationRef)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
         if (nodeService.exists(childRef.getParentRef()))
         {
            return nodeService.getPath(childRef.getParentRef());
         }
         else
         {
            return null;
         }
      }
   };
   
   private NodePropertyResolver resolverDisplayPath = new NodePropertyResolver() {
      public Object get(Node node) {
         ChildAssociationRef childRef =
            (ChildAssociationRef)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
         if (nodeService.exists(childRef.getParentRef()))
         {
            return Repository.getDisplayPath(nodeService.getPath(childRef.getParentRef()), true);
         }
         else
         {
            return "";
         }
      }
   };
   
   private NodePropertyResolver resolverFileType16 = new NodePropertyResolver() {
      public Object get(Node node) {
         return Utils.getFileTypeImage(node.getName(), true);
      }
   };
   
   private NodePropertyResolver resolverSmallIcon = new NodePropertyResolver() {
      public Object get(Node node) {
         QNameNodeMap props = (QNameNodeMap)node.getProperties();
         String icon = (String)props.getRaw("app:icon");
         return "/images/icons/" + (icon != null ? icon + "-16.gif" : BrowseBean.SPACE_SMALL_DEFAULT + ".gif");
      }
   };
   
   private NodePropertyResolver resolverFileType32 = new NodePropertyResolver() {
      public Object get(Node node) {
         return Utils.getFileTypeImage(node.getName(), false);
      }
   };
   
   private NodePropertyResolver resolverLargeIcon = new NodePropertyResolver() {
      public Object get(Node node) {
         QNameNodeMap props = (QNameNodeMap)node.getProperties();
         String icon = (String)props.getRaw("app:icon");
         return "/images/icons/" + (icon != null ? icon : CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME) + ".gif";
      }
   };
   
   private NodePropertyResolver resolverMimetype = new NodePropertyResolver() {
      public Object get(Node node) {
         ContentData content = (ContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
         return (content != null ? content.getMimetype() : null);
      }
   };
   
   private NodePropertyResolver resolverSize = new NodePropertyResolver() {
      public Object get(Node node) {
         ContentData content = (ContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
         return (content != null ? new Long(content.getSize()) : 0L);
      }
   };
   
   private NodePropertyResolver resolverEncoding = new NodePropertyResolver() {
      public Object get(Node node) {
         ContentData content = (ContentData)node.getProperties().get(ContentModel.PROP_CONTENT);
         return (content != null ? content.getEncoding() : null);
      }
   };
   
   private NodePropertyResolver resolverDeletedDate = new NodePropertyResolver() {
      public Object get(Node node) {
         return node.getProperties().get(ContentModel.PROP_ARCHIVED_DATE);
      }
   };
   
   private NodePropertyResolver resolverDeletedBy = new NodePropertyResolver() {
      public Object get(Node node) {
         return node.getProperties().get(ContentModel.PROP_ARCHIVED_BY);
      }
   };
   
   private NodePropertyResolver resolverIsFolder = new NodePropertyResolver() {
      public Object get(Node node) {
         return dictionaryService.isSubClass(node.getType(), ContentModel.TYPE_FOLDER);
      }
   };
   
   
   // ------------------------------------------------------------------------------
   // Action handlers
   
   /**
    * Search the deleted item store by name
    */
   public void searchName(ActionEvent event)
   {
      // simply clear the current list and refresh the screen
      // the search query text will be found and processed by the getItems() method
      contextUpdated();
      this.showItems = true;
      this.fullTextSearch = false;
   }
   
   /**
    * Search the deleted item store by text
    */
   public void searchContent(ActionEvent event)
   {
      // simply clear the current list and refresh the screen
      // the search query text will be found and processed by the getItems() method
      contextUpdated();
      this.showItems = true;
      this.fullTextSearch = true;
   }
   
   /**
    * Action handler to clear the current search results and show all items
    */
   public void clearSearch(ActionEvent event)
   {
      contextUpdated();
      this.searchText = null;
      this.showItems = true;
   }
   
   /**
    * Action handler called to prepare the selected item for an action
    */
   public void setupItemAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         try
         {
            // create the node ref, then our node representation
            NodeRef ref = new NodeRef(getArchiveRootRef().getStoreRef(), id);
            Node node = new Node(ref);
            
            node.addPropertyResolver("locationPath", resolverLocationPath);
            node.addPropertyResolver("deletedDate", resolverDeletedDate);
            node.addPropertyResolver("deletedBy", resolverDeletedBy);
            node.addPropertyResolver("isFolder", resolverIsFolder);
            node.addPropertyResolver("mimetype", resolverMimetype);
            node.addPropertyResolver("size", resolverSize);
            node.addPropertyResolver("encoding", resolverEncoding);
            
            if (this.dictionaryService.isSubClass(node.getType(), ContentModel.TYPE_FOLDER) == true && 
                this.dictionaryService.isSubClass(node.getType(), ContentModel.TYPE_SYSTEM_FOLDER) == false)
            {
               node.addPropertyResolver("icon", this.resolverLargeIcon);
            }
            else
            {
               node.addPropertyResolver("icon", this.resolverFileType32);
            }
            
            // prepare a node for the action context
            setItem(node);
            setDestination(null);
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
         }
      }
      else
      {
         setItem(null);
      }
      
      // clear the UI state in preparation for finishing the next action
      contextUpdated();
   }
   
   /**
    * Action handler to setup actions that act on lists 
    */
   public void setupListAction(ActionEvent event)
   {
      // clear the UI state in preparation for finishing the next action
      setDestination(null);
      contextUpdated();
   }
   
   /**
    * Delete single item OK button handler 
    */
   public String deleteItemOK()
   {
      Node item = getItem();
      if (item != null)
      {
         try
         {
            this.nodeArchiveService.purgeArchivedNode(item.getNodeRef());
            
            FacesContext fc = FacesContext.getCurrentInstance();
            String msg = MessageFormat.format(
                  Application.getMessage(fc, "delete_item_success"), item.getName());
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
            fc.addMessage(RICHLIST_MSG_ID, facesMsg);
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         }
      }
      return OUTCOME_DIALOGCLOSE;
   }
   
   /**
    * Recover single item OK button handler 
    */
   public String recoverItemOK()
   {
      String outcome = null;
      
      Node item = getItem();
      if (item != null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         try
         {
            String msg;
            FacesMessage errorfacesMsg = null;
            
            // restore the node - the user may have requested a restore to a different parent
            RestoreNodeReport report;
            if (this.destination == null)
            {
               report = this.nodeArchiveService.restoreArchivedNode(item.getNodeRef());
            }
            else
            {
               report = this.nodeArchiveService.restoreArchivedNode(item.getNodeRef(), this.destination, null, null);
            }
            switch (report.getStatus())
            {
               case SUCCESS:
                  msg = MessageFormat.format(
                        Application.getMessage(fc, MSG_RECOVERED_ITEM_SUCCESS), item.getName());
                  FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
                  fc.addMessage(RICHLIST_MSG_ID, facesMsg);
                  outcome = OUTCOME_DIALOGCLOSE;
                  break;
               
               case FAILURE_INVALID_PARENT:
                  msg = MessageFormat.format(
                        Application.getMessage(fc, MSG_RECOVERED_ITEM_PARENT), item.getName());
                  errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                  break;
               
               case FAILURE_PERMISSION:
                  msg = MessageFormat.format(
                        Application.getMessage(fc, MSG_RECOVERED_ITEM_PERMISSION), item.getName());
                  errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                  break;
               
               case FAILURE_INTEGRITY:
                  msg = MessageFormat.format(
                        Application.getMessage(fc, MSG_RECOVERED_ITEM_INTEGRITY), item.getName());
                  errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                  break;
               
               default:
                  String reason = report.getCause().getMessage();
                  msg = MessageFormat.format(
                        Application.getMessage(fc, MSG_RECOVERED_ITEM_FAILURE), item.getName(), reason);
                  errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                  break;
            }
            
            // report the failure if one occured we stay on the current screen
            if (errorfacesMsg != null)
            {
               fc.addMessage(null, errorfacesMsg);
            }
         }
         catch (Throwable err)
         {
            // most exceptions will be caught and returned as RestoreNodeReport objects by the service
            String reason = err.getMessage();
            String msg = MessageFormat.format(
                  Application.getMessage(fc, MSG_RECOVERED_ITEM_FAILURE), item.getName(), reason);
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
            fc.addMessage(null, facesMsg);
         }
      }
      
      return outcome;
   }
   
   /**
    * Action handler to recover the list items
    */
   public String recoverListedItemsOK()
   {
      if (inProgress == true) return null;
      
      inProgress = true;
      
      try
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         
         // restore the nodes - the user may have requested a restore to a different parent
         List<NodeRef> nodeRefs = new ArrayList<NodeRef>(this.listedItems.size());
         for (Node node : this.listedItems)
         {
            nodeRefs.add(node.getNodeRef());
         }
         List<RestoreNodeReport> reports;
         if (this.destination == null)
         {
            reports = this.nodeArchiveService.restoreArchivedNodes(nodeRefs);
         }
         else
         {
            reports = this.nodeArchiveService.restoreArchivedNodes(nodeRefs, this.destination, null, null);
         }
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
            tx.begin();
            
            saveReportDetail(reports);
            
            tx.commit();
         }
         catch (Throwable err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            // most exceptions will be caught and returned as RestoreNodeReport objects by the service
            String reason = err.getMessage();
            String msg = MessageFormat.format(
                  Application.getMessage(fc, Repository.ERROR_GENERIC), reason);
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
            fc.addMessage(null, facesMsg);
         }
      }
      finally
      {
         inProgress = false;
      }
      
      return OUTCOME_RECOVERY_REPORT;
   }
   
   /**
    * Action handler called to recover all items from the store (Admin only)
    */
   public String recoverAllItemsOK()
   {
      if (inProgress == true) return null;
      
      inProgress = true;
      
      try
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         
         // restore all nodes - the user may have requested a restore to a different parent
         List<RestoreNodeReport> reports;
         if (this.destination == null)
         {
            reports = this.nodeArchiveService.restoreAllArchivedNodes(Repository.getStoreRef());
         }
         else
         {
            reports = this.nodeArchiveService.restoreAllArchivedNodes(Repository.getStoreRef(), this.destination, null, null);
         }
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
            tx.begin();
            
            saveReportDetail(reports);
            
            tx.commit();
         }
         catch (Throwable err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            // most exceptions will be caught and returned as RestoreNodeReport objects by the service
            String reason = err.getMessage();
            String msg = MessageFormat.format(
                  Application.getMessage(fc, Repository.ERROR_GENERIC), reason);
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
            fc.addMessage(null, facesMsg);
         }
      }
      finally
      {
         inProgress = false;
      }
      
      return OUTCOME_RECOVERY_REPORT;
   }
   
   /**
    * @return outcome to close the main list screen and reset other beans ready for display
    */
   public String close()
   {
      // call beans to update UI context for other screens
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      return OUTCOME_DIALOGCLOSE;
   }
   
   /**
    * Action handler to delete the listed items
    */
   public String deleteListedItemsOK()
   {
      if (inProgress == true) return null;
      
      inProgress = true;
      
      try
      {
         List<NodeRef> nodeRefs = new ArrayList<NodeRef>(this.listedItems.size());
         for (Node node : this.listedItems)
         {
            nodeRefs.add(node.getNodeRef());
         }
         this.nodeArchiveService.purgeArchivedNodes(nodeRefs);
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
      }
      finally
      {
         inProgress = false;
      }
      
      return OUTCOME_DIALOGCLOSE;
   }
   
   /**
    * Action handler to delete all items 
    */
   public String deleteAllItemsOK()
   {
      if (inProgress == true) return null;
      
      inProgress = true;
      
      try
      {
         this.nodeArchiveService.purgeAllArchivedNodes(Repository.getStoreRef());
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
      }
      finally
      {
         inProgress = false;
      }
      
      return OUTCOME_DIALOGCLOSE;
   }
   
   /**
    * Action handler to initially setup the trashcan screen
    */
   public void setupTrashcan(ActionEvent event)
   {
      contextUpdated();
   }
   
   /**
    * Action handler called when the Date filter is changed by the user
    */
   public void dateFilterChanged(ActionEvent event)
   {
      UIModeList filterComponent = (UIModeList)event.getComponent();
      setDateFilter(filterComponent.getValue().toString());
      contextUpdated();
      this.showItems = true;
   }
   
   /**
    * Action handler called when the User filter is changed by the user
    */
   public void userFilterChanged(ActionEvent event)
   {
      UIModeList filterComponent = (UIModeList)event.getComponent();
      setUserFilter(filterComponent.getValue().toString());
      contextUpdated();
      this.showItems = true;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * @return the archive store root node ref
    */
   private NodeRef getArchiveRootRef()
   {
      if (this.archiveRootRef == null)
      {
         this.archiveRootRef = this.nodeArchiveService.getStoreArchiveNode(Repository.getStoreRef());
      }
      return this.archiveRootRef;
   }
   
   /**
    * @return the search query to use when displaying the list of deleted items
    */
   @SuppressWarnings("deprecation")
   private String buildSearchQuery()
   {
      String query;
      if (this.searchText == null || this.searchText.length() == 0)
      {
         // search for ALL items in the archive store
         query = String.format(SEARCH_ALL, archiveRootRef, ContentModel.ASPECT_ARCHIVED);
      }
      else
      {
         // search by name in the archive store
         String safeText = QueryParser.escape(this.searchText);
         if (safeText.indexOf(' ') == -1)
         {
            if (this.fullTextSearch)
            {
               query = String.format(SEARCH_TEXT, archiveRootRef, ContentModel.ASPECT_ARCHIVED, safeText);
            }
            else
            {
               query = String.format(SEARCH_NAME, archiveRootRef, ContentModel.ASPECT_ARCHIVED, safeText);
            }
         }
         else
         {
            if (this.fullTextSearch)
            {
               query = String.format(SEARCH_TEXT_QUOTED, archiveRootRef, ContentModel.ASPECT_ARCHIVED, safeText);
            }
            else
            {
               query = String.format(SEARCH_NAME_QUOTED, archiveRootRef, ContentModel.ASPECT_ARCHIVED, safeText);
            }
         }
      }
      
      // append user search clause
      String username = null;
      if (isAdminUser() == false)
      {
         // prefix the current username
         username = Application.getCurrentUser(FacesContext.getCurrentInstance()).getUserName();
      }
      else if (FILTER_USER_USER.equals(getUserFilter()))
      {
         // append the entered user if admin has requested a search
         username = getUserSearchText();
      }
      if (username != null && username.length() != 0)
      {
         query = String.format(SEARCH_USERPREFIX, username) + query;
      }
      
      // append date search clause
      if (FILTER_DATE_ALL.equals(getDateFilter()) == false)
      {
         Date toDate = new Date();
         Date fromDate = null;
         if (FILTER_DATE_TODAY.equals(getDateFilter()))
         {
            fromDate = new Date(toDate.getYear(), toDate.getMonth(), toDate.getDate(), 0, 0, 0);
         }
         else if (FILTER_DATE_WEEK.equals(getDateFilter()))
         {
            fromDate = new Date(toDate.getTime() - (1000L*60L*60L*24L*7L));
         }
         else if (FILTER_DATE_MONTH.equals(getDateFilter()))
         {
            fromDate = new Date(toDate.getTime() - (1000L*60L*60L*24L*30L));
         }
         if (fromDate != null)
         {
            SimpleDateFormat df = CachingDateFormat.getDateFormat();
            String strFromDate = QueryParser.escape(df.format(fromDate));
            String strToDate = QueryParser.escape(df.format(toDate));
            StringBuilder buf = new StringBuilder(128);
            buf.append("@").append(DATE_ATTR)
               .append(":").append("[").append(strFromDate)
               .append(" TO ").append(strToDate).append("] AND ");
            
            query = buf.toString() + query;
         }
      }
      
      return query;
   }
   
   /**
    * Save the detail of the items that were successfully or unsuccessfully restored
    * 
    * @param reports     The List of RestoreNodeReport objects to walk for results
    */
   private void saveReportDetail(List<RestoreNodeReport> reports)
   {
      // store the results ready for the next dialog page
      this.successItems = new ArrayList<Node>(reports.size());
      this.failureItems = new ArrayList<Node>(reports.size());
      for (RestoreNodeReport report : reports)
      {
         if (RestoreStatus.SUCCESS == report.getStatus())
         {
            Node node = new Node(report.getRestoredNodeRef());
            node.getProperties().put(PROP_RECOVERSTATUS, report.getStatus());
            this.successItems.add(node);
         }
         else
         {
            Node node = new Node(report.getArchivedNodeRef());
            node.getProperties().put(PROP_RECOVERSTATUS, report.getStatus());
            this.failureItems.add(node);
         }
      }
   }
   
   /**
    * Build an HTML table of the items that are to be or have been recovered.   
    * 
    * @param items          List of Node objects to display in the table
    * @param cssClass       CSS style to apply to the table
    * @param report         Set true to report the reason for any failure. This flag requires that the Node
    *                       object has a pseudo property "recoverstatus" containing the RestoreStatus.
    * @param archivedPath   Set true to show the path from the 'sys:archivedOriginalParentAssoc' property,
    *                       else the current Node Path will be used.
    *                   
    * 
    * @return HTML table of node info
    */
   private String buildItemsTable(List<Node> items, String cssClass, boolean report, boolean archivedPath)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      String contextPath = fc.getExternalContext().getRequestContextPath();
      
      StringBuilder buf = new StringBuilder(1024);
      
      // outer table
      buf.append("<table width=100% cellspacing=1 cellpadding=1 border=0 class='");
      buf.append(cssClass);
      buf.append("'>");
      // title row
      buf.append("<tr style='border-bottom:1px'><th></th><th align=left><b>");
      buf.append(Application.getMessage(fc, MSG_NAME));
      buf.append("</b></th>");
      if (report == true)
      {
         buf.append("<th align=left>");
         buf.append(Application.getMessage(fc, MSG_RECOVERY_REASON));
         buf.append("</th>");
      }
      else
      {
         buf.append("<th align=left><b>");
         buf.append(Application.getMessage(fc, MSG_LOCATION));
         buf.append("</b></th>");
      }
      buf.append("</tr>");
      for (Node node : items)
      {
         // listed item rows
         buf.append("<tr><td width=16>");
         String img;
         if (this.dictionaryService.isSubClass(node.getType(), ContentModel.TYPE_FOLDER))
         {
            String icon = (String)node.getProperties().get("app:icon");
            img = "/images/icons/" + (icon != null ? icon + "-16.gif" : BrowseBean.SPACE_SMALL_DEFAULT + ".gif");
         }
         else
         {
            img = Utils.getFileTypeImage(node.getName(), true);
         }
         buf.append("<img width=16 height=16 alt='' src='").append(contextPath).append(img).append("'>");
         buf.append("</td><td>");
         buf.append(node.getName());
         buf.append("</td>");
         
         if (report)
         {
            buf.append("<td>");
            String msg;
            switch ((RestoreStatus)node.getProperties().get(PROP_RECOVERSTATUS))
            {
               case FAILURE_INVALID_PARENT:
                  msg = MSG_RECOVERED_ITEM_PARENT_S;
                  break;
               
               case FAILURE_PERMISSION:
                  msg = MSG_RECOVERED_ITEM_PERMISSION_S;
                  break;
               
               case FAILURE_INTEGRITY:
                  msg = MSG_RECOVERED_ITEM_INTEGRITY_S;
                  break;
               
               default:
                  msg = MSG_RECOVERED_ITEM_FAILURE_S;
                  break;
            }
            buf.append(Application.getMessage(fc, msg));
            buf.append("</td>");
         }
         else
         {
            buf.append("<td>");
            if (archivedPath)
            {
               ChildAssociationRef childRef =
                  (ChildAssociationRef)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
               if (nodeService.exists(childRef.getParentRef()))
               {
                  buf.append(Repository.getNamePath(nodeService, nodeService.getPath(childRef.getParentRef()), null, "/", null));
               }
            }
            else
            {
               buf.append(Repository.getNamePath(nodeService, nodeService.getPath(node.getNodeRef()), null, "/", null));
            }
            buf.append("</td>");
         }

         buf.append("</tr>");
      }
      // end table
      buf.append("</table>");
      
      return buf.toString();
   }
   
   private boolean isAdminUser()
   {
      return Application.getCurrentUser(FacesContext.getCurrentInstance()).isAdmin();
   }
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      if (this.itemsRichList != null)
      {
         this.itemsRichList.setValue(null);
      }
      this.showItems = false;
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
}
