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
 * Domain Name Access Control Parser Class
 */
public class DomainAccessControlParser extends AccessControlParser
{

    /**
     * Default constructor
     */
    public DomainAccessControlParser()
    {
    }

    /**
     * Return the parser type
     * 
     * @return String
     */
    public String getType()
    {
        return "domain";
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

        // Get the domain name to check for

        String domainName = params.getAttribute("name");
        if (domainName == null || domainName.length() == 0)
            throw new ACLParseException("Domain name not specified");

        // Create the domain access control

        return new DomainAccessControl(domainName, getType(), access);
    }
}
