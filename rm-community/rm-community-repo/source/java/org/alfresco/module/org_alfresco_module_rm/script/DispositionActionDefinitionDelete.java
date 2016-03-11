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
