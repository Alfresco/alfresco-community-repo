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

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.config.DashboardsConfigElement;
import org.alfresco.web.config.DashboardsConfigElement.DashletDefinition;
import org.alfresco.web.config.DashboardsConfigElement.LayoutDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Describes the config for the Pages in a user Dashboard.
 * Multiple Pages are supported.
 * 
 * @author Kevin Roast
 */
public final class PageConfig implements Serializable
{
   private static final long serialVersionUID = 5464324390924278215L;

   private static Log logger = LogFactory.getLog(DashboardManager.class);
   
   private static final String ELEMENT_DASHBOARD = "dashboard";
   private static final String ELEMENT_PAGE = "page";
   private static final String ELEMENT_COLUMN = "column";
   private static final String ELEMENT_DASHLET = "dashlet";
   private static final String ATTR_ID = "id";
   private static final String ATTR_LAYOUTID = "layout-id";
   private static final String ATTR_REFID = "idref";
   
   private List<Page> pages = new ArrayList<Page>(4);
   private Page currentPage = null;
   
   
   /**
    * Default constructor
    */
   public PageConfig()
   {
   }
   
   /**
    * Copy constructor
    * 
    * @param copy    PageConfig to copy
    */
   public PageConfig(PageConfig copy)
   {
      this.pages = new ArrayList<Page>(copy.pages.size());
      for (Page page : copy.pages)
      {
         // invoke the copy constructor on each Page
         // which in turn calls the copy constructor of child classes
         this.pages.add(new Page(page));
      }
   }

   /**
    * @return The current page in the config
    */
   public Page getCurrentPage()
   {
      if (this.currentPage == null)
      {
         if (this.pages.size() != 0)
         {
            this.currentPage = pages.get(0);
         }
      }
      return this.currentPage;
   }
   
   /**
    * Set the current Page for the cnfig
    * 
    * @param pageId     ID of the page to set as current
    */
   public void setCurrentPage(String pageId)
   {
      for (Page page : pages)
      {
         if (page.getId().equals(pageId))
         {
            this.currentPage = page;
            break;
         }
      }
   }
   
   /**
    * Add a new Page to the list
    * 
    * @param page Page to add
    */
   public void addPage(Page page)
   {
      pages.add(page);
   }
   
   /**
    * Get a Page with the specified page Id
    * 
    * @param pageId     Of the page to return
    * 
    * @return Page or null if not found
    */
   public Page getPage(String pageId)
   {
      Page foundPage = null;
      for (Page page : pages)
      {
         if (page.getId().equals(pageId))
         {
            foundPage = page;
            break;
         }
      }
      return foundPage;
   }
   
   /**
    * Convert this config to an XML definition which can be serialized.
    * Example:
    * <code>
    * <?xml version="1.0"?>
    * <dashboard>
    *    <page id="main" layout-id="narrow-left-2column">
    *       <column>
    *          <dashlet idref="clock" />
    *          <dashlet idref="random-joke" />
    *       </column>
    *       <column>
    *          <dashlet idref="getting-started" />
    *          <dashlet idref="task-list" />
    *          <dashlet idref="my-checkedout-docs" />
    *          <dashlet idref="my-documents" />
    *       </column>
    *    </page>
    * </dashboard>
    * </code>
    * 
    * @return XML for this config
    */
   public String toXML()
   {
      try
      {
         Document doc = DocumentHelper.createDocument();
         
         Element root = doc.addElement(ELEMENT_DASHBOARD);
         for (Page page : pages)
         {
            Element pageElement = root.addElement(ELEMENT_PAGE);
            pageElement.addAttribute(ATTR_ID, page.getId());
            pageElement.addAttribute(ATTR_LAYOUTID, page.getLayoutDefinition().Id);
            for (Column column : page.getColumns())
            {
               Element columnElement = pageElement.addElement(ELEMENT_COLUMN);
               for (DashletDefinition dashletDef : column.getDashlets())
               {
                  columnElement.addElement(ELEMENT_DASHLET).addAttribute(ATTR_REFID, dashletDef.Id);
               }
            }
         }
         
         StringWriter out = new StringWriter(512);
         XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
         writer.setWriter(out);
         writer.write(doc);
         
         return out.toString();
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Unable to serialize Dashboard PageConfig to XML: " + err.getMessage(), err);
      }
   }
   
   @Override
   public String toString()
   {
      return toXML();
   }

