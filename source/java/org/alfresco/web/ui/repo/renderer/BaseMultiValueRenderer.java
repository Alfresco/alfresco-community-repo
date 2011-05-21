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
package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor.MultiValueEditorEvent;

/**
 * Base class for renderers of the MultiValueEditor component.
 * The current items are displayed as a list of options that can be
 * removed using a Remove button.
 * 
 * @author gavinc
 */
public abstract class BaseMultiValueRenderer extends BaseRenderer
{
   /** I18N message strings */
   protected final static String MSG_REMOVE = "remove";
   protected final static String MSG_SELECT_BUTTON = "select_button";
   protected final static String MSG_ADD_TO_LIST_BUTTON = "add_to_list_button";
   
   protected boolean highlightedRow;
   
   // ------------------------------------------------------------------------------
   // Renderer implemenation
   
   /**
    * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void decode(FacesContext context, UIComponent component)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName(component);
      String value = (String)requestMap.get(fieldId);
      
      int action = UIMultiValueEditor.ACTION_NONE;
      int removeIndex = -1;
      if (value != null && value.length() != 0)
      {
         // break up the action into it's parts
         int sepIdx = value.indexOf(UIMultiValueEditor.ACTION_SEPARATOR);
         if (sepIdx != -1)
         {
            action = Integer.parseInt(value.substring(0, sepIdx));
            removeIndex = Integer.parseInt(value.substring(sepIdx+1));
         }
         else
         {
            action = Integer.parseInt(value);
         }
      }
      
      if (action != UIMultiValueEditor.ACTION_NONE)
      {
         MultiValueEditorEvent event = new MultiValueEditorEvent(component, action, removeIndex);
         component.queueEvent(event);
      }
      
      super.decode(context, component);
   }

   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   @SuppressWarnings("static-access")
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }

      // reset the highlighted row flag
      this.highlightedRow = false;
      
      if (component instanceof UIMultiValueEditor)
      {
         ResponseWriter out = context.getResponseWriter();
         Map attrs = component.getAttributes();
         UIMultiValueEditor editor = (UIMultiValueEditor)component;
         
         // start outer table
         out.write("<table border='0' cellspacing='3' cellpadding='3'");
         this.outputAttribute(out, attrs.get("style"), "style");
         this.outputAttribute(out, attrs.get("styleClass"), "class");
         out.write(">");
         
         // render the area before the wrapped component
         renderPreWrappedComponent(context, out, editor);
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }
      
      if (component instanceof UIMultiValueEditor)
      {
         ResponseWriter out = context.getResponseWriter();
         UIMultiValueEditor editor = (UIMultiValueEditor)component;
         
         // get hold of the node service
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
         // render the area between the component and current items list
         renderPostWrappedComponent(context, out, editor);

         // show the currently selected items
         out.write("<tr><td style='padding-top:8px'>");
         out.write(editor.getSelectedItemsMsg());
         out.write("</td></tr>");

         out.write("<tr><td><table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>");
         out.write("<tr><td colspan='2' class='selectedItemsHeader'>");
         out.write(Application.getMessage(context, "name"));
         out.write("</td></tr>");
         
         List currentItems = (List)editor.getValue();;
         if (currentItems != null && currentItems.size() > 0)
         {
            for (int x = 0; x < currentItems.size(); x++)
            {  
               Object obj = currentItems.get(x);
               if (obj != null)
               {
                  if (obj instanceof NodeRef)
                  {
                     if (nodeService.exists((NodeRef)obj))
                     {
                        renderExistingItem(context, component, out, nodeService, x, obj);
                     }
                     else
                     {
                        // remove invalid NodeRefs from the list
                        currentItems.remove(x);
                     }
                  }
                  else
                  {
                     renderExistingItem(context, component, out, nodeService, x, obj);
                  }
               }
            }
         }
         else
         {
            out.write("<tr><td class='selectedItemsRow'>");
            out.write(editor.getNoSelectedItemsMsg());
            out.write("</td></tr>");
         }
         
         // close tables
         out.write("</table></td></tr></table>\n");
         
         // output a hidden field containing the current value
         out.write("<input type='hidden' id='");
         out.write(component.getClientId(context));
         out.write("_current_value");
         out.write("' name='");
         out.write(component.getClientId(context));
         out.write("_current_value");
         out.write("' value='");
         if (currentItems != null && currentItems.size() > 0)
         {
            out.write(currentItems.toString());
         }
         out.write("' />");
      }
   }

   /**
    * Renders the area of the component before the wrapped component appears.
    * 
    * @param context FacesContext
    * @param out The ResponseWriter to write to
    * @param editor The multi value editor component
    */
   protected abstract void renderPreWrappedComponent(FacesContext context, 
         ResponseWriter out, UIMultiValueEditor editor) throws IOException;
         
