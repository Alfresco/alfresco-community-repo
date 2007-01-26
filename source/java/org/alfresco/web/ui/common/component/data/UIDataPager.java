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
package org.alfresco.web.ui.common.component.data;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.apache.log4j.Logger;

import org.alfresco.web.app.Application;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;

/**
 * @author Kevin Roast
 */
public class UIDataPager extends UICommand
{
   private static Logger s_logger = Logger.getLogger(IDataContainer.class);
   
   private static final String LAST_PAGE = "last_page";
   private static final String NEXT_PAGE = "next_page";
   private static final String PREVIOUS_PAGE = "prev_page";
   private static final String FIRST_PAGE = "first_page";
   private static final String MSG_PAGEINFO = "page_info";
   
   
   // ------------------------------------------------------------------------------
   // Construction 
   
   /**
    * Default constructor
    */
   public UIDataPager()
   {
      setRendererType(null);
   }
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      IDataContainer dataContainer = getDataContainer();
      if (dataContainer == null)
      {
         throw new IllegalStateException("Must nest UISortLink inside component implementing IDataContainer!"); 
      }
      
      // this component will only render itself if the parent DataContainer is setup
      // with a valid "pageSize" property
      if (isRendered() == false || dataContainer.getPageSize() == -1)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      ResourceBundle bundle = Application.getBundle(context);
      StringBuilder buf = new StringBuilder(512);
      
      int currentPage = dataContainer.getCurrentPage();
      int pageCount = dataContainer.getPageCount();
      
      buf.append("<span");
      if (getAttributes().get("style") != null)
      {
         buf.append(" style=\"")
            .append(getAttributes().get("style"))
            .append('"');
      }
      if (getAttributes().get("styleClass") != null)
      {
         buf.append(" class=")
            .append(getAttributes().get("styleClass"));
      }
      buf.append('>');
      
      // output Page X of Y text
      buf.append(MessageFormat.format(bundle.getString(MSG_PAGEINFO), new Object[] {
            Integer.toString(currentPage + 1),  // current page can be zero if no data present
            Integer.toString(pageCount)
            }));
      
      buf.append("&nbsp;&nbsp;");
      
      // output HTML links or labels to render the paging controls
      // first page
      if (currentPage != 0)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(0));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_FIRSTPAGE, 16, 16, bundle.getString(FIRST_PAGE)));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_FIRSTPAGE_NONE, 16, 16, null));
      }
      
      buf.append("&nbsp;");
      
      // previous page
      if (currentPage != 0)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(currentPage - 1));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_PREVIOUSPAGE, 16, 16, bundle.getString(PREVIOUS_PAGE)));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_PREVIOUSPAGE_NONE, 16, 16, null));
      }
      
      buf.append("&nbsp;");
      
      // clickable digits for pages 1 to 10
      int totalIndex = (pageCount < 10 ? pageCount : 10);
      for (int i=0; i<totalIndex; i++)
      {
         if (i != currentPage)
         {
            buf.append("<a href='#' onclick=\"")
               .append(generateEventScript(i))
               .append("\">")
               .append(i + 1)
               .append("</a>&nbsp;");
         }
         else
         {
            buf.append("<b>")
               .append(i + 1)
               .append("</b>&nbsp;");
         }
      }
      // clickable digits for pages 20 to 100 (in jumps of 10)
      if (pageCount >= 20)
      {
         buf.append("...&nbsp;");
         totalIndex = (pageCount / 10) * 10;
         totalIndex = (totalIndex < 100 ? totalIndex : 100);
         for (int i=19; i<totalIndex; i += 10)
         {
            if (i != currentPage)
            {
               buf.append("<a href='#' onclick=\"")
                  .append(generateEventScript(i))
                  .append("\">")
                  .append(i + 1)
                  .append("</a>&nbsp;");
            }
            else
            {
               buf.append("<b>")
                  .append(i + 1)
                  .append("</b>&nbsp;");
            }
         }
      }
      // clickable digits for last page if > 10 and not already shown
      if ((pageCount > 10) && (pageCount % 10 != 0))
      {
         if (pageCount-1 != currentPage)
         {
            if (pageCount < 20)
            {
               buf.append("...&nbsp;");
            }
            buf.append("<a href='#' onclick=\"")
               .append(generateEventScript(pageCount-1))
               .append("\">")
               .append(pageCount)
               .append("</a>&nbsp;");
         }
         else
         {
            if (pageCount < 20)
            {
               buf.append("...&nbsp;");
            }
            buf.append("<b>")
               .append(pageCount)
               .append("</b>&nbsp;");
         }
      }
      
      // next page
      if ((dataContainer.getCurrentPage() < dataContainer.getPageCount() - 1) == true)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(currentPage + 1));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_NEXTPAGE, 16, 16, bundle.getString(NEXT_PAGE)));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_NEXTPAGE_NONE, 16, 16, null));
      }
      
      buf.append("&nbsp;");
      
      // last page
      if ((dataContainer.getCurrentPage() < dataContainer.getPageCount() - 1) == true)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(dataContainer.getPageCount() - 1));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_LASTPAGE, 16, 16, bundle.getString(LAST_PAGE)));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_LASTPAGE_NONE, 16, 16, null));
      }
      
      buf.append("</span>");
      
      out.write(buf.toString());
   }

   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      if (value != null && value.length() != 0)
      {
         // we were clicked - queue an event to represent the click
         // cannot handle the event here as other components etc. have not had
         // a chance to decode() - we queue an event to be processed later
         PageEvent actionEvent = new PageEvent(this, Integer.valueOf(value).intValue());
         this.queueEvent(actionEvent);
      }
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof PageEvent == false)
      {
         // let the super class handle events which we know nothing about
         super.broadcast(event);
      }
      else
      {
         // found a sort event for us!
         if (s_logger.isDebugEnabled())
            s_logger.debug("Handling paging event to index: " + ((PageEvent)event).Page);
         getDataContainer().setCurrentPage(((PageEvent)event).Page);
      } 
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Return the parent data container for this component
    */
   private IDataContainer getDataContainer()
   {
      return Utils.getParentDataContainer(getFacesContext(), this);
   }
   
   /**
    * Output the JavaScript event script to jump to a specified page
    * 
    * @param page    page index to generate script to jump too
    */
   private String generateEventScript(int page)
   {
      return Utils.generateFormSubmit(getFacesContext(), this, getHiddenFieldName(), Integer.toString(page));
   }
   
   /**
    * We use a hidden field name based on the parent data container component Id and
    * the string "pager" to give a field name that can be shared by all pager links
    * within a single data container component.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName()
   {
      UIComponent dataContainer = (UIComponent)Utils.getParentDataContainer(getFacesContext(), this);
      return dataContainer.getClientId(getFacesContext()) + NamingContainer.SEPARATOR_CHAR + "pager";
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the clicking of a sortable column.
    */
   private static class PageEvent extends ActionEvent
   {
      public PageEvent(UIComponent component, int page)
      {
         super(component);
         Page = page;
      }
      
      public int Page = 0;
   }
}
