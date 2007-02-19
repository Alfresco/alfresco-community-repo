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

package org.alfresco.service.cmr.avm;

import java.io.Serializable;


/**
 * A value class containing information about the layering state of a looked up
 * node.
 * @author britt
 */
public class LayeringDescriptor implements Serializable
{
    private static final long serialVersionUID = -6911813236493434123L;

    /**
     * Whether the node is a background node.
     */
    private boolean fIsBackground;
    
    /**
     * The store descriptor for the top level lookup.
     */
    private AVMStoreDescriptor fContainingStore;
    
    /**
     * The store descriptor for the layer on which the node was finally found.
     */
    private AVMStoreDescriptor fFinalStore;
    
    /**
     * Make one up.
     * @param isBackground
     * @param containingStore
     * @param finalStore
     */
    public LayeringDescriptor(boolean isBackground,
                              AVMStoreDescriptor containingStore,
                              AVMStoreDescriptor finalStore)
    {
        fIsBackground = isBackground;
        fContainingStore = containingStore;
        fFinalStore = finalStore;
    }
    
    /**
     * Get the store that the original path is in.
     * @return An AVMStoreDescriptor.
     */
    public AVMStoreDescriptor getPathAVMStore()
    {
        return fContainingStore;
    }
    
    /**
     * Get the store that the final node was in.
     * @return An AVMStoreDescriptor.
     */
    public AVMStoreDescriptor getNativeAVMStore()
    {
        return fFinalStore;
    }

    /**
     * Is the node a background node.
     * @return Whether the node is a background node.
     */
    public boolean isBackground()
    {
        return fIsBackground;
    }
}
