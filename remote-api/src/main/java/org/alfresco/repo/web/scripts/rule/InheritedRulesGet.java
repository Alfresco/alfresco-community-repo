/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.web.scripts.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.web.scripts.rule.ruleset.RuleRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;

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
