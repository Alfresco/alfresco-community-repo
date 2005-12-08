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
package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor.MultiValueEditorEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renders the MultiValueEditor component as a list of options that can be
 * removed using a Remove button
 * 
 * @author gavinc
 */
public class MultiValueListEditorRenderer extends BaseRenderer
{
   private static Log logger = LogFactory.getLog(MultiValueListEditorRenderer.class);
   
   /** I18N message strings */
   private final static String MSG_REMOVE = "remove";
   private final static String MSG_SELECT_BUTTON = "select_button";
   private final static String MSG_ADD_TO_LIST_BUTTON = "add_to_list_button";
   
   private boolean highlightedRow;
   
   // ------------------------------------------------------------------------------
   // Renderer implemenation
   
   /**
    * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void decode(FacesContext context, UIComponent component)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      Map valuesMap = context.getExternalContext().getRequestParameterValuesMap();
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
         String clientId = component.getClientId(context);
         UIMultiValueEditor editor = (UIMultiValueEditor)component;
         
         // start outer table
         out.write("<table border='0' cellspacing='4' cellpadding='4' class='selector'");
         this.outputAttribute(out, attrs.get("style"), "style");
         this.outputAttribute(out, attrs.get("styleClass"), "styleClass");
         out.write(">");
         
         // show the select an item message
         out.write("<tr><td>");
         out.write("1. ");
         out.write(editor.getSelectItemMsg());
         out.write("</td></tr>");
         
         if (editor.getAddingNewItem())
         {
            out.write("<tr><td style='padding-left:8px'>");
         }
         else
         {
            out.write("<tr><td style='padding-left:8px;'><input type='submit' value='");
            out.write(Application.getMessage(context, MSG_SELECT_BUTTON));
            out.write("' onclick=\"");
            out.write(generateFormSubmit(context, component, Integer.toString(UIMultiValueEditor.ACTION_SELECT)));
            out.write("\"/></td></tr>");
         }
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException
   {
      if (component instanceof UIMultiValueEditor)
      {
         ResponseWriter out = context.getResponseWriter();
         UIMultiValueEditor editor = (UIMultiValueEditor)component;
         
         // get hold of the node service
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
         if (editor.getAddingNewItem())
         {
            out.write("</td></tr>");
         }
         
         // show the add to list button but only if something has been selected
         out.write("<tr><td>2. <input type='submit'");
         if (editor.getAddingNewItem() == false && editor.getLastItemAdded() != null || 
             editor.getLastItemAdded() == null)
         {
            out.write(" disabled='true'");
         }
         out.write(" value='");
         out.write(Application.getMessage(context, MSG_ADD_TO_LIST_BUTTON));
         out.write("' onclick=\"");
         out.write(generateFormSubmit(context, component, Integer.toString(UIMultiValueEditor.ACTION_ADD)));
         out.write("\"/></td></tr>");
         
         out.write("<tr><td style='padding-top:8px'>");
         out.write(editor.getSelectedItemsMsg());
         out.write("</td></tr>");
         
         // show the current items
         out.write("<tr><td><table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>");
         out.write("<tr><td colspan='2' class='selectedItemsHeader'>");
         out.write(Application.getMessage(context, "name"));
         out.write("</td></tr>");
         
         List currentItems = (List)editor.getValue();
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
         out.write("</table></td></tr></table>");
      }
   }

   /**
    * Renders an existing item with a remove button
    * 
    * @param context FacesContext
    * @param component The UIComponent
    * @param out Writer to write output to
    * @param nodeService The NodeService
    * @param key The key of the item
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
         out.write(Repository.getNameForNode(nodeService, (NodeRef)value));
      }
      else
      {
         out.write(value.toString());
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
   private String getHiddenFieldName(UIComponent component)
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
   private String generateFormSubmit(FacesContext context, UIComponent component, String action)
   {
      return Utils.generateFormSubmit(context, component, getHiddenFieldName(component), action);
   }
}
