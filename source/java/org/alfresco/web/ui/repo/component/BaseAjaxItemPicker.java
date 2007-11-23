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
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;

import org.alfresco.web.app.Application;

/**
 * @author Kevin Roast
 */
public abstract class BaseAjaxItemPicker extends UIInput
{
   private static final String MSG_GO_UP = "go_up";
   private static final String MSG_OK = "ok";
   private static final String MSG_CANCEL = "cancel";
   
   /** label to be displayed before an item is selected */
   protected String label = null;
   
   /** id of the initially selected item, if value is not set */
   protected String initialSelectionId = null;
   
   /** flag to show whether the component is disabled */
   protected Boolean disabled;
   
   /** True for single select mode, false for multi-select mode */
   protected Boolean singleSelect;
   
   protected static int ACTION_DONE = 0;
   protected static int ACTION_CANCEL = 1;
   
   
   public BaseAjaxItemPicker()
   {
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   @Override
   public abstract String getFamily();

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.label = (String)values[1];
      this.singleSelect = (Boolean)values[2];
      this.initialSelectionId = (String)values[3];
      this.disabled = (Boolean)values[4];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[] {
         // standard component attributes are saved by the super class
         super.saveState(context),
         this.label,
         this.singleSelect,
         this.initialSelectionId,
         this.disabled};
      return (values);
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      
   }
   
   /**
    * @see javax.faces.component.UIInput#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      super.broadcast(event);
   }
   
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
      
      ResourceBundle msg = Application.getBundle(fc);
      
      // TODO: from submitted value or 'none'
      String selection = "none";
      
      // TODO: output images with context correctly
      
      String divId = getId();
      String objId = divId + "Obj"; 
      String contextPath = fc.getExternalContext().getRequestContextPath();
      out.write("<script type='text/javascript'>");
      out.write("function init" + divId + "() {");
      out.write(" window." + objId + " = new AlfPicker('" + divId + "','" + objId + "','" + getServiceCall() + "'," + getSingleSelect() + ");");
      if (getInitialSelection() != null)
      {
         out.write(" window." + objId + ".setStartId('" + getInitialSelection() + "');");
      }
      out.write("}");
      out.write(" window.addEvent('domready', init" + divId + ");");
      out.write("</script>");
      
      out.write("<div id='" + divId + "' class='picker'>") ;
      out.write(" <input id='" + divId + "-value' name='" + divId + "-value' type='hidden'>");
      out.write(" <div id='" + divId + "-noitems' class='pickerNoSelectedItems'>");
      out.write("  <span>&lt;" + selection + "&gt;</span>");
      out.write("  <span class='pickerActionButton'><a href='#' onclick='" + objId + ".showSelector();'>");
      out.write(msg.getString(getLabel()));
      out.write("</a></span>");
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
      out.write("      <div id='" + divId + "-nav-bread' class='pickerNavBreadcrumbPanel'></div>");
      out.write("      <a href='#' onclick='" + objId + ".breadcrumbToggle();'><span id='" + divId + "-nav-txt'></span><img border='0' src='");
      out.write(contextPath);
      out.write("/images/icons/arrow_open.gif'></a>");
      out.write("     </span>");
      out.write("    </div>");
      out.write("   </div>");
      // container for item selection
      out.write("   <div>");
      out.write("    <div id='" + divId + "-ajax-wait' class='pickerAjaxWait'></div>");
      out.write("    <div id='" + divId + "-results-list' class='pickerResultsList'></div>");
      out.write("   </div>");
      out.write("  </div>");
      out.write("  <div id='" + divId + "-finish' class='pickerFinishControls'>");
      out.write("   <div class='pickerDoneButton'><a href='#' onclick='" + objId + ".doneClicked();'>");
      out.write(msg.getString(MSG_OK));
      out.write("</a></div>");
      // TODO: Cancel button
      out.write("  </div>");
      out.write(" </div>");
      // container for selected items
      out.write(" <div id='" + divId + "-selected' class='pickerSelectedItems'></div>");
      out.write("</div>");
   }
   
   /**
    * @return the ajax service bean call, for instance 'PickerBean.getFolderNodes'
    */
   protected abstract String getServiceCall();
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return Returns the label.
    */
   public String getLabel()
   {
      ValueBinding vb = getValueBinding("label");
      if (vb != null)
      {
         this.label = (String)vb.getValue(getFacesContext());
      }
      
      return this.label;
   }
   
   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   /**
    * @return Returns the initial selecttion.
    */
   public String getInitialSelection()
   {
      ValueBinding vb = getValueBinding("initialSelection");
      if (vb != null)
      {
         this.initialSelectionId = (String)vb.getValue(getFacesContext());
      }
      
      return this.initialSelectionId;
   }
   
   /**
    * @param initialSelection The initial selection to set.
    */
   public void setInitialSelection(String initialSelection)
   {
      this.initialSelectionId = initialSelection;
   }
   
   /**
    * Determines whether the component should be rendered in a disabled state
    * 
    * @return Returns whether the component is disabled
    */
   public boolean isDisabled()
   {
      if (this.disabled == null)
      {
         ValueBinding vb = getValueBinding("disabled");
         if (vb != null)
         {
            this.disabled = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      return this.disabled == null ? Boolean.FALSE : this.disabled;
   }

   /**
    * Determines whether the component should be rendered in a disabled state
    * 
    * @param disabled true to disable the component
    */
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }
   
   /**
    * @return true is single select mode, false for multi-select
    */
   public Boolean getSingleSelect()
   {
      if (this.singleSelect == null)
      {
         ValueBinding vb = getValueBinding("singleSelect");
         if (vb != null)
         {
            this.singleSelect = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      return this.singleSelect == null ? Boolean.TRUE : this.singleSelect;
   }

   /**
    * @param singleSelect true for single select mode, false for multi-select
    */
   public void setSingleSelect(Boolean singleSelect)
   {
      this.singleSelect = singleSelect;
   }
   
   
   // ------------------------------------------------------------------------------
   // Protected helpers
   
   /**
    * We use a unique hidden field name based on our client Id.
    * This is on the assumption that there won't be many selectors on screen at once!
    * Also means we have less values to decode on submit.
    * 
    * @return hidden field name
    */
   protected String getHiddenFieldName()
   {
      return this.getClientId(getFacesContext());
   }
}
