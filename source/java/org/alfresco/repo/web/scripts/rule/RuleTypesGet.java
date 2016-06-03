package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RuleTypesGet extends AbstractRuleWebScript
{

    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RuleTypesGet.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get all rule types
        List<RuleType> ruletypes = ruleService.getRuleTypes();

        model.put("ruletypes", ruletypes);

        return model;
    }
}
