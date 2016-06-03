package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;
import org.alfresco.web.ui.repo.component.UISidebar;

/**
 * Tag class for using the sidebar component on a JSP page.
 * 
 * @author gavinc
 */
public class SidebarTag extends HtmlComponentTag
{
   private String activePlugin;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return UISidebar.COMPONENT_TYPE;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringBindingProperty(component, "activePlugin", this.activePlugin);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.activePlugin = null;
   }

   /**
    * Sets the activePlugin attribute
    * 
    * @param activePlugin
    */
   public void setActivePlugin(String activePlugin)
   {
      this.activePlugin = activePlugin;
   }
}
