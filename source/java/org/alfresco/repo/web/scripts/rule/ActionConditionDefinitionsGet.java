package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class ActionConditionDefinitionsGet extends AbstractRuleWebScript
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(ActionConditionDefinitionsGet.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get all action condition definitions
        List<ActionConditionDefinition> actionconditiondefinitions = actionService.getActionConditionDefinitions();

        model.put("actionconditiondefinitions", actionconditiondefinitions);

        return model;
    }
}
