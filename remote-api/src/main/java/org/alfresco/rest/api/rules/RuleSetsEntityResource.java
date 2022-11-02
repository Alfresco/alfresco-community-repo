/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.rules;

import java.util.List;

import org.alfresco.rest.api.RuleSets;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;

@EntityResource(name = "rule-sets", title = "Rule sets")
@Experimental
public class RuleSetsEntityResource implements EntityResourceAction.Create<RuleSet>
{
    private final RuleSets ruleSets;

    public RuleSetsEntityResource(RuleSets ruleSets)
    {
        this.ruleSets = ruleSets;
    }

    @Override
    @WebApiDescription(title="Create default rule set", description="Create the default rule set for a folder")
    @WebApiParam(name="request", title="A single rule set request", description="A single rule set request, multiple entries are not supported.",
            kind= ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false, required = true)
    public List<RuleSet> create(List<RuleSet> request, Parameters parameters)
    {
        if (request.size() != 1)
        {
            throw new IllegalArgumentException("You must specify exactly one entry with folder id in request");
        }
        return List.of(ruleSets.createRuleSet(request.get(0).getId()));
    }
}
