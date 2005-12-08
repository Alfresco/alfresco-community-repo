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
import java.util.Iterator;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.web.config.ClientConfigElement.CustomProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for client config values
 * 
 * @author Kevin Roast
 */
public class ClientElementReader implements ConfigElementReader
{
   public static final String ELEMENT_VIEWS = "views";
   public static final String ELEMENT_VIEW = "view";
   public static final String ELEMENT_VIEWDEFAULTS = "view-defaults";
   public static final String ELEMENT_PAGESIZE = "page-size";
   public static final String ELEMENT_SORTCOLUMN = "sort-column";
   public static final String ELEMENT_SORTDESCENDING = "sort-descending";
   public static final String ELEMENT_RECENTSPACESITEMS = "recent-spaces-items";
   public static final String ELEMENT_LANGUAGES = "languages";
   public static final String ELEMENT_LANGUAGE = "language";
   public static final String ATTRIBUTE_LOCALE = "locale";
   public static final String ATTRIBUTE_NAME = "name";
   public static final String ELEMENT_HELPURL = "help-url";
   public static final String ELEMENT_EDITLINKTYPE = "edit-link-type";
   public static final String ELEMENT_SEARCHMINIMUM = "search-minimum";
   public static final String ELEMENT_HOMESPACEPERMISSION = "home-space-permission";
   public static final String ELEMENT_ADVANCEDSEARCH = "advanced-search";
   public static final String ELEMENT_CONTENTTYPES = "content-types";
   public static final String ELEMENT_TYPE = "type";
   public static final String ELEMENT_CUSTOMPROPS = "custom-properties";
   public static final String ELEMENT_METADATA = "meta-data";
   public static final String ATTRIBUTE_TYPE = "type";
   public static final String ATTRIBUTE_PROPERTY = "property";
   public static final String ATTRIBUTE_ASPECT = "aspect";
   public static final String ATTRIBUTE_DISPLAYLABEL = "displayLabelId";
   
