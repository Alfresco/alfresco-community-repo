package org.alfresco.service.cmr.activities;

import java.io.Serializable;

public class FeedControl implements Serializable
{    
    private static final long serialVersionUID = -1934566916718472843L;
    
    private String siteId;
    private String appToolId;
    
    
    public FeedControl(String siteId, String appToolId)
    {
        if (siteId == null)
        {
            siteId = "";
        }
        this.siteId = siteId;
        
        if (appToolId == null)
        {
            appToolId = "";
        }
        this.appToolId = appToolId;
    }
    
    public String getSiteId()
    {
        return this.siteId;
    }
    
    public String getAppToolId()
    {
        return this.appToolId;
    }
}
