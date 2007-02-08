/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.auth.acl;

import org.alfresco.config.ConfigElement;

/**
 * Protocol Access Control Parser Class
 */
public class ProtocolAccessControlParser extends AccessControlParser
{
    /**
     * Default constructor
     */
    public ProtocolAccessControlParser()
    {
    }

    /**
     * Return the parser type
     * 
     * @return String
     */
    public String getType()
    {
        return "protocol";
    }

    /**
     * Validate the parameters and create a user access control
     * 
     * @param params ConfigElement
     * @return AccessControl
     * @throws ACLParseException
     */
    public AccessControl createAccessControl(ConfigElement params) throws ACLParseException
    {

        // Get the access type

        int access = parseAccessType(params);

        // Get the list of protocols to check for

        String protos = params.getAttribute("type");
        if (protos == null || protos.length() == 0)
            throw new ACLParseException("Protocol type not specified");

        // Validate the protocol list

        if (ProtocolAccessControl.validateProtocolList(protos) == false)
            throw new ACLParseException("Invalid protocol type");

        // Create the protocol access control

        return new ProtocolAccessControl(protos, getType(), access);
    }
}
