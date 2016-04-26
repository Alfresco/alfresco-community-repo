package org.alfresco.web.ui.common.component;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

/**
 * Component to represent a selectable list of images 
 * 
 * @author gavinc
 */
public class UIImagePicker extends UIInput
{   
   /**
    * Default constructor
    */
   public UIImagePicker()
   {
      // set the default renderer for an image picker component
      setRendererType("org.alfresco.faces.Radio");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.ImagePicker";
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[1];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      return (values);
   }
}
