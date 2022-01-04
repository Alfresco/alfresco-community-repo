/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
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
    private boolean auditable = true;

    /** Indicates whether the action is audited immediately or not */
    private boolean auditedImmediately = false;

    /** Application context */
    private ApplicationContext applicationContext;

    /** Records management audit service */
    private RecordsManagementAuditService auditService;

    /**
     * @return True if auditable, false otherwise
     */
    protected boolean isAuditable()
    {
        return this.auditable;
    }

    /**
     * @return True if audited immediately, false otherwise
     */
    protected boolean isAuditedImmediately()
    {
        return this.auditedImmediately;
    }

    /**
     * @return Application context
     */
    protected ApplicationContext getApplicationContext()
    {
        return this.applicationContext;
    }

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
    public void setApplicationContext(ApplicationContext applicationContext)
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
            auditService = (RecordsManagementAuditService) getApplicationContext().getBean("recordsManagementAuditService");
        }
        return auditService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#init()
     */
    @Override
    public void init()
    {
        if (!(this instanceof RecordsManagementAction))
        {
            super.init();
        }

        if (isAuditable())
        {
            // get the details of the action
            String name = getActionDefinition().getName();
            String title = getActionDefinition().getTitle();
            if (title == null || title.isEmpty())
            {
                // default to name if no title available
                title = name;
            }

            // register audit event
            getAuditService().registerAuditEvent(name, title);
        }
    }

	/**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#execute(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {
        // audit the execution of the action
        if (isAuditable())
        {
            if (isAuditedImmediately())
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
