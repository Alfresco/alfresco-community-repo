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