/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.model;

import java.util.List;

import org.alfresco.service.Experimental;

/**
 * Representation of action parameter constraint.
 * Helps to constraint the list of allowable values for an action parameter.
 * When action parameter has constraints defined (@see ActionDefinition.ParameterDefinition#getParameterConstraintName())
 * they will be listed here.
 *
 * @author mpichura
 */
@Experimental
public class ActionParameterConstraint
{
    /**
     * Constraint name.
     */
    private String constraintName;
    /**
     * List of objects representing constraint values along with additional data
     */
    private List<ConstraintData> constraintValues;

    public List<ConstraintData> getConstraintValues()
    {
        return constraintValues;
    }

    public void setConstraintValues(List<ConstraintData> constraintValues)
    {
        this.constraintValues = constraintValues;
    }

    public String getConstraintName()
    {
        return constraintName;
    }

    public void setConstraintName(String constraintName)
    {
        this.constraintName = constraintName;
    }

    public static class ConstraintData
    {
        public ConstraintData(final String value, final String label)
        {
            this.value = value;
            this.label = label;
        }
        /**
         * Actual constraint value
         */
        private String value;
        /**
         * A label associated to constraint's value
         */
        private String label;

        public String getValue()
        {
            return value;
        }

        public String getLabel()
        {
            return label;
        }
    }
}
