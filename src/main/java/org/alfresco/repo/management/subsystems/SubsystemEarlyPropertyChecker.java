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
package org.alfresco.repo.management.subsystems;

/**
 * Interface that describes an "early" checker for subsystem properties.
 * Useful when we want to check that a property value is valid before stopping / starting the subsystem.
 * 
 * @author abalmus
 */
public interface SubsystemEarlyPropertyChecker
{
    /**
     * Get the (optional) paired property name (e.g., if we want to check a port
     * number we might want to do that together with a specific local address).
     * 
     * @return The paired property name.
     */
    String getPairedPropertyName();
    
    /**
     * Check if a subsystem property is valid.
     * @param propertyName
     * @param propertyValue
     * @param pairedPropertyValue
     * @throws InvalidPropertyValueException 
     */
    void checkPropertyValue(String propertyName, String propertyValue, String pairedPropertyValue) throws InvalidPropertyValueException;
}
