/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.server.auth.acl;

import org.alfresco.config.ConfigElement;

/**
 * Access Control Parser Class
 * <p>
 * Creates an AccessControl instance by parsing a set of name/value parameters.
 */
public abstract class AccessControlParser
{

    // Constants
    //
    // Standard parameter names

    public final static String ParameterAccess = "access";

    // Access control type names

    private final static String[] _accessTypes = { "None", "Read", "Write" };

    /**
     * Return the access control type name that uniquely identifies this type of access control.
     * 
     * @return String
     */
    public abstract String getType();

    /**
     * Create an AccessControl instance by parsing the set of name/value parameters
     * 
     * @param params ConfigElement
     * @return AccessControl
     * @exception ACLParseException
     */
    public abstract AccessControl createAccessControl(ConfigElement params) throws ACLParseException;

    /**
     * Find the access parameter and parse the value
     * 
     * @param params ConfigElement
     * @return int
     * @exception ACLParseException
     */
    protected final int parseAccessType(ConfigElement params) throws ACLParseException
    {

        // Check if the parameter list is valid

        if (params == null)
            throw new ACLParseException("Empty parameter list");

        // Find the access type parameter

        String accessType = params.getAttribute(ParameterAccess);

        if (accessType == null || accessType.length() == 0)
            throw new ACLParseException("Required parameter 'access' missing");

        // Parse the access type value

        return parseAccessTypeString(accessType);
    }

    /**
     * Parse the access level type and validate
     * 
     * @param accessType String
     * @return int
     * @exception ACLParseException
     */
    public static final int parseAccessTypeString(String accessType) throws ACLParseException
    {

        // Check if the access type is valid

        if (accessType == null || accessType.length() == 0)
            throw new ACLParseException("Empty access type string");

        // Parse the access type value

        int access = -1;

        for (int i = 0; i < _accessTypes.length; i++)
        {

            // Check if the access type matches the current type

            if (accessType.equalsIgnoreCase(_accessTypes[i]))
                access = i;
        }

        // Check if we found a valid access type

        if (access == -1)
            throw new ACLParseException("Invalid access type, " + accessType);

        // Return the access type

        return access;
    }

    /**
     * Return the parser details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getType());
        str.append("]");

        return str.toString();
    }
}
