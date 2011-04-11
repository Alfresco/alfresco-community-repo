/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.node;

/**
 * @since 3.4
 * @author Nick Smith
 */
public interface FormFieldConstants
{
    /** Public constants */
    public static final String ON = "on";

    public static final String PROP = "prop";

    public static final String ASSOC = "assoc";

    public static final String FIELD_NAME_SEPARATOR = ":";

    public static final String DATA_KEY_SEPARATOR = "_";

    public static final String PROP_DATA_PREFIX = PROP + DATA_KEY_SEPARATOR;

    public static final String ASSOC_DATA_PREFIX = ASSOC + DATA_KEY_SEPARATOR;

    public static final String ASSOC_DATA_ADDED_SUFFIX = DATA_KEY_SEPARATOR + "added";

    public static final String ASSOC_DATA_REMOVED_SUFFIX = DATA_KEY_SEPARATOR + "removed";

    public static final String ADDED = "added";

    public static final String REMOVED = "removed";
    
    public static final String DOT_CHARACTER = ".";

    public static final String DOT_CHARACTER_REPLACEMENT = "#dot#";
    
    /** Protected constants */
    public static final String DEFAULT_CONTENT_MIMETYPE = "text/plain";
    
}
