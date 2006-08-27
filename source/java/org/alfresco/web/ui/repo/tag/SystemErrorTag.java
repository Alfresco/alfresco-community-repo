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
import java.util.ResourceBundle;

import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.ExternalAccessServlet;
import org.alfresco.web.bean.ErrorBean;

/**
 * A non-JSF tag library that displays the currently stored system error
 * 
 * @author gavinc
 */
public class SystemErrorTag extends TagSupport
{
   private static final long serialVersionUID = -7336055169875448199L;
   
   private static final String MSG_RETURN_TO_APP = "return_to_application";
   private static final String MSG_HIDE_DETAILS  = "hide_details";
   private static final String MSG_SHOW_DETAILS  = "show_details";
   private static final String MSG_LOGOUT        = "logout";
   
   private String styleClass;
   private String detailsStyleClass;
   private boolean showDetails = false;
   
   
   /**
    * @return Returns the showDetails.
    */
   public boolean isShowDetails()
   {
      return showDetails;
   }
   
   /**
    * @param showDetails The showDetails to set.
    */
   public void setShowDetails(boolean showDetails)
   {
      this.showDetails = showDetails;
   }
   
   /**
    * @return Returns the styleClass.
    */
   public String getStyleClass()
   {
      return styleClass;
   }
   
   /**
    * @param styleClass The styleClass to set.
    */
   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }
   
   /**
    * @return Returns the detailsStyleClass.
    */
   public String getDetailsStyleClass()
   {
      return detailsStyleClass;
   }

   /**
    * @param detailsStyleClass The detailsStyleClass to set.
    */
   public void setDetailsStyleClass(String detailsStyleClass)
   {
      this.detailsStyleClass = detailsStyleClass;
   }

   /**
    * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
    */
   public int doStartTag() throws JspException
   {
      String errorMessage = "No error currently stored";
      String errorDetails = "No details";
      
      // get the error details from the bean, this may be in a portlet
      // session or a normal servlet session.
      ErrorBean errorBean = null;
      RenderRequest renderReq  = (RenderRequest)pageContext.getRequest().
                                   getAttribute("javax.portlet.request");
      if (renderReq != null)
      {
         PortletSession session = renderReq.getPortletSession();
         errorBean = (ErrorBean)session.getAttribute(ErrorBean.ERROR_BEAN_NAME);
      }
      else
      {
         errorBean = (ErrorBean)pageContext.getSession().
                        getAttribute(ErrorBean.ERROR_BEAN_NAME);
      }

      if (errorBean != null)
      {
         errorMessage = errorBean.getLastErrorMessage();
         errorDetails = errorBean.getStackTrace();
      }
      else
      {
         // if we reach here the error was caught by the declaration in web.xml so
         // pull all the information from the request and create the error bean
         Throwable error = (Throwable)pageContext.getRequest().getAttribute("javax.servlet.error.exception");
         String uri = (String)pageContext.getRequest().getAttribute("javax.servlet.error.request_uri");
         
         // create and store the ErrorBean
         errorBean = new ErrorBean();
         pageContext.getSession().setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean);
         errorBean.setLastError(error);
         errorBean.setReturnPage(uri);
         errorMessage = errorBean.getLastErrorMessage();
         errorDetails = errorBean.getStackTrace();
      }
      
      try
      {
         Writer out = pageContext.getOut();
         
         ResourceBundle bundle = Application.getBundle(pageContext.getSession());
         
         out.write("<div");
         
         if (this.styleClass != null)
         {
            out.write(" class='");
            out.write(this.styleClass);
            out.write("'");
         }
         
         out.write(">");
         out.write(errorMessage);
         out.write("</div>");
         
         // work out initial state
         boolean hidden = !this.showDetails; 
         String display = "inline";
         String toggleTitle = "Hide";
         if (hidden)
         {
            display = "none";
            toggleTitle = "Show";
         }
         
         // output the script to handle toggling of details
         out.write("<script language='JavaScript'>\n");
         out.write("var hidden = ");
         out.write(Boolean.toString(hidden));
         out.write(";\n");   
         out.write("function toggleDetails() {\n");
         out.write("if (hidden) {\n");
         out.write("document.getElementById('detailsTitle').innerHTML = '");
         out.write(bundle.getString(MSG_HIDE_DETAILS));
         out.write("<br/><br/>';\n");
         out.write("document.getElementById('details').style.display = 'inline';\n");
         out.write("hidden = false;\n");
         out.write("} else {\n");
         out.write("document.getElementById('detailsTitle').innerHTML = '");
         out.write(bundle.getString(MSG_SHOW_DETAILS));
         out.write("';\n");
         out.write("document.getElementById('details').style.display = 'none';\n");
         out.write("hidden = true;\n");
         out.write("} } </script>\n");
         
         // output the initial toggle state
         out.write("<br/>");
         out.write("<a id='detailsTitle' href='javascript:toggleDetails();'>");
         out.write(toggleTitle);
         out.write(" Details</a>");
         
         out.write("<div style='padding-top:5px;display:");
         out.write(display);
         out.write("' id='details'");
         
         if (this.detailsStyleClass != null)
         {
            out.write(" class='");
            out.write(this.detailsStyleClass);
            out.write("'");
         }
         
         out.write(">");
         out.write(errorDetails);
         out.write("</div>");
         
         // output a link to return to the application
         out.write("\n<div style='padding-top:16px;'><a href='");
      
         if (Application.inPortalServer())
         {
            RenderResponse renderResp  = (RenderResponse)pageContext.getRequest().getAttribute(
                  "javax.portlet.response");
            if (renderResp == null)
            {
               throw new IllegalStateException("RenderResponse object is null");
            }
            
            PortletURL url = renderResp.createRenderURL();
            // NOTE: we don't have to specify the page for the portlet, just the VIEW_ID parameter
            //       being present will cause the current JSF view to be re-displayed
            url.setParameter("org.apache.myfaces.portlet.MyFacesGenericPortlet.VIEW_ID", "current-view");
            out.write(url.toString());
         }
         else
         {
            String returnPage = null;
            
            if (errorBean != null)
            {
               returnPage = errorBean.getReturnPage();
            }
            
            if (returnPage == null)
            {
               out.write("javascript:history.back();");
            }
            else
            {
               out.write(returnPage);
            }
         }
         
         out.write("'>");
         out.write(bundle.getString(MSG_RETURN_TO_APP));
         out.write("</a></div>");
                   
         // use External Access Servlet to generate a URL to relogin again
         // this can be used by the user if the app has got into a total mess
         if (Application.inPortalServer() == false)
         {
            out.write("\n<div style='padding-top:16px;'><a href='");
            out.write(((HttpServletRequest)pageContext.getRequest()).getContextPath());
            out.write(ExternalAccessServlet.generateExternalURL("logout", null));
            out.write("'>");
            out.write(bundle.getString(MSG_LOGOUT));
            out.write("</a></div>");
         }
      }
      catch (IOException ioe)
      {
         throw new JspException(ioe);
      }
      finally
      {
         // clear out the error bean otherwise the next error could be hidden
         pageContext.getSession().removeAttribute(ErrorBean.ERROR_BEAN_NAME);
      }
      
      return SKIP_BODY;
   }
   
   /**
    * @see javax.servlet.jsp.tagext.TagSupport#release()
    */
   public void release()
   {
      this.styleClass = null;
      
      super.release();
   }
}
