package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.action.ParameterConstraint;
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
public class ActionConstraintGet extends AbstractRuleWebScript
{

    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(ActionConstraintGet.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get request parameters
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String name = templateVars.get("name");

        // get specified parameter constraint
        ParameterConstraint parameterConstraint = actionService.getParameterConstraint(name);

        if (parameterConstraint == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find parameter constraint with name: " + name);
        }

        model.put("actionConstraint", parameterConstraint);

        return model;
    }
}
