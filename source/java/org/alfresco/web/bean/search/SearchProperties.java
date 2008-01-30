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
package org.alfresco.web.bean.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ExpiringValueCache;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.AdvancedSearchConfigElement;

public class SearchProperties
{
    private static final String MODE_ALL = "all";
    private static final String LOOKIN_ALL = "all";
    private static final String SAVED_SEARCHES_USER = "user";

    /** Client Config reference */
    private AdvancedSearchConfigElement searchConfigElement = null;

    /** Progressive panel UI state */
    private Map<String, Boolean> panels = new HashMap<String, Boolean>(5, 1.0f);

    /** Saved search properties */
    private String searchName;
    private String searchDescription;

    /** custom property names to values */
    private Map<String, Object> customProperties = new HashMap<String, Object>(5, 1.0f);

    /**
     * lookup of custom property QName string to DataTypeDefinition for the property
     */
    private Map<String, DataTypeDefinition> customPropertyLookup = null;

    /** content format list restricting searches */
    private List<SelectItem> contentFormats;

    /** content format selection */
    private String contentFormat;

    /** content type selection */
    private String contentType;

    /** content types for restricting searches */
    private List<SelectItem> contentTypes;

    /** folder type selection */
    private String folderType;

    /** folder types for restricting searches */
    private List<SelectItem> folderTypes;

    /** the text to search for */
    private String text = "";

    /** search mode */
    private String mode = MODE_ALL;

    /** folder lookin mode */
    private String lookin = LOOKIN_ALL;

    /** Space Selector location */
    private NodeRef location = null;

    /** categories to search */
    private List<Node> categories = new ArrayList<Node>(2);

    /** datamodel for table of categories to search */
    private DataModel categoriesDataModel = new ListDataModel();

    /** title attribute to search */
    private String title = null;

    /** description attribute to search */
    private String description = null;

    /** created attribute to search from */
    private Date createdDateFrom = null;

    /** created attribute to search to */
    private Date createdDateTo = null;

    /** modified attribute to search from */
    private Date modifiedDateFrom = null;

    /** modified attribute to search to */
    private Date modifiedDateTo = null;

    /** true to search location children as well as location */
    private boolean locationChildren = true;

    /** author (creator) attribute to search */
    private String author = null;

    private boolean modifiedDateChecked = false;
    private boolean createdDateChecked = false;

    /** cached ref to the global saved searches folder */
    private NodeRef globalSearchesRef = null;

    /** cached ref to the current users saved searches folder */
    private NodeRef userSearchesRef = null;

    /** ID to the last selected saved search */
    private String savedSearch = null;

    /** ModeList component value for selecting user/global searches */
    private String savedSearchMode = SAVED_SEARCHES_USER;

    /** name of the saved search to edit */
    private String editSearchName = null;

    /** form field for saving search as user/global */
    private boolean searchSaveGlobal = false;

    /** auto-expiring cache of the list of saved searches */
    private ExpiringValueCache<List<SelectItem>> cachedSavedSearches = new ExpiringValueCache<List<SelectItem>>();


    public AdvancedSearchConfigElement getSearchConfigElement()
    {
        return searchConfigElement;
    }

    public void setSearchConfigElement(AdvancedSearchConfigElement searchConfigElement)
    {
        this.searchConfigElement = searchConfigElement;
    }

    public Map<String, Boolean> getPanels()
    {
        return panels;
    }

    public void setPanels(Map<String, Boolean> panels)
    {
        this.panels = panels;
    }

    public String getSearchName()
    {
        return searchName;
    }

    public void setSearchName(String searchName)
    {
        this.searchName = searchName;
    }

    public String getSearchDescription()
    {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription)
    {
        this.searchDescription = searchDescription;
    }

    public Map<String, Object> getCustomProperties()
    {
        return customProperties;
    }

    public void setCustomProperties(Map<String, Object> customProperties)
    {
        this.customProperties = customProperties;
    }

    public Map<String, DataTypeDefinition> getCustomPropertyLookup()
    {
        return customPropertyLookup;
    }

    public void setCustomPropertyLookup(Map<String, DataTypeDefinition> customPropertyLookup)
    {
        this.customPropertyLookup = customPropertyLookup;
    }

    public List<SelectItem> getContentFormats()
    {
        return contentFormats;
    }

