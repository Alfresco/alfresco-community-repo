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

public interface ZeroEncoding
{

    static final String DELIMITER = ":";

    static final String REFERENCE_DELIMITER = "*";

    static final String STRING_PARAMETER = "s";

    /** depending on the resource type */
    static final String RESOURCE_PARAMETER[] = { "0", "1", "2" };

    static final String REFERENCE_PARAMETER = "r";

    public static final int VANILLA_PROTOCOL_CODE = 0;

    public static final int VIRTUAL_PROTOCOL_CODE = 3;

    public static final int NODE_PROTOCOL_CODE = 6;

    public static final int REPOSITORY_RESOURCE_CODE = 0;

    public static final int PATH_CODE = 0;

    public static final int NODE_CODE = 1;

    public static final int CLASSPATH_RESOURCE_CODE = 2;
}
