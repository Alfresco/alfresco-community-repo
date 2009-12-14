/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.mimetype;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.Pair;

/**
 * DAO services for <b>alf_mimetype</b> table
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface MimetypeDAO
{
    /**
     * @param id            the unique ID of the entity
     * @return              the Mimetype pair (id, mimetype) (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    Pair<Long, String> getMimetype(Long id);

    /**
     * @param mimetype      the Mimetype to query for
     * @return              the Mimetype pair (id, mimetype) or <tt>null</tt> if it doesn't exist
     */
    Pair<Long, String> getMimetype(String mimetype);
    
    /**
     * Retrieve an existing mimetype or create a new one if it doesn't exist.
     * 
     * @param mimetype      the Mimetype
     * @return              the Mimetype pair (id, mimetype) (never null)
     */
    Pair<Long, String> getOrCreateMimetype(String mimetype);
}
