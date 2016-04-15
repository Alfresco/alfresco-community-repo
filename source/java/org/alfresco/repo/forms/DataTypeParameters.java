/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.forms;

/**
 * Interface definition for an object used to represent any restrictions 
 * a data type may enforce.
 *
 * @author Gavin Cornwell
 */
public interface DataTypeParameters
{
    /**
     * Returns the parameters in a Java friendly manner i.e. as an Object.
     * The Object can be as complex as a multiple nested Map of Maps or as
     * simple as a String.
     * 
     * @return An Object representing the data type parameters
     */
    public Object getAsObject();
    
    /**
     * Returns the parameters represented as JSON.
     * <p>
     * Implementations can use whatever JSON libraries they
     * desire, the only rule is that the object returned must
     * toString() to either a JSON array or JSON object i.e.
     * [...] or {...}
     * </p>
     * 
     * @return JSON Object representing the parameters
     */
    public Object getAsJSON();
}
