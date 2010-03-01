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
package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

/**
 * An extractor that supports all values and does not conversion.
 * This implementation can be used as a base class, although there is little
 * abstraction necessary for the converters in general.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class SimpleValueDataExtractor extends AbstractDataExtractor
{
    /**
     * @return          Returns <tt>true</tt> always
     */
    public boolean isSupported(Serializable data)
    {
        return true;
    }

    /**
     * Just returns the value unchanged
     */
    public Serializable extractData(Serializable in) throws Throwable
    {
        return in;
    }
}
