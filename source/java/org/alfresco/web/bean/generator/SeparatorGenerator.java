package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a component to represent a separator.
 * 
 * @author gavinc
 */
public class SeparatorGenerator extends BaseComponentGenerator
{
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
      component.getAttributes().put("value", getHtml(component, item));
      
      return component;
   }
   
   /**
    * Returns the HTML to display for the separator
    * 
    * @param component The JSF component representing the separator
    * @param item The separator item
    * @return The HTML
    */
   protected String getHtml(UIComponent component, PropertySheetItem item)
   {
      return "<div style='margin-top: 6px; margin-bottom: 6px;'><hr/></div>";
   }
}