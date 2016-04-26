/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.web.ui.repo.tag;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.portlet.AlfrescoFacesPortlet;
import org.alfresco.web.app.servlet.BaseServlet;
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
            out.write(AlfrescoFacesPortlet.getActionURL(pageContext.getRequest()));
            out.write("'>");
         }
         else
         {
            HttpServletRequest req = (HttpServletRequest)pageContext.getRequest(); 
            out.write(req.getContextPath());
            out.write("/uploadFileServlet'>\n");
            out.write("<input type='hidden' name='return-page' value='");
            out.write(req.getContextPath() + BaseServlet.FACES_SERVLET + req.getServletPath());
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
