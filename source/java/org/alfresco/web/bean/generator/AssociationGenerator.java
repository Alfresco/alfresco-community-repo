package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a component to manage associations.
 *
 * @author gavinc
 */
public class AssociationGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_ASSOC_EDITOR);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }
   
   @Override
   protected void setupMandatoryValidation(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component, boolean realTimeChecking)
   {
      // TODO: the association editor component needs to use the 
      //       'current_value' hidden field rather than the standard
      //       'value' field as this is always null (it's used internally 
      //       by the component) for now disable mandatory checks completely
   }
}
