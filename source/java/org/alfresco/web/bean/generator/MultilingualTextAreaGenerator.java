package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a multilingual text area component.
 * 
 * @author gavinc
 */
public class MultilingualTextAreaGenerator extends TextAreaGenerator
{
   @Override
   public UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet,
            PropertySheetItem item)
   {
      UIComponent component = super.generateAndAdd(context, propertySheet, item);
      
      if ((component instanceof UIInput) && (component instanceof UISelectOne) == false)
      {
         component.setRendererType(RepoConstants.ALFRESCO_FACES_MLTEXTAREA_RENDERER);
      }
      else if ((component instanceof UIOutput))
      {
         component.setRendererType(RepoConstants.ALFRESCO_FACES_MLTEXT_RENDERER);
      }
      
      return component;
   }
}
