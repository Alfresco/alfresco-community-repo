/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.dashboard;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.NavigationBean;

/**
 * Backing bean for the Getting Started Dashlet page.
 * 
 * @author Kevin Roast
 */
public class GettingStartedBean
{
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
        return "addContent";
    }
    
    private String getHomeSpaceId()
    {
        return Application.getCurrentUser(FacesContext.getCurrentInstance()).getHomeSpaceId();
    }
}
