/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

    // ${alfrescoUrl} placeholder
    public static final Pattern REPO_PATTERN = Pattern.compile("\\$\\{alfrescoUrl\\}");

    // ${repoBaseUrl} placeholder
    public static final Pattern REPO_BASE_PATTERN = Pattern.compile("\\$\\{repoBaseUrl\\}");

    /**
     * Builds up the Url to Alfresco root url based on the settings in the
     *  {@link SysAdminParams}.
     * @return Alfresco base Url such as {@code https://col.ab.or.ate}
     *  or {@code http://localhost:8080}
     */
    public static String getAlfrescoBaseUrl(SysAdminParams sysAdminParams)
    {
        return buildBaseUrl(
                    sysAdminParams.getAlfrescoProtocol(),
                    sysAdminParams.getAlfrescoHost(),
                    sysAdminParams.getAlfrescoPort());
    }

    /**
     * Builds up the Url to Alfresco context based on the settings in the
     *  {@link SysAdminParams}.
     * @return Alfresco Url such as {@code https://col.ab.or.ate/alfresco}
     *  or {@code http://localhost:8080/alfresco}
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
     * @return Alfresco Url such as {@code https://col.ab.or.ate/share}
     *  or {@code http://localhost:8081/share}
     */
    public static String getShareUrl(SysAdminParams sysAdminParams)
    {
        return buildUrl(
                    sysAdminParams.getShareProtocol(),
                    sysAdminParams.getShareHost(),
                    sysAdminParams.getSharePort(),
                    sysAdminParams.getShareContext());
    }


    public static String getApiExplorerUrl(SysAdminParams sysAdminParams)
    {
        return getApiExplorerUrl(sysAdminParams, "", "");
    }

    /**
     * Builds URL to Api-Explorer based on the request only if the URL property is not provided
     *  {@link SysAdminParams}.
     * @return Rest-Api Url such as {@code https://col.ab.or.ate/api-explorer}
     *  or {@code http://localhost:8082/api-explorer}
     */
    public static String getApiExplorerUrl(SysAdminParams sysAdminParams, String requestURL, String requestURI)
    {
        if (!sysAdminParams.getApiExplorerUrl().isEmpty())
        {
            return sysAdminParams.getApiExplorerUrl();
        }
        if (!requestURI.isEmpty() && !requestURL.isEmpty())
        {
            return requestURL.replace(requestURI,"/api-explorer");
        }
        return "";
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

    public static String replaceAlfrescoUrlPlaceholder(String value, SysAdminParams sysAdminParams)
    {
        if (value != null)
        {
            return REPO_PATTERN.matcher(value).replaceAll(getAlfrescoUrl(sysAdminParams));
        }
        return value;
    }

    protected static String buildUrl(String protocol, String host, int port, String context)
    {
        String baseUrl = buildBaseUrl(protocol, host, port);
        return baseUrl + '/' + context;
    }

    protected static String buildBaseUrl(String protocol, String host, int port)
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
        return url.toString();
    }

    /**
     * Replaces the repo base url placeholder, namely {@literal ${repoBaseUrl}}, with value based on the settings in the
     * {@link SysAdminParams}.
     *
     * @param value          the string value which contains the repoBase url placeholder
     * @param sysAdminParams the {@code SysAdminParams} object
     * @return if the given {@code value} contains repoBase url placeholder,
     * the placeholder is replaced with repoBase url; otherwise, the given {@code value} is simply returned
     */
    public static String replaceRepoBaseUrlPlaceholder(String value, SysAdminParams sysAdminParams)
    {
        if (value != null)
        {
            return REPO_BASE_PATTERN.matcher(value).replaceAll(getAlfrescoBaseUrl(sysAdminParams));
        }
        return value;
    }

    public static String replaceUrlPlaceholder(Pattern pattern, String value, String replacement)
    {
        if (value != null)
        {
            return pattern.matcher(value).replaceAll(replacement);
        }
        return null;
    }
}
