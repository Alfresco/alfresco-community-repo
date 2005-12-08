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
