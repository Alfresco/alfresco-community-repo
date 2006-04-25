package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a component to manage child associations.
 * 
 * @author gavinc
 */
public class ChildAssociationGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_CHILD_ASSOC_EDITOR);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }
   
   @Override
   protected void setupMandatoryValidation(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component, boolean realTimeChecking, String idSuffix)
   {
      // Override the setup of the mandatory validation 
      // so we can send the _current_value id suffix.
      // We also enable real time so the page load
      // check disables the ok button if necessary, as the user
      // adds or removes items from the multi value list the 
      // page will be refreshed and therefore re-check the status.
      
      super.setupMandatoryValidation(context, propertySheet, item, 
            component, true, "_current_value");
   }
}
