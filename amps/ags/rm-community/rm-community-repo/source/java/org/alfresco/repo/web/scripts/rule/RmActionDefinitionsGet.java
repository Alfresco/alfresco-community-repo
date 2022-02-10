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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get the RM related action definition list.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RmActionDefinitionsGet extends DeclarativeWebScript
{
    private RecordsManagementActionService recordsManagementActionService;
    private ActionService extendedActionService;

    private List<String> whitelistedActions = WhitelistedDMActions.getActionsList();

    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    public void setExtendedActionService(ActionService extendedActionService)
    {
        this.extendedActionService = extendedActionService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        List<RecordsManagementAction> rmActions = recordsManagementActionService.getRecordsManagementActions();
        List<ActionDefinition> actions = extendedActionService.getActionDefinitions();
        Set<ActionDefinition> defs = new HashSet<>(rmActions.size());
        for (RecordsManagementAction action : rmActions)
        {
            if (action.isPublicAction())
            {
                defs.add(action.getRecordsManagementActionDefinition());
            }
        }
        // If there are any DM whitelisted actions for RM add them in the rule actions
        for (ActionDefinition actionDefinition : actions)
        {
            if (whitelistedActions.contains(actionDefinition.getName()))
            {
                defs.add(actionDefinition);
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("actiondefinitions", defs);

        return model;
    }
}
