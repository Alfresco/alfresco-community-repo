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

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.ExpiringValueCache;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.SearchContext.RangeProperties;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.AdvancedSearchConfigElement;
import org.alfresco.web.config.AdvancedSearchConfigElement.CustomProperty;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.UIPanel.ExpandedEvent;
import org.alfresco.web.ui.repo.component.UICategorySelector;
import org.alfresco.web.ui.repo.component.UISearchCustomProperties;

/**
 * Provides the form state and action event handling for the Advanced Search UI.
 * <p>
 * Integrates with the web-client ConfigService to retrieve configuration of custom
 * meta-data searching fields. Custom fields can be configured to appear in the UI
 * and they are they automatically added to the search query by this bean.
 * 
 * @author Kevin Roast
 */
public class AdvancedSearchBean
{
   /**
    * Default constructor
    */
   public AdvancedSearchBean()
   {
      // initial state of progressive panels that don't use the default
      panels.put(PANEL_CATEGORIES, false);
      panels.put(PANEL_ATTRS, false);
      panels.put(PANEL_CUSTOM, false);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }

   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param namespaceService The NamespaceService to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   /**
    * @param searchService      the search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @param permissionService      The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * @return Returns the progressive panels expanded state map.
    */
   public Map<String, Boolean> getPanels()
   {
      return this.panels;
   }

   /**
    * @param panels The progressive panels expanded state map.
    */
   public void setPanels(Map<String, Boolean> panels)
   {
      this.panels = panels;
   }
   
   /**
    * @return Returns the saved search Description.
    */
   public String getSearchDescription()
   {
      return this.searchDescription;
   }

   /**
    * @param searchDescription The saved search Description to set.
    */
   public void setSearchDescription(String searchDescription)
   {
      this.searchDescription = searchDescription;
   }

   /**
    * @return Returns the saved search Name.
    */
   public String getSearchName()
   {
      return this.searchName;
   }

   /**
    * @param searchName The saved search Name to set.
    */
   public void setSearchName(String searchName)
   {
      this.searchName = searchName;
   }

   /**
    * @return ID of the last saved search selected by the user
    */
   public String getSavedSearch()
   {
      return this.savedSearch;
   }

   /**
    * @param savedSearch   ID of the saved search selected by the user
    */
   public void setSavedSearch(String savedSearch)
   {
      this.savedSearch = savedSearch;
   }
   
   /**
    * @return name of the saved search to edit
    */
   public String getEditSearchName()
   {
      return this.editSearchName;
   }

   /**
    * @param editSearchName   name of the saved search to edit
    */
   public void setEditSearchName(String editSearchName)
   {
      this.editSearchName = editSearchName;
   }
   
   /**
    * @return Returns the searchSaveGlobal.
    */
   public boolean isSearchSaveGlobal()
   {
      return this.searchSaveGlobal;
   }

   /**
    * @param searchSaveGlobal The searchSaveGlobal to set.
    */
   public void setSearchSaveGlobal(boolean searchSaveGlobal)
   {
      this.searchSaveGlobal = searchSaveGlobal;
   }

   /**
    * @return Returns the folder to search, null for all.
    */
   public String getLookin()
   {
      return this.lookin;
   }
   
   /**
    * @param lookin   The folder to search in or null for all.
    */
   public void setLookin(String lookIn)
   {
      this.lookin = lookIn;
   }
   
   /**
    * @return Returns the location.
    */
   public NodeRef getLocation()
   {
      return this.location;
   }
   
   /**
    * @param location The location to set.
    */
   public void setLocation(NodeRef location)
   {
      this.location = location;
   }
   
   /**
    * @return Returns the search mode.
    */
   public String getMode()
   {
      return this.mode;
   }
   
   /**
    * @param mode The search mode to set.
    */
   public void setMode(String mode)
   {
      this.mode = mode;
   }
   
   /**
    * @return Returns the savedSearchMode.
    */
   public String getSavedSearchMode()
   {
      return this.savedSearchMode;
   }

   /**
    * @param savedSearchMode The savedSearchMode to set.
    */
   public void setSavedSearchMode(String savedSearchMode)
   {
      this.savedSearchMode = savedSearchMode;
   }
   
   /**
    * @return Returns the allow Edit mode.
    */
   public boolean isAllowEdit()
   {
      boolean allow = (this.savedSearch != null && NO_SELECTION.equals(this.savedSearch) == false);
      if (allow)  
      {
         NodeRef savedSearchRef = new NodeRef(Repository.getStoreRef(), this.savedSearch);
         allow = (permissionService.hasPermission(savedSearchRef, PermissionService.WRITE) == AccessStatus.ALLOWED);
      }
      return allow;
   }

   /**
    * @param allowEdit The allow Edit mode to set.
    */
   public void setAllowEdit(boolean allowEdit)
   {
      // dummy method for Bean interface compliance
   }
   
   /**
    * @return Returns the text to search for.
    */
   public String getText()
   {
      return this.text;
   }
   
   /**
    * @param text The text to set.
    */
   public void setText(String text)
   {
      this.text = text;
   }
   
