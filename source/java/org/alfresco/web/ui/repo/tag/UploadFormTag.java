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
