package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;

/**
 * Renders the MultiValueEditor component for use with field components
 * i.e. text, checkboxes, lists etc.
 * 
 * This renderer does not show a "select item" message or a select button,
 * the wrapped component is shown immediately with an add to list button
 * after it.
 * 
 * @author gavinc
 */
public class MultiValueFieldRenderer extends BaseMultiValueRenderer
{
   @Override
   protected void renderPreWrappedComponent(FacesContext context, ResponseWriter out, 
         UIMultiValueEditor editor) throws IOException
   {
      out.write("<tr><td>");
   }

   @SuppressWarnings("unchecked")
   @Override
   protected void renderPostWrappedComponent(FacesContext context, ResponseWriter out, 
         UIMultiValueEditor editor) throws IOException
   {
      out.write("&nbsp;<input type='button' value='");
      out.write(Application.getMessage(context, MSG_ADD_TO_LIST_BUTTON));
      out.write("' onclick=\"");
      out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_ADD)));
      out.write("\"/>");
      
      // if the wrapped component is an mltext field add the icon
      if (editor.getAttributes().get("mltext") != null)
      {
          String tooltip = Application.getMessage(context, "marker_tooltip");
          out.write("<img src='");
          out.write(context.getExternalContext().getRequestContextPath());
          out.write("/images/icons/multilingual_marker.gif' title='");
          out.write(tooltip);
          out.write("' style='margin-left:6px; vertical-align:-2px;'>");
      }
      
      out.write("</td></tr>");
   }
}
