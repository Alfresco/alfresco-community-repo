/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.caveat.scheme;

/**
 * An enumeration of the type of caveat groups.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public enum CaveatGroupType
{
    /** Each mark in the group implies all earlier marks also apply. */
    HIERARCHICAL,
    /** Many marks may be applied to content, and users need all marks to access it. */
    USER_REQUIRES_ALL,
    /** Many marks may be applied to content, and users can access it with any one mark. */
    USER_REQUIRES_ANY
}
