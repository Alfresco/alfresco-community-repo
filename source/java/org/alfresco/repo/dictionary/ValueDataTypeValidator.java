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

package org.alfresco.repo.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;

/**
 * A generic validator to validate string value against the target data type.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public interface ValueDataTypeValidator
{
    /**
     * Validates the given {@code value} against the given {@code dataType}.
     * <p>The supplied {@code dataType} must be a valid {@link QName} prefixed
     * string so it can be resolved into a fully qualified name data type
     * registered in the {@link DataTypeDefinition}.
     * 
     * @param dataType the target prefixed data type (e.g. {@code d:int}) which
     *            the string value should be converted to
     * @param value non-empty string value
     * @throws InvalidQNameException if the <tt>dataType</tt> is not a valid
     *             {@link QName} prefixed string (<b>{@code prefix:type} </b>)
     * @throws NamespaceException if the prefix of the given <tt>dataType</tt>
     *             is not mapped to a namespace URI
     * @throws AlfrescoRuntimeException if the given value cannot be converted
     *             into the given data type, or the data type is unknown
     */
    public void validateValue(String dataType, String value) throws InvalidQNameException, NamespaceException, AlfrescoRuntimeException;
}
