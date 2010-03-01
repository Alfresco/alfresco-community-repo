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
package org.alfresco.repo.dictionary.constraint;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.DictionaryException;

/**
 * Constraint implementation that defers to constraints registered with the
 * static instance of the {@link ConstraintRegistry}.
 * 
 * @see #setAllowedValues(List)
 * @see #setCaseSensitive(boolean)
 * 
 * @author Derek Hulley
 */
public final class RegisteredConstraint implements Constraint
{
    private static final String ERR_NAME_NOT_REGISTERED = "d_dictionary.constraint.registered.not_registered";

    private String shortName;
    private String registeredName;
    
    public RegisteredConstraint()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("RegisteredConstraint")
          .append("[ registeredName=").append(registeredName)
          .append(", constraint=").append(ConstraintRegistry.getInstance().getConstraint(registeredName))
          .append("]");
        return sb.toString();
    }

    public String getShortName()
    {
        return shortName;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    /**
     * Set the name of the constraint that will be used to look up the constraint
     * that will be delegated to.
     */
    public void setRegisteredName(String registeredName)
    {
        this.registeredName = registeredName;
    }

    public void initialize()
    {
        if (registeredName == null)
        {
            throw new DictionaryException(AbstractConstraint.ERR_PROP_NOT_SET, "registeredName");
        }
    }

    /**
     * @return      the constraint that matches the registered name
     */
    private Constraint getConstraint()
    {
        Constraint constraint = ConstraintRegistry.getInstance().getConstraint(registeredName);
        if (constraint == null)
        {
            throw new DictionaryException(ERR_NAME_NOT_REGISTERED, registeredName);
        }
        return constraint;
    }
    
    /**
     * Defers to the registered constraint
     */
    public String getType()
    {
        return getConstraint().getType();
    }
    
    /**
     * Defers to the registered constraint
     */
    public String getTitle()
    {
        return getConstraint().getTitle();
    }
    
    /**
     * Defers to the registered constraint
     */
    public Map<String, Object> getParameters()
    {
        return getConstraint().getParameters();
    }

    /**
     * Defers to the registered constraint
     */
    public void evaluate(Object value)
    {
        getConstraint().evaluate(value);
    }
}
