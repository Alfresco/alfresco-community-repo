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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.tenant.MultiTAdminServiceImpl;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate Tenant attributes (from 'alf_*attribute*' to 'alf_prop_*')
 * 
 * @author janv
 * @since 3.4
 */
public class MigrateAttrTenantsPatch extends AbstractPatch
{
    private Log logger = LogFactory.getLog(this.getClass());
    
    private static final String MSG_SUCCESS = "patch.migrateAttrTenants.result";
    
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
        
        TenantResultHandler handler = new TenantResultHandler();
        patchDAO.migrateOldAttrTenants(handler);
        
        if (handler.total > 0)
        {
            logger.info("Processed "+handler.total+" Tenant attrs in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, handler.total);
        // done
        return msg;
    }
    
    /**
     * Row handler for migrating tenants
     */
    private class TenantResultHandler implements ResultHandler
    {
        private int total = 0;
        
        private TenantResultHandler()
        {
        }
        @SuppressWarnings("unchecked")
        public void handleResult(ResultContext context)
        {
            Map<String, Object> result = (Map<String, Object>)context.getResultObject();
            
            String tenantDomain = (String)result.get("tenantDomain");
            Boolean isEnabled = (Boolean)result.get("isEnabled");
            String rootDir = (String)result.get("rootDir");
            
            Map<String, Serializable> tenantAttributes = new HashMap<String, Serializable>(7);
            tenantAttributes.put(MultiTAdminServiceImpl.TENANT_ATTRIBUTE_ENABLED, isEnabled.booleanValue());
            tenantAttributes.put(MultiTAdminServiceImpl.TENANT_ATTRIBUTE_ROOT_CONTENT_STORE_DIR, rootDir);
            
            attributeService.setAttribute(
                    (Serializable) tenantAttributes,
                    MultiTAdminServiceImpl.TENANTS_ATTRIBUTE_PATH, tenantDomain);
            
            if (logger.isTraceEnabled())
            {
                logger.trace("Set Tenant attr [tenantDomain="+tenantDomain+", isEnabled="+isEnabled+", rootDir="+rootDir+"]");
            }
            
            total++;
            
            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Handled " + total + " tenant attributes");
            }
        }
    }
}
