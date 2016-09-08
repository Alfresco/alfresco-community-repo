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
package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.aop.framework.ProxyFactoryBean;

public class RMActionProxyFactoryBean extends ProxyFactoryBean
{
    private static final long serialVersionUID = 539749542853266449L;

    protected RuntimeActionService runtimeActionService;

    private RecordsManagementActionService recordsManagementActionService;

    /**
     * Set action service
     * 
     * @param actionService
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

    public void registerAction()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                runtimeActionService.registerActionExecuter((ActionExecuter) getObject());
                recordsManagementActionService.register((RecordsManagementAction) getObject());
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
    }
}
