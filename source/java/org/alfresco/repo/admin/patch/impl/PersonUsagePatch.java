/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.node.db.NodeDaoService.ObjectArrayQueryCallback;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    
    private NodeDaoService nodeDaoService;
    private StoreRef personStoreRef;
    private TenantService tenantService;
    
    
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
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
        
        CountObjectArrayQueryCallback userHandler = new CountObjectArrayQueryCallback();
        
        nodeDaoService.getUsersWithoutUsageProp(tenantService.getName(personStoreRef), userHandler);
        
        return userHandler.getCount();
    }
    
    private class CountObjectArrayQueryCallback implements ObjectArrayQueryCallback
    {
        private int count;
        
        public CountObjectArrayQueryCallback()
        {
            count = 0;
        }
        
        public boolean handle(Object[] arr)
        {
            String uuid = (String)arr[0];
            
            nodeService.setProperty(new NodeRef(personStoreRef, uuid), ContentModel.PROP_SIZE_CURRENT, null);
            
            count++;
            
            return true; // continue to next node (more required)
        }
        
        public int getCount()
        {
            return count;
        }
    };
}
