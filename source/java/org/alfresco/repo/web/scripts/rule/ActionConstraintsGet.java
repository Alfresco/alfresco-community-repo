package org.alfresco.repo.web.scripts.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ParameterConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class ActionConstraintsGet extends AbstractRuleWebScript
{

    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(ActionConstraintsGet.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get request parameters
        String[] names = req.getParameterValues("name");

        List<ParameterConstraint> parameterConstraints = null;

        if (names != null && names.length > 0)
        {
            // filter is present in request
            parameterConstraints = new ArrayList<ParameterConstraint>();

            // find specified parameter constraints
            for (String name : names)
            {
                ParameterConstraint parameterConstraint = actionService.getParameterConstraint(name);

                if (parameterConstraint != null)
                {
                    parameterConstraints.add(parameterConstraint);
                }
            }
        }
        else
        {
            // no filter was provided, return all parameter constraints
            parameterConstraints = actionService.getParameterConstraints();
        }

        model.put("actionConstraints", parameterConstraints);

        return model;
    }
}
