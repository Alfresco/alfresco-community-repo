
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.myfaces.taglib.html.HtmlInputTextTag;

public class UploadInputTag extends HtmlInputTextTag
{

   public static String COMPONENT_TYPE = "org.alfresco.faces.UploadInput";

   private String framework;
   
   @Override
   public String getComponentType()
   {
      return UploadInputTag.COMPONENT_TYPE;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "javax.faces.Text";
   }

    @SuppressWarnings("unchecked")
    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);
        FacesContext context = getFacesContext();
        if (null != framework)
        {
            if (isValueReference(framework))
            {
                ValueBinding vb = context.getApplication().createValueBinding(framework);
                component.setValueBinding("maxlength", vb);
            }
            else
            {
                component.getAttributes().put("framework", framework);
            }
        } 
        component.getAttributes().put("immediate", true);
        component.getAttributes().put("style", "display:none;");
    }

   public void release()
   {
      super.release();
      this.framework = null;
   }

   public void setFramework(String framework)
   {
      this.framework = framework;
   }
}
