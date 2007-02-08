/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.pr;

import java.util.*;
import java.text.*;
import javax.servlet.jsp.PageContext;
import org.w3c.dom.*;
import org.alfresco.web.forms.*;

/**
 * Bean for getting data for press releases.
 * It's used by /media/releases/index.jsp to aggregate all forms created by press-release.xsd in 
 * /media/releases/content and generate an index page for them.
 */
public class PressReleaseBean
{
   /**
    * Loads all xml assets created by press-release.xsd in /media/releases/content and populates
    * PressReleaseBeans with their contents.
    *
    * @param pageContext the page context from the jsp, needed for accessing the 
    * servlet context for the ServletContextFormDataFunctionsAdapter class.
    *
    * @return a list of populated PressReleaseBeans.
    */
   public static List<PressReleaseBean> getPressReleases(final PageContext pageContext)
      throws Exception
   {
      final FormDataFunctions ef = 
         new ServletContextFormDataFunctionsAdapter(pageContext.getServletContext());

      final Map<String, Document> entries = 
         ef.parseXMLDocuments("press-release", "/media/releases/content");
      final List<PressReleaseBean> result = new ArrayList<PressReleaseBean>(entries.size());
      for (Map.Entry<String, Document> entry : entries.entrySet())
      {
         result.add(PressReleaseBean.loadPressRelease(entry.getValue(), entry.getKey()));
      }
      return result;
   }

   /**
    * Provides a list of press releases for a specified category.
    *
    * @param pageContext the page context from the jsp
    * @param category the category to search for
    *
    * @return all press releases within the specified category.
    */
   public static List<PressReleaseBean> getPressReleasesInCategory(final PageContext pageContext,
                                                                   final String category)
      throws Exception
   {
      final FormDataFunctions ef = 
         new ServletContextFormDataFunctionsAdapter(pageContext.getServletContext());

      final Map<String, Document> entries = 
         ef.parseXMLDocuments("press-release", "/media/releases/content");
      final List<PressReleaseBean> result = new ArrayList<PressReleaseBean>(entries.size());
      for (Map.Entry<String, Document> entry : entries.entrySet())
      {
         final Document d = entry.getValue();
         final Element cEl = (Element)
            d.getElementsByTagName("pr:category").item(0);
         
         if (category.equals(cEl.getFirstChild().getNodeValue()))
         {
            result.add(PressReleaseBean.loadPressRelease(d, entry.getKey()));
         }
      }
      return result;
   }

   /**
    * Returns a set of unique categories used by the press releases in sorted order.
    *
    * @param pageContext the page context variable from the jsp
    *
    * @return a set of unique categories used by the press releases in sorted order.
    */
   public static Set<String> getPressReleaseCategories(final PageContext pageContext)
      throws Exception
   {
      final FormDataFunctions ef = 
         new ServletContextFormDataFunctionsAdapter(pageContext.getServletContext());

      final TreeSet<String> result = new TreeSet<String>();
      final Map<String, Document> entries =
         ef.parseXMLDocuments("press-release", "/media/releases/content");
      for (Map.Entry<String, Document> entry : entries.entrySet())
      {
         final Element cEl = (Element)
            entry.getValue().getElementsByTagName("pr:category").item(0);
         result.add(cEl.getFirstChild().getNodeValue());
      }
      return result;
   }

   /**
    * Utility function to create an instance of a press release using
    * form instance data.
    *
    * @param d the xml document
    * @param fileName the filename of the file from which the document was loaded.
    *
    * @return a press release representing the content of the file.
    */
   private static PressReleaseBean loadPressRelease(final Document d,
                                                    final String fileName)
      throws Exception
   {
      final Element t = (Element)d.getElementsByTagName("pr:title").item(0);
      final Element a = (Element)d.getElementsByTagName("pr:abstract").item(0);
      final Element dateEl = (Element)d.getElementsByTagName("pr:launch_date").item(0);
      final Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateEl.getFirstChild().getNodeValue());
      String href = "/media/releases/content/" + fileName;
      href = href.replaceAll(".xml$", ".html");
      return new PressReleaseBean(t.getFirstChild().getNodeValue(),
                                  a.getFirstChild().getNodeValue(),
                                  date,
                                  href);
   }

   private final String title;
   private final String theAbstract;
   private final Date launchDate;
   private final String href;

   public PressReleaseBean(final String title, 
                           final String theAbstract, 
                           final Date launchDate, 
                           final String href)
   {
      this.title = title;
      this.theAbstract = theAbstract;
      this.launchDate = launchDate;
      this.href = href;
   }

   /** 
    * The title of the press release as defined in the xml asset. 
    */
   public String getTitle() 
   { 
      return this.title; 
   }

   /** 
    * The abstract of the press release as defined in the xml asset. 
    */
   public String getAbstract()
   {
      return this.theAbstract;
   }

   /** 
    * The launch date of the press release as defined in the xml asset. 
    */
   public Date getLaunchDate()
   {
      return this.launchDate;
   }

   /**
    * Returns the url within the webapp to the xml file describing this press release
    *
    * @return the url to the xml file which will be something like /media/releases/content/[filename].xml
    */
   public String getHref()
   {
      return this.href;
   }
}