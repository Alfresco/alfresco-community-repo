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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for the client
 * 
 * @author Kevin Roast
 */
public class ClientConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "client";
   public static final String VIEW_DETAILS = "details";
   public static final String VIEW_ICONS = "icons";
   public static final String VIEW_LIST = "list";
   public static final String VIEW_BUBBLE = "bubble";
   
   private static final String SEPARATOR = ":";
   
   // defaults for any config values not supplied
   private int defaultPageSize = 10;
   private String defaultView = "details";
   private String defaultSortColumn = "name";
   private String defaultSortOrder = "ascending";
   
   // list to store all the configured views
   private List<String> views = new ArrayList<String>(4);

   // map to store all the default views 
   private Map<String, String> defaultViews = new HashMap<String, String>(4);
   
   // map to store all default pages sizes for configured client views
   private Map<String, Integer> pagesSizes = new HashMap<String, Integer>(10);
   
   // map to store default sort columns for configured views
   private Map<String, String> sortColumns = new HashMap<String, String>(4);
   
   // list of pages that have been configured to have ascending sorts
   private List<String> descendingSorts = new ArrayList<String>(1);
   
   private int recentSpacesItems = 6;
   private int searchMinimum = 3;
   private String helpUrl = null;
   private String editLinkType = null;
   private Map<String, String> localeMap = new HashMap<String, String>();
   private List<String> languages = new ArrayList<String>(8);
   private String homeSpacePermission = null;
   private List<String> contentTypes = null;
   private List<CustomProperty> customProps = null;
   
   /**
    * Default Constructor
    */
   public ClientConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
      
      // add the default page sizes to the map
      this.pagesSizes.put(VIEW_DETAILS, defaultPageSize);
      this.pagesSizes.put(VIEW_LIST, defaultPageSize);
      this.pagesSizes.put(VIEW_ICONS, 9);
      this.pagesSizes.put(VIEW_BUBBLE, 5);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public ClientConfigElement(String name)
   {
      super(name);
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      return null;
   }

   /**
    * Adds a configured view
    * 
    * @param renderer The implementation class of the view (the renderer)
    */
   public void addView(String renderer)
   {
      this.views.add(renderer);
   }
   
   /**
    * Returns a map of configured views for the client
    * 
    * @return List of the implementation classes for the configured views
    */
   public List<String> getViews()
   {
      return this.views;
   }
   
   /**
    * Adds a default view setting
    * 
    * @param page The page to set the default view for
    * @param view The view name that will be the default
    */
   public void addDefaultView(String page, String view)
   {
      this.defaultViews.put(page, view);
   }
   
   /**
    * Returns the default view for the given page
    * 
    * @param page The page to get the default view for
    * @return The defualt view, if there isn't a configured default for the
    *         given page 'details' will be returned
    */
   public String getDefaultView(String page)
   {
      String view = this.defaultViews.get(page);
      
      if (view == null)
      {
         view = this.defaultView;
      }
      
      return view;
   }
   
   /**
    * Adds a configured page size to the internal store
    * 
    * @param page The name of the page i.e. browse, forums etc.
    * @param view The name of the view the size is for i.e. details, icons etc.
    * @param size The size of the page
    */
   public void addDefaultPageSize(String page, String view, int size)
   {
      this.pagesSizes.put(page + SEPARATOR + view, new Integer(size));
   }
   
   /**
    * Returns the page size for the given page and view combination
    * 
    * @param page The name of the page i.e. browse, forums etc.
    * @param view The name of the view the size is for i.e. details, icons etc.
    * @return The size of the requested page, if the combination doesn't exist
    *         the default for the view will be used, if the view doesn't exist either
    *         10 will be returned.
    */
   public int getDefaultPageSize(String page, String view)
   {
      Integer pageSize = this.pagesSizes.get(page + SEPARATOR + view);
      
      // try just the view if the combination isn't present
      if (pageSize == null)
      {
         pageSize = this.pagesSizes.get(view);
         
         // if the view is not present either default to 10
         if (pageSize == null)
         {
            pageSize = new Integer(10);
         }
      }
      
      return pageSize.intValue();
   }

   /**
    * Adds a default sorting column for the given page 
    * 
    * @param page The name of the page i.e. browse, forums etc.
    * @param column The name of the column to initially sort by
    */
   public void addDefaultSortColumn(String page, String column)
   {
      this.sortColumns.put(page, column);
   }
   
   /**
    * Returns the default sort column for the given page
    * 
    * @param page The name of the page i.e. browse, forums etc.
    * @return The name of the column to sort by, name is returned if 
    *         the page is not found
    */
   public String getDefaultSortColumn(String page)
   {
      String column = this.sortColumns.get(page);
      
      if (column == null)
      {
         column = this.defaultSortColumn;
      }
      
      return column;
   }
   
   /**
    * Sets the given page as using descending sorts
    * 
    * @param page The name of the page i.e. browse, forums etc.
    */
   public void addDescendingSort(String page)
   {
      this.descendingSorts.add(page);
   }
   
   /**
    * Determines whether the given page has been
    * configured to use descending sorting by default
    * 
    * @param page The name of the page i.e. browse, forums etc.
    * @return true if the page should use descending sorts
    */
   public boolean hasDescendingSort(String page)
   {
      return this.descendingSorts.contains(page);
   }
   
   /**
    * @return Returns the recentSpacesItems.
    */
   public int getRecentSpacesItems()
   {
      return this.recentSpacesItems;
   }

   /**
    * @param recentSpacesItems The recentSpacesItems to set.
    */
   /*package*/ void setRecentSpacesItems(int recentSpacesItems)
   {
      this.recentSpacesItems = recentSpacesItems;
   }
   
   /**
    * Add a language locale and display label to the list.
    * 
    * @param locale     Locale code
    * @param label      Display label
    */
   /*package*/ void addLanguage(String locale, String label)
   {
      this.localeMap.put(locale, label);
      this.languages.add(locale);
   }
   
   /**
    * @return List of supported language locale strings in config file order
    */
   public List<String> getLanguages()
   {
      return this.languages;
   }
   
   /**
    * @param locale     The locale string to lookup language label for
    * 
    * @return the language label for specified locale string, or null if not found
    */
   public String getLabelForLanguage(String locale)
   {
      return this.localeMap.get(locale);
   }

   /**
    * @return Returns the help Url.
    */
   public String getHelpUrl()
   {
      return this.helpUrl;
   }

   /**
    * @param helpUrl The help Url to set.
    */
   /*package*/ void setHelpUrl(String helpUrl)
   {
      this.helpUrl = helpUrl;
   }
   
   /**
    * @return Returns the edit link type.
    */
   public String getEditLinkType()
   {
      return this.editLinkType;
   }

   /**
    * @param editLinkType The edit link type to set.
    */
   /*package*/ void setEditLinkType(String editLinkType)
   {
      this.editLinkType = editLinkType;
   }

   /**
    * @return Returns the search minimum number of characters.
    */
   public int getSearchMinimum()
   {
      return this.searchMinimum;
   }

   /**
    * @param searchMinimum The searchMinimum to set.
    */
   /*package*/ void setSearchMinimum(int searchMinimum)
   {
      this.searchMinimum = searchMinimum;
   }

   /**
    * @return Returns the default Home Space permissions.
    */
   public String getHomeSpacePermission()
   {
      return this.homeSpacePermission;
   }

   /**
    * @param homeSpacePermission The default Home Space permission to set.
    */
   /*package*/ void setHomeSpacePermission(String homeSpacePermission)
   {
      this.homeSpacePermission = homeSpacePermission;
   }

   /**
    * @return Returns the contentTypes.
    */
   public List<String> getContentTypes()
   {
      return this.contentTypes;
   }

   /**
    * @param contentTypes The contentTypes to set.
    */
   /*package*/ void setContentTypes(List<String> contentTypes)
   {
      this.contentTypes = contentTypes;
   }
   
   /**
    * @return Returns the customProps.
    */
   public List<CustomProperty> getCustomProperties()
   {
      return this.customProps;
   }

   /**
    * @param customProps The customProps to set.
    */
   /*package*/ void setCustomProperties(List<CustomProperty> customProps)
   {
      this.customProps = customProps;
   }
   
   
   /**
    * Simple wrapper class for custom advanced search property
    * @author Kevin Roast
    */
   public static class CustomProperty
   {
      CustomProperty(String type, String aspect, String property, String labelId)
      {
         Type = type;
         Aspect = aspect;
         Property = property;
         LabelId = labelId;
      }
      
      public String Type;
      public String Aspect;
      public String Property;
      public String LabelId;
   }
}
