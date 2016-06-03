package org.alfresco.repo.web.scripts.rule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.web.scripts.rule.ruleset.RuleRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class RulePost extends AbstractRuleWebScript
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RulePost.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get request parameters
        NodeRef nodeRef = parseRequestForNodeRef(req);

        Rule rule = null;
        JSONObject json = null;

        try
        {
            // read request json
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            // parse request json
            rule = parseJsonRule(json);

            // check the rule
            checkRule(rule);

            // create rule
            ruleService.saveRule(nodeRef, rule);

            RuleRef ruleRef = new RuleRef(rule, fileFolderService.getFileInfo(ruleService.getOwningNodeRef(rule)));

            model.put("ruleRef", ruleRef);
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }

        return model;
    }
}
