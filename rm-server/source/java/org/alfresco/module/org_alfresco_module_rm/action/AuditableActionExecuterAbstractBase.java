/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Auditable action executer abstract base
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class AuditableActionExecuterAbstractBase extends ActionExecuterAbstractBase implements ApplicationContextAware
{
    /** Indicates whether the action is auditable or not */
    protected boolean auditable = true;

    /** Indicates whether the action is audited immediately or not */
    protected boolean auditedImmediately = false;

    /** Application context */
    protected ApplicationContext applicationContext;
    
    /** Records management audit service */
    private RecordsManagementAuditService auditService;

    /**
     * @param auditable true if auditable, false otherwise
     */
    public void setAuditable(boolean auditable)
    {
        this.auditable = auditable;
    }

    /**
     * @param auditedImmediately true if to be audited immediately, false to be audited after transaction commits
     */
    public void setAuditedImmediately(boolean auditedImmediately)
    {
        this.auditedImmediately = auditedImmediately;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * @return  records management audit service
     */
    private RecordsManagementAuditService getAuditService()
    {
        if (auditService == null)
        {
            auditService = (RecordsManagementAuditService)applicationContext.getBean("recordsManagementAuditService");
        }
        return auditService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#init()
     */
    @Override
    public void init()
    {
        if (this instanceof RecordsManagementAction == false)
        {
            super.init();
        }
            
        if (auditable == true)
        {
            getAuditService().registerAuditEvent(this.getActionDefinition().getName(), this.getActionDefinition().getTitle());
        }
    }
    
	/**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#execute(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {
        // audit the execution of the action
        if (auditable == true)
        {
            if (auditedImmediately == true)
            {
                // To be audited immediately before the action is executed, eg. to audit before actionedUponNodeRef gets deleted during the execution.
                getAuditService().auditEvent(actionedUponNodeRef, this.getActionDefinition().getName(), null, null, true);
            }
            else
            {
                // To be stacked up with other audit entries and audited after the transaction commits.
                getAuditService().auditEvent(actionedUponNodeRef, this.getActionDefinition().getName());
            }
        }

        // execute the action
        super.execute(action, actionedUponNodeRef);
    }
}
