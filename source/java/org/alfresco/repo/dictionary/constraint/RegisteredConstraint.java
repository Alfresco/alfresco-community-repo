/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
