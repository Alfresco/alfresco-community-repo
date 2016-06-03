package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.component.UIStoreSelector;

/**
 * Generates a content store selector component.
 */
public class StoreSelectorGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(UIStoreSelector.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }
   
}
