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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

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