    public void setContentFormats(List<SelectItem> contentFormats)
    {
        this.contentFormats = contentFormats;
    }

    public String getContentFormat()
    {
        return contentFormat;
    }

    public void setContentFormat(String contentFormat)
    {
        this.contentFormat = contentFormat;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public List<SelectItem> getContentTypes()
    {
        return contentTypes;
    }

    public void setContentTypes(List<SelectItem> contentTypes)
    {
        this.contentTypes = contentTypes;
    }

    public String getFolderType()
    {
        return folderType;
    }

    public void setFolderType(String folderType)
    {
        this.folderType = folderType;
    }

    public List<SelectItem> getFolderTypes()
    {
        return folderTypes;
    }

    public void setFolderTypes(List<SelectItem> folderTypes)
    {
        this.folderTypes = folderTypes;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public String getLookin()
    {
        return lookin;
    }

    public void setLookin(String lookin)
    {
        this.lookin = lookin;
    }

    public NodeRef getLocation()
    {
        return location;
    }

    public void setLocation(NodeRef location)
    {
        this.location = location;
    }

    public List<Node> getCategories()
    {
        return categories;
    }

    public void setCategories(List<Node> categories)
    {
        this.categories = categories;
    }

    public DataModel getCategoriesDataModel()
    {
        return categoriesDataModel;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getCreatedDateFrom()
    {
        return createdDateFrom;
    }

    public void setCreatedDateFrom(Date createdDateFrom)
    {
        this.createdDateFrom = createdDateFrom;
    }

    public Date getCreatedDateTo()
    {
        return createdDateTo;
    }

    public void setCreatedDateTo(Date createdDateTo)
    {
        this.createdDateTo = createdDateTo;
    }

    public Date getModifiedDateFrom()
    {
        return modifiedDateFrom;
    }

    public void setModifiedDateFrom(Date modifiedDateFrom)
    {
        this.modifiedDateFrom = modifiedDateFrom;
    }

    public Date getModifiedDateTo()
    {
        return modifiedDateTo;
    }

    public void setModifiedDateTo(Date modifiedDateTo)
    {
        this.modifiedDateTo = modifiedDateTo;
    }

    public boolean isLocationChildren()
    {
        return locationChildren;
    }

    public void setLocationChildren(boolean locationChildren)
    {
        this.locationChildren = locationChildren;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public boolean isModifiedDateChecked()
    {
        return modifiedDateChecked;
    }

    public void setModifiedDateChecked(boolean modifiedDateChecked)
    {
        this.modifiedDateChecked = modifiedDateChecked;
    }

    public boolean isCreatedDateChecked()
    {
        return createdDateChecked;
    }

    public void setCreatedDateChecked(boolean createdDateChecked)
    {
        this.createdDateChecked = createdDateChecked;
    }

    public NodeRef getGlobalSearchesRef()
    {
        return globalSearchesRef;
    }

    public void setGlobalSearchesRef(NodeRef globalSearchesRef)
    {
        this.globalSearchesRef = globalSearchesRef;
    }

    public NodeRef getUserSearchesRef()
    {
        return userSearchesRef;
    }

    public void setUserSearchesRef(NodeRef userSearchesRef)
    {
        this.userSearchesRef = userSearchesRef;
    }

    public String getSavedSearch()
    {
        return savedSearch;
    }

    public void setSavedSearch(String savedSearch)
    {
        this.savedSearch = savedSearch;
    }

    public String getSavedSearchMode()
    {
        return savedSearchMode;
    }

    public void setSavedSearchMode(String savedSearchMode)
    {
        this.savedSearchMode = savedSearchMode;
    }

    public String getEditSearchName()
    {
        return editSearchName;
    }

    public void setEditSearchName(String editSearchName)
    {
        this.editSearchName = editSearchName;
    }

    public boolean isSearchSaveGlobal()
    {
        return searchSaveGlobal;
    }

    public void setSearchSaveGlobal(boolean searchSaveGlobal)
    {
        this.searchSaveGlobal = searchSaveGlobal;
    }

    public ExpiringValueCache<List<SelectItem>> getCachedSavedSearches()
    {
        return cachedSavedSearches;
    }

    public void setCachedSavedSearches(ExpiringValueCache<List<SelectItem>> cachedSavedSearches)
    {
        this.cachedSavedSearches = cachedSavedSearches;
    }

}
