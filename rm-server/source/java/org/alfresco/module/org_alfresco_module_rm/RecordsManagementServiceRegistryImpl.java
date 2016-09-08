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
package org.alfresco.module.org_alfresco_module_rm;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.repo.service.ServiceDescriptorRegistry;

/**
 * Records management service registry implementation
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementServiceRegistryImpl extends ServiceDescriptorRegistry 
                                                  implements RecordsManagementServiceRegistry
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementActionService()
     */
    public RecordsManagementActionService getRecordsManagementActionService()
    {
        return (RecordsManagementActionService)getService(RECORDS_MANAGEMENT_ACTION_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementAdminService()
     */
    public RecordsManagementAdminService getRecordsManagementAdminService()
    {
        return (RecordsManagementAdminService)getService(RECORDS_MANAGEMENT_ADMIN_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementEventService()
     */
    public RecordsManagementEventService getRecordsManagementEventService()
    {
        return (RecordsManagementEventService)getService(RECORDS_MANAGEMENT_EVENT_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementService()
     */
    public RecordsManagementService getRecordsManagementService()
    {
        return (RecordsManagementService)getService(RECORDS_MANAGEMENT_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementSecurityService()
     */
    public RecordsManagementSecurityService getRecordsManagementSecurityService()
    {
        return (RecordsManagementSecurityService)getService(RECORDS_MANAGEMENT_SECURITY_SERVICE);
    }

    /*
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getRecordsManagementAuditService()
     */
    public RecordsManagementAuditService getRecordsManagementAuditService()
    {
        return (RecordsManagementAuditService)getService(RECORDS_MANAGEMENT_AUDIT_SERVICE);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getDictionaryService()
     */
    @Override
    public DispositionService getDispositionService()
    {
        return (DispositionService)getService(DISPOSITION_SERVICE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry#getCapabilityService()
     */
    @Override
    public CapabilityService getCapabilityService()
    {
        return (CapabilityService)getService(CAPABILITY_SERVICE);
    }
}
