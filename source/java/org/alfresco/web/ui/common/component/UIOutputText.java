package org.alfresco.web.ui.common.component;

import java.io.IOException;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import org.springframework.extensions.surf.util.URLEncoder;
import org.alfresco.web.ui.common.Utils;

/**
 * Component that simply renders text
 * 
 * @author gavinc
 */
public class UIOutputText extends UIOutput
{
   private Boolean encodeForJavaScript = null;
   
   /**
    * Default constructor
    */
   public UIOutputText()
   {
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.OutputText";
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.encodeForJavaScript = (Boolean)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.encodeForJavaScript;
      return values;
   }
   
   /**
    * Sets whether the text should be encoded for JavaScript consumption
    * 
    * @param encodeForJavaScript true to escape text
    */
   public void setEncodeForJavaScript(boolean encodeForJavaScript)
   {
      this.encodeForJavaScript = Boolean.valueOf(encodeForJavaScript);
   }

   /**
    * Returns whether the text is going to be encoded or not
    * 
    * @return true if the text is going to be encoded
    */
   public boolean isEncodeForJavaScript()
   {
      if (this.encodeForJavaScript == null)
      {
         ValueBinding vb = getValueBinding("encodeForJavaScript");
         if (vb != null)
         {
            this.encodeForJavaScript = (Boolean)vb.getValue(getFacesContext());
         }
         
         if (this.encodeForJavaScript == null)
         {
            this.encodeForJavaScript = Boolean.FALSE;
         }
      }
      
      return this.encodeForJavaScript.booleanValue();
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
      
      Object value = getValue();
      if (value != null)
      {
         Converter converter = getConverter();
         if (converter != null)
         {
            value = converter.getAsString(context, this, value);
         }
         
         ResponseWriter out = context.getResponseWriter();
   
         if (isEncodeForJavaScript())
         {
            out.write( URLEncoder.encode((String)getValue()) );
         }
         else
         {
            String style = (String)getAttributes().get("style");
            String styleClass = (String)getAttributes().get("styleClass");
            if (style != null || styleClass != null)
            {
               out.write("<span");
               if (style != null)
               {
                  out.write(" style='");
                  out.write(style);
                  out.write('\'');
               }
               if (styleClass != null)
               {
                  out.write(" class=");
                  out.write(styleClass);
               }
               out.write('>');
               out.write(Utils.encode(value.toString()));
               out.write("</span>");
            }
            else
            {
               out.write(Utils.encode(value.toString()));
            }
         }
      }
   }
}
