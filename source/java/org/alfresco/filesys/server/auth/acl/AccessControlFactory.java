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

import java.util.Hashtable;

import org.alfresco.config.ConfigElement;

/**
 * Access Control Factoy Class
 * <p>
 * The AccessControlFactory class holds a table of available AccessControlParsers that are used to
 * generate AccessControl instances.
 * <p>
 * An AccessControlParser has an associated unique type name that is used to call the appropriate
 * parser.
 */
public class AccessControlFactory
{

    // Access control parsers

    private Hashtable<String, AccessControlParser> m_parsers;

    /**
     * Class constructor
     */
    public AccessControlFactory()
    {
        m_parsers = new Hashtable<String, AccessControlParser>();
    }

    /**
     * Create an access control using the specified parameters
     * 
     * @param type String
     * @param params ConfigElement
     * @return AccessControl
     * @exception ACLParseException
     * @exception InvalidACLTypeException
     */
    public final AccessControl createAccessControl(String type, ConfigElement params) throws ACLParseException,
            InvalidACLTypeException
    {

        // Find the access control parser

        AccessControlParser parser = m_parsers.get(type);
        if (parser == null)
            throw new InvalidACLTypeException(type);

        // Parse the parameters and create a new AccessControl instance

        return parser.createAccessControl(params);
    }

    /**
     * Add a parser to the list of available parsers
     * 
     * @param parser AccessControlParser
     */
    public final void addParser(AccessControlParser parser)
    {
        m_parsers.put(parser.getType(), parser);
    }

    /**
     * Remove a parser from the available parser list
     * 
     * @param type String
     * @return AccessControlParser
     */
    public final AccessControlParser removeParser(String type)
    {
        return (AccessControlParser) m_parsers.remove(type);
    }
}
