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
package org.alfresco.repo.version.common;

import java.util.Collection;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
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

    /**
     * Convert the incomming node ref (with the version store protocol specified)
     * to the internal representation with the workspace protocol.
     *
     * @param nodeRef   the incomming verison protocol node reference
     * @return          the internal version node reference
     */
    public static NodeRef convertNodeRef(NodeRef nodeRef)
    {
        return new NodeRef(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID), nodeRef.getId());
    }
}
