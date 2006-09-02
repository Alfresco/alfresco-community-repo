/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

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
