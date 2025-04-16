/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Update workflow notification templates patch
 * 
 * @author Roy Wetherall
 */
public class UpdateWorkflowNotificationTemplatesPatch extends GenericEMailTemplateUpdatePatch
{
    private static final String[] LOCALES = new String[]{"de", "es", "fr", "it", "ja"};
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
