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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.actions.blogIntegration;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.repo.blogIntegration.BlogIntegrationRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.component.UIActionLink;


/**
 * Blog details action listener
 * 
 * @author Roy Wetherall
 */
public class BlogDetailsActionListener implements BlogIntegrationModel
{
    /** The service registry */
    private ServiceRegistry services;
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services) 
    {
        this.services = services;
    }
    
    /**
     * Listener's execute method
     */
    public void executeScript(ActionEvent event)
    {
        // Get the script to be executed
        UIActionLink link = (UIActionLink)event.getComponent();
        Map<String, String> params = link.getParameterMap();
        
        String action = params.get("action");
        
        String id = params.get("id");
        NodeRef documentNodeRef = new NodeRef(Repository.getStoreRef(), id);        
        if ("add".equals(action) == true)
        {
            this.services.getNodeService().addAspect(documentNodeRef, ASPECT_BLOG_DETAILS, null);
        }
        else if ("remove".equals(action) == true)
        {
            this.services.getNodeService().removeAspect(documentNodeRef, ASPECT_BLOG_DETAILS);
        }
        else
        {
            throw new BlogIntegrationRuntimeException("Invalid action has been specified '" + action + "'");
        }
        
        FacesContext context = FacesContext.getCurrentInstance();
        BrowseBean browseBean = (BrowseBean)FacesHelper.getManagedBean(context, "BrowseBean");        
        browseBean.getActionSpace().reset();
        UIComponent comp = context.getViewRoot().findComponent("dialog:dialog-body:document-props");
        comp.getChildren().clear();
    }
}
