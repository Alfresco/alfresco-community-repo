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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.transaction.UserTransaction;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author Mike Hatfield
 */
public class UIAjaxTagPicker extends BaseAjaxItemPicker
{
   private static final String MSG_CLICK_TO_SELECT_TAG = "click_to_select_tag";
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.AjaxTagPicker";
   }

   @Override
   protected String getServiceCall()
   {
      return "PickerBean.getTagNodes";
   }

   @Override
   protected String getDefaultIcon()
   {
      return "/images/icons/category_small.gif";
   }
   
   @Override
   public Boolean getSingleSelect()
   {
      // Tag component is never in single select mode, but the base class needs to know this
      return false;
   }
   
   @Override
   public String getLabel()
   {
      // Tagger label only retrieved from a value binding when null
      if (this.label == null)
      {
         super.getLabel();
      }
      return this.label;
   }

   @Override
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext fc) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = fc.getResponseWriter();
      
      String formClientId = Utils.getParentForm(fc, this).getClientId(fc);
      Map attrs = this.getAttributes();
      ResourceBundle msg = Application.getBundle(fc);
      
      // get values from submitted value or none selected
      String selectedValues = null;
      String selectedNames = null;
      String selectedItems = null;
      List<NodeRef> submitted = null;

      submitted = (List<NodeRef>)getSubmittedValue();
      if (submitted == null)
      {
         submitted = (List<NodeRef>)getValue();
      }
      // special case to submit empty lists on multi-select values
      else if (submitted.equals("empty"))
      {
         submitted = null;
      }

      if (submitted != null)
      {
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(fc, true);
            tx.begin();
            
            StringBuilder nameBuf = new StringBuilder(128);
            StringBuilder valueBuf = new StringBuilder(128);
            StringBuilder itemBuf = new StringBuilder(256);
            NodeService nodeService = (NodeService)FacesContextUtils.getRequiredWebApplicationContext(
                  fc).getBean("nodeService");
            for (NodeRef value : submitted)
            {
               String name = (String)nodeService.getProperty(value, ContentModel.PROP_NAME);
               String icon = (String)nodeService.getProperty(value, ApplicationModel.PROP_ICON);
               if (nameBuf.length() != 0)
               {
                  nameBuf.append(", ");
                  valueBuf.append(",");
                  itemBuf.append(",");
               }
               nameBuf.append(name);
               valueBuf.append(value.toString());
               itemBuf.append(getItemJson(value.toString(), name, icon));
            }
            selectedNames = nameBuf.toString();
            selectedValues = valueBuf.toString();
            selectedItems = "[" + itemBuf.toString() + "]";
            
            // commit the transaction
            tx.commit();
         }
         catch (Throwable err)
         {
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
      
      // generate the Ids for our script object and containing DIV element
      String divId = getId();
      String objId = divId + "Obj"; 
      
      // generate the script to create and init our script object
      String contextPath = fc.getExternalContext().getRequestContextPath();
      out.write("<script type='text/javascript'>");
      out.write("function init" + divId + "() {");
      out.write(" window." + objId + " = new AlfTagger('" + divId + "','" + objId + "','" + getServiceCall() +
                "','" + formClientId + "');");
	   out.write(" window." + objId + ".setChildNavigation(false);");
      if (getDefaultIcon() != null)
      {
         out.write(" window." + objId + ".setDefaultIcon('" + getDefaultIcon() + "');");
      }
      if (selectedItems != null)
      {
         out.write(" window." + objId + ".setSelectedItems('" + selectedItems + "');");
      }
      out.write("}");
      out.write("window.addEvent('domready', init" + divId + ");");
      out.write("</script>");
      
      // generate the DIV structure for our component as expected by the script object
      out.write("<div id='" + divId + "' class='picker'>") ;
      out.write(" <input id='" + getHiddenFieldName() + "' name='" + getHiddenFieldName() + "' type='hidden' value='");
      if (selectedValues != null)
      {
         out.write(selectedValues);
      }
      out.write("'>");
      // current selection displayed as link and message to launch the selector
      out.write(" <div id='" + divId + "-noitems'");
      if (attrs.get("style") != null)
      {
         out.write(" style=\"");
         out.write((String)attrs.get("style"));
         out.write('"');
      }
      if (attrs.get("styleClass") != null)
      {
         out.write(" class=");
         out.write((String)attrs.get("styleClass"));
      }
      out.write(">");
		if (isDisabled())
		{
		   out.write("   <span>");
         if (selectedNames != null)
         {
            out.write(selectedNames);
         }
         out.write("   </span>");
		}
		else
		{
      	out.write("  <span class='pickerActionButton'><a href='javascript:" + objId + ".showSelector();'>");
      	if (selectedNames == null)
      	{
      	   if (getLabel() == "")
      	   {
      	      setLabel(msg.getString(MSG_CLICK_TO_SELECT_TAG));
      	   }
         	out.write(getLabel());
      	}
			else
			{
				out.write(selectedNames);
			}
         out.write("   </a></span>");
		}
      out.write(" </div>");
      // container for item navigation
      out.write(" <div id='" + divId + "-selector' class='pickerSelector'>");
      out.write("  <div class='pickerResults'>");
      out.write("   <div class='pickerResultsHeader'>");
      out.write("    <div class='pickerNavControls'>");
      out.write("     <span class='pickerNavUp'>");
      out.write("      <a id='" + divId + "-nav-up' href='#'><img src='");
      out.write(contextPath);
      out.write("/images/icons/arrow_up.gif' border='0' alt='");
      out.write(msg.getString(MSG_GO_UP));
      out.write("' title='");
      out.write(msg.getString(MSG_GO_UP));
      out.write("'></a>"); 
      out.write("     </span>");
      out.write("     <span class='pickerNavBreadcrumb'>");
      out.write("      <span id='" + divId + "-nav-txt' class='pickerNavBreadcrumbText'></span></a>");
      out.write("     </span>");
		out.write("     <span class='pickerNavAddTag'>");
		out.write("      <span class='pickerAddTagIcon'></span>");
		out.write("      <span id='" + divId + "-addTag-linkContainer' class='pickerAddTagLinkContainer'>");
		out.write("       <a href='#' onclick='window." + objId + ".showAddTagForm(); return false;'>Add a tag</a>");
		out.write("      </span>");
		out.write("      <span id='" + divId + "-addTag-formContainer' class='pickerAddTagFormContainer'>");
		out.write("       <input id='" + divId + "-addTag-box' class='pickerAddTagBox' name='" + divId + "-addTag-box' type='text'>");
		out.write("       <img id='" + divId + "-addTag-ok' class='pickerAddTagImage' alt='Add' src='");
      out.write(contextPath);
		out.write("/images/office/action_successful.gif'>");
		out.write("       <img id='" + divId + "-addTag-cancel' class='pickerAddTagImage' alt='Cancel' src='");
      out.write(contextPath);
		out.write("/images/office/action_failed.gif'>");
		out.write("      </span>");
		out.write("     </span>");
      out.write("     <span id='" + divId + "-nav-add'></span>");
      out.write("    </div>");
      out.write("   </div>");
      // container for item selection
      out.write("   <div>");
      out.write("    <div id='" + divId + "-ajax-wait' class='pickerAjaxWait'");
      String height = getHeight();
      if (height != null)
      {
         out.write(" style='height:" + height + "'");
      }
      out.write("></div>");
      out.write("    <div id='" + divId + "-results-list' class='pickerResultsList'");
      if (height != null)
      {
         out.write(" style='height:" + height + "'");
      }
      out.write("></div>");
      out.write("   </div>");
      out.write("  </div>");
      // controls (OK & Cancel buttons etc.)
      out.write("  <div class='pickerFinishControls'>");
      out.write("   <div id='" + divId + "-finish' style='float:left' class='pickerButtons'><a href='javascript:" + objId + ".doneClicked();'>");
      out.write(msg.getString(MSG_OK));
      out.write("</a></div>");
      out.write("   <div style='float:right' class='pickerButtons'><a href='javascript:" + objId + ".cancelClicked();'>");
      out.write(msg.getString(MSG_CANCEL));
      out.write("</a></div>");
      out.write("  </div>");
      out.write(" </div>");
      // container for the selected items
      out.write(" <div id='" + divId + "-selected' class='pickerSelectedItems'></div>");
      out.write("</div>");
   }

}
