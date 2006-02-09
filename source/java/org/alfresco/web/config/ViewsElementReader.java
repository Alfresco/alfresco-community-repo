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

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for client views
 * 
 * @author Gavin Cornwell
 */
public class ViewsElementReader implements ConfigElementReader
{
   public static final String ELEMENT_VIEW = "view";
   public static final String ELEMENT_VIEWIMPL = "view-impl";
   public static final String ELEMENT_VIEWDEFAULTS = "view-defaults";
   public static final String ELEMENT_PAGESIZE = "page-size";
   public static final String ELEMENT_SORTCOLUMN = "sort-column";
   public static final String ELEMENT_SORTDIRECTION = "sort-direction";
   
   private static Log logger = LogFactory.getLog(ViewsElementReader.class);
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      ViewsConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ViewsConfigElement.CONFIG_ELEMENT_ID) == false)
         {
            throw new ConfigException("ViewsElementReader can only parse " +
                  ViewsConfigElement.CONFIG_ELEMENT_ID + " elements, the element passed was '" + 
                  name + "'");
         }
         
         configElement = new ViewsConfigElement();
         
         // get the configured views
         Iterator<Element> renderers = element.elementIterator(ELEMENT_VIEWIMPL);
         while (renderers.hasNext())
         {
            Element renderer = renderers.next();
            configElement.addView(renderer.getTextTrim());
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
               
               // get the sort direction option
               Element sortDir = page.element(ELEMENT_SORTDIRECTION);
               if (sortDir != null)
               {
                  configElement.addSortDirection(pageName, sortDir.getTextTrim());
               }
               
               // process the page-size element
               processPageSizeElement(page.element(ELEMENT_PAGESIZE), 
                     pageName, configElement);
            }
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
         ViewsConfigElement configElement)
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
