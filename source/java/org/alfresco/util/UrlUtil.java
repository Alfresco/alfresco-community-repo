/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.util;

import org.alfresco.repo.admin.SysAdminParams;


/**
 * Alfresco URL related utility functions.
 * 
 * @since 3.5
 */
public class UrlUtil
{
    /**
     * Builds up the Url to Alfresco based on the settings in the 
     *  {@link SysAdminParams}. 
     * @return Alfresco Url such as https://col.ab.or.ate/alfresco/
     *  or http://localhost:8080/alfresco/
     */
    public static String getAlfrescoUrl(SysAdminParams sysAdminParams)
    {
        return buildUrl(
                sysAdminParams.getAlfrescoProtocol(),
                sysAdminParams.getAlfrescoHost(),
                sysAdminParams.getAlfrescoPort(),
                sysAdminParams.getAlfrescoContext());
    }
    
    /**
     * Builds up the Url to Share based on the settings in the 
     *  {@link SysAdminParams}. 
     * @return Alfresco Url such as https://col.ab.or.ate/share/
     *  or http://localhost:8081/share/
     */
    public static String getShareUrl(SysAdminParams sysAdminParams)
    {
        return buildUrl(
                sysAdminParams.getShareProtocol(),
                sysAdminParams.getShareHost(),
                sysAdminParams.getSharePort(),
                sysAdminParams.getShareContext());
    }
    
    protected static String buildUrl(String protocol, String host, int port, String context)
    {
        StringBuilder url = new StringBuilder();
        url.append(protocol);
        url.append("://");
        url.append(host);
        if ("http".equals(protocol) && port == 80)
        {
            // Not needed
        }
        else if ("https".equals(protocol) && port == 443)
        {
            // Not needed
        }
        else
        {
            url.append(':');
            url.append(port);
        }
        url.append('/');
        url.append(context);
        return url.toString();
    }
}
