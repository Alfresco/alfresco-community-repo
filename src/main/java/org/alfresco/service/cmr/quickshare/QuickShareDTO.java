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
package org.alfresco.service.cmr.quickshare;

import java.io.Serializable;
import java.util.Date;

/**
 * Data transfer object for holding quick share information.
 *
 * @author Alex Miller
 * @since Cloud/4.2
 */
public class QuickShareDTO implements Serializable
{
    private static final long serialVersionUID = -2163618127531335360L;

    private String sharedId;
    private Date expiresAt;

    /**
     * Default constructor
     * 
     * @param sharedId The quick share id
     */
    public QuickShareDTO(String sharedId)
    {
        this(sharedId, null);
    }

    public QuickShareDTO(String sharedId, Date expiresAt)
    {
        this.sharedId = sharedId;
        this.expiresAt = expiresAt;
    }

    /**
     * Copy constructor
     */
    public QuickShareDTO(QuickShareDTO from) 
    {
        this(from.getId(), from.getExpiresAt());
    }
    
    /**
     * @return The share id
     */
    public String getId()
    {
        return this.sharedId;
    }

    public Date getExpiresAt()
    {
        return expiresAt;
    }
}
