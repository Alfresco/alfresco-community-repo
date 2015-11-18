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
package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.io.Serializable;

/**
 * Constants for use by the caveats feature.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatConstants
{
    /** This utility class should not be instantiated. */
    private CaveatConstants() {}

    /** Key for accessing the persisted caveat groups and marks in the attribute service. */
    public static final Serializable[] CAVEAT_ATTRIBUTE_KEY = new String[] { "org.alfresco", "module.org_alfresco_module_rm", "caveat.groups" };
    /** The default prefix of caveat-related properties. */
    public static final String DEFAULT_CAVEAT_PREFIX = "rm.caveat.";
}
