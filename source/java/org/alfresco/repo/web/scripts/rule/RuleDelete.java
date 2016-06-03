package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class RuleDelete extends AbstractRuleWebScript
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RuleDelete.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        NodeRef nodeRef = parseRequestForNodeRef(req);

        // get request parameters
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String ruleId = templateVars.get("rule_id");

        Rule ruleToDelete = null;

        // get all rules for given nodeRef
        List<Rule> rules = ruleService.getRules(nodeRef, false);

        // filter by rule id
        for (Rule rule : rules)
        {
            if (rule.getNodeRef().getId().equalsIgnoreCase(ruleId))
            {
                ruleToDelete = rule;
                break;
            }
        }

        if (ruleToDelete == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find rule with id: " + ruleId);
        }

        // delete rule
        ruleService.removeRule(nodeRef, ruleToDelete);

        return model;
    }
}
