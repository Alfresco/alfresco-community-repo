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
package org.alfresco.filesys.smb.dcerpc.server;

/**
 * DCE Pipe Handler Class
 * <p>
 * Contains a list of the available DCE pipe handlers.
 */
public class DCEPipeHandler
{

    // DCE/RPC pipe request handlers

    private static DCEHandler[] _handlers = {
            new SrvsvcDCEHandler(),
            null, // samr
            null, // winreg
            new WkssvcDCEHandler(),
            null, // NETLOGON
            null, // lsarpc
            null, // spoolss
            null, // netdfs
            null, // service control
            null, // eventlog
            null // netlogon1
    };

    /**
     * Return the DCE/RPC request handler for the pipe type
     * 
     * @param typ int
     * @return DCEHandler
     */
    public final static DCEHandler getHandlerForType(int typ)
    {
        if (typ >= 0 && typ < _handlers.length)
            return _handlers[typ];
        return null;
    }
}
