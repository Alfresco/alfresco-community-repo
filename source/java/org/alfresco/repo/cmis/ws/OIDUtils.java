/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Dmitry Lazurkin
 *
 */
public class OIDUtils
{

    /**
     * Returns OID for node reference
     *
     * @param nodeRef node reference
     * @return OID
     */
    public static String toOID(NodeRef nodeRef)
    {
        return OIDType.DOCUMENT_FOLDER.ordinal() + nodeRef.toString();
    }

    /**
     * Returns OID for association reference
     *
     * @param assocRef association reference
     * @return OID
     */
    public static String toOID(AssociationRef assocRef)
    {
        return OIDType.RELATIONSHIP.ordinal() + assocRef.toString();
    }

    /**
     * Returns node reference for OID
     *
     * @param oid OID
     * @return node reference
     */
    public static NodeRef OIDtoNodeRef(String oid)
    {
        NodeRef nodeRef = null;

        OIDType oidType = OIDType.getOIDType(oid);
        if (oidType != null && oidType.equals(OIDType.DOCUMENT_FOLDER))
        {
            try
            {
                nodeRef = new NodeRef(oid.substring(1));
            }
            catch (Exception e)
            {
                // expected if OID string is bad
            }
        }

        return nodeRef;
    }

    /**
     * Returns association reference for OID
     *
     * @param oid OID
     * @return association reference
     */
    public static AssociationRef OIDtoAssocRef(String oid)
    {
        AssociationRef assocRef = null;

        OIDType oidType = OIDType.getOIDType(oid);
        if (oidType != null && oidType.equals(OIDType.RELATIONSHIP))
        {
            try
            {
                assocRef = new AssociationRef(oid.substring(1));
            }
            catch (Exception e)
            {
                // expected if oid string is bad
            }
        }

        return assocRef;
    }

}
