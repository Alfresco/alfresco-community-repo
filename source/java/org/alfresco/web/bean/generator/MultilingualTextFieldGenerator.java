package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a multilingual text field component.
 * 
 * @author gavinc
 */
public class MultilingualTextFieldGenerator extends TextFieldGenerator
{
   @Override
   public UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet,
            PropertySheetItem item)
   {
      UIComponent component = super.generateAndAdd(context, propertySheet, item);
      
      if ((component instanceof UISelectOne) == false && 
          (component instanceof UIMultiValueEditor) == false)
      {
         component.setRendererType(RepoConstants.ALFRESCO_FACES_MLTEXT_RENDERER);
      }
      
      return component;
   }
}
