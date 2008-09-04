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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Property filter class
 *
 * @author Dmitry Lazurkin
 *
 */
public class PropertyFilter
{
    private static final Pattern PROPERTY_FILTER_REGEX = Pattern.compile("^(\\*)|([\\p{Upper}\\p{Digit}_]+(,[\\p{Upper}\\p{Digit}_]+)*)$");

    private Set<String> properties;
    private EnumSet<CmisProperty> disabledProperties = EnumSet.noneOf(CmisProperty.class);

    /**
     * Constructor
     *
     * @param filter filter string
     * @throws FilterNotValidException if filter string isn't valid
     */
    public PropertyFilter(String filter) throws FilterNotValidException
    {
        if (filter != null && filter.equals("") == false)
        {
            if (PROPERTY_FILTER_REGEX.matcher(filter).matches() == false)
            {
                throw new FilterNotValidException(filter, ExceptionUtils.createBasicFault(null, "Filter isn't valid"));
            }

            if (filter.equals("*") == false)
            {
                properties = new HashSet<String>(Arrays.asList(filter.split(",")));
            }
        }
    }

    /**
     * @param property property
     * @return if property is allow by filter then returns true else false
     */
    public boolean allow(CmisProperty property)
    {
        return disabledProperties.contains(property) == false && (properties == null || properties.contains(property.name()));
    }

    /**
     * Disables property
     *
     * @param property property
     */
    public void disableProperty(CmisProperty property)
    {
        disabledProperties.add(property);
    }

    /**
     * Enables property
     *
     * @param property property
     */
    public void enableProperty(CmisProperty property)
    {
        disabledProperties.remove(property);
    }

}
