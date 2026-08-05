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
 * Thread-local context for propagating the content attachment (download vs. preview) flag
 * from the web layer (ContentStreamer, CMISConnector, WebDAV) to {@link ContentServiceImpl}.
 * <p>
 * This replaces the previous approach of using {@code AlfrescoTransactionSupport.bindResource}
 * with the key {@code "contentService.attachment"}, which was unreliable because the value
 * was not always available in the transaction context.
 * <p>
 * Usage pattern:
 * <pre>
 * ContentDownloadContext.setAttachment(true);
 * try {
 *     contentService.getReader(nodeRef, propertyQName);
 * } finally {
 *     ContentDownloadContext.clear();
 * }
 * </pre>
 *
 * @since 26.3
 */
public final class ContentDownloadContext
{
    private static final ThreadLocal<Boolean> ATTACHMENT = new ThreadLocal<>();

    private ContentDownloadContext()
    {
        // Utility class – do not instantiate
    }

    /**
     * Set the attachment flag for the current thread.
     *
     * @param attachment {@code true} if the content access is a download (attachment),
     *                   {@code false} if it is a preview / inline read.
     */
    public static void setAttachment(boolean attachment)
    {
        ATTACHMENT.set(attachment);
    }

    /**
     * Return the attachment flag for the current thread, or {@code null} if not set.
     *
     * @return {@link Boolean#TRUE} for download, {@link Boolean#FALSE} for preview, or {@code null} if unset.
     */
    public static Boolean getAttachment()
    {
        return ATTACHMENT.get();
    }

    /**
     * Return whether the current request is a download (attachment).
     * If the flag has not been set, this returns {@code false}.
     *
     * @return {@code true} if the current request is a download.
     */
    public static boolean isDownload()
    {
        Boolean val = ATTACHMENT.get();
        return val != null && val;
    }

    /**
     * Clear the attachment flag for the current thread.
     * Must be called in a {@code finally} block to prevent thread-local leaks.
     */
    public static void clear()
    {
        ATTACHMENT.remove();
    }
}

