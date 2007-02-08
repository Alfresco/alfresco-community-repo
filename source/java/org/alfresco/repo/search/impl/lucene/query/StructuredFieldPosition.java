/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

/**
 * Elements used to test agains path and Qname
 * 
 * @author andyh
 */
public interface StructuredFieldPosition
{

    /**
     * Does this element match
     * 
     * @param start -
     *            the start postion of the paths terms
     * @param end -
     *            the end position of the paths terms
     * @param offset -
     *            the current offset in the path
     * @return returns the next match position (usually offset + 1) or -1 if it
     *         does not match.
     * @throws IOException
     */
    public int matches(int start, int end, int offset) throws IOException;

    /**
     * If this position is last in the chain and it is terminal it will ensure
     * it is an exact match for the length of the chain found. If false, it will
     * effectively allow prefix mathces for the likes of descendant-and-below
     * style queries.
     * 
     * @return
     */
    public boolean isTerminal();

    /**
     * Is this an absolute element; that is, it knows its exact position.
     * 
     * @return
     */
    public boolean isAbsolute();

    /**
     * This element only knows its position relative to the previous element.
     * 
     * @return
     */
    public boolean isRelative();

    /**
     * Get the test to search for in the term query. This may be null if it
     * should not have a term query
     * 
     * @return
     */
    public String getTermText();

    /**
     * If absolute return the position. If relative we could compute the
     * position knowing the previous term unless this element is preceded by a
     * descendat and below style element
     * 
     * @return
     */
    public int getPosition();

    /**
     * A reference to the caching term positions this element uses. This may be
     * null which indicates all terms match, in that case there is no action
     * against the index
     * 
     * @param tps
     */
    public void setCachingTermPositions(CachingTermPositions tps);

    public CachingTermPositions getCachingTermPositions();

    /**
     * Normally paths would require onlt parent chaining. for some it is parent
     * and child chaining.
     * 
     * @return
     */

    public boolean linkSelf();
    
    public boolean linkParent();

    public boolean allowslinkingByParent();
    
    public boolean allowsLinkingBySelf();
    
    public boolean isDescendant();
    
    public boolean matchesAll();
}
