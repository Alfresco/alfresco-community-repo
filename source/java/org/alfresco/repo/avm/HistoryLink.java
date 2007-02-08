/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

/**
 * Interface for the ancestor-descendent relationship.
 * @author britt
 */
public interface HistoryLink
{
    /**
     * Set the ancestor part of this.
     * @param ancestor
     */
    public void setAncestor(AVMNode ancestor);
    
    /**
     * Get the ancestor part of this.
     * @return The ancestor.
     */
    public AVMNode getAncestor();

    /**
     * Set the descendent part of this.
     * @param descendent
     */
    public void setDescendent(AVMNode descendent);
    
    /**
     * Get the descendent part of this.
     * @return The descendent of this link.
     */
    public AVMNode getDescendent();
}
