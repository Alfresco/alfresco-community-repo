/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Declare record action
 *
 * @author Roy Wetherall
 */
public class DeclareRecordAction extends RMActionExecuterAbstractBase
{
    private static final String MISSING_PROPERTIES = "missingProperties";
    
    /** action name */
    public static final String NAME = "declareRecord";

    /** Record service */
    private RecordService recordService;

    /**
     * Sets the record service
     *
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        ParameterCheck.mandatory("actionedUponNodeRef", actionedUponNodeRef);
        try
        {
            recordService.complete(actionedUponNodeRef);
        }
        catch (IntegrityException e)
        {
            Map<String, String> errorMap = getErrorMessageMap();
            if (e.getMsgId().equals(errorMap.get(MISSING_PROPERTIES)))
            {
                action.setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, MISSING_PROPERTIES);
            }
        }

    }

    /**
     * TODO: needs to be properties file
     * Temporary Helper method to get the map of complete record error message keys with associated message text
     * @return errorMap
     */
    private Map<String, String> getErrorMessageMap() {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("missingProperties", "The record has missing mandatory properties.");
        errorMap.put("alreadyCompleted", "The record is already completed.");
        errorMap.put("unsuitableNode", "The node is not a record or the record does not exist or is frozen.");
        return errorMap;
    }
}
