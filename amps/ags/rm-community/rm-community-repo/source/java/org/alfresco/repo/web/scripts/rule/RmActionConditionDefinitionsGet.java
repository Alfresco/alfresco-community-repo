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

package org.alfresco.repo.web.scripts.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionCondition;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get the RM related action condition definition list.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RmActionConditionDefinitionsGet extends DeclarativeWebScript
{
    private ActionService actionService;

    private RecordsManagementActionService recordsManagementActionService;

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        List<ActionConditionDefinition> dmDefs = actionService.getActionConditionDefinitions();
        List<RecordsManagementActionCondition> conditions = recordsManagementActionService.getRecordsManagementActionConditions();

        List<ActionConditionDefinition> defs = new ArrayList<>(dmDefs.size() + conditions.size());
        defs.addAll(dmDefs);
        for (RecordsManagementActionCondition condition: conditions)
        {
            if (condition.isPublicCondition())
            {
                defs.add(condition.getRecordsManagementActionConditionDefinition());
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("actionconditiondefinitions", defs);

        return model;
    }
}
