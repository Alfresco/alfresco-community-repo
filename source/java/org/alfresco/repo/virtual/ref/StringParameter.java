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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.ref;

/**
 * Helper class that has one {@link String} as a value attribute, value that can
 * be retrieved and used in virtualization process.
 * <p>
 * It also provides the possibility of converting the value attribute into a
 * string representation according to {@link Encodings} definition, using
 * provided {@link Stringifier} objects.
 */
public class StringParameter extends ValueParameter<String>
{
    public StringParameter(String value)
    {
        super(value);
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyParameter(this);
    }
}
