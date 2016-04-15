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
