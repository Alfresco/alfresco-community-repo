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

package org.alfresco.rest.api.model.rules;

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.CompositeAction;

import java.util.List;
import java.util.stream.Collectors;

@Experimental
public class Rule
{

    private String id;
    private String name;

    public static Rule from(final org.alfresco.service.cmr.rule.Rule ruleModel) {
        if (ruleModel == null) {
            return null;
        }

        final Rule rule = new Rule();
        rule.id = ruleModel.getNodeRef().getId();
        rule.name = ruleModel.getTitle();

        return rule;
    }

    @UniqueId
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Rule{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
