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

import java.util.Map;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizer;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate Chaining User Registry Synchronizer attributes (from 'alf_*attribute*' to 'alf_prop_*')
 * 
 * @author janv
 * @since 3.4
 */
public class MigrateAttrChainingURSPatch extends AbstractPatch
{
    private Log logger = LogFactory.getLog(this.getClass());
    
    private static final String MSG_SUCCESS = "patch.migrateAttrChainingURS.result";
    
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
        
        ChainingURSResultHandler handler = new ChainingURSResultHandler();
        patchDAO.migrateOldAttrChainingURS(handler);
        
        if (handler.total > 0)
        {
            logger.info("Processed "+handler.total+" Chaining URS attrs in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, handler.total);
        // done
        return msg;
    }
    
    private class ChainingURSResultHandler implements ResultHandler
    {
        private int total = 0;
        
        private ChainingURSResultHandler()
        {
        }
        @SuppressWarnings("unchecked")
        public void handleResult(ResultContext context)
        {
            Map<String, Object> result = (Map<String, Object>)context.getResultObject();
            
            String label = (String)result.get("label");
            String zoneId = (String)result.get("zoneId");
            Long lastModified = (Long)result.get("lastModified");
            
            attributeService.setAttribute(
                    lastModified,
                    ChainingUserRegistrySynchronizer.ROOT_ATTRIBUTE_PATH, label, zoneId);
            
            if (logger.isTraceEnabled())
            {
                logger.trace("Set Chaining URS attr [label="+label+", zoneId="+zoneId+", lastModified="+lastModified+"]");
            }
            
            total++;
            
            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Handled " + total + " Chaining URS attributes");
            }
        }
    }
}
