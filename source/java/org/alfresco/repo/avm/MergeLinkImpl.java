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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.io.Serializable;

/**
 * This contains a single merged from-to relationship.
 * @author britt
 */
class MergeLinkImpl implements MergeLink, Serializable
{
    private static final long serialVersionUID = 6672271083042424944L;

    /**
     * The node that was merged from.
     */
    private AVMNode fFrom;
    
    /**
     * The node that was merged to.
     */
    private AVMNode fTo;
    
    /**
     * Set the from part.
     * @param from
     */
    public void setMfrom(AVMNode from)
    {
        fFrom = from;
    }

    /**
     * Get the from part.
     * @return The from part.
     */
    public AVMNode getMfrom()
    {
        return fFrom;
    }

    /**
     * Set the to part.
     * @param to
     */
    public void setMto(AVMNode to)
    {
        fTo = to;
    }

    /**
     * Get the to part.
     * @return The to part.
     */
    public AVMNode getMto()
    {
        return fTo;
    }

    /**
     * Override of equals.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof MergeLink))
        {
            return false;
        }
        MergeLink o = (MergeLink)obj;
        return fFrom.equals(o.getMfrom()) && fTo.equals(o.getMto());
    }

    /**
     * Get the hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fFrom.hashCode() + fTo.hashCode();
    }
}
