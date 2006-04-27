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

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.data.UIRichList;

/**
 * @author Kevin Roast
 */
public class TrashcanBean implements IContextListener
{
   /** NodeService bean reference */
   protected NodeService nodeService;

   /** SearchService bean reference */
   protected SearchService searchService;
   
   /** Component reference for Deleted Items RichList control */
   protected UIRichList itemsRichList;
   
   /** Search text */
   private String searchText;
   
   
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
    * @param searchService      the search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
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
    * @return the list of deleted items to display
    */
   public List<Node> getItems()
   {
      // TODO: need the following MapNode properties:
      //          deletedDate, locationPath, displayPath, deletedUsername [only for admin user]
      // TODO: get deleted items from deleted items store
      //       use a search - also use filters by name/username
      return Collections.<Node>emptyList();
   }
   
   
   // ------------------------------------------------------------------------------
   // Action handlers
   
   // TODO:
   //       need the following navigation outcomes
   // DONE     deleteItem, recoverItem, recoverAllItems, deleteAllItems, recoverListedItems, deleteListedItems
   //       need the following Action Handlers:
   //          deleteItemOK, recoverItemOK, deleteAllItemsOK, recoverAllItemsOK, recoverListedItemsOK, deleteListedItemsOK
   //       and following Action Event Handlers:
   //          setupItemAction, search
   //       and following getters:
   //          listedItems, item (setup by setupItemAction!)
   
   
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
   }
}
