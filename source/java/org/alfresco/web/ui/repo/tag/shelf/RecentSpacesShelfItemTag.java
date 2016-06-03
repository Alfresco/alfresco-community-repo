package org.alfresco.web.ui.repo.tag.shelf;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;
import org.alfresco.web.ui.repo.component.shelf.UIRecentSpacesShelfItem;

/**
 * @author Kevin Roast
 */
public class RecentSpacesShelfItemTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.RecentSpacesShelfItem";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // self rendering component
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringBindingProperty(component, "value", this.value);
      if (isValueReference(this.navigateActionListener))
      {
         MethodBinding vb = getFacesContext().getApplication().createMethodBinding(this.navigateActionListener, ACTION_CLASS_ARGS);
         ((UIRecentSpacesShelfItem)component).setNavigateActionListener(vb);
      }
      else
      {
         throw new FacesException("Navigate Action listener method binding incorrectly specified: " + this.navigateActionListener);
      }
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      
      this.value = null;
      this.navigateActionListener = null;
   }
   
   /**
    * Set the value used to bind the recent spaces list to the component
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the navigateActionListener
    *
    * @param navigateActionListener     the navigateActionListener
    */
   public void setNavigateActionListener(String navigateActionListener)
   {
      this.navigateActionListener = navigateActionListener;
   }


   /** the navigateActionListener */
   private String navigateActionListener;
   
   /** the value */
   private String value;
}
