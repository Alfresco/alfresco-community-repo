/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

/**
 * Cmis properties enumeration
 *
 * @author Dmitry Lazurkin
 *
 */
public enum CmisProperty
{
    // Base properties
    OBJECT_ID,
    URI,
    OBJECT_TYPE_ID,
    CREATED_BY,
    CREATION_DATE,
    LAST_MODIFIED_BY,
    LAST_MODIFICATION_DATE,
    CHANGE_TOKEN,

    // Folder Object Type properties
    PARENT,
    ALLOWED_CHILD_OBJECT_TYPES,

    // Document Object Type properties
    IS_IMMUTABLE,
    IS_LATEST_VERSION,
    IS_MAJOR_VERSION,
    IS_LATEST_MAJOR_VERSION,
    VERSION_SERIES_IS_CHECKED_OUT,
    VERSION_SERIES_CHECKED_OUT_BY,
    VERSION_SERIES_CHECKED_OUT_OID,
    CHECKIN_COMMENT,
    CONTENT_STREAM_ALLOWED,
    CONTENT_STREAM_LENGTH,
    CONTENT_STREAM_MIME_TYPE,
    CONTENT_STREAM_FILENAME,
    CONTENT_STREAM_URI,

    // Document Object Type and Folder Object Type properties
    NAME;
}