   /**
    * Renders the area of the component after the wrapped component but before the list
    * of currently selected values.
    * 
    * @param context FacesContext
    * @param out The ResponseWriter to write to
    * @param editor The multi value editor component
    */      
   protected abstract void renderPostWrappedComponent(FacesContext context, 
         ResponseWriter out, UIMultiValueEditor editor) throws IOException; 
         
   /**
    * Renders an existing item with a remove button
    * 
    * @param context FacesContext
    * @param component The UIComponent
    * @param out Writer to write output to
    * @param nodeService The NodeService
    * @param index The index of the item
    * @param value The item's value
    * @throws IOException
    */
   protected void renderExistingItem(FacesContext context, UIComponent component, ResponseWriter out, 
         NodeService nodeService, int index, Object value) throws IOException
   {
      out.write("<tr><td class='");
      if (this.highlightedRow)
      {
         out.write("selectedItemsRowAlt");
      }
      else
      {
         out.write("selectedItemsRow");
      }
      out.write("'>");
      
      if (value instanceof NodeRef) 
      { 
          String name; 
          if (ContentModel.TYPE_CATEGORY.equals(nodeService.getType((NodeRef)value))) 
          { 
              name = Repository.getNameForCategoryNode(nodeService, (NodeRef)value); 
          } 
          else 
          { 
              name = Repository.getNameForNode(nodeService, (NodeRef)value); 
          } 
          out.write(Utils.encode(name)); 
      }
      else if (value instanceof Date)
      {
         XMLDateConverter converter = (XMLDateConverter)context.getApplication().
               createConverter(RepoConstants.ALFRESCO_FACES_XMLDATE_CONVERTER);
         UIComponent childComponent = (UIComponent)component.getChildren().get(0);
         Boolean showTime = (Boolean)childComponent.getAttributes().get("showTime");
         if (showTime != null && showTime.booleanValue())
         {
            converter.setPattern(Application.getMessage(context, "date_time_pattern"));
         }
         else
         {
            converter.setPattern(Application.getMessage(context, "date_pattern"));
         }
         
         out.write(converter.getAsString(context, childComponent, value));
      }
      else if (value instanceof Boolean)
      {
         Converter converter = context.getApplication().createConverter(
               RepoConstants.ALFRESCO_FACES_BOOLEAN_CONVERTER);
         out.write(converter.getAsString(context, 
               (UIComponent)component.getChildren().get(0), value));
      }
      else
      {
         out.write(Utils.encode(value.toString()));
      }

      out.write("&nbsp;&nbsp;");
      out.write("</td><td class='");
      if (this.highlightedRow)
      {
         out.write("selectedItemsRowAlt");
      }
      else
      {
         out.write("selectedItemsRow");
      }
      out.write("'><a href='#' title='");
      out.write(Application.getMessage(context, MSG_REMOVE));
      out.write("' onclick=\"");
      out.write(generateFormSubmit(context, component, UIMultiValueEditor.ACTION_REMOVE + UIMultiValueEditor.ACTION_SEPARATOR + index));
      out.write("\"><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/delete.gif' border='0' width='13' height='16'/></a>");
      
      this.highlightedRow = !this.highlightedRow;
   }

   /**
    * We use a hidden field per picker instance on the page.
    * 
    * @return hidden field name
    */
   protected String getHiddenFieldName(UIComponent component)
   {
      return component.getClientId(FacesContext.getCurrentInstance());
   }
   
   /**
    * Generate FORM submit JavaScript for the specified action
    *  
    * @param context    FacesContext
    * @param component  The UIComponent
    * @param action     Action string
    * 
    * @return FORM submit JavaScript
    */
   protected String generateFormSubmit(FacesContext context, UIComponent component, String action)
   {
      return Utils.generateFormSubmit(context, component, getHiddenFieldName(component), action);
   }
}
