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
package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;
import java.util.List;

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
    public long getCreationTime();

    public List<Throwable> getReferences();

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