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

import java.util.Objects;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;

@Experimental
public class Rule
{
    private String id;
    private String name;

    public static Rule from(final org.alfresco.service.cmr.rule.Rule ruleModel) {
        if (ruleModel == null) {
            return null;
        }

        return builder()
                .setId(ruleModel.getNodeRef().getId())
                .setName(ruleModel.getTitle())
                .createRule();
    }

    /**
     * Convert the REST model object to the equivalent service POJO.
     *
     * @param nodes The nodes API.
     * @return The rule service POJO.
     */
    public org.alfresco.service.cmr.rule.Rule toServiceModel(Nodes nodes)
    {
        org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule();
        if (id != null)
        {
            NodeRef nodeRef = nodes.validateOrLookupNode(id, null);
            ruleModel.setNodeRef(nodeRef);
        }
        ruleModel.setTitle(name);
        return ruleModel;
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
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Rule))
        {
            return false;
        }
        Rule rule = (Rule) o;
        return Objects.equals(id, rule.id) &&
                Objects.equals(name, rule.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name);
    }

    @Override
    public String toString()
    {
        return "Rule{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }

    public static RuleBuilder builder()
    {
        return new RuleBuilder();
    }

    /** Builder class. */
    public static class RuleBuilder
    {
        private String id;
        private String name;

        public RuleBuilder setId(String id)
        {
            this.id = id;
            return this;
        }

        public RuleBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public Rule createRule()
        {
            Rule rule = new Rule();
            rule.setId(id);
            rule.setName(name);
            return rule;
        }
    }
}