   private static Log logger = LogFactory.getLog(ClientElementReader.class);
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      ClientConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ClientConfigElement.CONFIG_ELEMENT_ID) == false)
         {
            throw new ConfigException("ClientElementReader can only parse " +
                  ClientConfigElement.CONFIG_ELEMENT_ID + "elements, the element passed was '" + 
                  name + "'");
         }
         
         configElement = new ClientConfigElement();
         
         // get the configured views
         Element views = element.element(ELEMENT_VIEWS);
         if (views != null)
         {
            Iterator<Element> renderers = views.elementIterator(ELEMENT_VIEW);
            while (renderers.hasNext())
            {
               Element renderer = renderers.next();
               configElement.addView(renderer.getTextTrim());
            }
         }
         
         // get all the view related default settings
         Element viewDefaults = element.element(ELEMENT_VIEWDEFAULTS);
         if (viewDefaults != null)
         {
            Iterator<Element> pages = viewDefaults.elementIterator();
            while (pages.hasNext())
            {
               Element page = pages.next();
               String pageName = page.getName();
               
               // get the default view mode for the page
               Element defaultView = page.element(ELEMENT_VIEW);
               if (defaultView != null)
               {
                  String viewName = defaultView.getTextTrim();
                  configElement.addDefaultView(pageName, viewName);
               }
               
               // get the initial sort column
               Element sortColumn = page.element(ELEMENT_SORTCOLUMN);
               if (sortColumn != null)
               {
                  String column = sortColumn.getTextTrim();
                  configElement.addDefaultSortColumn(pageName, column);
               }
               
               // get the sort descending option
               Element sortDesc = page.element(ELEMENT_SORTDESCENDING);
               if (sortDesc != null)
               {
                  Boolean descending = new Boolean(sortDesc.getTextTrim());
                  if (descending.booleanValue() == true)
                  { 
                     configElement.addDescendingSort(pageName);
                  }
               }
               
               // process the page-size element
               processPageSizeElement(page.element(ELEMENT_PAGESIZE), 
                     pageName, configElement);
            }
         }
         
         // get the languages sub-element
         Element languages = element.element(ELEMENT_LANGUAGES);
         if (languages != null)
         {
            Iterator<Element> langsItr = languages.elementIterator(ELEMENT_LANGUAGE);
            while (langsItr.hasNext())
            {
               Element language = langsItr.next();
               String localeCode = language.attributeValue(ATTRIBUTE_LOCALE);
               String label = language.getTextTrim();
               
               if (localeCode != null && localeCode.length() != 0 &&
                   label != null && label.length() != 0)
               {
                  // store the language code against the display label
                  configElement.addLanguage(localeCode, label);
               }
            }
         }
         
         // get the recent space max items
         Element recentSpaces = element.element(ELEMENT_RECENTSPACESITEMS);
         if (recentSpaces != null)
         {
            configElement.setRecentSpacesItems(Integer.parseInt(recentSpaces.getTextTrim()));
         }
         
         // get the Help url
         Element helpUrl = element.element(ELEMENT_HELPURL);
         if (helpUrl != null)
         {
            configElement.setHelpUrl(helpUrl.getTextTrim());
         }
         
         // get the edit link type
         Element editLinkType = element.element(ELEMENT_EDITLINKTYPE);
         if (editLinkType != null)
         {
            configElement.setEditLinkType(editLinkType.getTextTrim());
         }
         
         // get the minimum number of characters for valid search string
         Element searchMin = element.element(ELEMENT_SEARCHMINIMUM);
         if (searchMin != null)
         {
            configElement.setSearchMinimum(Integer.parseInt(searchMin.getTextTrim()));
         }
         
         // get the default permission for newly created users Home Spaces
         Element permission = element.element(ELEMENT_HOMESPACEPERMISSION);
         if (permission != null)
         {
            configElement.setHomeSpacePermission(permission.getTextTrim());
         }
         
         // get the Advanced Search config block
         Element advsearch = element.element(ELEMENT_ADVANCEDSEARCH);
         if (advsearch != null)
         {
            // get the list of content types
            Element contentTypes = advsearch.element(ELEMENT_CONTENTTYPES);
            Iterator<Element> typesItr = contentTypes.elementIterator(ELEMENT_TYPE);
            List<String> types = new ArrayList<String>(5);
            while (typesItr.hasNext())
            {
               Element contentType = typesItr.next();
               String type = contentType.attributeValue(ATTRIBUTE_NAME);
               if (type != null)
               {
                  types.add(type);
               }
            }
            configElement.setContentTypes(types);
            
            // get the list of custom properties to display
            Element customProps = advsearch.element(ELEMENT_CUSTOMPROPS);
            Iterator<Element> propsItr = customProps.elementIterator(ELEMENT_METADATA);
            List<CustomProperty> props = new ArrayList<CustomProperty>(5);
            while (propsItr.hasNext())
            {
               Element propElement = propsItr.next();
               String type = propElement.attributeValue(ATTRIBUTE_TYPE);
               String aspect = propElement.attributeValue(ATTRIBUTE_ASPECT);
               String prop = propElement.attributeValue(ATTRIBUTE_PROPERTY);
               String labelId = propElement.attributeValue(ATTRIBUTE_DISPLAYLABEL);
               props.add(new ClientConfigElement.CustomProperty(type, aspect, prop, labelId));
            }
            configElement.setCustomProperties(props);
         }
      }
      
      return configElement;
   }
   
   /**
    * Processes a page-size element
    * 
    * @param pageSizeElement The element to process
    * @param page The page the page-size element belongs to
    * @param configElement The config element being populated
    */
   @SuppressWarnings("unchecked")
   private void processPageSizeElement(Element pageSizeElement, String page, 
         ClientConfigElement configElement)
   {
      if (pageSizeElement != null)
      {
         Iterator<Element> views = pageSizeElement.elementIterator();
         while (views.hasNext())
         {
            Element view = views.next();
            String viewName = view.getName();
            String pageSize = view.getTextTrim();
            try
            {
               configElement.addDefaultPageSize(page, viewName, Integer.parseInt(pageSize));
            }
            catch (NumberFormatException nfe)
            {
               if (logger.isWarnEnabled())
               {
                  logger.warn("Failed to set page size for view '" + viewName + 
                        "' in page '" + page + "' as '" + pageSize + 
                        "' is an invalid number!");
               }
            }
         }
      }
   }
}
