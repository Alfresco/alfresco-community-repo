/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
 * Enumeration with "header" values for Alfresco derived Storage Properties
 * Values of this enum should be used when adding Alfresco derived key-value pairs in Storage Properties map.
 * Subject to expand/change.
 *
 * @author mpichura
 */
public enum ObjectStorageProps {
    /**
     * Object's content is archived and not immediately accessible.
     */
    X_ALF_ARCHIVED("x-alf-archived"),
    /**
     * Object's content retrieval from archive is in progress
     */
    X_ALF_ARCHIVE_RESTORE_IN_PROGRESS("x-alf-archive-restore-in-progress"),
    /**
     * Expiry date and time of object's content retrieved from archive.
     * Use YYYYMMDDThhmmssZ (ISO-8601) datetime format when using this value as key in Storage Properties map.
     */
    X_ALF_ARCHIVE_RESTORE_EXPIRY("x-alf-archive-restore-expiry");

    ObjectStorageProps(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

}
