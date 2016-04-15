package org.alfresco.repo.web.scripts.quickshare;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * QuickShare/PublicView
 * 
 * GET web script to get property system.quickshare.enabled value
 * 
 * @author sergey.shcherbovich
 * @since 4.2
 */
public class QuickShareEnabledGet extends AbstractQuickShareContent
{
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        model.put("enabled", isEnabled());
        
        return model;
    }
}