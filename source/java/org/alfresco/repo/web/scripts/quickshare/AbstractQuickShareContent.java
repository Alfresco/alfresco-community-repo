package org.alfresco.repo.web.scripts.quickshare;

import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;


/**
 * QuickShare/PublicView
 * 
 * @author janv
 * @since Cloud/4.2
 */
public abstract class AbstractQuickShareContent extends DeclarativeWebScript
{
    protected QuickShareService quickShareService;
    
    private boolean enabled = true;
    
    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    protected boolean isEnabled()
    {
        return this.enabled;
    }
}