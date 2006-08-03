package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;

/**
 * Generates a component to represent a separator that gets rendered 
 * as a header.
 * 
 * @author gavinc
 */
public class HeaderSeparatorGenerator extends SeparatorGenerator
{
   @Override
   protected String getHtml(UIComponent component, PropertySheetItem item)
   {
      String html = "<div class='wizardSectionHeading mainSubTitle' style='margin-top: 6px; margin-bottom: 6px;'>&nbsp;" + 
                    item.getDisplayLabel() + "</div>";
      
      return html;
   }
}
