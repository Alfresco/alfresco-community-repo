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

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to delete a dispostion action definition.
 *
 * @author Gavin Cornwell
 */
public class DispositionActionDefinitionDelete extends DispositionAbstractBase
{
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // parse the request to retrieve the schedule object
        DispositionSchedule schedule = parseRequestForSchedule(req);

        // parse the request to retrieve the action definition object
        DispositionActionDefinition actionDef = parseRequestForActionDefinition(req, schedule);

        // remove the action definition from the schedule
        removeDispositionActionDefinitions(schedule, actionDef);

        // return the disposition schedule model
        return getDispositionScheduleModel(req);
    }

    /**
     * Helper method to remove a disposition action definition and the following definition(s)
     *
     * @param schedule The disposition schedule
     * @param actionDef The disposition action definition
     */
    private void removeDispositionActionDefinitions(DispositionSchedule schedule, DispositionActionDefinition actionDef)
    {
        int index = actionDef.getIndex();
        List<DispositionActionDefinition> dispositionActionDefinitions = schedule.getDispositionActionDefinitions();
        for (DispositionActionDefinition dispositionActionDefinition : dispositionActionDefinitions)
        {
            if (dispositionActionDefinition.getIndex() >= index)
            {
                getDispositionService().removeDispositionActionDefinition(schedule, dispositionActionDefinition);
            }
        }
    }
}
