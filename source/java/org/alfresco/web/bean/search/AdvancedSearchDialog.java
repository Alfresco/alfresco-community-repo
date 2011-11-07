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
package org.alfresco.web.bean.search;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
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
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.search.SearchContext.RangeProperties;
import org.alfresco.web.config.AdvancedSearchConfigElement;
import org.alfresco.web.config.AdvancedSearchConfigElement.CustomProperty;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.UIPanel.ExpandedEvent;
import org.alfresco.web.ui.repo.component.UIAjaxCategoryPicker;
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
public class AdvancedSearchDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 3658148969240122732L;

   /** PermissionService */
   transient private PermissionService permissionService;

   /**
    * Default constructor
    */
   public void init(java.util.Map<String, String> parameters)
   {
      super.init(parameters);
      properties.getPanels().put(PANEL_CATEGORIES, false);
      properties.getPanels().put(PANEL_ATTRS, false);
      properties.getPanels().put(PANEL_CUSTOM, false);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   protected PermissionService getPermissionService()
   {
      if (permissionService == null)
      {
         permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
      }
      return permissionService;
   }
   
   public void setProperties(SearchProperties properties)
   {
      this.properties = properties;
   }
   
   /**
    * @return Returns the allow Edit mode.
    */
   public boolean isAllowEdit()
   {
      boolean allow = (properties.getSavedSearch() != null && NO_SELECTION.equals(properties.getSavedSearch()) == false);
      if (allow)
      {
         NodeRef savedSearchRef = new NodeRef(Repository.getStoreRef(), properties.getSavedSearch());
         allow = (getPermissionService().hasPermission(savedSearchRef,
                     PermissionService.WRITE) == AccessStatus.ALLOWED);
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
    * Returns the properties for current categories JSF DataModel
    * 
    * @return JSF DataModel representing the current categories to search against
    */
   public DataModel getCategoriesDataModel()
   {
      // only set the wrapped data once otherwise the rowindex is reset
      if (properties.getCategoriesDataModel().getWrappedData() == null)
      {
         properties.getCategoriesDataModel().setWrappedData(properties.getCategories());
      }
      
      return properties.getCategoriesDataModel();
   }
   
   /**
    * @return Returns a list of content object types to allow the user to select from
    */
   public List<SelectItem> getContentTypes()
   {
      if ((properties.getContentTypes() == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         DictionaryService dictionaryService = Repository.getServiceRegistry(context).getDictionaryService();
         
         // add the well known cm:content object type by default
         properties.setContentTypes(new ArrayList<SelectItem>(5));
         properties.getContentTypes().add(new SelectItem(ContentModel.TYPE_CONTENT.toString(), 
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
                     
                     properties.getContentTypes().add(new SelectItem(idQName.toString(), label));
                  }
               }
            }
         }
      }
      
      return properties.getContentTypes();
   }
   
   /**
    * @return Returns a list of folder object types to allow the user to select from
    */
   public List<SelectItem> getFolderTypes()
   {
      if ((properties.getFolderTypes() == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         DictionaryService dictionaryService = Repository.getServiceRegistry(context).getDictionaryService();
         
         // add the well known cm:folder object type by default
         properties.setFolderTypes(new ArrayList<SelectItem>(5));
         properties.getFolderTypes().add(new SelectItem(ContentModel.TYPE_FOLDER.toString(), 
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
                     
                     properties.getFolderTypes().add(new SelectItem(idQName.toString(), label));
                  }
               }
            }
         }
      }
      
      return properties.getFolderTypes();
   }
   
   /**
    * @return Returns a list of content formats to allow the user to select from
    */
   public List<SelectItem> getContentFormats()
   {
      if ((properties.getContentFormats() == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         properties.setContentFormats(new ArrayList<SelectItem>(80));
         ServiceRegistry registry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
         MimetypeService mimetypeService = registry.getMimetypeService();
         
         // get the mime type display names
         Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
         for (String mimeType : mimeTypes.keySet())
         {
            properties.getContentFormats().add(new SelectItem(mimeType, mimeTypes.get(mimeType)));
         }
         
         // make sure the list is sorted by the values
         QuickSort sorter = new QuickSort(properties.getContentFormats(), "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         // add the "All Formats" constant marker at the top of the list (default selection)
         properties.getContentFormats().add(0, new SelectItem("", Application.getMessage(FacesContext.getCurrentInstance(), MSG_ALL_FORMATS)));
      }
      
      return properties.getContentFormats();
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Handler to clear the advanced search screen form details
    */
   public void reset(ActionEvent event)
   {
      resetFields();
      properties.setSavedSearch(null);
   }
   
   private void resetFields()
   {
      properties.setText("");
      properties.setMode(MODE_ALL);
      properties.setLookin(LOOKIN_ALL);
      properties.setContentType(null);
      properties.setContentFormat(null);
      properties.setFolderType(null);
      properties.setLocation(null);
      properties.setLocationChildren(true);
      properties.setCategories(new ArrayList<Node>(2));
      properties.setCategoriesDataModel(null);
      properties.setTitle(null);
      properties.setDescription(null);
      properties.setAuthor(null);
      properties.setCreatedDateFrom(null);
      properties.setCreatedDateTo(null);
      properties.setModifiedDateFrom(null);
      properties.setModifiedDateTo(null);
      properties.setCreatedDateChecked(false);
      properties.setModifiedDateChecked(false);
      properties.getCustomProperties().clear();
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
      search.setText(properties.getText());
      
      // set whether to force AND operation on text terms
      search.setForceAndTerms(Application.getClientConfig(FacesContext.getCurrentInstance()).getForceAndTerms());
      
      if (properties.getMode().equals(MODE_ALL))
      {
         search.setMode(SearchContext.SEARCH_ALL);
      }
      else if (properties.getMode().equals(MODE_FILES_TEXT))
      {
         search.setMode(SearchContext.SEARCH_FILE_NAMES_CONTENTS);
      }
      else if (properties.getMode().equals(MODE_FILES))
      {
         search.setMode(SearchContext.SEARCH_FILE_NAMES);
      }
      else if (properties.getMode().equals(MODE_FOLDERS))
      {
         search.setMode(SearchContext.SEARCH_SPACE_NAMES);
      }
      
      // additional attributes search
      if (properties.getDescription() != null && properties.getDescription().length() != 0)
      {
         search.addAttributeQuery(ContentModel.PROP_DESCRIPTION, properties.getDescription());
      }
      if (properties.getTitle() != null && properties.getTitle().length() != 0)
      {
         search.addAttributeQuery(ContentModel.PROP_TITLE, properties.getTitle());
      }
      if (properties.getAuthor() != null && properties.getAuthor().length() != 0)
      {
         search.addAttributeQuery(ContentModel.PROP_AUTHOR, properties.getAuthor());
      }
      if (properties.getContentFormat() != null && properties.getContentFormat().length() != 0)
      {
         search.setMimeType(properties.getContentFormat());
      }
      if (properties.isCreatedDateChecked())
      {   
         SimpleDateFormat df = CachingDateFormat.getDateFormat();
         
         Calendar cal = Calendar.getInstance();
         cal.setTime(properties.getCreatedDateFrom());
         cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
         cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
         cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
         cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND)); 
         String strCreatedDate = df.format(cal.getTime());
         
         cal.setTime(properties.getCreatedDateTo());
         cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
         cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
         cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
         cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND)); 
         cal.add(Calendar.DAY_OF_YEAR, 1);
         cal.add(Calendar.MILLISECOND, -1);
         String strCreatedDateTo = df.format(cal.getTime());
         
         search.addRangeQuery(ContentModel.PROP_CREATED, strCreatedDate, strCreatedDateTo, true);
      }
      if (properties.isModifiedDateChecked())
      {
         SimpleDateFormat df = CachingDateFormat.getDateFormat();
         
         Calendar cal = Calendar.getInstance();
         cal.setTime(properties.getModifiedDateFrom());
         cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
         cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
         cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
         cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND)); 
         String strModifiedDate = df.format(cal.getTime());
         
         cal.setTime(properties.getModifiedDateTo());
         cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
         cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
         cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
         cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND)); 
         cal.add(Calendar.DAY_OF_YEAR, 1);
         cal.add(Calendar.MILLISECOND, -1);
         String strModifiedDateTo = df.format(cal.getTime());
         
         search.addRangeQuery(ContentModel.PROP_MODIFIED, strModifiedDate, strModifiedDateTo, true);
      }

      // in case of dynamic config, only lookup once
      Map<String, DataTypeDefinition> customPropertyLookup = getCustomPropertyLookup();
      
      // walk each of the custom properties add add them as additional attributes
      for (String qname : properties.getCustomProperties().keySet())
      {
         Object value = properties.getCustomProperties().get(qname);
         DataTypeDefinition typeDef = customPropertyLookup.get(qname);
         if (typeDef != null)
         {
            QName typeName = typeDef.getName();
            if (DataTypeDefinition.DATE.equals(typeName) || DataTypeDefinition.DATETIME.equals(typeName))
            {
               // only apply date to search if the user has checked the enable checkbox
               if (value != null && Boolean.valueOf(value.toString()) == true)
               {
                  SimpleDateFormat df = CachingDateFormat.getDateFormat();
                  String strDateFrom = df.format(properties.getCustomProperties().get(
                        UISearchCustomProperties.PREFIX_DATE_FROM + qname));
                  String strDateTo = df.format(properties.getCustomProperties().get(
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
            else if (value != null)
            {
               // is the value from a list?
               String strVal = value.toString();
               Object item = properties.getCustomProperties().get(
                     UISearchCustomProperties.PREFIX_LOV_ITEM + qname);
               if (item != null)
               {
                  // ListOfValues custom property - use a fixed value query if set
                  if (((Boolean)value) == true)
                  {
                     search.addFixedValueQuery(QName.createQName(qname), item.toString());
                  }
               }
               else if (strVal != null && strVal.length() != 0)
               {
                  if (DataTypeDefinition.INT.equals(typeName) || DataTypeDefinition.LONG.equals(typeName) ||
                      DataTypeDefinition.FLOAT.equals(typeName) || DataTypeDefinition.DOUBLE.equals(typeName))
                  {
                     search.addFixedValueQuery(QName.createQName(qname), strVal);
                  }
                  else
                  {
                     // by default use toString() value - this is for text fields and unknown types
                     search.addAttributeQuery(QName.createQName(qname), strVal);
                  }
               }
            }
         }
      }
      
      // location path search
      if (properties.getLookin().equals(LOOKIN_OTHER) && properties.getLocation() != null)
      {
         search.setLocation(SearchContext.getPathFromSpaceRef(properties.getLocation(), properties.isLocationChildren()));
      }
      
      // category path search
      if (properties.getCategories().size() != 0)
      {
         String[] paths = new String[properties.getCategories().size()];
         for (int i=0; i<paths.length; i++)
         {
            Node category = properties.getCategories().get(i);
            boolean includeChildren = (Boolean)category.getProperties().get(INCLUDE_CHILDREN);
            paths[i] = SearchContext.getPathFromSpaceRef(category.getNodeRef(), includeChildren);
         }
         search.setCategories(paths);
      }
      
      // content type restriction
      if (properties.getContentType() != null)
      {
         search.setContentType(properties.getContentType());
      }
      
      // folder type restriction
      if (properties.getFolderType() != null)
      {
         search.setFolderType(properties.getFolderType());
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
      properties.setSearchDescription(null);
      properties.setSearchName(null);
      properties.setSearchSaveGlobal(false);
      
      return "dialog:saveSearch";
   }

   /**
    * Action handler called to initiate the saved search screen for Edit
    */
   public String saveEditSearch()
   {
      properties.setSearchDescription(null);
      properties.setSearchName(null);
      properties.setEditSearchName(null);

      // load previously selected search for overwrite
      try
      {
         NodeRef searchRef = new NodeRef(Repository.getStoreRef(), properties.getSavedSearch());
         Node searchNode = new Node(searchRef);
         if (getNodeService().exists(searchRef) && searchNode.hasPermission(PermissionService.WRITE))
         {
            Node node = new Node(searchRef);
            properties.setSearchName(node.getName());
            properties.setEditSearchName(properties.getSearchName());
            properties.setSearchDescription((String)node.getProperties().get(ContentModel.PROP_DESCRIPTION));
         }
         else
         {
            // unable to overwrite existing saved search
            properties.setSavedSearch(null);
         }
      }
      catch (Throwable err)
      {
         // unable to overwrite existing saved search for some other reason
         properties.setSavedSearch(null);
      }
      
      return "dialog:editSearch";
   }

   /**
    * @return list of saved searches as SelectItem objects
    */
   public List<SelectItem> getSavedSearches()
   {
      List<SelectItem> savedSearches = properties.getCachedSavedSearches().get();
      if (savedSearches == null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         ServiceRegistry services = Repository.getServiceRegistry(fc);
         
         // get the searches list from the current user or global searches location
         NodeRef searchesRef = null;
         if (SAVED_SEARCHES_USER.equals(properties.getSavedSearchMode()) == true)
         {
            searchesRef = getUserSearchesRef();
         }
         else if (SAVED_SEARCHES_GLOBAL.equals(properties.getSavedSearchMode()) == true)
         {
            searchesRef = getGlobalSearchesRef();
         }
         
         // read the content nodes under the folder
         if (searchesRef != null)
         {
            DictionaryService dd = services.getDictionaryService();
            
            List<ChildAssociationRef> childRefs = getNodeService().getChildAssocs(
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
         properties.getCachedSavedSearches().put(savedSearches);
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
      properties.setSavedSearchMode(viewMode);
      
      // clear existing caches and values
      // the values will be re-queried when the client requests the saved searches list
      properties.getCachedSavedSearches().clear();
      properties.setSavedSearch(null);
   }
   
   /**
    * Action handler called when a saved search is selected by the user
    */
   public void selectSearch(ActionEvent event)
   {
      if (NO_SELECTION.equals(properties.getSavedSearch()) == false)
      {
         // read an XML serialized version of the SearchContext object
         NodeRef searchSearchRef = new NodeRef(Repository.getStoreRef(), properties.getSavedSearch());
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
      
      properties.setText(search.getText());
      
      switch (search.getMode())
      {
         case SearchContext.SEARCH_ALL:
            properties.setMode(MODE_ALL);
            break;
         case SearchContext.SEARCH_FILE_NAMES_CONTENTS:
            properties.setMode(MODE_FILES_TEXT);
            break;
         case SearchContext.SEARCH_FILE_NAMES:
            properties.setMode(MODE_FILES);
            break;
         case SearchContext.SEARCH_SPACE_NAMES:
            properties.setMode(MODE_FOLDERS);
            break;
      }
      properties.getPanels().put(PANEL_RESTRICT, true);
      
      if (search.getLocation() != null)
      {
         properties.setLocationChildren(search.getLocation().endsWith("//*"));
         properties.setLocation(findNodeRefFromPath(search.getLocation()));
         properties.setLookin(LOOKIN_OTHER);
         properties.getPanels().put(PANEL_LOCATION, true);
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
               properties.getCategories().add(categoryNode);
            }
         }
         properties.getPanels().put(PANEL_CATEGORIES, true);
      }
      
      properties.setContentType(search.getContentType());
      properties.setContentFormat(search.getMimeType());
      properties.setFolderType(search.getFolderType());
      
      properties.setDescription(search.getAttributeQuery(ContentModel.PROP_DESCRIPTION));
      properties.setTitle(search.getAttributeQuery(ContentModel.PROP_TITLE));
      properties.setAuthor(search.getAttributeQuery(ContentModel.PROP_AUTHOR));
      if (properties.getContentType() != null || properties.getContentFormat() != null ||
          properties.getDescription() != null || properties.getTitle() != null ||
          properties.getAuthor() != null)
      {
         properties.getPanels().put(PANEL_ATTRS, true);
      }
      
      RangeProperties createdDate = search.getRangeProperty(ContentModel.PROP_CREATED);
      if (createdDate != null)
      {
         properties.setCreatedDateFrom(Utils.parseXMLDateFormat(createdDate.lower));
         properties.setCreatedDateTo(Utils.parseXMLDateFormat(createdDate.upper));
         properties.setCreatedDateChecked(true);
         properties.getPanels().put(PANEL_ATTRS, true);
      }
      RangeProperties modifiedDate = search.getRangeProperty(ContentModel.PROP_MODIFIED);
      if (modifiedDate != null)
      {
         properties.setModifiedDateFrom(Utils.parseXMLDateFormat(modifiedDate.lower));
         properties.setModifiedDateTo(Utils.parseXMLDateFormat(modifiedDate.upper));
         properties.setModifiedDateChecked(true);
         properties.getPanels().put(PANEL_ATTRS, true);
      }
      
      // in case of dynamic config, only lookup once
      Map<String, DataTypeDefinition> customPropertyLookup = getCustomPropertyLookup();
      
      // custom fields - calculate which are required to set through the custom properties lookup table
      for (String qname : customPropertyLookup.keySet())
      {
         DataTypeDefinition typeDef = customPropertyLookup.get(qname);
         if (typeDef != null)
         {
            QName typeName = typeDef.getName();
            if (DataTypeDefinition.DATE.equals(typeName) || DataTypeDefinition.DATETIME.equals(typeName))
            {
               RangeProperties dateProps = search.getRangeProperty(QName.createQName(qname));
               if (dateProps != null)
               {
                  properties.getCustomProperties().put(UISearchCustomProperties.PREFIX_DATE_FROM + qname,
                        Utils.parseXMLDateFormat(dateProps.lower));
                  properties.getCustomProperties().put(UISearchCustomProperties.PREFIX_DATE_TO + qname,
                        Utils.parseXMLDateFormat(dateProps.upper));
                  properties.getCustomProperties().put(qname, true);
                  properties.getPanels().put(PANEL_CUSTOM, true);
               }
            }
            else if (DataTypeDefinition.BOOLEAN.equals(typeName))
            {
               String strBool = search.getFixedValueQuery(QName.createQName(qname));
               if (strBool != null)
               {
                  properties.getCustomProperties().put(qname, Boolean.parseBoolean(strBool));
                  properties.getPanels().put(PANEL_CUSTOM, true);
               }
            }
            else if (DataTypeDefinition.NODE_REF.equals(typeName) || DataTypeDefinition.CATEGORY.equals(typeName))
            {
               String strNodeRef = search.getFixedValueQuery(QName.createQName(qname));
               if (strNodeRef != null)
               {
                  properties.getCustomProperties().put(qname, new NodeRef(strNodeRef));
                  properties.getPanels().put(PANEL_CUSTOM, true);
               }
            }
            else if (DataTypeDefinition.INT.equals(typeName) || DataTypeDefinition.LONG.equals(typeName) ||
                     DataTypeDefinition.FLOAT.equals(typeName) || DataTypeDefinition.DOUBLE.equals(typeName))
            {
               // currently numbers are rendered as text in UISearchCustomProperties component
               // this code will need updating if that changes!
               properties.getCustomProperties().put(qname, search.getFixedValueQuery(QName.createQName(qname)));
               properties.getPanels().put(PANEL_CUSTOM, true);
            }
            else
            {
               // a default datatype may indicate either an attribute query, or if a Fixed Value
               // is present then it's a LOV constraint with a value selected
               Object value = search.getFixedValueQuery(QName.createQName(qname));
               if (value != null)
               {
                  properties.getCustomProperties().put(UISearchCustomProperties.PREFIX_LOV_ITEM + qname, value);
                  properties.getCustomProperties().put(qname, Boolean.TRUE);
               }
               else
               {
                  properties.getCustomProperties().put(qname, search.getAttributeQuery(QName.createQName(qname)));
               }
               properties.getPanels().put(PANEL_CUSTOM, true);
            }
         }
      }
   }
   
   /**
    * Return NodeRef to the last Node referenced on the end of the specified xpath value
    * 
    * @param xpath   XPath - note that any /* or //* will be removed to find trailing node
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
      NodeRef rootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId(FacesContext.getCurrentInstance()));
      List<NodeRef> results = null;
      try
      {
         results = getSearchService().selectNodes(
               rootRef,
               xpath,
               null,
               getNamespaceService(),
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
   protected NodeRef getUserSearchesRef()
   {
      if (properties.getUserSearchesRef() == null)
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
               results = getSearchService().selectNodes(
                     globalRef,
                     xpath,
                     null,
                     getNamespaceService(),
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
                  properties.setUserSearchesRef(results.get(0));
               }
               else if (results.size() == 0 && new Node(globalRef).hasPermission(PermissionService.ADD_CHILDREN))
               {
                  // attempt to create folder for this user for first time
                  // create the preferences Node for this user
                  ChildAssociationRef childRef = getNodeService().createNode(
                        globalRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.APP_MODEL_1_0_URI, QName.createValidLocalName(user.getUserName())),
                        ContentModel.TYPE_FOLDER,
                        null);
                  
                  properties.setUserSearchesRef(childRef.getChildRef());
               }
            }
         }
      }
      
      return properties.getUserSearchesRef();
   }
   
   /**
    * @return the cached reference to the global Saved Searches folder
    */
   protected NodeRef getGlobalSearchesRef()
   {
      if (properties.getGlobalSearchesRef() == null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" +
                        Application.getGlossaryFolderName(fc) + "/" +
                        Application.getSavedSearchesFolderName(fc);
         
         List<NodeRef> results = null;
         try
         {
            results = getSearchService().selectNodes(
                  getNodeService().getRootNode(Repository.getStoreRef()),
                  xpath,
                  null,
                  getNamespaceService(),
                  false);
         }
         catch (AccessDeniedException err)
         {
            // ignore and return null
         }
         
         if (results != null && results.size() == 1)
         {
            properties.setGlobalSearchesRef(results.get(0));
         }
      }
      
      return properties.getGlobalSearchesRef();
   }
   
   /**
    * Action handler called when the Add button is pressed to add the current Category selection
    */
   @SuppressWarnings("unchecked")
   public void addCategory(ActionEvent event)
   {
      UIAjaxCategoryPicker selector = (UIAjaxCategoryPicker)event.getComponent().findComponent("catSelector");
      UISelectBoolean chkChildren = (UISelectBoolean)event.getComponent().findComponent("chkCatChildren");
      
      List<NodeRef> categoryRefs = (List<NodeRef>)selector.getValue();
      if (categoryRefs != null)
      {
         for (NodeRef categoryRef : categoryRefs)
         {
            Node categoryNode = new MapNode(categoryRef);
            // add a value bound propery used to indicate if searching across children is selected
            categoryNode.getProperties().put(INCLUDE_CHILDREN, chkChildren.isSelected());
            properties.getCategories().add(categoryNode);
         }
         // clear selector value after the list has been populated
         selector.setValue(null);
      }
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a category
    */
   public void removeCategory(ActionEvent event)
   {
      Node node = (Node) properties.getCategoriesDataModel().getRowData();
      if (node != null)
      {
         properties.getCategories().remove(node);
      }
   }
   
   /**
    * @return ClientConfigElement
    */
   private AdvancedSearchConfigElement getSearchConfig()
   {
      if (properties.getSearchConfigElement() == null)
      {
         properties.setSearchConfigElement((AdvancedSearchConfigElement)Application.getConfigService(
               FacesContext.getCurrentInstance()).getConfig("Advanced Search").
               getConfigElement(AdvancedSearchConfigElement.CONFIG_ELEMENT_ID));
      }
      
      return properties.getSearchConfigElement();
   }
   
   /**
    * Helper map to lookup custom property QName strings against a DataTypeDefinition
    * 
    * @return custom property lookup Map
    */
   private Map<String, DataTypeDefinition> getCustomPropertyLookup()
   {
      if ((properties.getCustomPropertyLookup() == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         properties.setCustomPropertyLookup(new HashMap<String, DataTypeDefinition>(7, 1.0f));
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
                  properties.getCustomPropertyLookup().put(propQName.toString(), propDef.getDataType());
               }
            }
         }
      }
      return properties.getCustomPropertyLookup();
   }
   
   /**
    * Save the state of the progressive panel that was expanded/collapsed
    */
   public void expandPanel(ActionEvent event)
   {
      if (event instanceof ExpandedEvent)
      {
         properties.getPanels().put(event.getComponent().getId(), ((ExpandedEvent) event).State);
      }
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_SAVE);
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   protected SearchProperties properties;
   
   private static final String MSG_ALL_FORMATS = "all_formats";
   private static final String MSG_ERROR_RESTORE_SEARCH = "error_restore_search";
   private static final String MSG_SELECT_SAVED_SEARCH = "select_saved_search";
   private static final String MSG_SAVE = "save";
   
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
}
