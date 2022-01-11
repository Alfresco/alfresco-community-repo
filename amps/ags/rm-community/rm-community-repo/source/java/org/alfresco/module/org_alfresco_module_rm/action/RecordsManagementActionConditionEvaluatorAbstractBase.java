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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Records management action condition evaluator abstract base implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class RecordsManagementActionConditionEvaluatorAbstractBase extends ActionConditionEvaluatorAbstractBase
                                                                            implements RecordsManagementActionCondition,
                                                                                       BeanNameAware
{
    /** records management action service */
    private RecordsManagementActionService recordsManagementActionService;

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** bean name */
    private String beanName;

    /** public condition */
    private boolean publicCondition = true;

    private RetryingTransactionHelper retryingTransactionHelper;

    /**
     * @return Records management action service
     */
    protected RecordsManagementActionService getRecordsManagementActionService()
    {
        return this.recordsManagementActionService;
    }

    /**
     * @return File plan service
     */
    protected FilePlanService getFilePlanService()
    {
        return this.filePlanService;
    }

    /**
     * @param recordsManagementActionService    records management action service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @param retryingTransactionHelper retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#init()
     */
    @Override
    public void init()
    {
        // override to prevent condition being registered with the core action service

        // run the following code as System
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute()
                    {
                        getRecordsManagementActionService().register(RecordsManagementActionConditionEvaluatorAbstractBase.this);

                        return null;
                    }
                };

                retryingTransactionHelper.doInTransaction(callback);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

    }

    @Override
    public void setPublicCondition(boolean publicCondition)
    {
        this.publicCondition = publicCondition;
    }

    /**
     * @see org.alfresco.repo.action.CommonResourceAbstractBase#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name)
    {
        this.beanName = name;
        super.setBeanName(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionCondition#getBeanName()
     */
    public String getBeanName()
    {
        return this.beanName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getLabel()
     */
    public String getLabel()
    {
        return getActionConditionDefintion().getTitle();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getDescription()
     */
    public String getDescription()
    {
        return getActionConditionDefintion().getDescription();
    }

    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#getActionConditionDefintion()
     *
     * TODO base class should provide "createActionDefinition" method that can be over-ridden like the ActionExecuter
     * base class to prevent duplication of code and a cleaner extension.
     */
    @Override
    public ActionConditionDefinition getActionConditionDefintion()
    {
        if (this.actionConditionDefinition == null)
        {
            this.actionConditionDefinition = new RecordsManagementActionConditionDefinitionImpl(beanName);
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setTitleKey(getTitleKey());
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setDescriptionKey(getDescriptionKey());
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setConditionEvaluator(beanName);
            ((RecordsManagementActionConditionDefinitionImpl)actionConditionDefinition).setLocalizedParameterDefinitions(getLocalizedParameterDefinitions());
        }
        return this.actionConditionDefinition;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> arg0)
    {
        // No param implementation by default
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionCondition#isPublicCondition()
     */
    @Override
    public boolean isPublicCondition()
    {
        return publicCondition;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionCondition#getRecordsManagementActionConditionDefinition()
     */
    @Override
    public RecordsManagementActionConditionDefinition getRecordsManagementActionConditionDefinition()
    {
        return (RecordsManagementActionConditionDefinition)getActionConditionDefintion();
    }

}
