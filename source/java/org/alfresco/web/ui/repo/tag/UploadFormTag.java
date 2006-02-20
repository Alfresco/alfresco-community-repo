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
