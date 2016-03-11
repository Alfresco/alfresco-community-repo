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

        List<ActionConditionDefinition> defs = new ArrayList<ActionConditionDefinition>(dmDefs.size()+conditions.size());
        defs.addAll(dmDefs);
        for (RecordsManagementActionCondition condition: conditions)
        {
            if (condition.isPublicCondition())
            {
                defs.add(condition.getRecordsManagementActionConditionDefinition());
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("actionconditiondefinitions", defs);

        return model;
    }
}
