package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIChildAssociationEditor;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a text field component.
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

   public UIComponent generate(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      // generate the standard child association editor
      UIChildAssociationEditor component = (UIChildAssociationEditor)generate(context, 
            item.getName());
      
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