   /**
    * Returns the properties for current categories JSF DataModel
    * 
    * @return JSF DataModel representing the current categories to search against
    */
   public DataModel getCategoriesDataModel()
   {
      if (this.categoriesDataModel == null)
      {
         this.categoriesDataModel = new ListDataModel();
      }
      
      this.categoriesDataModel.setWrappedData(this.categories);
      
      return this.categoriesDataModel;
   }
   
   /**
    * @return Returns true to search location children, false for just the specified location.
    */
   public boolean getLocationChildren()
   {
      return this.locationChildren;
   }
   
   /**
    * @param locationChildren    True to search location children, false for just the specified location.
    */
   public void setLocationChildren(boolean locationChildren)
   {
      this.locationChildren = locationChildren;
   }
   
   /**
    * @return Returns the createdDateFrom.
    */
   public Date getCreatedDateFrom()
   {
      return this.createdDateFrom;
   }

   /**
    * @param createdDateFrom The createdDateFrom to set.
    */
   public void setCreatedDateFrom(Date createdDate)
   {
      this.createdDateFrom = createdDate;
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return this.description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return Returns the modifiedDateFrom.
    */
   public Date getModifiedDateFrom()
   {
      return this.modifiedDateFrom;
   }

   /**
    * @param modifiedDateFrom The modifiedDateFrom to set.
    */
   public void setModifiedDateFrom(Date modifiedDate)
   {
      this.modifiedDateFrom = modifiedDate;
   }
   
   /**
    * @return Returns the createdDateTo.
    */
   public Date getCreatedDateTo()
   {
      return this.createdDateTo;
   }

   /**
    * @param createdDateTo The createdDateTo to set.
    */
   public void setCreatedDateTo(Date createdDateTo)
   {
      this.createdDateTo = createdDateTo;
   }

   /**
    * @return Returns the modifiedDateTo.
    */
   public Date getModifiedDateTo()
   {
      return this.modifiedDateTo;
   }

   /**
    * @param modifiedDateTo The modifiedDateTo to set.
    */
   public void setModifiedDateTo(Date modifiedDateTo)
   {
      this.modifiedDateTo = modifiedDateTo;
   }

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return this.title;
   }

   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return Returns the author.
    */
   public String getAuthor()
   {
      return this.author;
   }

   /**
    * @param author The author to set.
    */
   public void setAuthor(String author)
   {
      this.author = author;
   }
   
   /**
    * @return Returns the modifiedDateChecked.
    */
   public boolean isModifiedDateChecked()
   {
      return this.modifiedDateChecked;
   }

   /**
    * @param modifiedDateChecked The modifiedDateChecked to set.
    */
   public void setModifiedDateChecked(boolean modifiedDateChecked)
   {
      this.modifiedDateChecked = modifiedDateChecked;
   }

   /**
    * @return Returns the createdDateChecked.
    */
   public boolean isCreatedDateChecked()
   {
      return this.createdDateChecked;
   }
   
   /**
    * @return Returns the content type currenty selected
    */
   public String getContentType()
   {
      return this.contentType;
   }

   /**
    * @param contentType Sets the currently selected content type
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }
   
   /**
    * @return Returns the folder type currenty selected
    */
   public String getFolderType()
   {
      return this.folderType;
   }

   /**
    * @param folderType Sets the currently selected folder type
    */
   public void setFolderType(String folderType)
   {
      this.folderType = folderType;
   }
   
   /**
    * @return Returns the contentFormat.
    */
   public String getContentFormat()
   {
      return this.contentFormat;
   }

   /**
    * @param contentFormat The contentFormat to set.
    */
   public void setContentFormat(String contentFormat)
   {
      this.contentFormat = contentFormat;
   }
   
   /**
    * @return Returns the custom properties Map.
    */
   public Map<String, Object> getCustomProperties()
   {
      return this.customProperties;
   }

   /**
    * @param customProperties The custom properties Map to set.
    */
   public void setCustomProperties(Map<String, Object> customProperties)
   {
      this.customProperties = customProperties;
   }

   /**
    * @param createdDateChecked The createdDateChecked to set.
    */
   public void setCreatedDateChecked(boolean createdDateChecked)
   {
      this.createdDateChecked = createdDateChecked;
   }
   
   /**
    * @return Returns a list of content object types to allow the user to select from
    */
   public List<SelectItem> getContentTypes()
   {
      if (this.contentTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         DictionaryService dictionaryService = Repository.getServiceRegistry(context).getDictionaryService();
         
         // add the well known cm:content object type by default
         this.contentTypes = new ArrayList<SelectItem>(5);
         this.contentTypes.add(new SelectItem(ContentModel.TYPE_CONTENT.toString(), 
               dictionaryService.getType(ContentModel.TYPE_CONTENT).getTitle()));
         
         // add any configured content sub-types to the list
         List<String> types = getSearchConfig().getContentTypes();
         if (types != null)
         {
            for (String type : types)
            {
               QName idQName = Repository.resolveToQName(type);
               if (idQName != null)
               {
                  TypeDefinition typeDef = dictionaryService.getType(idQName);
                  
                  if (typeDef != null && dictionaryService.isSubClass(typeDef.getName(), ContentModel.TYPE_CONTENT))
                  {
                     // try and get label from the dictionary
                     String label = typeDef.getTitle();
                     
                     // else just use the localname
                     if (label == null)
                     {
                        label = idQName.getLocalName();
                     }
                     
                     this.contentTypes.add(new SelectItem(idQName.toString(), label));
                  }
               }
            }
         }
      }
      
      return this.contentTypes;
   }
   
