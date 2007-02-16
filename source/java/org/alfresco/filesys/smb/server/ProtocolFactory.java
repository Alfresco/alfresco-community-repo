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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.server;

import org.alfresco.filesys.smb.Dialect;

/**
 * SMB Protocol Factory Class.
 * <p>
 * The protocol factory class generates protocol handlers for SMB dialects.
 */
class ProtocolFactory
{

    /**
     * ProtocolFactory constructor comment.
     */
    public ProtocolFactory()
    {
        super();
    }

    /**
     * Return a protocol handler for the specified SMB dialect type, or null if there is no
     * appropriate protocol handler.
     * 
     * @param dialect int
     * @return ProtocolHandler
     */
    protected static ProtocolHandler getHandler(int dialect)
    {

        // Determine the SMB dialect type

        ProtocolHandler handler = null;

        switch (dialect)
        {

        // Core dialect

        case Dialect.Core:
        case Dialect.CorePlus:
            handler = new CoreProtocolHandler();
            break;

        // LanMan dialect

        case Dialect.DOSLanMan1:
        case Dialect.DOSLanMan2:
        case Dialect.LanMan1:
        case Dialect.LanMan2:
        case Dialect.LanMan2_1:
            handler = new LanManProtocolHandler();
            break;

        // NT dialect

        case Dialect.NT:
            handler = new NTProtocolHandler();
            break;
        }

        // Return the protocol handler

        return handler;
    }
}