package org.alfresco.repo.web.scripts.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.rule.ruleset.RuleRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class RulesGet extends AbstractRuleWebScript
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RulesGet.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get request parameters
        NodeRef nodeRef = parseRequestForNodeRef(req);
        String ruleType = req.getParameter("ruleType");

        RuleType type = ruleService.getRuleType(ruleType);

        if (type == null)
        {
            ruleType = null;
        }

        // get all rules (excluding inherited) filtered by rule type
        List<Rule> rules = ruleService.getRules(nodeRef, false, ruleType);

        List<RuleRef> ruleRefs = new ArrayList<RuleRef>();

        for (Rule rule : rules)
        {
            ruleRefs.add(new RuleRef(rule, fileFolderService.getFileInfo(ruleService.getOwningNodeRef(rule))));
        }

        model.put("ruleRefs", ruleRefs);

        return model;
    }
}
