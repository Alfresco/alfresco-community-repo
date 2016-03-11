package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.service.cmr.action.ActionDefinition;
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
        List<RecordsManagementAction> actions = recordsManagementActionService.getRecordsManagementActions();
        Set<ActionDefinition> defs = new HashSet<ActionDefinition>(actions.size());
        for (RecordsManagementAction action : actions)
        {
            if (action.isPublicAction())
            {
                defs.add(action.getRecordsManagementActionDefinition());
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("actiondefinitions", defs);

        return model;
    }
}
