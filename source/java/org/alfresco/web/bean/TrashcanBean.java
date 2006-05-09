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
package org.alfresco.web.bean;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.QNameNodeMap;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;

/**
 * @author Kevin Roast
 */
public class TrashcanBean implements IContextListener
{
   private static final String MSG_RECOVERED_ITEM_INTEGRITY = "recovered_item_integrity";
   private static final String MSG_RECOVERED_ITEM_PERMISSION = "recovered_item_permission";
   private static final String MSG_RECOVERED_ITEM_PARENT = "recovered_item_parent";
   private static final String MSG_RECOVERED_ITEM_FAILURE = "recovered_item_failure";
   private static final String MSG_RECOVERED_ITEM_SUCCESS = "recovered_item_success";
   
   private static final String OUTCOME_DIALOGCLOSE = "dialog:close";
   
   private static final String RICHLIST_ID = "trashcan-list";
   private static final String RICHLIST_MSG_ID = "trashcan" + ':' + RICHLIST_ID;
   
   private final static String NAME_ATTR   = Repository.escapeQName(ContentModel.PROP_NAME);
   
   private final static String SEARCH_ALL  = "PARENT:\"%s\" AND ASPECT:\"%s\"";
   private final static String SEARCH_NAME = "PARENT:\"%s\" AND ASPECT:\"%s\" AND (@" + NAME_ATTR + ":*%s* TEXT:%s)";
   private final static String SEARCH_NAME_QUOTED = "PARENT:\"%s\" AND ASPECT:\"%s\" AND (@" + NAME_ATTR + ":\"%s\" TEXT:\"%s\")";
   
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
   
   /** Currently listed items */
   private List<Node> listedItems = Collections.<Node>emptyList();
   
   /** Current action context Node */
   private Node actionNode;
   
   /** Root node to the spaces store archive store*/
   private NodeRef archiveRootRef = null;
   
   
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
            String query = getSearchQuery();
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
                  
                  if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true && 
                      this.dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
                  {
                     MapNode node = new MapNode(nodeRef, this.nodeService, false);
                     node.addPropertyResolver("locationPath", resolverLocationPath);
                     node.addPropertyResolver("displayPath", resolverDisplayPath);
                     node.addPropertyResolver("deletedDate", resolverDeletedDate);
                     node.addPropertyResolver("deletedBy", resolverDeletedBy);
                     node.addPropertyResolver("typeIcon", this.resolverSmallIcon);
                     itemNodes.add(node);
                  }
                  else
                  {
                     MapNode node = new MapNode(nodeRef, this.nodeService, false);
                     node.addPropertyResolver("locationPath", resolverLocationPath);
                     node.addPropertyResolver("displayPath", resolverDisplayPath);
                     node.addPropertyResolver("deletedDate", resolverDeletedDate);
                     node.addPropertyResolver("deletedBy", resolverDeletedBy);
                     node.addPropertyResolver("typeIcon", this.resolverFileType16);
                     itemNodes.add(node);
                  }
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
         //ChildAssociationRef childRef = (ChildAssociationRef)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
         //return nodeService.getPath(childRef.getChildRef());
         return (Path)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PATH);
      }
   };
   
   private NodePropertyResolver resolverDisplayPath = new NodePropertyResolver() {
      public Object get(Node node) {
         //ChildAssociationRef childRef = (ChildAssociationRef)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
         return Repository.getDisplayPath((Path)node.getProperties().get(ContentModel.PROP_ARCHIVED_ORIGINAL_PATH));
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
   
   
   // ------------------------------------------------------------------------------
   // Action handlers
   
   // TODO:
   //       need the following Action Handlers:
   //          deleteItemOK, recoverItemOK, deleteAllItemsOK, recoverAllItemsOK, recoverListedItemsOK, deleteListedItemsOK
   
   /**
    * Search the deleted item store by name
    */
   public void search(ActionEvent event)
   {
      // simply clear the current list and refresh the screen
      // the search query text will be found and processed by the getItems() method
      contextUpdated();
      this.showItems = true;
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
            
            // resolve icon in-case one has not been set
            //node.addPropertyResolver("icon", this.resolverSpaceIcon);
            
            // prepare a node for the action context
            setItem(node);
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
            
            RestoreNodeReport report = this.nodeArchiveService.restoreArchivedNode(item.getNodeRef());
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
    * Action handler to reset all filters and search
    */
   public void resetAll(ActionEvent event)
   {
      // TODO: reset all filter and search
   }
   
   /**
    * Action handler to initially setup the trashcan screen
    */
   public void setupTrashcan(ActionEvent event)
   {
      contextUpdated();
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
   private String getSearchQuery()
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
            query = String.format(SEARCH_NAME, archiveRootRef, ContentModel.ASPECT_ARCHIVED, safeText, safeText);
         }
         else
         {
            query = String.format(SEARCH_NAME_QUOTED, archiveRootRef, ContentModel.ASPECT_ARCHIVED, safeText, safeText);
         }
      }
      return query;
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
}
