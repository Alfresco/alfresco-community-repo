package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Update workflow notification templates patch
 * 
 * @author Roy Wetherall
 */
public class AddDutchEmailTemplatesPatch extends GenericEMailTemplateUpdatePatch
{   
    private static final String[] LOCALES = new String[] {"nl"};    
    private static final String[] PATHS = new String[] 
    {
        "alfresco/templates/activities-email-templates/",
        "alfresco/bootstrap/notification/",
        "alfresco/templates/notify_email_templates/",
        "alfresco/templates/new-user-templates/",
        "alfresco/templates/invite-email-templates/",
        "alfresco/templates/following-email-templates/"
    };    
    private static final String[] BASE_FILES = new String[] 
    {
        "activities-email.ftl",
        "wf-email.html.ftl",
        "notify.htm",
        "new-user-email.html",
        "invite-email.html.ftl",
        "following-email.html.ftl"
    };
    private static final String[] XPATHS = new String[]
    {
        "/app:company_home/app:dictionary/app:email_templates/cm:activities/cm:activities-email.ftl",
        "/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:invite-email.html.ftl",
        "/app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/cm:notify_user_email.html.ftl",
        "/app:company_home/app:dictionary/app:email_templates/cm:invite/cm:new-user-email.html.ftl",
        "/app:company_home/app:dictionary/app:email_templates/cm:invite/cm:invite-email.html.ftl",
        "/app:company_home/app:dictionary/app:email_templates/app:following/cm:following-email.html.ftl"
    };
    
    private int currentIndex = 0;
    
    private Repository repository;
    
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    @Override
    protected String getPath()
    {
        return PATHS[currentIndex];
    }
    
    @Override
    protected String getBaseFileName()
    {
        return BASE_FILES[currentIndex];
    }
    
    @Override
    protected String[] getLocales()
    {
        return LOCALES;
    }
    
    @Override
    protected NodeRef getBaseTemplate()
    {
        List<NodeRef> refs = searchService.selectNodes(
            repository.getRootHome(), 
            XPATHS[currentIndex], 
            null, 
            namespaceService, 
            false);
        if (refs.size() != 1)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage("patch.addDutchEmailTemplatesPatch.error"));
        }
        return refs.get(0);
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {   
        while (currentIndex < BASE_FILES.length)
        {
            updateTemplates();  
            currentIndex ++;
        }        
        
        return I18NUtil.getMessage("patch.addDutchEmailTemplatesPatch.result");
    }
}
