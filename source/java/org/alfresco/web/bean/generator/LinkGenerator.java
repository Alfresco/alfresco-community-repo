/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
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
            UIOutput output = createOutputTextComponent(context, "label_" + component.getId());
            output.setValueBinding("value", component.getValueBinding("value"));
            component.getChildren().add(output);
        }
    }
}
