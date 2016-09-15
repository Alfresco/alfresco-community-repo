/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
    public Constraint getRegisteredConstraint()
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
        return getRegisteredConstraint().getType();
    }
    
    /**
     * Defers to the registered constraint
     */
    public String getTitle()
    {
        return getRegisteredConstraint().getTitle();
    }
    
    /**
     * Defers to the registered constraint
     */
    public Map<String, Object> getParameters()
    {
        return getRegisteredConstraint().getParameters();
    }

    /**
     * Defers to the registered constraint
     */
    public void evaluate(Object value)
    {
        getRegisteredConstraint().evaluate(value);
    }
}
