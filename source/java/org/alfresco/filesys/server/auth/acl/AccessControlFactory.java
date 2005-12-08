/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
