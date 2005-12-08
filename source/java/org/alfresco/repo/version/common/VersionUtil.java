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
package org.alfresco.repo.version.common;

import java.util.Collection;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.version.ReservedVersionNameException;

/**
 * Helper class containing helper methods for the versioning services.
 * 
 * @author Roy Wetherall
 */
public class VersionUtil
{
    /**
     * Reserved property names
     */
    public static final String[] RESERVED_PROPERTY_NAMES = new String[]{
        VersionModel.PROP_CREATED_DATE, 
        VersionModel.PROP_FROZEN_NODE_ID, 
        VersionModel.PROP_FROZEN_NODE_STORE_ID, 
        VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL,
        VersionModel.PROP_FROZEN_NODE_TYPE,
        VersionModel.PROP_FROZEN_ASPECTS,
        VersionModel.PROP_VERSION_LABEL,
        VersionModel.PROP_VERSION_NUMBER};
    
    /**
     * Checks that the names of the additional version properties are valid and that they do not clash
     * with the reserved properties.
     * 
     * @param versionProperties  the property names 
     * @return                   true is the names are considered valid, false otherwise
     * @throws                   ReservedVersionNameException
     */
    public static void checkVersionPropertyNames(Collection<String> names)
        throws ReservedVersionNameException
    {
        for (String name : RESERVED_PROPERTY_NAMES)
        {
            if (names.contains(name) == true)
            {
                throw new ReservedVersionNameException(name);
            }
        }
    }
}