   /**
    * @return Returns a list of folder object types to allow the user to select from
    */
   public List<SelectItem> getFolderTypes()
   {
      if (this.folderTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         DictionaryService dictionaryService = Repository.getServiceRegistry(context).getDictionaryService();
         
         // add the well known cm:folder object type by default
         this.folderTypes = new ArrayList<SelectItem>(5);
         this.folderTypes.add(new SelectItem(ContentModel.TYPE_FOLDER.toString(), 
               dictionaryService.getType(ContentModel.TYPE_FOLDER).getTitle()));
         
         // add any configured folder sub-types to the list
         List<String> types = getSearchConfig().getFolderTypes();
         if (types != null)
         {
            for (String type : types)
            {
               QName idQName = Repository.resolveToQName(type);
               if (idQName != null)
               {
                  TypeDefinition typeDef = dictionaryService.getType(idQName);
                  
                  if (typeDef != null && dictionaryService.isSubClass(typeDef.getName(), ContentModel.TYPE_FOLDER))
                  {
                     // try and get label from the dictionary
                     String label = typeDef.getTitle();
                     
                     // else just use the localname
                     if (label == null)
                     {
                        label = idQName.getLocalName();
                     }
                     
                     this.folderTypes.add(new SelectItem(idQName.toString(), label));
                  }
               }
            }
         }
      }
      
      return this.folderTypes;
   }
   
