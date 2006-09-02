package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a component to represent a separator.
 * <p>The HTML to be used for the separator is configured via the 
 * <code>setHtml</code> method.
 * 
 * @author gavinc
 */
public class HtmlSeparatorGenerator extends BaseComponentGenerator
{
   protected String html = "<b>default</b>";
   
   /**
    * Returns the HTML configured to be used for this separator
    * 
    * @return The HTML to display
    */
   public String getHtml()
   {
      return html;
   }

   /**
    * Sets the HTML to display for the separator
    * 
    * @param html The HTML
    */
   public void setHtml(String html)
   {
      this.html = html;
   }
   
   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = this.createOutputTextComponent(context, id);      
      component.getAttributes().put("escape", Boolean.FALSE);
      
      return component;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      UIComponent component = this.generate(context, item.getName());
      
      // set the HTML to use 
      component.getAttributes().put("value", getResolvedHtml(component, item));
      
      return component;
   }

   /**
    * Returns the resolved HTML to use for the separator.
    * <p>In the default case we just return the HTML set 
    * via setHtml however subclasses may choose to generate
    * the resulting HTML using a combination of the HTML set
    * via setHtml and the given PropertySheetItem.
    * 
    * @param component The JSF component representing the separator
    * @param item The separator item
    * @return The resolved HTML
    */
   protected String getResolvedHtml(UIComponent component, PropertySheetItem item)
   {
      // In the default case we just return the HTML set via setHtml
      
      return this.html;
   }
}