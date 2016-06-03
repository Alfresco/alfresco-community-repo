package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Update workflow notification templates patch
 * 
 * @author Roy Wetherall
 */
public class UpdateWorkflowNotificationTemplatesPatch extends GenericEMailTemplateUpdatePatch
{  
    private static final String[] LOCALES = new String[] {"de", "es", "fr", "it", "ja"};
    private static final String PATH = "alfresco/bootstrap/notification/";
    private static final String BASE_FILE = "wf-email.html.ftl";
    
    @Override
    protected String getPath()
    {
        return PATH;
    }
    
    @Override
    protected String getBaseFileName()
    {
        return BASE_FILE;
    }
    
    @Override
    protected String[] getLocales()
    {
        return LOCALES;
    }
    
    @Override
    protected NodeRef getBaseTemplate()
    {
        return new NodeRef(WorkflowNotificationUtils.WF_ASSIGNED_TEMPLATE);
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {   
        updateTemplates();        
        return I18NUtil.getMessage("patch.updateWorkflowNotificationTemplates.result");
    }
}
