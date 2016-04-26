package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;

/**
 * Renders the MultiValueEditor component for use with picker components.
 * 
 * This renderer shows a "select items" message and a select button. When
 * the select button is pressed the wrapped component will appear and the
 * add to list button will be enabled.
 * 
 * @author gavinc
 */
public class MultiValueSelectorRenderer extends BaseMultiValueRenderer
{
   @Override
   protected void renderPreWrappedComponent(FacesContext context, ResponseWriter out,
         UIMultiValueEditor editor) throws IOException
   {
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
         out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_SELECT)));
         out.write("\"/></td></tr>");
      }
   }
   
   @Override
   protected void renderPostWrappedComponent(FacesContext context, ResponseWriter out,
         UIMultiValueEditor editor) throws IOException
   {
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
      out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_ADD)));
      out.write("\"/></td></tr>");
   }
}
