/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * This class is responsible for initialising any Classification-specific data on server bootstrap.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceBootstrap extends AbstractLifecycleBean
{
    private final AuthenticationUtil        authenticationUtil;
    private final ClassificationServiceImpl classificationServiceImpl;
    private final TransactionService        transactionService;

    public ClassificationServiceBootstrap(AuthenticationUtil authUtil,
                                          ClassificationServiceImpl cService,
                                          TransactionService txService)
    {
        this.authenticationUtil    = authUtil;
        this.classificationServiceImpl = cService;
        this.transactionService    = txService;
    }

    @Override protected void onBootstrap(ApplicationEvent event)
    {
        authenticationUtil.runAsSystem(new org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute()
                    {
                        classificationServiceImpl.initConfiguredClassificationLevels();
                        classificationServiceImpl.initConfiguredClassificationReasons();
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);
                return null;
            }
        });
    }

    @Override protected void onShutdown(ApplicationEvent event)
    {
        // Intentionally empty.
    }
}
