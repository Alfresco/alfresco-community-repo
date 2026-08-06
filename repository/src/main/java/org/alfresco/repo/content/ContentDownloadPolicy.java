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
 * Defines the download auditing policy for content access.
 * <p>
 * This enum merges the former {@code audit.content.enableContentDownload} and
 * {@code audit.content.unknownReadAsDownload} flags into a single configuration:
 * {@code audit.content.downloadPolicy}.
 *
 * <ul>
 *   <li><b>NONE</b> – Download auditing is disabled. All content reads fire only the onContentRead policy.</li>
 *   <li><b>STANDARD</b> – Audit only explicit downloads (attachment=true). Unknown reads (CMIS, WebDAV)
 *       are treated as plain reads.</li>
 *   <li><b>EXTENDED</b> – Audit explicit downloads <em>and</em> treat unknown reads (CMIS, WebDAV)
 *       as downloads.</li>
 * </ul>
 *
 * @since 26.3
 */
public enum ContentDownloadPolicy
{
    /**
     * Download auditing disabled. All content access fires only onContentRead.
     */
    NONE,

    /**
     * Audit only explicit downloads (where the caller sets attachment=true).
     * Unknown reads (CMIS, WebDAV) fire only onContentRead.
     */
    STANDARD,

    /**
     * Audit explicit downloads and also treat unknown reads (CMIS, WebDAV) as downloads.
     */
    EXTENDED
}

