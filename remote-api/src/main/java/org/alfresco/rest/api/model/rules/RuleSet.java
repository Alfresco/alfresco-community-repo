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

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;

@Experimental
public class RuleSet
{
    public static final String DEFAULT_ID = "-default-";

    private String id;
    private NodeRef owningFolder;
    private InclusionType inclusionType;
    private List<NodeRef> inheritedBy;

    public static RuleSet of(String id)
    {
        return builder()
            .id(id)
            .create();
    }

    public static boolean isNotDefaultId(final String id) {
        return !isDefaultId(id);
    }

    public static boolean isDefaultId(final String id) {
        return DEFAULT_ID.equals(id);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public NodeRef getOwningFolder()
    {
        return owningFolder;
    }

    public void setOwningFolder(NodeRef owningFolder)
    {
        this.owningFolder = owningFolder;
    }

    public InclusionType getInclusionType()
    {
        return inclusionType;
    }

    public void setInclusionType(InclusionType inclusionType)
    {
        this.inclusionType = inclusionType;
    }

    public List<NodeRef> getInheritedBy()
    {
        return inheritedBy;
    }

    public void setInheritedBy(List<NodeRef> inheritedBy)
    {
        this.inheritedBy = inheritedBy;
    }

    @Override
    public String toString()
    {
        return "RuleSet{"
                + new StringJoiner(", ")
                    .add("id='" + id + "'")
                    .add("owningFolder='" + owningFolder + "'")
                    .add("inclusionType='" + inclusionType + "'")
                    .add("inheritedBy='" + inheritedBy + "'")
                    .toString()
                + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RuleSet ruleSet = (RuleSet) o;
        return Objects.equals(id, ruleSet.id)
                && Objects.equals(owningFolder, ruleSet.owningFolder)
                && inclusionType == ruleSet.inclusionType
                && Objects.equals(inheritedBy, ruleSet.inheritedBy);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, owningFolder, inclusionType, inheritedBy);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String id;
        private NodeRef owningFolder;
        private InclusionType inclusionType;
        private List<NodeRef> inheritedBy;

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder owningFolder(NodeRef owningFolder)
        {
            this.owningFolder = owningFolder;
            return this;
        }

        public Builder inclusionType(InclusionType inclusionType)
        {
            this.inclusionType = inclusionType;
            return this;
        }

        public Builder inheritedBy(List<NodeRef> inheritedBy)
        {
            this.inheritedBy = inheritedBy;
            return this;
        }

        public RuleSet create()
        {
            final RuleSet ruleSet = new RuleSet();
            ruleSet.setId(id);
            ruleSet.setOwningFolder(owningFolder);
            ruleSet.setInclusionType(inclusionType);
            ruleSet.setInheritedBy(inheritedBy);
            return ruleSet;
        }
    }
}
