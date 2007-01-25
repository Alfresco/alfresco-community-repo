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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
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
import org.alfresco.web.bean.SearchContext;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @author Kevin Roast
 */
public class UISimpleSearch extends UICommand
{
   // ------------------------------------------------------------------------------
   // Component implementation

   /**
    * Default Constructor
    */
   public UISimpleSearch()
   {
      // specifically set the renderer type to null to indicate to the framework
      // that this component renders itself - there is no abstract renderer class
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.SimpleSearch";
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.search = (SearchContext)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.search;
      return (values);
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = Utils.getActionHiddenFieldName(context, this);
      String value = (String)requestMap.get(fieldId);
      // we are clicked if the hidden field contained our client id
      if (value != null)
      {
         if (value.equals(this.getClientId(context)))
         {
            String searchText = (String)requestMap.get(getClientId(context));
            
            if (searchText.length() != 0)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Search text submitted: " + searchText);
               int option = -1;
               String optionFieldName = getClientId(context) + NamingContainer.SEPARATOR_CHAR + OPTION_PARAM;
               String optionStr = (String)requestMap.get(optionFieldName);
               if (optionStr.length() != 0)
               {
                  option = Integer.parseInt(optionStr);
               }
               if (logger.isDebugEnabled())
                  logger.debug("Search option submitted: " + option);
               
               // queue event so system can perform a search and update the component
               SearchEvent event = new SearchEvent(this, searchText, option);
               this.queueEvent(event);
            }
         }
         else if (value.equals(ADVSEARCH_PARAM))
         {
            // found advanced search navigation action
            // TODO: TEMP: set this outcome from a component attribute!
            AdvancedSearchEvent event = new AdvancedSearchEvent(this, "advSearch");
            this.queueEvent(event);
         }
      }
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      FacesContext fc = getFacesContext();
      if (event instanceof SearchEvent)
      {
         // update the component parameters from the search event details
         SearchEvent searchEvent = (SearchEvent)event;
         
         // construct the Search Context object
         SearchContext context = new SearchContext();
         context.setText(searchEvent.SearchText);
         context.setMode(searchEvent.SearchMode);
         context.setForceAndTerms(Application.getClientConfig(fc).getForceAndTerms());
         this.search = context;
         
         super.broadcast(event);
      }
      else if (event instanceof AdvancedSearchEvent)
      {
         // special case to navigate to the advanced search screen
         AdvancedSearchEvent searchEvent = (AdvancedSearchEvent)event;
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, searchEvent.Outcome);
         
