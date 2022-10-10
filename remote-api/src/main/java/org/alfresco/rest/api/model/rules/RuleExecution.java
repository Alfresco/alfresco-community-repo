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
public class RuleExecution
{
    private boolean eachSubFolderIncluded;

    public boolean getIsEachSubFolderIncluded()
    {
        return eachSubFolderIncluded;
    }

    public void setIsEachSubFolderIncluded(boolean eachSubFolderIncluded)
    {
        this.eachSubFolderIncluded = eachSubFolderIncluded;
    }

    @Override
    public String toString()
    {
        return "RuleExecution{" + "eachSubFolderIncluded=" + eachSubFolderIncluded + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RuleExecution that = (RuleExecution) o;
        return eachSubFolderIncluded == that.eachSubFolderIncluded;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eachSubFolderIncluded);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private boolean eachSubFolderIncluded;

        public Builder eachSubFolderIncluded(boolean eachSubFolderIncluded)
        {
            this.eachSubFolderIncluded = eachSubFolderIncluded;
            return this;
        }

        public RuleExecution create()
        {
            final RuleExecution ruleExecution = new RuleExecution();
            ruleExecution.setIsEachSubFolderIncluded(eachSubFolderIncluded);
            return ruleExecution;
        }
    }
}
