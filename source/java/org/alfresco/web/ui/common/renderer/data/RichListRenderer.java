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
package org.alfresco.web.ui.common.renderer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.data.UIColumn;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author kevinr
 */
public class RichListRenderer extends BaseRenderer
{
   // ------------------------------------------------------------------------------
   // Renderer implemenation 
   
   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeBegin(FacesContext context, UIComponent component)
         throws IOException
   {
      // always check for this flag - as per the spec
      if (component.isRendered() == true)
      {
         ResponseWriter out = context.getResponseWriter();
         Map attrs = component.getAttributes();
         out.write("<table cellspacing=0 cellpadding=0");
         outputAttribute(out, attrs.get("styleClass"), "class");
         outputAttribute(out, attrs.get("style"), "style");
         outputAttribute(out, attrs.get("width"), "width");
         out.write(">");
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeChildren(FacesContext context, UIComponent component)
         throws IOException
   {
      if (component.isRendered() == true)
      {
         // the RichList component we are working with
         UIRichList richList = (UIRichList)component;
         
         // prepare the component current row against the current page settings
         richList.bind();
         
         // collect child column components so they can be passed to the renderer
         List<UIColumn> columnList = new ArrayList<UIColumn>(8);
         for (Iterator i=richList.getChildren().iterator(); i.hasNext(); /**/)
         {
            UIComponent child = (UIComponent)i.next();
            if (child instanceof UIColumn)
            {
               columnList.add((UIColumn)child);
            }
         }
         
         UIColumn[] columns = new UIColumn[columnList.size()];
         columnList.toArray(columns);
         
         // get the renderer instance
         IRichListRenderer renderer = (IRichListRenderer)richList.getViewRenderer();
         if (renderer == null)
         {
            throw new IllegalStateException("IRichListRenderer must be available in UIRichList!");
         }
         
         // call render-before to output headers if required
         ResponseWriter out = context.getResponseWriter();
         out.write("<thead>");
         renderer.renderListBefore(context, richList, columns);
         out.write("</thead>");
         out.write("<tbody>");
         if (richList.isDataAvailable() == true)
         {
            while (richList.isDataAvailable() == true)
            {
               // render each row in turn
               renderer.renderListRow(context, richList, columns, richList.nextRow());
            }
         }
         else
         {
            // if no items present, render the facet with the "no items found" message
            UIComponent emptyComponent = richList.getEmptyMessage();
            if (emptyComponent != null)
            {
               emptyComponent.encodeBegin(context);
               emptyComponent.encodeChildren(context);
               emptyComponent.encodeEnd(context);
            }
         }
         // call render-after to output footers if required
         renderer.renderListAfter(context, richList, columns);
         out.write("</tbody>");
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeEnd(FacesContext context, UIComponent component)
         throws IOException
   {
      // always check for this flag - as per the spec
      if (component.isRendered() == true)
      {
         ResponseWriter out = context.getResponseWriter();
         out.write("</table>");
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      // we are responsible for rendering our child components
      // this renderer is a valid use of this mode - it can render the various
      // column descriptors as a number of different list view types e.g.
      // details, icons, list etc.
      return true;
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class to implement a Details view for the RichList component
    * 
    * @author kevinr
    */
   public static class DetailsViewRenderer implements IRichListRenderer
   {
      public static final String VIEWMODEID = "details";
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#getViewModeID()
       */
      public String getViewModeID()
      {
         return VIEWMODEID;
      }

      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListBefore(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[])
       */
      public void renderListBefore(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         // render column headers as labels
         out.write("<tr");
         outputAttribute(out, richList.getAttributes().get("headerStyleClass"), "class");
         out.write('>');
         for (int i=0; i<columns.length; i++)
         {
            UIColumn column = columns[i];
            
            if (column.isRendered() == true)
            {
               // render column header tag
               out.write("<th");
               outputAttribute(out, column.getAttributes().get("width"), "width");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               
               // output the header facet if any
               UIComponent header = column.getHeader();
               if (header != null)
               {
                  header.encodeBegin(context);
                  if (header.getRendersChildren())
                  {
                     header.encodeChildren(context);
                  }
                  header.encodeEnd(context);
               }
               
               // we don't render child controls for the header row
               out.write("</th>");
            }
         }
         out.write("</tr>");
         
         this.rowIndex = 0;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListRow(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[], java.lang.Object)
       */
      public void renderListRow(FacesContext context, UIRichList richList, UIColumn[] columns, Object row)
            throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         // output row or alt style row if set
         out.write("<tr");
         String rowStyle = (String)richList.getAttributes().get("rowStyleClass");
         String altStyle = (String)richList.getAttributes().get("altRowStyleClass");
         if (altStyle != null && (this.rowIndex++ & 1) == 1)
         {
            rowStyle = altStyle;
         }         
         outputAttribute(out, rowStyle, "class");
         out.write('>');
         
         // find the actions column if it exists
         UIColumn actionsColumn = null;
         for (int i=0; i<columns.length; i++)
         {
            if (columns[i].isRendered() == true && columns[i].getActions() == true)
            {
               actionsColumn = columns[i];
               break;
            }
         }
         
         // output each column in turn and render all children
         boolean renderedFirst = false;
         for (int i=0; i<columns.length; i++)
         {
            UIColumn column = columns[i];
            
            if (column.isRendered() == true)
            {
               out.write("<td");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               
               // for details view, we show the small column icon for the first column
               if (renderedFirst == false)
               {
                  UIComponent smallIcon = column.getSmallIcon();
                  if (smallIcon != null)
                  {
                     smallIcon.encodeBegin(context);
                     if (smallIcon.getRendersChildren())
                     {
                        smallIcon.encodeChildren(context);
                     }
                     smallIcon.encodeEnd(context);
                     out.write("&nbsp;");
                  }
                  renderedFirst = true;
               }
               
               if (column.getChildCount() != 0)
               {
                  if (column == actionsColumn)
                  {
                     out.write("<nobr>");
                  }
                  
                  // allow child controls inside the columns to render themselves
                  Utils.encodeRecursive(context, column);
                  
                  if (column == actionsColumn)
                  {
                     out.write("</nobr>");
                  }
               }
               
               out.write("</td>");
            }
         }
         out.write("</tr>");
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListAfter(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[])
       */
      public void renderListAfter(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         out.write("<tr><td colspan=99 align=right>");
         for (Iterator i=richList.getChildren().iterator(); i.hasNext(); /**/)
         {
            // output all remaining child components that are not UIColumn
            UIComponent child = (UIComponent)i.next();
            if (child instanceof UIColumn == false)
            {
               Utils.encodeRecursive(context, child);
            }
         }
         out.write("</td></tr>");
      }
      
      private int rowIndex = 0;
   }
   
   
   /**
    * Class to implement a List view for the RichList component
    * 
    * @author kevinr
    */
   public static class ListViewRenderer implements IRichListRenderer
   {
      // maximum displayable textual lines within a single item cell
      private final static int MAX_DISPLAYABLE_LINES = 3;
      
      private final static String END_ROW_SEPARATOR = "</tr><tr><td colspan=10><div style='padding:3px'></div></td></tr>";
      private final static String COLUMN_SPACER = "<td><div style='padding-left:8px'></div></td>";
      
      public static final String VIEWMODEID = "list";
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#getViewModeID()
       */
      public String getViewModeID()
      {
         return VIEWMODEID;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListBefore(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[])
       */
      public void renderListBefore(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException
      {
//         ResponseWriter out = context.getResponseWriter();
         
         // render column headers as labels
         // TODO: add "showHeaders" to RichList to allow hiding of header facets for some view modes
         /*
         out.write("<tr");
         outputAttribute(out, richList.getAttributes().get("headerStyleClass"), "class");
         out.write('>');
         for (int i=0; i<columns.length; i++)
         {
            UIColumn column = columns[i];
            
            if (column.isRendered() == true)
            {
               out.write("<th");
               outputAttribute(out, column.getAttributes().get("width"), "width");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               
               // output the header facet if any
               UIComponent header = column.getHeader();
               if (header != null)
               {
                  header.encodeBegin(context);
                  header.encodeChildren(context);
                  header.encodeEnd(context);
               }
            }
            
            // we don't render child controls for the header row
            out.write("</th>");
         }
         out.write("</tr>");
         */
         
         this.rowIndex = 0;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListRow(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[], java.lang.Object)
       */
      public void renderListRow(FacesContext context, UIRichList richList, UIColumn[] columns, Object row) throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         // start new row if we are on an even column in this view
         // we show 2 columns, left to right
         String rowStyle = (String)richList.getAttributes().get("rowStyleClass");
         if ((this.rowIndex & 1) == 0)
         {
            out.write("<tr");
            outputAttribute(out, rowStyle, "class");
            out.write('>');
         }
         
         // find the actions column if it exists
         // and the primary column (which must exist)
         UIColumn primaryColumn = null;
         UIColumn actionsColumn = null;
         for (int i=0; i<columns.length; i++)
         {
            if (columns[i].isRendered() == true)
            {
               if (columns[i].getPrimary() == true)
               {
                  primaryColumn = columns[i];
               }
               else if (columns[i].getActions() == true)
               {
                  actionsColumn = columns[i];
               }
            }
         }
         if (primaryColumn == null)
         {
            logger.warn("No primary column found for RichList definition: " + richList.getId());
         }
         
         // get the icon from the primary column
         out.write("<td width=50%><table cellspacing=0 cellpadding=2 border=0>");
         if (primaryColumn != null)
         {
            UIColumn column = primaryColumn;
            
            if (column.isRendered() == true)
            {
               out.write("<tr><td rowspan=10");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               
               // output the large icon for this column
               UIComponent icon = column.getLargeIcon();
               if (icon != null)
               {
                  icon.encodeBegin(context);
                  if (icon.getRendersChildren())
                  {
                     icon.encodeChildren(context);
                  }
                  icon.encodeEnd(context);
               }
               out.write("</td>");
               
               // start the next cell which contains the first column component
               out.write("<td width=100%");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               if (column.getChildCount() != 0)
               {
                  // allow child controls inside the column to render themselves
                  Utils.encodeRecursive(context, column);
               }
               out.write("</td>");
               
               // output actions column if any
               if (actionsColumn != null)
               {
                  out.write("<td");
                  outputAttribute(out, actionsColumn.getAttributes().get("style"), "style");
                  outputAttribute(out, actionsColumn.getAttributes().get("styleClass"), "class");
                  out.write("><nobr>");
                  
                  if (actionsColumn.getChildCount() != 0)
                  {
                     // allow child controls inside the columns to render themselves
                     Utils.encodeRecursive(context, actionsColumn);
                  }
                  out.write("</nobr></td>");
               }
               
               out.write("</tr>");
            }
         }
         
         // render remaining columns as lines of data up to a max display limit
         for (int i = 0; i < columns.length; i++)
         {
            UIColumn column = columns[i];
            
            int count = 1;
            if (column.isRendered() == true && count < MAX_DISPLAYABLE_LINES &&
                column.getActions() == false && column.getPrimary() == false)
            {
               // output row or alt style row if set
               out.write("<tr valign=top");
               outputAttribute(out, rowStyle, "class");
               out.write("><td colspan=2");     // render into above actions column
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               if (column.getChildCount() != 0)
               {
                  // allow child controls inside the columns to render themselves
                  Utils.encodeRecursive(context, column);
               }
               // end this cell and end row
               out.write("</td></tr>");
               
               count++;
            }
         }
         
         out.write("</table></td>");
         
         if ((this.rowIndex & 1) == 1)
         {
            // end row and output a blank padding row/div
            out.write(END_ROW_SEPARATOR);
         }
         else
         {
            out.write(COLUMN_SPACER);
         }
         
         this.rowIndex++;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListAfter(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[])
       */
      public void renderListAfter(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         // finish last row if required (we used an open-ended column rendering algorithm)
         if ( ((this.rowIndex-1) & 1) != 1)
         {
            out.write(END_ROW_SEPARATOR);
         }
         
         out.write("<tr><td colspan=99 align=right>");
         for (Iterator i=richList.getChildren().iterator(); i.hasNext(); /**/)
         {
            // output all remaining child components that are not UIColumn
            UIComponent child = (UIComponent)i.next();
            if (child instanceof UIColumn == false)
            {
               Utils.encodeRecursive(context, child);
            }
         }
         out.write("</td></tr>");
      }
      
      private int rowIndex = 0;
   }
   
   
   /**
    * Class to implement an Icon view for the RichList component
    * 
    * @author kevinr
    */
   public static class IconViewRenderer implements IRichListRenderer
   {
      // number of vertical columns to render before starting new row
      private final static int COLUMNS = 3;
      
      // calculation for percentage of table row per column
      private final static String COLUMN_PERCENT = Integer.toString(100/COLUMNS) + "%";
      
      // maximum displayable textual lines within a single item cell
      private final static int MAX_DISPLAYABLE_LINES = 3;
      
      private final static String END_ROW_SEPARATOR = "</tr><tr><td colspan=10><div style='padding:3px'></div></td></tr>";
      
      public static final String VIEWMODEID = "icons";
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#getViewModeID()
       */
      public String getViewModeID()
      {
         return VIEWMODEID;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListBefore(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[])
       */
      public void renderListBefore(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException
      {
         // no headers for this renderer
         this.rowIndex = 0;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListRow(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[], java.lang.Object)
       */
      public void renderListRow(FacesContext context, UIRichList richList, UIColumn[] columns, Object row)
            throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         // start new row as per number of columns in this icon view
         if (this.rowIndex % COLUMNS == 0)
         {
            out.write("<tr");
            outputAttribute(out, richList.getAttributes().get("rowStyleClass"), "class");
            out.write('>');
         }
         
         // find primary column (which must exist)
         UIColumn primaryColumn = null;
         for (int i=0; i<columns.length; i++)
         {
            if (columns[i].isRendered() == true && columns[i].getPrimary() == true)
            {
               primaryColumn = columns[i];
               break;
            }
         }
         if (primaryColumn == null)
         {
            logger.warn("No primary column found for RichList definition: " + richList.getId());
         }
         
         // output primary column as the icon label
         out.write("<td width=");
         out.write(COLUMN_PERCENT);
         out.write("><table cellspacing=0 cellpadding=2 border=0>");
         if (primaryColumn != null)
         {
            UIColumn column = primaryColumn;
            
            if (column.isRendered() == true)
            {
               out.write("<tr><td rowspan=10");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               
               // output the large icon for this column
               UIComponent largeIcon = column.getLargeIcon();
               if (largeIcon != null)
               {
                  largeIcon.encodeBegin(context);
                  if (largeIcon.getRendersChildren())
                  {
                     largeIcon.encodeChildren(context);
                  }
                  largeIcon.encodeEnd(context);
               }
               out.write("</td>");
               
               // start the next cell which contains the first column component
               out.write("<td");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               if (column.getChildCount() != 0)
               {
                  // allow child controls inside the columns to render themselves
                  Utils.encodeRecursive(context, column);
               }
               out.write("</td></tr>");
            }
         }
         
         // render remaining columns as lines of data up to a max display limit
         for (int i=0; i<columns.length; i++)
         {
            UIColumn column = columns[i];
            
            int count = 1;
            if (column.isRendered() == true && column.getPrimary() == false &&
                (count < MAX_DISPLAYABLE_LINES || column.getActions() == true) )
            {
               out.write("<tr><td");
               outputAttribute(out, column.getAttributes().get("style"), "style");
               outputAttribute(out, column.getAttributes().get("styleClass"), "class");
               out.write('>');
               if (column.getChildCount() != 0)
               {
                  // allow child controls inside the columns to render themselves
                  Utils.encodeRecursive(context, column);
               }
               out.write("</td></tr>");
               
               count++;
            }
         }
         
         out.write("</table></td>");
         
         if (this.rowIndex % COLUMNS == COLUMNS-1)
         {
            // end row and output a blank padding row/div
            out.write(END_ROW_SEPARATOR);
         }
         
         this.rowIndex++;
      }
      
      /**
       * @see org.alfresco.web.ui.common.renderer.data.IRichListRenderer#renderListAfter(javax.faces.context.FacesContext, org.alfresco.web.ui.common.component.data.UIColumn[])
       */
      public void renderListAfter(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException
      {
         ResponseWriter out = context.getResponseWriter();
         
         // finish last row if required (we used an open-ended column rendering algorithm)
         if ((this.rowIndex-1) % COLUMNS != COLUMNS-1)
         {
            out.write(END_ROW_SEPARATOR);
         }
         
         out.write("<tr><td colspan=99 align=right>");
         for (Iterator i=richList.getChildren().iterator(); i.hasNext(); /**/)
         {
            // output all remaining child components that are not UIColumn
            UIComponent child = (UIComponent)i.next();
            if (child instanceof UIColumn == false)
            {
               Utils.encodeRecursive(context, child);
            }
         }
         out.write("</td></tr>");
      }
      
      private int rowIndex = 0;
   }
   
   
   private static Log logger = LogFactory.getLog(RichListRenderer.class);
}
