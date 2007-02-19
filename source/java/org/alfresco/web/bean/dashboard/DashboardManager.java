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
package org.alfresco.web.bean.dashboard;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.faces.context.FacesContext;

import org.alfresco.config.ConfigService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.config.DashboardsConfigElement;
import org.alfresco.web.config.DashboardsConfigElement.DashletDefinition;
import org.alfresco.web.config.DashboardsConfigElement.LayoutDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean that manages the Dashboard framework.
 * 
 * @author Kevin Roast
 */
public class DashboardManager
{
   private static Log logger = LogFactory.getLog(DashboardManager.class);
   
   private static final String PREF_DASHBOARD = "dashboard";
   private static final String LAYOUT_DEFAULT = "default";
   private static final String DASHLET_STARTEDDEFAULT = "getting-started";
   private static final String DASHLET_TASKSDEFAULT = "tasks-todo";
   
   private static final String JSP_DUMMY = "/jsp/dashboards/dummy.jsp";
   
   private PageConfig pageConfig = null;
   private DashletRenderingList renderingList = null;
   private DashletTitleList titleList = null;
   
   /**
    * @return The layout JSP page for the current My Alfresco dashboard page
    */
   public String getLayoutPage()
   {
      String layout = null;
      Page page = getPageConfig().getCurrentPage();
      if (page != null)
      {
         layout = page.getLayoutDefinition().JSPPage;
      }
      return layout;
   }
   
   /**
    * Helper to init the dashboard for display
    */
   public void initDashboard()
   {
      this.renderingList = null;
      this.titleList = null;
   }
   
   /**
    * @return JSF List getter to return which dashlets are available for rendering
    */
   public List getDashletAvailable()
   {
      if (this.renderingList == null)
      {
         this.renderingList = new DashletRenderingList(getPageConfig());
      }
      return this.renderingList;
   }
   
   /**
    * @return JSF List getter to return dashlet title strings
    */
   public List getDashletTitle()
   {
      if (this.titleList == null)
      {
         this.titleList = new DashletTitleList(getPageConfig());
      }
      return this.titleList;
   }
   
   /**
    * Return the JSP for the specified dashlet index
    * 
    * @param index   Zero based index from the left most column working top-bottom then left-right
    * 
    * @return JSP page for the dashlet or a blank dummy page if not found
    */
   public String getDashletPage(int index)
   {
      String page = JSP_DUMMY;
      DashletDefinition def = getDashletDefinitionByIndex(getPageConfig(), index);
      if (def != null)
      {
         page = def.JSPPage;
      }
      return page;
   }
   
   /**
    * @return the PageConfig for the current My Alfresco dashboard page
    */
   public PageConfig getPageConfig()
   {
      if (this.pageConfig == null)
      {
         PageConfig pageConfig;
         
         DashboardsConfigElement config = getDashboardConfig();
         
         // read the config for this user from the Preferences
         String xml = (String)PreferencesService.getPreferences().getValue(PREF_DASHBOARD);
         if (xml != null && xml.length() != 0)
         {
            if (logger.isDebugEnabled())
               logger.debug("PageConfig found: " + xml);
            
            // process the XML config and convert into a PageConfig object
            pageConfig = new PageConfig();
            pageConfig.fromXML(config, xml);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("No PageConfig found, creating default instance.");
            
            // create default config for the first access for a user
            pageConfig = new PageConfig();
            LayoutDefinition layout = config.getLayoutDefinition(LAYOUT_DEFAULT);
            if (layout != null)
            {
               Page page = new Page("default", layout);
               Column defaultColumn = new Column();
               
               // add the default dashlet(s) to the column
               DashletDefinition dashlet = config.getDashletDefinition(DASHLET_STARTEDDEFAULT);
               if (dashlet != null)
               {
                  defaultColumn.addDashlet(dashlet);
               }
               dashlet = config.getDashletDefinition(DASHLET_TASKSDEFAULT);
               if (dashlet != null)
               {
                  defaultColumn.addDashlet(dashlet);
               }
               
               // add the column to the page and we are done
               page.addColumn(defaultColumn);
               pageConfig.addPage(page);
            }
         }
         
         this.pageConfig = pageConfig;
      }
      
      return this.pageConfig;
   }
   
   /**
    * Persist the supplied PageConfig for the current user
    */
   public void savePageConfig(PageConfig config)
   {
      this.pageConfig = config;
      
      // reset cached values
      initDashboard();
      
      // persist the changes
      PreferencesService.getPreferences().setValue(PREF_DASHBOARD, this.pageConfig.toXML());
   }
   
