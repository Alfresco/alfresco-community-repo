/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
