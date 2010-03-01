/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;

/**
 * This creates web project tables for AVMLockingService as needed.
 * @author britt
 */
public class AVMLockingPatch extends AbstractPatch
{
    private static final String STORE = "workspace://SpacesStore";
    private static final String MSG_SUCCESS = "patch.AVMLocking.result";

    private AVMLockingService fLockingService;
    
    public void setAvmLockingService(AVMLockingService service)
    {
        fLockingService = service;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        ResultSet results =
            searchService.query(new StoreRef(STORE), "lucene", "TYPE:\"wca:webfolder\"");
        try
        {
            for (NodeRef nodeRef : results.getNodeRefs())
            {
                String webProject = (String)nodeService.getProperty(nodeRef, WCMAppModel.PROP_AVMSTORE);
                fLockingService.addWebProject(webProject);
            }
        }
        finally
        {
            results.close();
        }
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
}
