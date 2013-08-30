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
package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.repo.policy.PolicyComponent;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Class to represent an audit event
 *
 * @author Gavin Cornwell
 * @author Roy Wetherall
 */
public class AuditEvent
{
	/** Name */
    protected String name;
    
    /** Label */
    protected String label;
    
    protected RecordsManagementAuditService recordsManagementAuditService;    
    
    protected PolicyComponent policyComponent;
    
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
 
    public void init()
    {
        recordsManagementAuditService.registerAuditEvent(this);
    }

    public AuditEvent()
    {     
    }
    
    public AuditEvent(String name, String label)
    {
        this.name = name;
        this.label = label;
    }
    
    /**
     * @return  audit event name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * @param name  audit event name
     */
    public void setName(String name) 
    {
		this.name = name;
	}

    /**
     * @return   audit event label
     */
    public String getLabel()
    {
    	String lookup = I18NUtil.getMessage(label);
    	if (lookup == null)
    	{
    		lookup = label;
    	}
    	return lookup;
    }
    
    /**
     * @param label audit event label
     */
    public void setLabel(String label) 
    {
		this.label = label;
	}
}
