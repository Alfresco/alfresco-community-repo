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

package org.alfresco.service.cmr.attributes;

import java.util.Map;

/**
 * An implementation of this is passed into an AttrQuery to aid it
 * in generating the actual predicate.
 * @author britt
 */
public interface AttrQueryHelper
{
    /**
     * Get the next integer suffix for named arguments.
     * @return The next integer suffix.
     */
    public int getNextSuffix();
    
    /**
     * As an AttrQuery is generating the predicate, it
     * tells this helper about its parameter names and bindings.
     * @param name The name of the parameter
     * @param value The binding.
     */
    public void setParameter(String name, String value);
    
    /**
     * Get the parameter bindings for a generated predicate.
     * @return The parameter bindings.
     */
    public Map<String, String> getParameters();
}
