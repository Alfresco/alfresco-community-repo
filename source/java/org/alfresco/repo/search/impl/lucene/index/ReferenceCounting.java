/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;

/**
 * Reference counting and caching for read only index access.
 * 
 * When this object is invalid for reuse and all referees have gone the implementation should release all 
 * resources held (release the caches, close the index readers etc)
 * 
 * @author andyh
 *
 */
public interface ReferenceCounting
{
    /**
     * Get the number of references
     * @return
     */
    public int getReferenceCount();

    /**
     * Mark is invalid for reuse. 
     * @throws IOException
     */
    public void setInvalidForReuse() throws IOException;
    
    /**
     * Determine if valid for reuse
     * @return
     */
    public boolean isInvalidForReuse();
    
    /**
     * Get the id for this reader.
     * @return
     */
    public String getId();
}