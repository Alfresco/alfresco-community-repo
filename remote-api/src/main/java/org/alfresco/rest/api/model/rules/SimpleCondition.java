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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.HasTagEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class SimpleCondition
{
    private String field;
    private String comparator;
    private String parameter;

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getComparator()
    {
        return comparator;
    }

    public void setComparator(String comparator)
    {
        this.comparator = comparator;
    }

    public String getParameter()
    {
        return parameter;
    }

    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    @Override
    public String toString()
    {
        return "SimpleCondition{" + "field='" + field + '\'' + ", comparator='" + comparator + '\'' + ", parameter='" + parameter + '\'' + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SimpleCondition that = (SimpleCondition) o;
        return Objects.equals(field, that.field) && Objects.equals(comparator, that.comparator) && Objects.equals(parameter, that.parameter);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field, comparator, parameter);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String field;
        private String comparator;
        private String parameter;

        public Builder field(String field)
        {
            this.field = field;
            return this;
        }

        public Builder comparator(String comparator)
        {
            this.comparator = comparator;
            return this;
        }

        public Builder parameter(String parameter)
        {
            this.parameter = parameter;
            return this;
        }

        public SimpleCondition create() {
            final SimpleCondition condition = new SimpleCondition();
            condition.setField(field);
            condition.setComparator(comparator);
            condition.setParameter(parameter);
            return condition;
        }
    }
}
