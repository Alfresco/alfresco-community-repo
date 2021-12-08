/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.content;

import org.alfresco.service.Experimental;

/**
 * Enumeration with values for archive-restore parameter keys.
 * Values of this enum should be used as keys when requesting for content restore from archive.
 * Subject to expand/change.
 *
 * @author mpichura
 */
@Experimental
public enum ContentRestoreParams
{
    /**
     * Restore expiry in days. Corresponding value should be integer.
     */
    EXPIRY_DAYS,
    /**
     * Priority for restore from archive. Corresponding value should one of Standard/High
     */
    RESTORE_PRIORITY
}
