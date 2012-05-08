/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.tenant.MultiTAdminServiceImpl;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.AttributeService.AttributeQueryCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate Tenant attributes to table (alf_tenants)
 * 
 * @author janv
 * @since 4.0
 */
public class MigrateTenantsFromAttrsToTablePatch extends AbstractPatch
{
    private Log logger = LogFactory.getLog(this.getClass());
    
    private static final String MSG_SUCCESS = "patch.migrateTenantsFromAttrsToTable.result";
    
    private AttributeService attributeService;
    private TenantAdminDAO tenantAdminDAO;
    
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }
    
    public void setTenantAdminDAO(TenantAdminDAO tenantAdminDAO)
    {
        this.tenantAdminDAO = tenantAdminDAO;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        final List<TenantEntity> tenantsToMigrate = new ArrayList<TenantEntity>(10);
        
        attributeService.getAttributes(new AttributeQueryCallback()
        {
            public boolean handleAttribute(Long id, Serializable value, Serializable[] keys)
            {
                if ((keys.length >= 2) && (value != null))
                {
                    String tenantDomain = (String)keys[1];
                    
                    @SuppressWarnings("unchecked")
                    Map<String,Serializable> valueMap = (Map<String,Serializable>)value;
                    
                    Boolean isEnabled = (Boolean)valueMap.get(MultiTAdminServiceImpl.TENANT_ATTRIBUTE_ENABLED);
                    String rootDir = (String)valueMap.get(MultiTAdminServiceImpl.TENANT_ATTRIBUTE_ROOT_CONTENT_STORE_DIR);
                    
                    TenantEntity tenantEntity = new TenantEntity(new String(tenantDomain));
                    tenantEntity.setEnabled((isEnabled != null ? isEnabled : false));
                    tenantEntity.setContentRoot(new String(rootDir));
                    
                    TenantEntity tenantEntityCreated = tenantAdminDAO.createTenant(tenantEntity);
                    tenantsToMigrate.add(tenantEntityCreated);
                    
                    if (logger.isInfoEnabled())
                    {
                        logger.info("... migrated: "+tenantEntityCreated);
                    }
                }
                
                return true;
            }
        },
        MultiTAdminServiceImpl.TENANTS_ATTRIBUTE_PATH);
        
        int tenantCount = tenantsToMigrate.size();
        if (tenantCount > 0)
        {
            for (TenantEntity tenantEntity : tenantsToMigrate)
            {
                attributeService.removeAttribute(MultiTAdminServiceImpl.TENANTS_ATTRIBUTE_PATH, tenantEntity.getTenantDomain());
            }
            
            final List<String> checkTenants = new ArrayList<String>(10);
            attributeService.getAttributes(new AttributeQueryCallback()
            {
                public boolean handleAttribute(Long id, Serializable value, Serializable[] keys)
                {
                    if ((keys.length >= 2) && (value != null))
                    {
                        String tenantDomain = (String)keys[1];
                        logger.error("Unexpected tenant attribute: "+tenantDomain+" [not migrated/removed]");
                        
                        checkTenants.add(tenantDomain);
                    }
                    
                    return true;
                }
            },
            MultiTAdminServiceImpl.TENANTS_ATTRIBUTE_PATH);
            
            if (checkTenants.size() > 0)
            {
                throw new AlfrescoRuntimeException("Incomplete migration of tenant attributes to tenant table: "+checkTenants.size());
            }
            
            tenantAdminService.startTenants();
            
            logger.info("Processed "+tenantCount+" Tenant attrs in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, tenantCount);
        // done
        return msg;
    }
}
