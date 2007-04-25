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
package org.alfresco.web.ui.repo.tag;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.alfresco.web.app.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A non-JSF tag library that adds the HTML begin and end tags if running in servlet mode
 * 
 * @author gavinc
 */
public class PageTag extends TagSupport
{
   private static final long serialVersionUID = 8142765393181557228L;
   
   private final static String SCRIPTS_START = "<script type=\"text/javascript\" src=\"";
   private final static String SCRIPTS_MENU = "/scripts/menu.js\"></script>";
   private final static String SCRIPTS_WEBDAV = "/scripts/webdav.js\"></script>";
   private final static String STYLES_START  = "<link rel=\"stylesheet\" href=\"";
   private final static String STYLES_MAIN  = "/css/main.css\" TYPE=\"text/css\">\n";

/**
 * Please ensure you understand the terms of the license before changing the contents of this file.
 */

   private final static String ALF_URL   = "http://www.alfresco.com";
   private final static String ALF_LOGO  = "http://www.alfresco.com/images/alfresco_community_horiz20.gif";
   private final static String SF_LOGO  = "/images/logo/sflogo.php.png";
   private final static String ALF_TEXT  = "Alfresco Community";
   private final static String ALF_COPY  = "Supplied free of charge with " +
        "<a class=footer href='http://www.alfresco.com/services/support/communityterms/#support'>no support</a>, " +
        "<a class=footer href='http://www.alfresco.com/services/support/communityterms/#certification'>no certification</a>, " +
        "<a class=footer href='http://www.alfresco.com/services/support/communityterms/#maintenance'>no maintenance</a>, " +
        "<a class=footer href='http://www.alfresco.com/services/support/communityterms/#warranty'>no warranty</a> and " +
        "<a class=footer href='http://www.alfresco.com/services/support/communityterms/#indemnity'>no indemnity</a> by " +
        "<a class=footer href='http://www.alfresco.com'>Alfresco</a> or its " +
        "<a class=footer href='http://www.alfresco.com/partners/'>Certified Partners</a>. " +
        "<a class=footer href='http://www.alfresco.com/services/support/'>Click here for support</a>. " +
        "Alfresco Software Inc. &copy; 2005-2006 All rights reserved.";
   
   private static Log logger = LogFactory.getLog(PageTag.class);
   private static String alfresco = null;
   private static String loginPage = null;
   
   private long startTime = 0;
   private String title;
   private String titleId;
   
   /**
    * @return The title for the page
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param title Sets the page title
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return The title message Id for the page
    */
   public String getTitleId()
   {
      return titleId;
   }

   /**
    * @param titleId Sets the page title message Id
    */
   public void setTitleId(String titleId)
   {
      this.titleId = titleId;
   }
   
   public void release()
   {
      super.release();
      title = null;
      titleId = null;
   }

   /**
    * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
    */
   public int doStartTag() throws JspException
   {
      if (logger.isDebugEnabled())
         startTime = System.currentTimeMillis();
      
      try
      {
         Writer out = pageContext.getOut();
         
         if (Application.inPortalServer() == false)
         {
            out.write("<html><head><title>");
            if (this.titleId != null && this.titleId.length() != 0)
            {
               out.write(Application.getMessage(pageContext.getSession(), this.titleId));
            }
            else if (this.title != null && this.title.length() != 0)
            {
               out.write(this.title);
            }
            else
            {
               out.write("Alfresco Web Client");
            }
            out.write("</title></head>");
            out.write("<body>\n");
         }
         
         String reqPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
         
         // CSS style includes
         out.write(STYLES_START);
         out.write(reqPath);
         out.write(STYLES_MAIN);
         
         // menu javascript
         out.write(SCRIPTS_START);
         out.write(reqPath);
         out.write(SCRIPTS_MENU);
         
         // webdav javascript
         out.write(SCRIPTS_START);
         out.write(reqPath);
         out.write(SCRIPTS_WEBDAV);
         
         // base yahoo file
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/yahoo/yahoo/yahoo-min.js\"></script>");
         
         // io handling (AJAX)
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/yahoo/connection/connection-min.js\"></script>");
         
         // DOM handling
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/yahoo/dom/dom-min.js\"></script>");
         
         // event handling
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/yahoo/event/event-min.js\"></script>");
         
         // animation
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/yahoo/animation/animation-min.js\"></script>");
         
         // drag-drop
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/yahoo/dragdrop/dragdrop-min.js\"></script>");
         
         // common Alfresco util methods
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/common.js\"></script>");
         
         // pop-up panel helper objects
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(reqPath);
         out.write("/scripts/ajax/summary-info.js\"></script>");
         
         // set the context path used by some Alfresco script objects
         out.write("<script type=\"text/javascript\">");
         out.write("setContextPath('");
         out.write(reqPath);
         out.write("');</script>\n");
      }
      catch (IOException ioe)
      {
         throw new JspException(ioe.toString());
      }
      
      return EVAL_BODY_INCLUDE;
   }

   /**
    * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
    */
   public int doEndTag() throws JspException
   {
      try
      {
         HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
         if (req.getRequestURI().endsWith(getLoginPage()) == false)
         {
            pageContext.getOut().write(getAlfrescoButton());
         }
         
         if (Application.inPortalServer() == false)
         {
            pageContext.getOut().write("\n</body></html>");
         }
      }
      catch (IOException ioe)
      {
         throw new JspException(ioe.toString());
      }
      
      if (logger.isDebugEnabled())
      {
         long endTime = System.currentTimeMillis();
         logger.debug("Time to generate page: " + (endTime - startTime) + "ms");
      }
      
      return super.doEndTag();
   }
   
   private String getLoginPage()
   {
      if (loginPage == null)
      {
         loginPage = Application.getLoginPage(pageContext.getServletContext());
      }
      
      return loginPage;
   }

/**
 * Please ensure you understand the terms of the license before changing the contents of this file.
 */

   private String getAlfrescoButton()
   {
      if (alfresco == null)
      {
         String reqPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
         alfresco = "<center><table><tr><td>" +
                    "<a href='" + ALF_URL + "'>" +
                    "<img border=0 alt='' title='" + ALF_TEXT + "' align=absmiddle src='" + ALF_LOGO + "'>" +
                    "</a></td><td align=center>" +
                    "<span class=footer>" + ALF_COPY +
                    "</span></td><td><a href='http://sourceforge.net/projects/alfresco'><img border=0 alt='' title='SourceForge' align=absmiddle src='" +
                    reqPath + SF_LOGO + "'></a>" +
                    "</td></tr></table></center>";
      }
      
      return alfresco;
   }
}
