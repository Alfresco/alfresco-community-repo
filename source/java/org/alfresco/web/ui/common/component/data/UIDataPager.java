/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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

import org.alfresco.web.app.Application;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class UIDataPager extends UICommand
{
   private static Log    s_logger = LogFactory.getLog(IDataContainer.class);
   
   private static final String LAST_PAGE = "last_page";
   private static final String NEXT_PAGE = "next_page";
   private static final String PREVIOUS_PAGE = "prev_page";
   private static final String FIRST_PAGE = "first_page";
   private static final String MSG_PAGEINFO = "page_info";
   
   private static final int VISIBLE_PAGE_RANGE = 3;
   private static final int AMOUNT_FIRST_PAGES = 7;
  
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
      
      final String formClientId = Utils.getParentForm(context, this).getClientId(context);
      final String pageInputId = getPageInputId();
      final String hiddenFieldName = getHiddenFieldName();
      
      ResponseWriter out = context.getResponseWriter();
      ResourceBundle bundle = Application.getBundle(context);
      StringBuilder buf = new StringBuilder(512);
      
      int currentPage = dataContainer.getCurrentPage();
      int pageCount = dataContainer.getPageCount();
      
      Object displayInput = getAttributes().get("displayInput");

      String beginTag = "<span";
      String endTag = "</span>";
      String divStyle = "";
      String inputStyle = "height:13px;";
      String imageVericalAlign = null;
      String imageStyle = "margin-top:0px;";
      StringBuilder inputPageNumber = new StringBuilder(128);

      if (displayInput != null && ((Boolean) displayInput) == false)
      {
         imageStyle = null;
         inputPageNumber.append(currentPage + 1);
      }
      else
      {
         final boolean isIE = Utils.USER_AGENT_MSIE.equals(Utils.getUserAgent(context));
         if (isIE)
         {
            beginTag = "<div";
            endTag = "</div>";
            divStyle = "padding:1px;";
            inputStyle = "height:13px; vertical-align:middle;";
            imageVericalAlign = "middle";
            imageStyle = "margin-top:0px;";
            inputPageNumber.append("<input type=\"text\" maxlength=\"3\" value=\"").append(currentPage + 1).append("\" style=\"width: 24px; margin-left: 4px;").append(inputStyle).append("\" ");
            inputPageNumber.append("onkeydown=\"").append(generateIE6InputOnkeydownScript(pageInputId, formClientId, hiddenFieldName)).append("\" ");
            inputPageNumber.append("id=\"").append(pageInputId).append("\" />");
         }
         else
         {
             inputPageNumber.append("<input type=\"text\" maxlength=\"3\" value=\"").append(currentPage + 1).append("\" style=\"width: 24px; margin-left: 4px;").append(inputStyle).append("\" ");
             inputPageNumber.append("onkeypress=\"").append(generateInputOnkeyPressScript(pageInputId, formClientId, hiddenFieldName)).append("\" ");
             inputPageNumber.append("onkeydown=\"").append(generateInputOnkeydownScript()).append("\" ");
             inputPageNumber.append("id=\"").append(pageInputId).append("\" />");
         }
      }
      
      buf.append(beginTag);
      if (getAttributes().get("style") != null)
      {
         buf.append(" style=\"")
            .append(getAttributes().get("style"))
            .append(divStyle)
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
            inputPageNumber.toString(),  // current page can be zero if no data present
            Integer.toString(pageCount)
            }));
      
      buf.append("&nbsp;&nbsp;");
      
      // output HTML links or labels to render the paging controls
      // first page
      if (currentPage != 0)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(0, hiddenFieldName));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_FIRSTPAGE, 16, 16, bundle.getString(FIRST_PAGE), null, imageVericalAlign, imageStyle));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_FIRSTPAGE_NONE, 16, 16, null, null, imageVericalAlign, imageStyle));
      }
      
      buf.append("&nbsp;");
      
      // previous page
      if (currentPage != 0)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(currentPage - 1, hiddenFieldName));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_PREVIOUSPAGE, 16, 16, bundle.getString(PREVIOUS_PAGE), null, imageVericalAlign, imageStyle));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_PREVIOUSPAGE_NONE, 16, 16, null, null, imageVericalAlign, imageStyle));
      }
      
      buf.append("&nbsp;");
      
      Object objType = getAttributes().get("dataPagerType");
      
      PagerType type = PagerType.TRACKPAGE;
      if (objType instanceof String)
      {
         try
         {
             type = PagerType.valueOf((String)objType);
         }
         catch (Throwable ex)
         {
             s_logger.warn("DataPager id:" + this.getId() + " with incorrect 'numberPageType' attribute");
         }
      }
      
      switch (type)
      {
         case STANDARD:
            encodeType0(buf, currentPage, pageCount);
            break;
         case DECADES:
            encodeType1(buf, currentPage, pageCount);
            break;
         case TRACKPAGE:
            encodeType2(buf, currentPage, pageCount);
            break;
         default:
            encodeType2(buf, currentPage, pageCount);
      }
      
      // next page
      if ((dataContainer.getCurrentPage() < dataContainer.getPageCount() - 1) == true)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(currentPage + 1, hiddenFieldName));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_NEXTPAGE, 16, 16, bundle.getString(NEXT_PAGE), null, imageVericalAlign, imageStyle));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_NEXTPAGE_NONE, 16, 16, null, null, imageVericalAlign, imageStyle));
      }
      
      buf.append("&nbsp;");
      
      // last page
      if ((dataContainer.getCurrentPage() < dataContainer.getPageCount() - 1) == true)
      {
         buf.append("<a href='#' onclick=\"");
         buf.append(generateEventScript(dataContainer.getPageCount() - 1, hiddenFieldName));
         buf.append("\">");
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_LASTPAGE, 16, 16, bundle.getString(LAST_PAGE), null, imageVericalAlign, imageStyle));
         buf.append("</a>");
      }
      else
      {
         buf.append(Utils.buildImageTag(context, WebResources.IMAGE_LASTPAGE_NONE, 16, 16, null, null, imageVericalAlign, imageStyle));
      }
      
      buf.append(endTag);
      
      out.write(buf.toString());
   }
   
   private void createClicableDigitForPage(int num, StringBuilder buf)
   {
	   buf.append("<a href='#' onclick=\"")
          .append(generateEventScript(num, getHiddenFieldName()))
          .append("\">")
          .append(num + 1)
          .append("</a>&nbsp;");
   }
   
   private void createDigitForPage(int num, StringBuilder buf)
   {
	   buf.append("<b>")
          .append(num + 1)
          .append("</b>&nbsp;");
   }
   
   private void encodeType0(StringBuilder buf, int currentPage, int pageCount)
   {
	   int totalIndex = (pageCount < 10 ? pageCount : 10);
	   for (int i=0; i<totalIndex; i++)
	   {
	      if (i != currentPage)
	      {
	         createClicableDigitForPage(i, buf);
	      }
	      else
	      {
	         createDigitForPage(i, buf);
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
	            createClicableDigitForPage(i, buf);
	         }
	         else
	         {
	            createDigitForPage(i, buf);
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

	         createClicableDigitForPage(pageCount - 1, buf);
	      }
	      else
	      {
	         if (pageCount < 20)
	         {
	            buf.append("...&nbsp;");
	         }
	         createDigitForPage(pageCount - 1, buf);
	      }
	   }
   }
   
   private void encodeType1(StringBuilder buf, int currentPage, int pageCount)
   {
      // clickable digits for pages 1 to 10
      int totalIndex = (pageCount < 10 ? pageCount : 10);
      int number = -1;
      if (currentPage == 0)
      {
         number = generateClickableDigitForPageCurrent(currentPage, pageCount, buf, false, currentPage + VISIBLE_PAGE_RANGE + 1 < pageCount);
      }
      
      if (currentPage > VISIBLE_PAGE_RANGE + 1)
      {
         createClicableDigitForPage(0, buf);
      }
      
      if (currentPage <= 9 && currentPage != 0)
      {
         number = generateClickableDigitForPageCurrent(currentPage, pageCount, buf, currentPage > VISIBLE_PAGE_RANGE + 1, currentPage + VISIBLE_PAGE_RANGE + 1 < pageCount);
      }
      
      // clickable digits for pages 20 to 100, 101 to 200, ...  (in jumps of 10)
      if (number <= 9 && currentPage < 100 && pageCount > 9)
      {
         createClicableDigitForPage(9, buf);
      }
      
      // clickable digits for pages 20 to 100 (in jumps of 10)
      if (pageCount >= 10)
      {
         int i = 19;
         totalIndex = (pageCount / 10) * 10;
         totalIndex = (totalIndex < 100 ? totalIndex : 100);
         int stepIndex = (currentPage + 1) / 100;
         if (stepIndex > 0)
         {
            i = (100 * stepIndex);
            totalIndex = i + 100;
            i--;
            if (pageCount < totalIndex)
            {
               totalIndex = pageCount;
            }
         }
         for (; i < totalIndex; i += 10)
         {
            if (i <= currentPage && currentPage <= (i + VISIBLE_PAGE_RANGE))
            {
               generateClickableDigitForPageCurrent(currentPage, pageCount, buf, true, (currentPage + 1 + VISIBLE_PAGE_RANGE) < totalIndex);
               continue;
            }
            if (currentPage < i && currentPage > (i-10 + VISIBLE_PAGE_RANGE))
            {
               number = generateClickableDigitForPageCurrent(currentPage, pageCount, buf, true, (currentPage + VISIBLE_PAGE_RANGE + 2) < totalIndex);
               if (number + 1 < pageCount)
               {
                  buf.append("...&nbsp;");
               }
               if (currentPage + VISIBLE_PAGE_RANGE >= i)
               {
                  continue;
               }
            }
            if (i != currentPage)
            {
               createClicableDigitForPage(i, buf);
            }
            else
            {
               createDigitForPage(i, buf);
            }
         }
      }
      
      // clickable digits for last page
      if (number != pageCount && totalIndex < pageCount)
      {
         if (pageCount > number)
         {
            createClicableDigitForPage(pageCount - 1, buf);
         }
         else
         {
            createDigitForPage(pageCount - 1, buf);
         }
      }
   }
   
   private void encodeType2(StringBuilder buf, int currentPage, int pageCount)
   {
	   int number = AMOUNT_FIRST_PAGES;
	   if (currentPage + VISIBLE_PAGE_RANGE < AMOUNT_FIRST_PAGES)
	   {
	      int totalIndex = AMOUNT_FIRST_PAGES < pageCount ? AMOUNT_FIRST_PAGES : pageCount;
	      for (int i = 0; i < totalIndex; i++)
	      {
	         if (i != currentPage)
	         {
	            createClicableDigitForPage(i, buf);
	         }
	         else
	         {
	            createDigitForPage(i, buf);
	         }
	      }
	      if (currentPage + VISIBLE_PAGE_RANGE + 1 < pageCount)
         {
	         buf.append("...&nbsp;");
         }
	   }
	   else
	   {
	      createClicableDigitForPage(0, buf);
	      number = generateClickableDigitForPageCurrent(currentPage, pageCount, buf, currentPage > VISIBLE_PAGE_RANGE + 1, (currentPage + 1 + VISIBLE_PAGE_RANGE) < pageCount);
	   }
	   
	   if (number < pageCount)
	   {
	      if (pageCount > number)
	      {
	         createClicableDigitForPage(pageCount - 1, buf);
	      }
	      else
	      {
	         createDigitForPage(pageCount - 1, buf);
	      }
	   }
   }
   
   //clickable digits for pages current page - 3 and current page + 3
   private int generateClickableDigitForPageCurrent(int currentPage, int pageCount, StringBuilder buf, boolean startDivider, boolean finishDivider)
   {
      int startPage = currentPage - VISIBLE_PAGE_RANGE;
      if (startPage < 0)
      {
         startPage = 0;
      }
      if (startDivider)
      {
         buf.append("...&nbsp;");
      }
      for (int i = startPage; i < currentPage; i++)
      {
         createClicableDigitForPage(i, buf);
      }
      
      buf.append("<b>")
         .append(currentPage + 1)
         .append("</b>&nbsp;");
      
      int i = currentPage + 1;
      int finishPage = currentPage + VISIBLE_PAGE_RANGE;
      if (finishPage >= pageCount)
      {
         finishPage = pageCount - 1;
      }
      for (; i <= finishPage; i++)
      {
         createClicableDigitForPage(i, buf);
      }
      if (finishDivider)
      {
         buf.append("...&nbsp;");
      }
      
      return i;
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
   private String generateEventScript(int page, String hiddenFieldName)
   {
      return Utils.generateFormSubmit(getFacesContext(), this, hiddenFieldName, Integer.toString(page));
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
   
   /**
    * Output the JavaScript event script to handle onkeypress event in the Page Number input.
    * It validates and sends appropriate page number on 'Enter'.
    * 
    * @return JavaScript code
    */
   private String generateInputOnkeyPressScript(String pageInputId, String formClientId, String hiddenFieldName)
   {
       final StringBuilder script = new StringBuilder(128);
       script.append("return validateAndSubmit(event,'")
             .append(getPageInputId())
             .append("','")
             .append(formClientId)
             .append("','")
             .append(hiddenFieldName)
             .append("');");
       return script.toString();
   }
   
   /**
    * Output the JavaScript event script to handle onkeydown event in the Page Number input.
    * It handles only digits and some 'useful' keys.
    * @return JavaScript code
    */
   private String generateInputOnkeydownScript()
   {
       return "return onlyDigits(event);";
   }

   /**
    * Output the JavaScript event script to handle onkeydown event in the Page Number input (For IE6 browser).
    * It handles only digits and some 'useful' keys.
    * @return JavaScript code
    */
   private String generateIE6InputOnkeydownScript(String pageInputId, String formClientId, String hiddenFieldName)
   {
       final StringBuilder script = new StringBuilder(128);
       script.append("return onlyDigitsIE6(event,'")
             .append(getPageInputId())
             .append("','")
             .append(formClientId)
             .append("','")
             .append(hiddenFieldName)
             .append("');");
       return script.toString();
   }
   
   private String getPageInputId()
   {
       return getHiddenFieldName() + NamingContainer.SEPARATOR_CHAR + "pageNamber";
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the clicking of a sortable column.
    */
   private static class PageEvent extends ActionEvent
   {
       private static final long serialVersionUID = -5338654505243607790L;

       public PageEvent(UIComponent component, int page)
       {
           super(component);
           Page = page;
       }
       public int Page = 0;
   }
   
   
   /**
    * Enumeration of the available Data Pager display types see ETWOONE-389
    */
   private enum PagerType
   {
      STANDARD, DECADES, TRACKPAGE
   }
}
