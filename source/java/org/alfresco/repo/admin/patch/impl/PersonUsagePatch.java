/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.patch.PatchDAO.StringHandler;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch to add person usage ('cm:sizeCurrent') property to person (if missing)
 * 
 * @author janv
 */
public class PersonUsagePatch extends AbstractPatch
{
    private static Log logger = LogFactory.getLog(PersonUsagePatch.class);
    
    /** Success messages. */
    private static final String MSG_SUCCESS1 = "patch.personUsagePatch.result1";
    private static final String MSG_SUCCESS2 = "patch.personUsagePatch.result2";
    
    private PatchDAO patchDAO;
    private StoreRef personStoreRef;
    private TenantService tenantService;
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setPersonStoreUrl(String storeUrl)
    {
        this.personStoreRef = new StoreRef(storeUrl);
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    
    @Override
    protected String applyInternal() throws Exception
    {
        logger.info("Checking for people with missing 'cm:sizeCurrent' property ...");
        
        int count = addPersonSizeCurrentProperty();
        
        String msg = null;
        if (count > 0)
        {
            logger.info("... missing 'cm:sizeCurrent' property added to "+count+" people");
            msg = I18NUtil.getMessage(MSG_SUCCESS1, count);
        }
        else
        {
            logger.info("... no people were missing the 'cm:sizeCurrent' property");
            msg = I18NUtil.getMessage(MSG_SUCCESS2);
        }
        
        return msg;
    }
    
    private int addPersonSizeCurrentProperty()
    {
        // get people (users) with missing 'cm:sizeCurrent' property
        
        CountQueryCallback userHandler = new CountQueryCallback();
        
        patchDAO.getUsersWithoutUsageProp(tenantService.getName(personStoreRef), userHandler);
        
        return userHandler.getCount();
    }
    
    private class CountQueryCallback implements StringHandler
    {
        private int count;
        
        public CountQueryCallback()
        {
            count = 0;
        }
        
        public void handle(String uuid)
        {
            nodeService.setProperty(new NodeRef(personStoreRef, uuid), ContentModel.PROP_SIZE_CURRENT, null);
            
            count++;
        }
        
        public int getCount()
        {
            return count;
        }
    };
}
