/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Update following email templates patch
 * 
 * @author Roy Wetherall
 */
public class UpdateFollowingEmailTemplatesPatch extends GenericEMailTemplateUpdatePatch
{  
    private Repository repository;
    
    private static final String PATH = "alfresco/templates/following-email-templates/";
    private static final String BASE_FILE = "following-email.html.ftl";
    private static final String XPATH = "/app:company_home/app:dictionary/app:email_templates/app:following/cm:following-email.html.ftl";
    
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
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
    protected NodeRef getBaseTemplate()
    {
        List<NodeRef> refs = searchService.selectNodes(
                repository.getRootHome(), 
                XPATH, 
                null, 
                namespaceService, 
                false);
        if (refs.size() != 1)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage("patch.updateFollowingEmailTemplatesPatch.error"));
        }
        return refs.get(0);
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {   
        updateTemplates();        
        return I18NUtil.getMessage("patch.updateFollowingEmailTemplatesPatch.result");
    }
}