   /**
    * @return Returns a list of content formats to allow the user to select from
    */
   public List<SelectItem> getContentFormats()
   {
      if (this.contentFormats == null)
      {
         this.contentFormats = new ArrayList<SelectItem>(80);
         ServiceRegistry registry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
         MimetypeService mimetypeService = registry.getMimetypeService();
         
         // get the mime type display names
         Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
         for (String mimeType : mimeTypes.keySet())
         {
            this.contentFormats.add(new SelectItem(mimeType, mimeTypes.get(mimeType)));
         }
         
         // make sure the list is sorted by the values
         QuickSort sorter = new QuickSort(this.contentFormats, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         // add the "All Formats" constant marker at the top of the list (default selection)
         this.contentFormats.add(0, new SelectItem("", Application.getMessage(FacesContext.getCurrentInstance(), MSG_ALL_FORMATS)));
      }
      
      return this.contentFormats;
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Handler to clear the advanced search screen form details
    */
   public void reset(ActionEvent event)
   {
      resetFields();
      this.savedSearch = null;
   }
   
   private void resetFields()
   {
      this.text = "";
      this.mode = MODE_ALL;
      this.lookin = LOOKIN_ALL;
      this.contentType = null;
      this.contentFormat = null;
      this.folderType = null;
      this.location = null;
      this.locationChildren = true;
      this.categories = new ArrayList<Node>(2);
      this.title = null;
      this.description = null;
      this.author = null;
      this.createdDateFrom = null;
      this.createdDateTo = null;
      this.modifiedDateFrom = null;
      this.modifiedDateTo = null;
      this.createdDateChecked = false;
      this.modifiedDateChecked = false;
      this.customProperties.clear();
   }
   
   /**
    * Handler to perform a search based on the current criteria
    */
   public String search()
   {
      // construct the Search Context and set on the navigation bean
      // then simply navigating to the browse screen will cause it pickup the Search Context
      SearchContext search = new SearchContext();
      
      // set the full-text/name field value 
      search.setText(this.text);
      
      // set whether to force AND operation on text terms
      search.setForceAndTerms(Application.getClientConfig(FacesContext.getCurrentInstance()).getForceAndTerms());
      
      if (this.mode.equals(MODE_ALL))
      {
         search.setMode(SearchContext.SEARCH_ALL);
      }
      else if (this.mode.equals(MODE_FILES_TEXT))
      {
         search.setMode(SearchContext.SEARCH_FILE_NAMES_CONTENTS);
      }
      else if (this.mode.equals(MODE_FILES))
      {
         search.setMode(SearchContext.SEARCH_FILE_NAMES);
      }
      else if (this.mode.equals(MODE_FOLDERS))
      {
         search.setMode(SearchContext.SEARCH_SPACE_NAMES);
      }
      
      // additional attributes search
      if (this.description != null && this.description.length() != 0)
      {
         search.addAttributeQuery(ContentModel.PROP_DESCRIPTION, this.description);
      }
      if (this.title != null && this.title.length() != 0)
      {
         search.addAttributeQuery(ContentModel.PROP_TITLE, this.title);
      }
      if (this.author != null && this.author.length() != 0)
      {
         search.addAttributeQuery(ContentModel.PROP_AUTHOR, this.author);
      }
      if (this.contentFormat != null && this.contentFormat.length() != 0)
      {
         search.setMimeType(this.contentFormat);
      }
      if (this.createdDateChecked == true)
      {
         SimpleDateFormat df = CachingDateFormat.getDateFormat();
         String strCreatedDate = df.format(this.createdDateFrom);
         String strCreatedDateTo = df.format(this.createdDateTo);
         search.addRangeQuery(ContentModel.PROP_CREATED, strCreatedDate, strCreatedDateTo, true);
      }
      if (this.modifiedDateChecked == true)
      {
         SimpleDateFormat df = CachingDateFormat.getDateFormat();
         String strModifiedDate = df.format(this.modifiedDateFrom);
         String strModifiedDateTo = df.format(this.modifiedDateTo);
         search.addRangeQuery(ContentModel.PROP_MODIFIED, strModifiedDate, strModifiedDateTo, true);
      }
      
      // walk each of the custom properties add add them as additional attributes
      for (String qname : this.customProperties.keySet())
      {
         Object value = this.customProperties.get(qname);
         DataTypeDefinition typeDef = getCustomPropertyLookup().get(qname);
         if (typeDef != null)
         {
            QName typeName = typeDef.getName();
            if (DataTypeDefinition.DATE.equals(typeName) || DataTypeDefinition.DATETIME.equals(typeName))
            {
               // only apply date to search if the user has checked the enable checkbox
               if (value != null && Boolean.valueOf(value.toString()) == true)
               {
                  SimpleDateFormat df = CachingDateFormat.getDateFormat();
                  String strDateFrom = df.format(this.customProperties.get(
                        UISearchCustomProperties.PREFIX_DATE_FROM + qname));
                  String strDateTo = df.format(this.customProperties.get(
                        UISearchCustomProperties.PREFIX_DATE_TO + qname));
                  search.addRangeQuery(QName.createQName(qname), strDateFrom, strDateTo, true);
               }
            }
            else if (DataTypeDefinition.BOOLEAN.equals(typeName))
            {
               if (((Boolean)value) == true)
               {
                  search.addFixedValueQuery(QName.createQName(qname), value.toString());
               }
            }
            else if (DataTypeDefinition.NODE_REF.equals(typeName) || DataTypeDefinition.CATEGORY.equals(typeName))
            {
               if (value != null)
               {
                  search.addFixedValueQuery(QName.createQName(qname), value.toString());
               }
            }
            else if (DataTypeDefinition.INT.equals(typeName) || DataTypeDefinition.LONG.equals(typeName) ||
                     DataTypeDefinition.FLOAT.equals(typeName) || DataTypeDefinition.DOUBLE.equals(typeName))
            {
               String strVal = value.toString();
               if (strVal != null && strVal.length() != 0)
               {
                  search.addFixedValueQuery(QName.createQName(qname), strVal);
               }
            }
            else if (value != null)
            {
               // by default use toString() value - this is for text fields and unknown types
               String strVal = value.toString();
               if (strVal != null && strVal.length() != 0)
               {
                  search.addAttributeQuery(QName.createQName(qname), strVal);
               }
            }
         }
      }
      
      // location path search
      if (this.lookin.equals(LOOKIN_OTHER) && this.location != null)
      {
         search.setLocation(SearchContext.getPathFromSpaceRef(this.location, this.locationChildren));
      }
      
      // category path search
      if (this.categories.size() != 0)
      {
         String[] paths = new String[this.categories.size()];
         for (int i=0; i<paths.length; i++)
         {
            Node category = this.categories.get(i);
            boolean includeChildren = (Boolean)category.getProperties().get(INCLUDE_CHILDREN);
            paths[i] = SearchContext.getPathFromSpaceRef(category.getNodeRef(), includeChildren);
         }
         search.setCategories(paths);
      }
      
      // content type restriction
      if (this.contentType != null)
      {
         search.setContentType(this.contentType);
      }
      
      // folder type restriction
      if (this.folderType != null)
      {
         search.setFolderType(this.folderType);
      }
      
      // set the Search Context onto the top-level navigator bean
      // this causes the browse screen to switch into search results view
      this.navigator.setSearchContext(search);
      
      return OUTCOME_BROWSE;
   }
   
   /**
    * Action handler called to initiate the saved search screen for Create
    */
   public String saveNewSearch()
   {
      this.searchDescription = null;
      this.searchName = null;
      this.searchSaveGlobal = false;
      
      return "saveNewSearch";
   }
   
   /**
    * Action handler called to initiate the saved search screen for Edit
    */
   public String saveEditSearch()
   {
      this.searchDescription = null;
      this.searchName = null;
      this.editSearchName = null;
      
      // load previously selected search for overwrite
      try
      {
         NodeRef searchRef = new NodeRef(Repository.getStoreRef(), this.savedSearch);
         Node searchNode = new Node(searchRef);
         if (this.nodeService.exists(searchRef) && searchNode.hasPermission(PermissionService.WRITE))
         {
            Node node = new Node(searchRef);
            this.searchName = node.getName();
            this.editSearchName = this.searchName;
            this.searchDescription = (String)node.getProperties().get(ContentModel.PROP_DESCRIPTION);
         }
         else
         {
            // unable to overwrite existing saved search
            this.savedSearch = null;
         }
      }
      catch (Throwable err)
      {
         // unable to overwrite existing saved search for some other reason
         this.savedSearch = null;
      }
      
      return "saveEditSearch";
   }
   
   /**
    * Action handler called to save a new search
    */
   public String saveNewSearchOK()
   {
      String outcome = OUTCOME_BROWSE;
      
      NodeRef searchesRef;
      if (isSearchSaveGlobal() == true)
      {
         searchesRef = getGlobalSearchesRef();
      }
      else
      {
         searchesRef = getUserSearchesRef();
      }
      
      SearchContext search = this.navigator.getSearchContext();
      if (searchesRef != null && search != null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // create new content node as the saved search object
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
            props.put(ContentModel.PROP_NAME, this.searchName);
            props.put(ContentModel.PROP_DESCRIPTION, this.searchDescription);
            ChildAssociationRef childRef = this.nodeService.createNode(
                  searchesRef,
                  ContentModel.ASSOC_CONTAINS,
                  QName.createQName(NamespaceService.ALFRESCO_URI, QName.createValidLocalName(this.searchName)),
                  ContentModel.TYPE_CONTENT,
                  props);
            
            ContentService contentService = Repository.getServiceRegistry(context).getContentService();
            ContentWriter writer = contentService.getWriter(childRef.getChildRef(), ContentModel.PROP_CONTENT, true);
            
            // get a writer to our new node ready for XML content
            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
            writer.setEncoding("UTF-8");
            
            // output an XML serialized version of the SearchContext object
            writer.putContent(search.toXML());
            
            tx.commit();
            
            this.cachedSavedSearches.clear();
            this.savedSearch = null;
         }
         catch (Throwable e)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_SAVE_SEARCH), e.getMessage()), e);
            outcome = null;
         }
      }
      
      return outcome;
   }
   
   /**
    * Action handler called to save an existing search
    */
   public String saveEditSearchOK()
   {
      String outcome = OUTCOME_BROWSE;
      
      SearchContext search = this.navigator.getSearchContext();
      if (search != null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // handle Edit e.g. Overwrite of existing search
            // detect if was previously selected saved search (e.g. NodeRef not null)
            NodeRef searchRef = new NodeRef(Repository.getStoreRef(), this.savedSearch);
            if (this.nodeService.exists(searchRef))
            {
               Map<QName, Serializable> props = this.nodeService.getProperties(searchRef);
               props.put(ContentModel.PROP_NAME, this.searchName);
               props.put(ContentModel.PROP_DESCRIPTION, this.searchDescription);
               this.nodeService.setProperties(searchRef, props);
               
               ContentService contentService = Repository.getServiceRegistry(context).getContentService();
               ContentWriter writer = contentService.getWriter(searchRef, ContentModel.PROP_CONTENT, true);
               
               // get a writer to our new node ready for XML content
               writer.setMimetype(MimetypeMap.MIMETYPE_XML);
               writer.setEncoding("UTF-8");
               
               // output an XML serialized version of the SearchContext object
               writer.putContent(search.toXML());
               
               tx.commit();
            }
            
            this.cachedSavedSearches.clear();
            this.savedSearch = null;
         }
         catch (Throwable e)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_SAVE_SEARCH), e.getMessage()), e);
            outcome = null;
         }
      }
      
      return outcome;
   }
   
   /**
    * @return list of saved searches as SelectItem objects
    */
   public List<SelectItem> getSavedSearches()
   {
      List<SelectItem> savedSearches = cachedSavedSearches.get();
      if (savedSearches == null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         ServiceRegistry services = Repository.getServiceRegistry(fc);
         
         // get the searches list from the current user or global searches location
         NodeRef searchesRef = null;
         if (SAVED_SEARCHES_USER.equals(getSavedSearchMode()) == true)
         {
            searchesRef = getUserSearchesRef();
         }
         else if (SAVED_SEARCHES_GLOBAL.equals(getSavedSearchMode()) == true)
         {
            searchesRef = getGlobalSearchesRef();
         }
         
         // read the content nodes under the folder
         if (searchesRef != null)
         {
            DictionaryService dd = services.getDictionaryService();
            
            List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(
                  searchesRef,
                  ContentModel.ASSOC_CONTAINS,
                  RegexQNamePattern.MATCH_ALL);
            
            savedSearches = new ArrayList<SelectItem>(childRefs.size() + 1);
            if (childRefs.size() != 0)
            {
               for (ChildAssociationRef ref : childRefs)
               {
                  Node childNode = new Node(ref.getChildRef());
                  if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT))
                  {
                     savedSearches.add(new SelectItem(childNode.getId(), childNode.getName()));
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(savedSearches, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
         }
         else
         {
            // handle missing/access denied folder case
            savedSearches = new ArrayList<SelectItem>(1);
         }
         
         // add an entry (at the start) to instruct the user to select a saved search
         savedSearches.add(0, new SelectItem(NO_SELECTION,
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_SELECT_SAVED_SEARCH)));
         
         // store in the cache (will auto-expire)
         cachedSavedSearches.put(savedSearches);       
      }
      
      return savedSearches;
   }
   
   /**
    * Change the current saved searches list mode based on user selection
    */
   public void savedSearchModeChanged(ActionEvent event)
   {
      UIModeList savedModeList = (UIModeList)event.getComponent();
      
      // get the saved searches list mode
      String viewMode = savedModeList.getValue().toString();
      
      // persist
      setSavedSearchMode(viewMode);
      
      // clear existing caches and values
      // the values will be re-queried when the client requests the saved searches list
      this.cachedSavedSearches.clear();
      this.savedSearch = null;
   }
   
   /**
    * Action handler called when a saved search is selected by the user
    */
   public void selectSearch(ActionEvent event)
   {
      if (NO_SELECTION.equals(savedSearch) == false)
      {
         // read an XML serialized version of the SearchContext object
         NodeRef searchSearchRef = new NodeRef(Repository.getStoreRef(), savedSearch);
         ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
         ContentService cs = services.getContentService();
         try
         {
            if (services.getNodeService().exists(searchSearchRef))
            {
               ContentReader reader = cs.getReader(searchSearchRef, ContentModel.PROP_CONTENT);
               SearchContext search = new SearchContext().fromXML(reader.getContentString());
               
               // if we get here we read the serialized object successfully
               // now setup the UI to match the new SearchContext object
               initialiseFromContext(search);
            }
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), MSG_ERROR_RESTORE_SEARCH), err.getMessage()), err);
         }
      }
   }
   
   /**
    * Initialise the Advanced Search UI screen from a SearchContext
    * 
    * @param search  the SearchContext to retrieve state from
    */
   private void initialiseFromContext(SearchContext search)
   {
      resetFields();
      
      this.text = search.getText();
      
      switch (search.getMode())
      {
         case SearchContext.SEARCH_ALL:
            this.mode = MODE_ALL;
            break;
         case SearchContext.SEARCH_FILE_NAMES_CONTENTS:
            this.mode = MODE_FILES_TEXT;
            break;
         case SearchContext.SEARCH_FILE_NAMES:
            this.mode = MODE_FILES;
            break;
         case SearchContext.SEARCH_SPACE_NAMES:
            this.mode = MODE_FOLDERS;
            break;
      }
      this.panels.put(PANEL_RESTRICT, true);
      
      if (search.getLocation() != null)
      {
         this.locationChildren = search.getLocation().endsWith("//*");
         this.location = findNodeRefFromPath(search.getLocation());
         this.lookin = LOOKIN_OTHER;
         this.panels.put(PANEL_LOCATION, true);
      }
      
      String[] categories = search.getCategories();
      if (categories != null && categories.length != 0)
      {
         for (String category : categories)
         {
            NodeRef categoryRef = findNodeRefFromPath(category);
            if (categoryRef != null)
            {
               Node categoryNode = new MapNode(categoryRef);
               // add a value bound propery used to indicate if searching across children is selected
               categoryNode.getProperties().put(INCLUDE_CHILDREN, category.endsWith("//*"));
               this.categories.add(categoryNode);
            }
         }
         this.panels.put(PANEL_CATEGORIES, true);
      }
      
      this.contentType = search.getContentType();
      this.contentFormat = search.getMimeType();
      this.folderType = search.getFolderType();
      
      this.description = search.getAttributeQuery(ContentModel.PROP_DESCRIPTION);
      this.title = search.getAttributeQuery(ContentModel.PROP_TITLE);
      this.author = search.getAttributeQuery(ContentModel.PROP_AUTHOR);
      if (this.contentType != null || this.contentFormat != null ||
          this.description != null || this.title != null || this.author != null)
      {
         this.panels.put(PANEL_ATTRS, true);
      }
      
      RangeProperties createdDate = search.getRangeProperty(ContentModel.PROP_CREATED);
      if (createdDate != null)
      {
         this.createdDateFrom = Utils.parseXMLDateFormat(createdDate.lower);
         this.createdDateTo = Utils.parseXMLDateFormat(createdDate.upper);
         this.createdDateChecked = true;
         this.panels.put(PANEL_ATTRS, true);
      }
      RangeProperties modifiedDate = search.getRangeProperty(ContentModel.PROP_MODIFIED);
      if (modifiedDate != null)
      {
         this.modifiedDateFrom = Utils.parseXMLDateFormat(modifiedDate.lower);
         this.modifiedDateTo = Utils.parseXMLDateFormat(modifiedDate.upper);
         this.modifiedDateChecked = true;
         this.panels.put(PANEL_ATTRS, true);
      }
      
      // custom fields - calculate which are required to set through the custom properties lookup table
      for (String qname : getCustomPropertyLookup().keySet())
      {
         DataTypeDefinition typeDef = getCustomPropertyLookup().get(qname);
         if (typeDef != null)
         {
            QName typeName = typeDef.getName();
            if (DataTypeDefinition.DATE.equals(typeName) || DataTypeDefinition.DATETIME.equals(typeName))
            {
               RangeProperties dateProps = search.getRangeProperty(QName.createQName(qname));
               if (dateProps != null)
               {
                  this.customProperties.put(UISearchCustomProperties.PREFIX_DATE_FROM + qname,
                        Utils.parseXMLDateFormat(dateProps.lower));
                  this.customProperties.put(UISearchCustomProperties.PREFIX_DATE_TO + qname,
                        Utils.parseXMLDateFormat(dateProps.upper));
                  this.customProperties.put(qname, true);
                  this.panels.put(PANEL_CUSTOM, true);
               }
            }
            else if (DataTypeDefinition.BOOLEAN.equals(typeName))
            {
               String strBool = search.getFixedValueQuery(QName.createQName(qname));
               if (strBool != null)
               {
                  this.customProperties.put(qname, Boolean.parseBoolean(strBool));
                  this.panels.put(PANEL_CUSTOM, true);
               }
            }
            else if (DataTypeDefinition.NODE_REF.equals(typeName) || DataTypeDefinition.CATEGORY.equals(typeName))
            {
               String strNodeRef = search.getFixedValueQuery(QName.createQName(qname));
               if (strNodeRef != null)
               {
                  this.customProperties.put(qname, new NodeRef(strNodeRef));
                  this.panels.put(PANEL_CUSTOM, true);
               }
            }
            else if (DataTypeDefinition.INT.equals(typeName) || DataTypeDefinition.LONG.equals(typeName) ||
                     DataTypeDefinition.FLOAT.equals(typeName) || DataTypeDefinition.DOUBLE.equals(typeName))
            {
               // currently numbers are rendered as text in UISearchCustomProperties component
               // this code will need updating if that changes!
               this.customProperties.put(qname, search.getFixedValueQuery(QName.createQName(qname)));
               this.panels.put(PANEL_CUSTOM, true);
            }
            else
            {
               this.customProperties.put(qname, search.getAttributeQuery(QName.createQName(qname)));
               this.panels.put(PANEL_CUSTOM, true);
            }
         }
      }
   }
   
   /**
    * Return NodeRef to the last Node referenced on the end of the specified xpath value
    * 
    * @param xpath      XPath - note that any /* or //* will be removed to find trailing node
    * 
    * @return NodeRef if found null otherwise
    */
   private NodeRef findNodeRefFromPath(String xpath)
   {
      if (xpath.endsWith("//*"))
      {
         xpath = xpath.substring(0, xpath.lastIndexOf("//*"));
      }
      else if (xpath.endsWith("/*"))
      {
         xpath = xpath.substring(0, xpath.lastIndexOf("/*"));
      }
      NodeRef rootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
      List<NodeRef> results = null;
      try
      {
         results = searchService.selectNodes(
               rootRef,
               xpath,
               null,
               namespaceService,
               false);
      }
      catch (AccessDeniedException err)
      {
         // ignore and return null
      }
      
      return (results != null && results.size() == 1) ? results.get(0) : null;
   }
   
   /**
    * @return the cached reference to the shared Saved Searches folder
    */
   private NodeRef getUserSearchesRef()
   {
      if (userSearchesRef == null)
      {
         NodeRef globalRef = getGlobalSearchesRef();
         if (globalRef != null)
         {
            FacesContext fc = FacesContext.getCurrentInstance();
            User user = Application.getCurrentUser(fc);
            String userName = ISO9075.encode(user.getUserName());
            String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + QName.createValidLocalName(userName);
            
            List<NodeRef> results = null;
            try
            {
               results = searchService.selectNodes(
                     globalRef,
                     xpath,
                     null,
                     namespaceService,
                     false);
            }
            catch (AccessDeniedException err)
            {
               // ignore and return null
            }
            
            if (results != null)
            {
               if (results.size() == 1)
               {
                  userSearchesRef = results.get(0);
               }
               else if (results.size() == 0 && new Node(globalRef).hasPermission(PermissionService.ADD_CHILDREN))
               {
                  // attempt to create folder for this user for first time
                  // create the preferences Node for this user
                  Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
                  props.put(ContentModel.PROP_NAME, user.getUserName());
                  ChildAssociationRef childRef = nodeService.createNode(
                        globalRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.APP_MODEL_1_0_URI, QName.createValidLocalName(user.getUserName())),
                        ContentModel.TYPE_FOLDER,
                        props);
                  
                  userSearchesRef = childRef.getChildRef();
               }
            }
         }
      }
      
      return userSearchesRef;
   }
   
   /**
    * @return the cached reference to the global Saved Searches folder
    */
   private NodeRef getGlobalSearchesRef()
   {
      if (globalSearchesRef == null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" +
                        Application.getGlossaryFolderName(fc) + "/" +
                        Application.getSavedSearchesFolderName(fc);
         
         List<NodeRef> results = null;
         try
         {
            results = searchService.selectNodes(
                  nodeService.getRootNode(Repository.getStoreRef()),
                  xpath,
                  null,
                  namespaceService,
                  false);
         }
         catch (AccessDeniedException err)
         {
            // ignore and return null
         }
         
         if (results != null && results.size() == 1)
         {
            globalSearchesRef = results.get(0);
         }
      }
      
      return globalSearchesRef;
   }
   
   /**
    * Action handler called when the Add button is pressed to add the current Category selection
    */
   public void addCategory(ActionEvent event)
   {
      UICategorySelector selector = (UICategorySelector)event.getComponent().findComponent("catSelector");
      UISelectBoolean chkChildren = (UISelectBoolean)event.getComponent().findComponent("chkCatChildren");
      
      NodeRef categoryRef = (NodeRef)selector.getValue();
      if (categoryRef != null)
      {
         Node categoryNode = new MapNode(categoryRef);
         // add a value bound propery used to indicate if searching across children is selected
         categoryNode.getProperties().put(INCLUDE_CHILDREN, chkChildren.isSelected());
         this.categories.add(categoryNode);
      }
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a category
    */
   public void removeCategory(ActionEvent event)
   {
      Node node = (Node)this.categoriesDataModel.getRowData();
      if (node != null)
      {
         this.categories.remove(node);
      }
   }
   
   /**
    * @return ClientConfigElement
    */
   private AdvancedSearchConfigElement getSearchConfig()
   {
      if (searchConfigElement == null)
      {
         searchConfigElement = (AdvancedSearchConfigElement)Application.getConfigService(
               FacesContext.getCurrentInstance()).getConfig("Advanced Search").
               getConfigElement(AdvancedSearchConfigElement.CONFIG_ELEMENT_ID);
      }
      
      return searchConfigElement;
   }
   
   /**
    * Helper map to lookup custom property QName strings against a DataTypeDefinition
    * 
    * @return custom property lookup Map
    */
   private Map<String, DataTypeDefinition> getCustomPropertyLookup()
   {
      if (customPropertyLookup == null)
      {
         customPropertyLookup = new HashMap<String, DataTypeDefinition>(7, 1.0f);
         List<CustomProperty> customProps = getSearchConfig().getCustomProperties();
         if (customProps != null)
         {
            DictionaryService dd = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
            for (CustomProperty customProp : customProps)
            {
               PropertyDefinition propDef = null;
               QName propQName = Repository.resolveToQName(customProp.Property);
               if (customProp.Type != null)
               {
                  QName type = Repository.resolveToQName(customProp.Type);
                  TypeDefinition typeDef = dd.getType(type);
                  propDef = typeDef.getProperties().get(propQName);
               }
               else if (customProp.Aspect != null)
               {
                  QName aspect = Repository.resolveToQName(customProp.Aspect);
                  AspectDefinition aspectDef = dd.getAspect(aspect);
                  propDef = aspectDef.getProperties().get(propQName);
               }
               
               if (propQName != null && propDef != null)
               {
                  customPropertyLookup.put(propQName.toString(), propDef.getDataType());
               }
            }
         }
      }
      return customPropertyLookup;
   }
   
   /**
    * Save the state of the progressive panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      if (event instanceof ExpandedEvent)
      {
         this.panels.put(event.getComponent().getId(), ((ExpandedEvent)event).State);
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data 
   
   private static final String MSG_ALL_FORMATS = "all_formats";
   private static final String MSG_ERROR_SAVE_SEARCH = "error_save_search";
   private static final String MSG_ERROR_RESTORE_SEARCH = "error_restore_search";
   private static final String MSG_SELECT_SAVED_SEARCH = "select_saved_search";
   
   private static final String OUTCOME_BROWSE = "browse";
   
   private static final String PANEL_CUSTOM = "custom-panel";
   private static final String PANEL_ATTRS = "attrs-panel";
   private static final String PANEL_CATEGORIES = "categories-panel";
   private static final String PANEL_RESTRICT = "restrict-panel";
   private static final String PANEL_LOCATION = "location-panel";
   
   private static final String INCLUDE_CHILDREN = "includeChildren";
   
   private static final String MODE_ALL = "all";
   private static final String MODE_FILES_TEXT = "files_text";
   private static final String MODE_FILES = "files";
   private static final String MODE_FOLDERS = "folders";
   
   private static final String LOOKIN_ALL = "all";
   private static final String LOOKIN_OTHER = "other";
   
   private static final String SAVED_SEARCHES_USER = "user";
   private static final String SAVED_SEARCHES_GLOBAL = "global";
   
   private static final String NO_SELECTION = "NONE";
   
   /** The NodeService to be used by the bean */
   protected NodeService nodeService;
   
   /** The NamespaceService to be used by the bean */
   protected NamespaceService namespaceService;
   
   /** The NavigationBean reference */
   protected NavigationBean navigator;
   
   /** SearchService bean reference */
   protected SearchService searchService;
   
   /** PermissionService */
   protected PermissionService permissionService;
   
   /** Client Config reference */
   protected AdvancedSearchConfigElement searchConfigElement = null;
   
   /** Progressive panel UI state */
   private Map<String, Boolean> panels = new HashMap(5, 1.0f);
   
   /** Saved search properties */
   private String searchName;
   private String searchDescription;
   
   /** custom property names to values */
   private Map<String, Object> customProperties = new HashMap(5, 1.0f);
   
   /** lookup of custom property QName string to DataTypeDefinition for the property */
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
   private DataModel categoriesDataModel = null;
   
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
}
