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

import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.alfresco.web.app.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A non-JSF tag library that outputs the form tag for uploading files, this tag
 * is sensitive to whether the application is running in servlet or portlet mode
 * 
 * @author gavinc
 */
public class UploadFormTag extends TagSupport
{
   private static final long serialVersionUID = 4064734856565167835L;
    
   private static Log logger = LogFactory.getLog(UploadFormTag.class);
   
   /**
    * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
    */
   public int doStartTag() throws JspException
   {
      try
      {
         Writer out = pageContext.getOut();
         
         out.write("<form name='upload-form' acceptCharset='UTF-8' method='post' enctype='multipart/form-data' action='");
         
         if (Application.inPortalServer())
         {
            RenderResponse renderResp  = (RenderResponse)pageContext.getRequest().
                                   getAttribute("javax.portlet.response");
            if (renderResp == null)
            {
               throw new IllegalStateException("RenderResponse object is null. The web application is not executing within a portal server!");
            }
            
            out.write(renderResp.createActionURL().toString());
            out.write("'>");
         }
         else
         {
            HttpServletRequest req = (HttpServletRequest)pageContext.getRequest(); 
            out.write(req.getContextPath());
            out.write("/uploadFileServlet'>\n");
            out.write("<input type='hidden' name='return-page' value='");
            out.write(req.getContextPath() + "/faces" + req.getServletPath());
            out.write("'>\n");
         }
      }
      catch (IOException ioe)
      {
         throw new JspException(ioe.toString());
      }
      
      return EVAL_BODY_INCLUDE;
   }

   public int doEndTag() throws JspException
   {
      try
      {
         pageContext.getOut().write("\n</form>");
      }
      catch (IOException ioe)
      {
         throw new JspException(ioe.toString());
      }
      
      return super.doEndTag();
   }
}