   /**
    * Deserialise this PageConfig instance from the specified XML stream.
    * 
    * @param xml
    */
   public void fromXML(DashboardsConfigElement config, String xml)
   {
      try
      {
         SAXReader reader = new SAXReader();
         Document document = reader.read(new StringReader(xml));
         Element rootElement = document.getRootElement();
         
         // walk the pages found in xml
         Iterator itrPages = rootElement.elementIterator(ELEMENT_PAGE);
         while (itrPages.hasNext())
         {
            Element pageElement = (Element)itrPages.next();
            String layoutId = pageElement.attributeValue(ATTR_LAYOUTID);
            LayoutDefinition layoutDef = config.getLayoutDefinition(layoutId);
            if (layoutDef != null)
            {
               // found the layout now build the page and read the columns
               Page page = new Page(pageElement.attributeValue(ATTR_ID), layoutDef);
               Iterator itrColumns = pageElement.elementIterator(ELEMENT_COLUMN);
               while (itrColumns.hasNext())
               {
                  Column column = new Column();
                  
                  // read and resolve the dashlet definitions for this column
                  Element columnElement = (Element)itrColumns.next();
                  Iterator itrDashlets = columnElement.elementIterator(ELEMENT_DASHLET);
                  while (itrDashlets.hasNext())
                  {
                     String dashletId = ((Element)itrDashlets.next()).attributeValue(ATTR_REFID);
                     DashletDefinition dashletDef = config.getDashletDefinition(dashletId);
                     if (dashletDef != null)
                     {
                        column.addDashlet(dashletDef);
                     }
                     else if (logger.isWarnEnabled())
                     {
                        logger.warn("Failed to resolve Dashboard Dashlet Definition ID: " + dashletId);
                     }
                  }
                  
                  // add the column of dashlets to the page
                  page.addColumn(column);
               }
               
               // add the page to this config instance 
               this.addPage(page);
            }
            else if (logger.isWarnEnabled())
            {
               logger.warn("Failed to resolve Dashboard Layout Definition ID: " + layoutId);
            }
         }
      }
      catch (DocumentException docErr)
      {
         // if we cannot parse, then we simply revert to default
      }
   }
}

/**
 * Simple class to represent a Page in a Dashboard.
 * Each Page has a Layout associated with it, and a number of Column definitions.
 */
final class Page implements Serializable
{
   private static final long serialVersionUID = 8023042580316126423L;
   
   private String id;
   private LayoutDefinition layoutDef;
   private List<Column> columns = new ArrayList<Column>(4);
   
   /**
    * Constructor
    * 
    * @param id
    * @param layout
    */
   public Page(String id, LayoutDefinition layout)
   {
      if (id == null || id.length() == 0)
      {
         throw new IllegalArgumentException("ID for a Dashboard Page is mandatory.");
      }
      if (layout == null)
      {
         throw new IllegalArgumentException("Layout for a Dashboard Page is mandatory.");
      }
      this.id = id;
      this.layoutDef = layout;
   }
   
   /**
    * Copy Constructor
    * 
    * @param copy    Page to build a copy from
    */
   public Page(Page copy)
   {
      this.id = copy.id;
      this.layoutDef = copy.layoutDef;
      for (Column column : copy.columns)
      {
         Column cloneColumn = new Column(column);
         addColumn(cloneColumn);
      }
   }
   
   public String getId()
   {
      return this.id;
   }
   
   public LayoutDefinition getLayoutDefinition()
   {
      return this.layoutDef;
   }
   
   public void setLayoutDefinition(LayoutDefinition layout)
   {
      if (layout == null)
      {
         throw new IllegalArgumentException("Layout for a Dashboard Page is mandatory.");
      }
      
      // correct column collection based on new layout definition
      while (this.columns.size() < layout.Columns)
      {
         addColumn(new Column());
      }
      if (this.columns.size() > layout.Columns)
      {
         this.columns = this.columns.subList(0, layout.Columns);
      }
      
      this.layoutDef = layout;
   }
   
   public void addColumn(Column column)
   {
      this.columns.add(column);
   }
   
   public List<Column> getColumns()
   {
      return this.columns;
   }
}

/**
 * Simple class representing a single Column in a dashboard Page.
 * Each column contains a list of Dashlet definitions.
 */
final class Column implements Serializable
{
   private static final long serialVersionUID = 6462844390234508010L;
   
   private List<DashletDefinition> dashlets = new ArrayList<DashletDefinition>(4);
   
   /**
    * Default constructor
    */
   public Column()
   {
   }
   
   /**
    * Copy constructor
    * 
    * @param copy    Column to copy
    */
   public Column(Column copy)
   {
      this.dashlets = new ArrayList<DashletDefinition>(copy.dashlets.size());
      this.dashlets.addAll(copy.dashlets);
   }
   
   public void addDashlet(DashletDefinition dashlet)
   {
      dashlets.add(dashlet);
   }
   
   public List<DashletDefinition> getDashlets()
   {
      return this.dashlets;
   }
   
   public void trimDashlets(int max)
   {
      if (max >= 0)
      {
         int maxTrim = Math.min(this.dashlets.size(), max);
         this.dashlets = this.dashlets.subList(0, maxTrim);
      }
   }
}
