/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.util;

import org.alfresco.repo.admin.SysAdminParams;

import java.util.regex.Pattern;

/**
 * Alfresco URL related utility functions.
 * 
 * @since 3.5
 */
public class UrlUtil
{
    // ${shareUrl} placeholder
    public static final Pattern PATTERN = Pattern.compile("\\$\\{shareUrl\\}");

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

    /**
     * Replaces the share url placeholder, namely {@literal ${shareUrl}}, with <b>share</b> url.
     *
     * @param value          the string value which contains the share url placeholder
     * @param sysAdminParams the {@code SysAdminParams} object
     * @return if the given {@code value} contains share url placeholder,
     * the placeholder is replaced with share url; otherwise, the given {@code value} is simply returned
     */
    public static String replaceShareUrlPlaceholder(String value, SysAdminParams sysAdminParams)
    {
        if (value != null)
        {
            return PATTERN.matcher(value).replaceAll(
                        getShareUrl(sysAdminParams));
        }
        return value;
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
