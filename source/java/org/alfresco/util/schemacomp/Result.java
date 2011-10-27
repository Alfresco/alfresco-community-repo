/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp;

/**
 * Base class for the result of a differencing or validation operation.
 *  
 * @author Matt Ward
 */
public class Result
{
    public enum Strength { WARN, ERROR };
    protected final Strength strength;
    
    /**
     * @param strength
     */
    public Result(Strength strength)
    {
        this.strength = (strength != null ? strength : Strength.ERROR);
    }

    /**
     * @return the strength
     */
    public Strength getStrength()
    {
        return this.strength;
    }
}
