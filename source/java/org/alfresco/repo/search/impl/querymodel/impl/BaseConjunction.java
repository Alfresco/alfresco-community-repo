/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.querymodel.impl;

import java.util.List;

import org.alfresco.repo.search.impl.querymodel.Conjunction;
import org.alfresco.repo.search.impl.querymodel.Constraint;

/**
 * @author andyh
 *
 */
public class BaseConjunction extends BaseConstraint implements Conjunction
{

    private List<Constraint> constraints;

    public BaseConjunction(List<Constraint> constraints)
    {
        this.constraints = constraints;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Conjunction#getConstraints()
     */
    public List<Constraint> getConstraints()
    {
        return constraints;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Constraint#evaluate()
     */
    public boolean evaluate()
    {
        throw new UnsupportedOperationException();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseConjunction[");
        builder.append("constraints=").append(getConstraints());
        builder.append("]");
        return builder.toString();
    }
}
