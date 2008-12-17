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
package org.alfresco.web.bean.actions.blogIntegration;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.blogIntegration.BlogIntegrationImplementation;
import org.alfresco.repo.blogIntegration.BlogIntegrationService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.generator.BaseComponentGenerator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Blog selector generator.
 * 
 * @author Roy Wetherall
 */
public class BlogSelectorGenerator extends BaseComponentGenerator
{
    /** Node */
    protected Node node;
    
    /** Blog integration service */
    BlogIntegrationService blogIntegrationService;
    
    /**
     * Set the blog integration service
     * 
     * @param blogIntegrationService    the blog integration service
     */
    public void setBlogIntegrationService(BlogIntegrationService blogIntegrationService)
    {
        this.blogIntegrationService = blogIntegrationService;
    }
    
    /**
     * @see org.alfresco.web.bean.generator.IComponentGenerator#generate(javax.faces.context.FacesContext, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public UIComponent generate(FacesContext context, String id)
    {
        UIComponent component = context.getApplication().createComponent(UISelectOne.COMPONENT_TYPE);
        FacesHelper.setupComponentId(context, component, id);        
  
        // create the list of choices
        UISelectItems itemsComponent = (UISelectItems)context.getApplication().createComponent("javax.faces.SelectItems");
  
        itemsComponent.setValue(getBlogItems());
   
        // add the items as a child component
        component.getChildren().add(itemsComponent);
           
       return component;
    }
    
    /**
     * Gets the items to put in the drop down control using the blog integration service.
     * 
     * @return  SelectItem[]    array of select items
     */
    protected SelectItem[] getBlogItems()
    {       
        List<BlogIntegrationImplementation> blogs = this.blogIntegrationService.getBlogIntegrationImplementations();
        SelectItem[] items = new SelectItem[blogs.size()];
        int index = 0;
        for (BlogIntegrationImplementation blog : blogs)
        {
            items[index] = new SelectItem(blog.getName(), blog.getDisplayName());
            index ++;
        }
        
        return items;
    }
    
    /**
     * @see org.alfresco.web.bean.generator.BaseComponentGenerator#createComponent(javax.faces.context.FacesContext, org.alfresco.web.ui.repo.component.property.UIPropertySheet, org.alfresco.web.ui.repo.component.property.PropertySheetItem)
     */
    @Override
    protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, PropertySheetItem item) {
       
       this.node = propertySheet.getNode();
        
       return super.createComponent(context, propertySheet, item);
    }
}