   /**
    * @return The externally configured WebClient config element for the Dashboards
    */
   public static DashboardsConfigElement getDashboardConfig()
   {
      ConfigService service = Application.getConfigService(FacesContext.getCurrentInstance());
      DashboardsConfigElement config = (DashboardsConfigElement)service.getConfig("Dashboards").getConfigElement(
            DashboardsConfigElement.CONFIG_ELEMENT_ID);
      return config;
   }
   
   /**
    * Helper to get the DashDefinition as the zero based index, working from the left most column
    * top-bottom then working left-right.
    * 
    * @param index   Zero based index from the left most column working top-bottom then left-right
    * 
    * @return DashletDefinition if found or null if no dashlet at the specified index
    */
   private static DashletDefinition getDashletDefinitionByIndex(PageConfig config, int index)
   {
      DashletDefinition def = null;
      
      LayoutDefinition layoutDef = config.getCurrentPage().getLayoutDefinition();
      List<Column> columns = config.getCurrentPage().getColumns();
      int columnCount = columns.size();
      int selectedColumn = index / layoutDef.ColumnLength;
      if (selectedColumn < columnCount)
      {
         List<DashletDefinition> dashlets = columns.get(selectedColumn).getDashlets();
         if (index % layoutDef.ColumnLength < dashlets.size())
         {
            def = dashlets.get(index % layoutDef.ColumnLength);
         }
      }
      if (logger.isDebugEnabled())
         logger.debug("Searching for dashlet at index: " + index +
               " and found " + (def != null ? def.JSPPage : null));
      
      return def;
   }
   
   
   /**
    * Dashlet rendering list.
    * 
    * Returns true from the get() method if the specified dashlet is available for rendering.
    */
   private static class DashletRenderingList extends JSFHelperList
   {
      PageConfig config;
      
      public DashletRenderingList(PageConfig config)
      {
         this.config = config;
      }
      
      /**
       * @see java.util.List#get(int)
       */
      public Object get(int index)
      {
         return getDashletDefinitionByIndex(config, index) != null;
      }
   }
   
   /**
    * Dashlet title list.
    * 
    * Returns the title string from the get() method if the specified dashlet is available.
    */
   private static class DashletTitleList extends JSFHelperList
   {
      PageConfig config;
      
      public DashletTitleList(PageConfig config)
      {
         this.config = config;
      }
      
      /**
       * @see java.util.List#get(int)
       */
      public Object get(int index)
      {
         String result = "";
         
         DashletDefinition def = getDashletDefinitionByIndex(config, index);
         if (def != null)
         {
            if (def.LabelId != null)
            {
               result = Application.getMessage(FacesContext.getCurrentInstance(), def.LabelId);
            }
            else if (def.Label != null)
            {
               result = def.Label;
            }
         }
         
         return result;
      }
   }
   
   /**
    * Helper class that implements a dummy List contract for use by JSF List getter methods
    */
   private static abstract class JSFHelperList implements List
   {
      //
      // Satisfy List interface contract
      //
      
      public void add(int arg0, Object arg1)
      {
      }

      public boolean add(Object arg0)
      {
         return false;
      }

      public boolean addAll(Collection arg0)
      {
         return false;
      }

      public boolean addAll(int arg0, Collection arg1)
      {
         return false;
      }

      public void clear()
      {
      }

      public boolean contains(Object arg0)
      {
         return false;
      }

      public boolean containsAll(Collection arg0)
      {
         return false;
      }

      public int indexOf(Object arg0)
      {
         return 0;
      }

      public boolean isEmpty()
      {
         return false;
      }
      
      public Iterator iterator()
      {
         return null;
      }

      public int lastIndexOf(Object arg0)
      {
         return 0;
      }

      public ListIterator listIterator()
      {
         return null;
      }

      public ListIterator listIterator(int arg0)
      {
         return null;
      }

      public Object remove(int arg0)
      {
         return null;
      }

      public boolean remove(Object arg0)
      {
         return false;
      }

      public boolean removeAll(Collection arg0)
      {
         return false;
      }

      public boolean retainAll(Collection arg0)
      {
         return false;
      }

      public Object set(int arg0, Object arg1)
      {
         return null;
      }

      public int size()
      {
         return 0;
      }

      public List subList(int arg0, int arg1)
      {
         return null;
      }

      public Object[] toArray()
      {
         return null;
      }

      public Object[] toArray(Object[] arg0)
      {
         return null;
      }
   }
}
