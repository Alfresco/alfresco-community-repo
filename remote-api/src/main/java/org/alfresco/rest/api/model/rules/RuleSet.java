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

import org.alfresco.service.Experimental;

@Experimental
public class RuleSet
{
    public static final String DEFAULT_ID = "-default-";

    private String id;

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

    @Override
    public String toString()
    {
        return "RuleSet{" + "id='" + id + '\'' + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RuleSet ruleSet = (RuleSet) o;
        return Objects.equals(id, ruleSet.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String id;

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public RuleSet create()
        {
            final RuleSet ruleSet = new RuleSet();
            ruleSet.setId(id);
            return ruleSet;
        }
    }
}
