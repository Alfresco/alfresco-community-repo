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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Reject action for an unfiled record
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RejectAction extends RMActionExecuterAbstractBase
{
    /** Message properties */
    private static final String MSG_REJECT_NO_REASON = "rm.action.reject-no-reason";
    private static final String MSG_REJECT_ONLY_UNFILED_RECORDS = "rm.action.reject-only-unfiled-records";

    /** Parameter names */
    public static final String PARAM_REASON = "reason";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        recordService.rejectRecord(actionedUponNodeRef, (String) action.getParameterValue(PARAM_REASON));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#isExecutableImpl(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, boolean)
     */
    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent,
            Map<String, Serializable> parameters, boolean throwException)
    {
        if (recordService.isRecord(filePlanComponent) == true && recordService.isFiled(filePlanComponent) == false)
        {
            if (parameters != null && StringUtils.isNotBlank((String) parameters.get(PARAM_REASON)))
            {
                return true;
            }
            else
            {
                if (throwException)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_REJECT_NO_REASON));
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            if (throwException)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_REJECT_ONLY_UNFILED_RECORDS));
            }
            else
            {
                return false;
            }
        }
    }
}
