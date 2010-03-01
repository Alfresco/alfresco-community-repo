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

import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Multiplicity;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class BaseArgumentDefinition implements ArgumentDefinition
{
    private Multiplicity multiplicity;

    private String name;
    
    private QName type;
    
    private boolean mandatory;
    
    public BaseArgumentDefinition(Multiplicity multiplicity, String name, QName type, boolean mandatory)
    {
        this.multiplicity = multiplicity;
        this.name = name;
        this.type = type;
        this.mandatory = mandatory;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.ArgumentDefinition#getMutiplicity()
     */
    public Multiplicity getMutiplicity()
    {
        return multiplicity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.ArgumentDefinition#getName()
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.ArgumentDefinition#getType()
     */
    public QName getType()
    {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.ArgumentDefinition#isMandatory()
     */
    public boolean isMandatory()
    {
       return mandatory;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseArgumentDefinition[");
        builder.append("name=").append(getName()).append(", ");
        builder.append("multiplicity=").append(getMutiplicity()).append(", ");
        builder.append("mandatory=").append(isMandatory()).append(", ");
        builder.append("type=").append(getType());
        builder.append("] ");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BaseArgumentDefinition other = (BaseArgumentDefinition) obj;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }
 
    
}
