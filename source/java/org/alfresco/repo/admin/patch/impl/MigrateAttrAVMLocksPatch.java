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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.avm.locking.AVMLockingServiceImpl;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate AVM lock attributes (from 'alf_*attribute*' to 'alf_prop_*')
 * 
 * @author janv
 * @since 3.4
 */
public class MigrateAttrAVMLocksPatch extends AbstractPatch
{
    private Log logger = LogFactory.getLog(this.getClass());
    
    private static final String MSG_SUCCESS = "patch.migrateAttrAVMLocks.result";
    
    private AttributeService attributeService;
    private PatchDAO patchDAO;
    
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        AVMLockResultHandler handler = new AVMLockResultHandler();
        patchDAO.migrateOldAttrAVMLocks(handler);
        
        if (handler.total > 0)
        {
            logger.info("Processed "+handler.total+" AVM Lock attrs in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, handler.total);
        // done
        return msg;
    }
    
    /**
     * Row handler for migrating AVM Locks
     */
    private class AVMLockResultHandler implements ResultHandler
    {
        private int total = 0;
        
        private AVMLockResultHandler()
        {
        }
        @SuppressWarnings("unchecked")
        public void handleResult(ResultContext context)
        {
            Map<String, Object> result = (Map<String, Object>)context.getResultObject();
            
            String wpStoreId = (String)result.get("wpStoreId");
            String path = (String)result.get("relPath");
            String avmStore = (String)result.get("avmStore");
            String lockOwner = (String)result.get("owner1");
            
            String relPath = AVMLockingServiceImpl.normalizePath(path);
            
            HashMap<String, String> lockData = new HashMap<String, String>(2);
            lockData.put(AVMLockingServiceImpl.KEY_LOCK_OWNER, lockOwner);
            lockData.put(WCMUtil.LOCK_KEY_STORE_NAME, avmStore);

            if (!attributeService.exists(AVMLockingServiceImpl.KEY_AVM_LOCKS, wpStoreId, relPath))
            {
                attributeService.createAttribute(lockData, AVMLockingServiceImpl.KEY_AVM_LOCKS, wpStoreId, relPath);
                if (logger.isTraceEnabled())
                {
                    logger.trace("Set AVM Lock attr [wpStoreId=" + wpStoreId + ", relPath=" + relPath + ", lockOwner=" + lockOwner + ", avmStore=" + avmStore + "]");
                }
                total++;
            }
            else
            {
                logger.warn("'" + path + "' path has duplicates in normalized form. AVM lock unique attribute creation has been skipped");
            }

            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Handled " + total + " AVM Lock attributes");
            }
        }
    }
}
