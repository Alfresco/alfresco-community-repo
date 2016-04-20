package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.apache.myfaces.renderkit.html.HtmlTextareaRenderer;

/**
 * Renders a multilingual text area.
 * <p>
 * Renders the default output followed by an icon
 * to represent multilingual properties.
 * </p>
 * 
 * @author gavinc
 */
public class MultilingualTextAreaRenderer extends HtmlTextareaRenderer
{
   @Override
   protected void encodeTextArea(FacesContext facesContext, UIComponent uiComponent) throws IOException 
   {
      // to workaround a bug in MyFaces where it appears a new line gets removed 
      // in the process view/edit process add it back (ETWOONE-91)
      Object value = ((ValueHolder) uiComponent).getValue();
      String valueStr = null;
      if (value != null)
      {
         valueStr = "\r\n" + (String)value;
      }
      ((ValueHolder) uiComponent).setValue(valueStr);
      super.encodeTextArea(facesContext, uiComponent);
}

   @Override
   public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
   {
      super.encodeEnd(facesContext, component);
      
      String tooltip = Application.getMessage(facesContext, "marker_tooltip");
      ResponseWriter out = facesContext.getResponseWriter();
      out.write("<img src='");
      out.write(facesContext.getExternalContext().getRequestContextPath());
      out.write("/images/icons/multilingual_marker.gif' title='");
      out.write(tooltip);
      out.write("' style='margin-left:6px; vertical-align:-2px;'>");
   }
}
