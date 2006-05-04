/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.server.auth.ntlm;

/**
 * Target Information Class
 * 
 * <p>Contains the target information from an NTLM message.
 * 
 * @author GKSpencer
 */
public class TargetInfo
{
    // Target type and name
    
    private int m_type;
    private String m_name;
    
    /**
     * Class constructor
     * 
     * @param type int
     * @param name String
     */
    public TargetInfo(int type, String name)
    {
        m_type = type;
        m_name = name;
    }
    
    /**
     * Return the target type
     * 
     * @return int
     */
    public final int isType()
    {
        return m_type;
    }
    
    /**
     * Return the target name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }
    
    /**
     * Return the target information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getTypeAsString(isType()));
        str.append(":");
        str.append(getName());
        str.append("]");
        
        return str.toString();
    }
    
    /**
     * Return the target type as a string
     * 
     * @param typ int
     * @return String
     */
    public final static String getTypeAsString(int typ)
    {
        String typStr = null;
        
        switch ( typ)
        {
        case NTLM.TargetServer:
            typStr = "Server";
            break;
        case NTLM.TargetDomain:
            typStr = "Domain";
            break;
        case NTLM.TargetFullDNS:
            typStr = "DNS";
            break;
        case NTLM.TargetDNSDomain:
            typStr = "DNS Domain";
            break;
        default:
            typStr = "Unknown 0x" + Integer.toHexString(typ);
            break;
        }
        
        return typStr;
    }
}
