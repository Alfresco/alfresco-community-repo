/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.content;

/**
 * Defines the unified audit policy for content downloads - which audit events should be fired
 * when content access is identified as a download.
 *
 * <p>Configured via a single property:
 * <pre>
 * audit.content.policy=READ_AND_DOWNLOAD
 * </pre>
 *
 * <p>Valid values:
 * <ul>
 *   <li>{@code READ_ONLY} - only fire onContentRead</li>
 *   <li>{@code DOWNLOAD_ONLY} - only fire onContentDownload</li>
 *   <li>{@code READ_AND_DOWNLOAD} - fire both onContentRead and onContentDownload</li>
 * </ul>
 *
 * @since 26.4
 */
public enum ContentAuditPolicy
{
    /**
     * Only fire the {@code onContentRead} policy.
     */
    READ_ONLY(true, false),

    /**
     * Fire both {@code onContentRead} and {@code onContentDownload} policies.
     */
    READ_AND_DOWNLOAD(true, true);

    private final boolean fireRead;
    private final boolean fireDownload;

    ContentAuditPolicy(boolean fireRead, boolean fireDownload)
    {
        this.fireRead = fireRead;
        this.fireDownload = fireDownload;
    }

    /**
     * @return true if the onContentRead policy should be fired
     */
    public boolean isFireRead()
    {
        return fireRead;
    }

    /**
     * @return true if the onContentDownload policy should be fired
     */
    public boolean isFireDownload()
    {
        return fireDownload;
    }

    /**
     * Parse a policy string, case-insensitive. Returns null if the value is not recognized.
     *
     * @param value the string value to parse
     * @return the matching ContentAuditPolicy, or null if not recognized
     */
    public static ContentAuditPolicy fromString(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }
        try
        {
            return valueOf(value.trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}

