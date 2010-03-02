/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.dashboard;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.NavigationBean;

/**
 * Backing bean for the Getting Started Dashlet page.
 * 
 * @author Kevin Roast
 */
public class GettingStartedBean implements Serializable
{
    private static final long serialVersionUID = 5170536816906380802L;
    
    private NavigationBean navigationBean;
    
        
    /**
     * @param navigationBean The navigationBean to set.
     */
    public void setNavigationBean(NavigationBean navigationBean)
    {
        this.navigationBean = navigationBean;
    }

    public String browseHomeSpace()
    {
        this.navigationBean.setCurrentNodeId(getHomeSpaceId());
        return "browse";
    }
    
    public String createSpace()
    {
        this.navigationBean.setCurrentNodeId(getHomeSpaceId());
        return "dialog:createSpace";
    }
    
    public String createContent()
    {
        this.navigationBean.setCurrentNodeId(getHomeSpaceId());
        return "wizard:createContent";
    }
    
    public String addContent()
    {
        this.navigationBean.setCurrentNodeId(getHomeSpaceId());
        return "dialog:addContent";
    }
    
    private String getHomeSpaceId()
    {
        return Application.getCurrentUser(FacesContext.getCurrentInstance()).getHomeSpaceId();
    }
}
