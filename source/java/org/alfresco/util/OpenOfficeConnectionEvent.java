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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the status of the Open Office Connection. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 */
public class OpenOfficeConnectionEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 8834274840220309384L;

    /**
     * The Constructor.
     * 
     * @param metaData
     *            the meta data map
     */
    public OpenOfficeConnectionEvent(Map<String, Object> metaData)
    {
        super(metaData);
    }

    /**
     * Gets the meta data map.
     * 
     * @return the meta data map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMetaData()
    {
        return (Map<String, Object>) getSource();
    }
}
