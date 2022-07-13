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
import java.util.Map;

import static org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY;
import static org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator.PARAM_OPERATION;
import static org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator.PARAM_VALUE;

public class SimpleCondition
{

    private String field;
    private String comparator;
    private String parameter;

    public static SimpleCondition from(final Map<String, Serializable> parameters) {
        if (parameters == null) {
            return null;
        }

        final SimpleCondition simpleCondition = new SimpleCondition();
        if (parameters.get(PARAM_CONTENT_PROPERTY) != null)
        {
            simpleCondition.field = parameters.get(PARAM_CONTENT_PROPERTY).toString().toLowerCase();
        } else {
            simpleCondition.field = parameters.get(PARAM_OPERATION).toString();
        }
        simpleCondition.comparator = parameters.get(PARAM_OPERATION).toString();
        simpleCondition.parameter = parameters.get(PARAM_VALUE).toString();

        return simpleCondition;
    }

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
}
