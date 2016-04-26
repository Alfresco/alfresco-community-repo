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
public class InheritedRulesGet extends AbstractRuleWebScript
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(InheritedRulesGet.class);

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

        // get all rules (including inherited) filtered by rule type
        List<Rule> inheritedRules = ruleService.getRules(nodeRef, true, ruleType);

        // get owned rules (excluding inherited) filtered by rule type
        List<Rule> ownedRules = ruleService.getRules(nodeRef, false, ruleType);

        // remove owned rules
        inheritedRules.removeAll(ownedRules);

        List<RuleRef> inheritedRuleRefs = new ArrayList<RuleRef>();

        for (Rule rule : inheritedRules)
        {
            inheritedRuleRefs.add(new RuleRef(rule, fileFolderService.getFileInfo(ruleService.getOwningNodeRef(rule))));
        }

        model.put("inheritedRuleRefs", inheritedRuleRefs);

        return model;
    }
}