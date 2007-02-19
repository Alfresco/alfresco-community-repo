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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Reference to a node store
 * 
 * @author Derek Hulley
 */
public final class StoreRef implements EntityRef, Serializable
{
    private static final long serialVersionUID = 3905808565129394486L;

    public static final String PROTOCOL_WORKSPACE = "workspace";
    public static final String PROTOCOL_AVM = "avm";
    
    public static final String URI_FILLER = "://";

    private final String protocol;
    private final String identifier;

    /**
     * @param protocol
     *            well-known protocol for the store, e.g. <b>workspace</b> or
     *            <b>versionstore</b>
     * @param identifier
     *            the identifier, which may be specific to the protocol
     */
    public StoreRef(String protocol, String identifier)
    {
        if (protocol == null)
        {
            throw new IllegalArgumentException("Store protocol may not be null");
        }
        if (identifier == null)
        {
            throw new IllegalArgumentException("Store identifier may not be null");
        }

        this.protocol = protocol;
        this.identifier = identifier;
    }

    public StoreRef(String string)
    {
        int dividerPatternPosition = string.indexOf(URI_FILLER);
        if(dividerPatternPosition == -1)
        {
            throw new AlfrescoRuntimeException("Invalid store ref: Does not contain " + URI_FILLER + "   " + string);
        }
        this.protocol = string.substring(0, dividerPatternPosition);
        this.identifier = string.substring(dividerPatternPosition+3);
    }

    public String toString()
    {
        return protocol + URI_FILLER + identifier;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof StoreRef)
        {
            StoreRef that = (StoreRef) obj;
            return (this.protocol.equals(that.protocol)
                    && this.identifier.equals(that.identifier));
        } else
        {
            return false;
        }
    }
    
    /**
     * Creates a hashcode from both the {@link #getProtocol()} and {@link #getIdentifier()}
     */
    public int hashCode()
    {
        return (protocol.hashCode() + identifier.hashCode());
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getIdentifier()
    {
        return identifier;
    }
}