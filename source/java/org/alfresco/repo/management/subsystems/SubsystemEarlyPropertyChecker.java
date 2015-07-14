/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
     * Check if a subsystem property is valid.
     * @param propertyName
     * @param propertyValue
     * @throws InvalidPropertyValueException 
     */
    void checkPropertyValue(String propertyName, String propertyValue) throws InvalidPropertyValueException;
}
