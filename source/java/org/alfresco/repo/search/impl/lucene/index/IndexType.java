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

/**
 * The type of an entry in this index. 
 * 
 * @author Andy Hind
 */
public enum IndexType
{
    /**
     * Identifies the main index. This is always a fully optimised index.
     */
    INDEX,
    
    /**
     * An overlay. This is an optimised index with a deletion list. To commit an overlay requires no deletions against other indexes. Deletions are done when an overlay turns
     * into or is merged into a index. Overlays are periodically merged into an index. An overlay can require or have background properties indexed.
     */
    DELTA,
    
    /**
     * A long running overlay definition against the index. Not yet supported.
     * This, itself, may have transactional additions. 
     */
    OVERLAY,
 
    OVERLAY_DELTA;
    
    
}