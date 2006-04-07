package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIAssociationEditor;
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

   public UIComponent generate(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      // generate the standard association editor
      UIAssociationEditor component = (UIAssociationEditor)generate(context, item.getName());
    
      AssociationDefinition assocDef = this.getAssociationDefinition(context, 
            propertySheet.getNode(), item.getName());

      // set the association name and set to disabled if appropriate
      component.setAssociationName(assocDef.getName().toString());
      if (propertySheet.inEditMode() == false || item.isReadOnly() || assocDef.isProtected())
      {
         component.setDisabled(true);
      }
      
      // setup the converter if one was specified
      setupConverter(context, propertySheet, item, component);
      
      return component;
   }
}
