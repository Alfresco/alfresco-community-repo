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

package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.aop.framework.ProxyFactoryBean;

/**
 * RM action proxy factory bean.
 *
 * @author Roy Wetherall
 */
public class RMActionProxyFactoryBean extends ProxyFactoryBean
{
    private static final long serialVersionUID = 539749542853266449L;

    /** Runtime action service */
    protected RuntimeActionService runtimeActionService;

    /** Records management action service */
    protected RecordsManagementActionService recordsManagementActionService;

    /** Records management audit service */
    protected RecordsManagementAuditService recordsManagementAuditService;

    /** transaction service */
    private TransactionService transactionService;

    /**
     * Set action service
     *
     * @param runtimeActionService
     */
    public void setRuntimeActionService(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }

    /**
     * Set records management service
     *
     * @param recordsManagementActionService
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * Set records management service
     *
     * @param recordsManagementAuditService
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    /**
     * @param transactionService    transaction service
     * @since 2.4.a
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Register the action
     */
    public void registerAction()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        RecordsManagementAction action = (RecordsManagementAction)getObject();
                        recordsManagementActionService.register(action);

                        return null;
                    }
                });

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

    }
}