         // NOTE: we don't call super() here so that our nav outcome is the one that occurs!
      }
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      ResourceBundle bundle = (ResourceBundle)Application.getBundle(context);
      
      // script for dynamic simple search menu drop-down options
      out.write("<script>");
      out.write("function _noenter(event) {" +
                "if (event && event.keyCode == 13) {" +
                "   _searchSubmit();return false; }" +
                "else {" +
                "   return true; } }");
      out.write("function _searchSubmit() {");
      out.write(Utils.generateFormSubmit(context, this, Utils.getActionHiddenFieldName(context, this), getClientId(context)));
      out.write("}");
      out.write("</script>");
      
      // outer table containing search drop-down icon, text box and search Go image button
      out.write("<table cellspacing=4 cellpadding=0>");
      out.write("<tr><td style='padding-top:2px'>");
      
      String searchImage = Utils.buildImageTag(context, "/images/icons/search_icon.gif", 15, 15,
            bundle.getString(MSG_GO), "_searchSubmit();");
      
      out.write(Utils.buildImageTag(context, "/images/icons/search_controls.gif", 27, 13,
            bundle.getString(MSG_OPTIONS), "javascript:_toggleMenu(event, '_alfsearch');"));
      
      // dynamic DIV area containing search options
      out.write("<br><div id='_alfsearch' style='position:absolute;display:none'>");
      out.write("<table border=0 class='moreActionsMenu' cellspacing=4 cellpadding=0>");
      
      // output each option - setting the current one to CHECKED
      String optionFieldName = getClientId(context) + NamingContainer.SEPARATOR_CHAR + OPTION_PARAM;
      String radioOption = "<tr><td class='userInputForm'><input type='radio' name='" + optionFieldName + "'";
      out.write(radioOption);
      out.write(" VALUE='0'");
      int searchMode = getSearchMode();
      if (searchMode == 0) out.write(" CHECKED");
      out.write("><nobr>" + bundle.getString(MSG_ALL_ITEMS) + "</nobr></td></tr>");
      out.write(radioOption);
      out.write(" VALUE='1'");
      if (searchMode == 1) out.write(" CHECKED");
      out.write("><nobr>" + bundle.getString(MSG_FILE_NAMES_CONTENTS) + "</nobr></td></tr>");
      out.write(radioOption);
      out.write(" VALUE='2'");
      if (searchMode == 2) out.write(" CHECKED");
      out.write("><nobr>" + bundle.getString(MSG_FILE_NAMES_ONLY) + "</nobr></td></tr>");
      out.write(radioOption);
      out.write(" VALUE='3'");
      if (searchMode == 3) out.write(" CHECKED");
      out.write("><nobr>" + bundle.getString(MSG_SPACE_NAMES_ONLY) + "</nobr></td></tr>");
      
      // row with table containing advanced search link and Search Go button 
      out.write("<tr><td><table width=100%><tr><td>");
      // generate a link that will cause an action event to navigate to the advanced search screen
      out.write("<a class='small' href='#' onclick=\"");
      out.write(Utils.generateFormSubmit(context, this, Utils.getActionHiddenFieldName(context, this), ADVSEARCH_PARAM));
      out.write("\">");
      out.write(bundle.getString(MSG_ADVANCED_SEARCH));
      out.write("</a>");
      out.write("</td><td align=right>");
      out.write(searchImage);
      out.write("</td></tr></table></td></tr>");
      out.write("</table></div>");
      
      // input text box
      out.write("</td><td>");
      out.write("<input name='");
      out.write(getClientId(context));
      // TODO: style and class from component properties!
      out.write("' onkeypress=\"return _noenter(event)\"");
      out.write(" type='text' maxlength='1024' style='width:130px;padding-top:3px;font-size:10px' value=\"");
      // output previous search text stored in this component!
      out.write(Utils.replace(getLastSearch(), "\"", "&quot;"));
      out.write("\">");
      
      // search Go image button
      out.write("</td><td>");
      out.write(searchImage);
      
      // end outer table
      out.write("</td></tr></table>");
   }
   
   /**
    * Return the current Search Context
    */
   public SearchContext getSearchContext()
   {
      return this.search;
   }
   
   /**
    * @return The last set search text value
    */
   public String getLastSearch()
   {
      if (search != null)
      {
         return this.search.getText();
      }
      else
      {
         return "";
      }
   }
   
   /** 
    * @return The current search mode (see constants) 
    */
   public int getSearchMode()
   {
      if (search != null)
      {
         return this.search.getMode();
      }
      else
      {
         return SearchContext.SEARCH_ALL;
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   private static Log logger = LogFactory.getLog(UISimpleSearch.class);
   
   /** I18N message Ids */
   private static final String MSG_ADVANCED_SEARCH = "advanced_search";
   private static final String MSG_OPTIONS = "options";
   private static final String MSG_GO = "go";
   private static final String MSG_SPACE_NAMES_ONLY = "space_names";
   private static final String MSG_FILE_NAMES_ONLY = "file_names";
   private static final String MSG_FILE_NAMES_CONTENTS = "file_names_contents";
   private static final String MSG_ALL_ITEMS = "all_items";
   
   private static final String OPTION_PARAM = "_option";
   private static final String ADVSEARCH_PARAM = "_advsearch";
   
   /** last search context used */
   private SearchContext search = null;
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing a search execution from the UISimpleSearch component.
    */
   public static class SearchEvent extends ActionEvent
   {
      private static final long serialVersionUID = 3918135612344774322L;

      public SearchEvent(UIComponent component, String text, int mode)
      {
         super(component);
         SearchText = text;
         SearchMode = mode;
      }
      
      public String SearchText;
      public int SearchMode;
   }
   
   public static class AdvancedSearchEvent extends ActionEvent
   {
      public AdvancedSearchEvent(UIComponent component, String outcome)
      {
         super(component);
         Outcome = outcome;
      }
      
      public String Outcome;
   }
}
