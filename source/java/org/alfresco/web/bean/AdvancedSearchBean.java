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

import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.config.ClientConfigElement.CustomProperty;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
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
      panels.put("categories-panel", false);
      panels.put("attrs-panel", false);
      panels.put("custom-panel", false);
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
         
         // add the well known cm:content object type by default
         this.contentTypes = new ArrayList<SelectItem>(5);
         this.contentTypes.add(new SelectItem(ContentModel.TYPE_CONTENT.toString(), 
               Application.getMessage(context, MSG_CONTENT)));
         
         // add any configured content sub-types to the list
         List<String> types = getClientConfig().getContentTypes();
         if (types != null)
         {
            DictionaryService dictionaryService = Repository.getServiceRegistry(context).getDictionaryService();
            for (String type : types)
            {
               QName idQName = Repository.resolveToQName(type);
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
            
            // make sure the list is sorted by the label
            QuickSort sorter = new QuickSort(this.contentTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
            sorter.sort();
         }
      }
      
      return this.contentTypes;
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
      this.text = "";
      this.mode = MODE_ALL;
      this.lookin = LOOKIN_ALL;
      this.contentType = null;
      this.location = null;
      this.locationChildren = false;
      this.categories = new ArrayList<Node>(2);
      this.title = null;
      this.description = null;
      this.author = null;
      this.createdDateFrom = null;
      this.modifiedDateFrom = null;
      this.createdDateChecked = false;
      this.modifiedDateChecked = false;
      this.customProperties.clear();
   }
   
   /**
    * Handler to perform a search based on the current criteria
    */
   public String search()
   {
      String outcome = null;
      
      if (this.text != null && this.text.length() != 0)
      {
         // construct the Search Context and set on the navigation bean
         // then simply navigating to the browse screen will cause it pickup the Search Context
         SearchContext search = new SearchContext();
         
         search.setText(this.text);
         
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
               else
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
            search.setLocation(SearchContext.getPathFromSpaceRef(
                  new NodeRef(Repository.getStoreRef(), this.location.getId()), this.locationChildren));
         }
         
         // category path search
         if (this.categories.size() != 0)
         {
            String[] paths = new String[this.categories.size()];
            for (int i=0; i<paths.length; i++)
            {
               Node category = this.categories.get(i);
               boolean includeChildren = (Boolean)category.getProperties().get("includeChildren");
               paths[i] = SearchContext.getPathFromSpaceRef(category.getNodeRef(), includeChildren);
            }
            search.setCategories(paths);
         }
         
         // content type restriction
         if (this.contentType != null)
         {
            search.setContentType(this.contentType);
         }
         
         // set the Search Context onto the top-level navigator bean
         // this causes the browse screen to switch into search results view
         this.navigator.setSearchContext(search);
         
         outcome = "browse";
      }
      
      return outcome;
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
         //final boolean incChildren = chkChildren.isSelected();
         Node categoryNode = new MapNode(categoryRef);
         categoryNode.getProperties().put("includeChildren", chkChildren.isSelected());
         /*categoryNode.addPropertyResolver("includeChildren", new NodePropertyResolver() {
            public Object get(Node node) {
               return incChildren;
            };
         });*/
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
   private ClientConfigElement getClientConfig()
   {
      if (clientConfigElement == null)
      {
         ConfigService configService = Application.getConfigService(FacesContext.getCurrentInstance());
         clientConfigElement = (ClientConfigElement)configService.getGlobalConfig().getConfigElement(
               ClientConfigElement.CONFIG_ELEMENT_ID);
      }
      return clientConfigElement;
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
         List<CustomProperty> customProps = getClientConfig().getCustomProperties();
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
               customPropertyLookup.put(propQName.toString(), propDef.getDataType());
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
   
   private static final String MSG_CONTENT = "content";
   private static final String MSG_ALL_FORMATS = "all_formats";
   
   private static final String MODE_ALL = "all";
   private static final String MODE_FILES_TEXT = "files_text";
   private static final String MODE_FILES = "files";
   private static final String MODE_FOLDERS = "folders";
   
   private static final String LOOKIN_ALL = "all";
   private static final String LOOKIN_OTHER = "other";
   
   /** The NodeService to be used by the bean */
   private NodeService nodeService;
   
   /** The NamespaceService to be used by the bean */
   private NamespaceService namespaceService;
   
   /** The NavigationBean reference */
   private NavigationBean navigator;
   
   /** Client Config reference */
   private ClientConfigElement clientConfigElement = null;
   
   /** Progressive panel UI state */
   private Map<String, Boolean> panels = new HashMap(5, 1.0f);
   
   /** custom property names to values */
   private Map<String, Object> customProperties = new HashMap(5, 1.0f);
   
   /** lookup of custom property QName string to DataTypeDefinition for the property */
   private Map<String, DataTypeDefinition> customPropertyLookup = null;
   
   /** content types to for restricting searches */
   private List<SelectItem> contentTypes;
   
   /** content format list restricting searches */
   private List<SelectItem> contentFormats;
   
   /** content type selection */
   private String contentType;
   
   /** content format selection */
   private String contentFormat;
   
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
}
