package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Link Generator.
 * 
 * Generates a link tag which opend the URL into a new browsers window.
 * 
 * @author Roy Wetherall
 */
public class LinkGenerator extends BaseComponentGenerator
{
    private boolean inEditMode = false;
    
    /**
     * @see org.alfresco.web.bean.generator.IComponentGenerator#generate(javax.faces.context.FacesContext, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public UIComponent generate(FacesContext context, String id)
    {
        UIComponent component = null;
        if (this.inEditMode == false)
        {
            component = context.getApplication().createComponent(UIOutput.COMPONENT_TYPE);
            component.setRendererType("javax.faces.Link");
            component.getAttributes().put("target", "new");
        }
        else
        {
            component = context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_INPUT);
            component.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
        }
        
        FacesHelper.setupComponentId(context, component, id);    
        return component;
    }
    
    /**
     * @see org.alfresco.web.bean.generator.BaseComponentGenerator#createComponent(javax.faces.context.FacesContext, org.alfresco.web.ui.repo.component.property.UIPropertySheet, org.alfresco.web.ui.repo.component.property.PropertySheetItem)
     */
    @Override
    protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, PropertySheetItem item)
    {
        this.inEditMode = propertySheet.inEditMode();
        return this.generate(context, item.getName());
    }

    /**
     * @see org.alfresco.web.bean.generator.BaseComponentGenerator#setupProperty(javax.faces.context.FacesContext, org.alfresco.web.ui.repo.component.property.UIPropertySheet, org.alfresco.web.ui.repo.component.property.PropertySheetItem, org.alfresco.service.cmr.dictionary.PropertyDefinition, javax.faces.component.UIComponent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void setupProperty(FacesContext context, UIPropertySheet propertySheet,
          PropertySheetItem item, PropertyDefinition propertyDef, UIComponent component)
    {
        super.setupProperty(context, propertySheet, item, propertyDef, component);
        
        if (component.getRendererType().equals("javax.faces.Link") == true)
        {
            // Add child text component
            UIOutput output = createOutputTextComponent(context, "linklabel_" + component.getId());
            output.setValueBinding("value", component.getValueBinding("value"));
            component.getChildren().add(output);
        }
    }
}
