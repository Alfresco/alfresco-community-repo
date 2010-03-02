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
package org.alfresco.web.bean.trashcan;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.data.UIRichList;

public class TrashcanDialogProperty implements Serializable
{
    private static final long serialVersionUID = -8007742798807270570L;
    
    private static final String FILTER_DATE_ALL = "all";
    private static final String FILTER_USER_ALL = "all";

    /** NodeArchiveService bean reference */
    transient private NodeArchiveService nodeArchiveService;

    /** Component reference for Deleted Items RichList control */
    protected UIRichList itemsRichList;

    /** Search text */
    private String searchText = null;

    /** We show an empty list until a Search or Show All is executed */
    private boolean showItems = false;

    private boolean fullTextSearch = false;

    /** Currently listed items */
    private List<Node> listedItems = Collections.<Node> emptyList();

    private List<Node> successItems = Collections.<Node> emptyList();

    private List<Node> failureItems = Collections.<Node> emptyList();

    /** Current action context Node */
    private Node actionNode;

    /** Root node to the spaces store archive store */
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

    /**
     * @param nodeArchiveService The nodeArchiveService to set.
     */
    public void setNodeArchiveService(NodeArchiveService nodeArchiveService)
    {
        this.nodeArchiveService = nodeArchiveService;
    }

    /**
     * @return Returns the itemsRichList.
     */
    public UIRichList getItemsRichList()
    {
        return this.itemsRichList;
    }

    /**
     * @param itemsRichList The itemsRichList to set.
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
     * @param destination The alternative destination to use if recovery fails.
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
     * @return count of the items that failed to recover
     */
    public int getFailureItemsCount()
    {
        return this.failureItems.size();
    }

    /**
     * @param node The item context for the current action
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

    public boolean isShowItems()
    {
        return showItems;
    }

    public void setShowItems(boolean showItems)
    {
        this.showItems = showItems;
    }

    public boolean isFullTextSearch()
    {
        return fullTextSearch;
    }

    public void setFullTextSearch(boolean fullTextSearch)
    {
        this.fullTextSearch = fullTextSearch;
    }

    public List<Node> getSuccessItems()
    {
        return successItems;
    }

    public void setSuccessItems(List<Node> successItems)
    {
        this.successItems = successItems;
    }

    public List<Node> getFailureItems()
    {
        return failureItems;
    }

    public void setFailureItems(List<Node> failureItems)
    {
        this.failureItems = failureItems;
    }

    public Node getActionNode()
    {
        return actionNode;
    }

    public void setActionNode(Node actionNode)
    {
        this.actionNode = actionNode;
    }

    public NodeRef getArchiveRootRef()
    {
        return archiveRootRef;
    }

    public void setArchiveRootRef(NodeRef archiveRootRef)
    {
        this.archiveRootRef = archiveRootRef;
    }

    public boolean isInProgress()
    {
        return inProgress;
    }

    public void setInProgress(boolean inProgress)
    {
        this.inProgress = inProgress;
    }

    /**
     *@return nodeArchiveService
     */
    public NodeArchiveService getNodeArchiveService()
    {
       //check for null for cluster environment
        if (nodeArchiveService == null)
        {
           nodeArchiveService = (NodeArchiveService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "nodeArchiveService");
        }
        return nodeArchiveService;
    }

}
