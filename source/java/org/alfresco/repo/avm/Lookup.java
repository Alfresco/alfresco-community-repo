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

package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

/**
 * This holds all the information necessary to perform operations
 * on AVMNodes, and is structured internally as a list of path components
 * from the root directory of a repository.
 * @author britt
 */
class Lookup
{
    /**
     * Is this lookup valid?
     */
    private boolean fValid;
    
    /**
     * The AVMStore.
     */
    private AVMStore fAVMStore;

    /**
     * The name of the AVMStore.
     */
    private String fStoreName;
    
    /**
     * The components that make up this path.
     */
    private List<LookupComponent> fComponents;
    
    /**
     * The final store in resolving layers.
     */
    private AVMStore fFinalStore;
    
    /**
     * Whether, at this point, a layered node has been hit.
     * Used while building a Lookup.
     */
    private boolean fLayeredYet;
    
    /**
     * Whether we are directly contained at this point.
     */
    private boolean fDirectlyContained;
 
    /**
     * The first LayeredDirectoryNode in the path.
     */
    private LayeredDirectoryNode fTopLayer;
    
    /**
     * The path index of the top LayeredDirectoryNode in the path.
     */
    private int fTopLayerIndex;
    
    /**
     * The lowest layered directory node's index seen so far.
     */
    private int fLowestLayerIndex;
    
    /**
     * The current component being looked at by this lookup.
     */
    private int fPosition;
    
    /**
     * Whether a needs-to-be-copied component has been seen.
     */
    private boolean fNeedsCopying;
    
    public Lookup(Lookup other, AVMNodeDAO nodeDAO, AVMStoreDAO storeDAO)
    {
        fValid = true;
        fAVMStore = storeDAO.getByID(other.fAVMStore.getId());
        if (fAVMStore == null)
        {
            fValid = false;
            return;
        }
        fStoreName = fAVMStore.getName();
        fComponents = new ArrayList<LookupComponent>();
        fLayeredYet = other.fLayeredYet;
        if (other.fTopLayer != null)
        {
            fTopLayer = (LayeredDirectoryNode)nodeDAO.getByID(other.fTopLayer.getId());
            if (fTopLayer == null)
            {
                fValid = false;
                return;
            }
        }
        fPosition = other.fPosition;
        fTopLayerIndex = other.fTopLayerIndex;
        fLowestLayerIndex = other.fLowestLayerIndex;
        fNeedsCopying = other.fNeedsCopying;
        fDirectlyContained = other.fDirectlyContained;
        if (fLayeredYet)
        {
            for (LookupComponent comp : other.fComponents)
            {
                LookupComponent newComp = new LookupComponent();
                newComp.setName(comp.getName());
                newComp.setIndirection(comp.getIndirection());
                newComp.setNode(nodeDAO.getByID(comp.getNode().getId()));
                if (newComp.getNode() == null)
                {
                    fValid = false;
                    return;
                }
                fComponents.add(newComp);
            }
        }
        else
        {
            // If this is not a layered lookup then we do not
            // need to reload any of the actual nodes except for
            // the last.
            int i = 0;
            for (; i < fPosition; ++i)
            {
                LookupComponent comp = other.fComponents.get(i);
                LookupComponent newComp = new LookupComponent();
                newComp.setName(comp.getName());
                fComponents.add(newComp);
            }
            LookupComponent comp = other.fComponents.get(i);
            LookupComponent newComp = new LookupComponent();
            newComp.setName(comp.getName());
            newComp.setNode(nodeDAO.getByID(comp.getNode().getId()));
            if (newComp.getNode() == null)
            {
                fValid = false;
                return;
            }
            fComponents.add(newComp);
        }
        fFinalStore = storeDAO.getByID(other.fFinalStore.getId());
        if (fFinalStore == null)
        {
            fValid = false;
        }
    }
    
    /**
     * Create a new one.
     * @param store The AVMStore that's being looked in.
     * @param storeName The name of that AVMStore.
     */
    public Lookup(AVMStore store, String storeName)
    {
        fValid = true;
        fAVMStore = store;
        fStoreName = storeName;
        fComponents = new ArrayList<LookupComponent>();
        fLayeredYet = false;
        fTopLayer = null;
        fPosition = -1;
        fTopLayerIndex = -1;
        fLowestLayerIndex = -1;
        fNeedsCopying = false;
        fDirectlyContained = true;
        fFinalStore = store;
    }
    
    /**
     * Is this a valid lookup?
     */
    public boolean isValid()
    {
        return fValid;
    }
    
    // TODO This is badly in need of cleanup.
    /**
     * Add a new node to the lookup.
     * @param node The node to add.
     * @param name The name of the node in the path.
     * @param write Whether this is in the context of 
     * a write operation.
     */
    public void add(AVMNode node, String name, boolean write)
    {
        LookupComponent comp = new LookupComponent();
        comp.setName(name);
        comp.setNode(node);
        if (fPosition >= 0 && fDirectlyContained && 
                fComponents.get(fPosition).getNode().getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            fDirectlyContained = ((DirectoryNode)fComponents.get(fPosition).getNode()).directlyContains(node);
        }
        if (!write)
        {
            if (node.getType() == AVMNodeType.LAYERED_DIRECTORY)
            {
                LayeredDirectoryNode oNode = (LayeredDirectoryNode)node;
                if (oNode.getPrimaryIndirection())
                {
                    comp.setIndirection(oNode.getUnderlying());
                }
                else
                {
                    comp.setIndirection(computeIndirection(name));
                }
                fLayeredYet = true;
                // Record the first layer seen.
                if (fTopLayer == null)
                {
                    fTopLayer = oNode;
                    fTopLayerIndex = fPosition + 1;
                }
                fLowestLayerIndex = fPosition + 1;
            }
            fComponents.add(comp);
            fPosition++;
            return;
        }
        if (!node.getIsNew())
        {
            fNeedsCopying = true;
        }
        else
        {
            if (fPosition >= 0 && !fDirectlyContained)
            {
                fNeedsCopying = true;
            }
        }
        // Record various things if this is layered.
        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            LayeredDirectoryNode oNode = (LayeredDirectoryNode)node;
            // Record the indirection path that should be used.
            if (oNode.getPrimaryIndirection())
            {
                comp.setIndirection(oNode.getUnderlying());
            }
            else
            {
                comp.setIndirection(computeIndirection(name));
            }
            fLayeredYet = true;
            // Record the first layer seen.
            if (fTopLayer == null)
            {
                fTopLayer = oNode;
                fTopLayerIndex = fPosition + 1;
            }
            fLowestLayerIndex = fPosition + 1;
        }
        // In a write context a plain directory contained in a layer will
        // be copied so we will need to compute an indirection path.
        else if (fLayeredYet)
        {
            comp.setIndirection(computeIndirection(name));
        }
        fComponents.add(comp);
        fPosition++;
        // If we are in a write context do copy on write.
        if (fNeedsCopying)
        {
            node = node.copy(this);
            node.setVersionID(fAVMStore.getNextVersionID());
            fComponents.get(fPosition).setNode(node);
            if (fPosition == 0)
            {
                // Inform the store of a new root.
                fAVMStore.setNewRoot((DirectoryNode)node);
                AVMDAOs.Instance().fAVMStoreDAO.update(fAVMStore);
                return;
            }
            // Not the root. Check if we are the top layer and insert this into it's parent.
            if (fPosition == fTopLayerIndex)
            {
                fTopLayer = (LayeredDirectoryNode)node;
            }
            ((DirectoryNode)fComponents.get(fPosition - 1).getNode()).putChild(name, node);
        }
    }
    
    /**
     * A helper for keeping track of indirection.
     * @param name The name of the being added node.
     * @return The indirection for the being added node.
     */
    private String computeIndirection(String name)
    {
        String parentIndirection = fComponents.get(fPosition).getIndirection();
        if (parentIndirection.endsWith("/"))
        {
            return parentIndirection + name;
        }
        else
        {
            return parentIndirection + "/" + name;
        }
    }
    
    /**
     * Get the current node we're looking at.
     * @return The current node.   
     */
    public AVMNode getCurrentNode()
    {
        return fComponents.get(fPosition).getNode();
    }
    
    /**
     * Is the current path layered.
     * @return Whether the current position in the path is layered.
     */
    public boolean isLayered()
    {
        return fLayeredYet;
    }

    /**
     * Determine if a node is directly in this layer.
     * @return Whether this node is directly in this layer.
     */
    public boolean isInThisLayer()
    {
        return fLayeredYet && fDirectlyContained;
    }
    
    /**
     * Get the number of nodes.
     * @return The number of nodes.
     */
    public int size()
    {
        return fComponents.size();
    }
    
    /**
     * Calculate the indirection path at this node.
     * @return The indirection path all the way down to the current node.
     */
    public String getIndirectionPath()
    {
        // The path is the underlying path of the lowest layer (in the path sense) 
        // that is directly contained by the top layer and is a primary indirection node.
        int pos = fLowestLayerIndex;
        AVMNode node = fComponents.get(pos).getNode();
        LayeredDirectoryNode oNode = null;
        while (pos >= fTopLayerIndex && node.getType() != AVMNodeType.LAYERED_DIRECTORY &&
               ((oNode = (LayeredDirectoryNode)node).getLayerID() != fTopLayer.getLayerID() ||
                !oNode.getPrimaryIndirection()))
        {
            pos--;
            node = fComponents.get(pos).getNode();
        }
        oNode = (LayeredDirectoryNode)node;
        // We've found it.
        StringBuilder builder = new StringBuilder();
        builder.append(oNode.getUnderlying());
        for (int i = pos + 1; i <= fPosition; i++)
        {
            builder.append("/");
            builder.append(fComponents.get(i).getName());
        }
        return builder.toString();
    }
    
    /**
     * Get the computed indirection for the current node.
     * @return The indirection.
     */
    public String getCurrentIndirection()
    {
        String value = fComponents.get(fPosition).getIndirection();
        return value;
    }
    
    /**
     * Get the topmost Layered directory node.  Topmost in the
     * path lookup sense.
     * @return The topmost layered directory node.
     */
    public LayeredDirectoryNode getTopLayer()
    {
        return fTopLayer;
    }
    
    /**
     * Get the store that this path is in.
     * @return The store.
     */
    public AVMStore getAVMStore()
    {
        return fAVMStore;
    }
    
    /**
     * Get the path represented by this lookup.
     * @return The canonical path for this lookup.
     */
    public String getRepresentedPath()
    {
        if (fComponents.size() == 1)
        {
            return fStoreName + ":/";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(fStoreName);
        builder.append(':');
        int count = fComponents.size();
        for (int i = 1; i < count; i++)
        {
            builder.append('/');
            builder.append(fComponents.get(i).getName());
        }
        return builder.toString();
    }
    
    /**
     * Gets the final name in the lookup.
     * @return The final name in the lookup.
     */
    public String getBaseName()
    {
        return fComponents.get(fPosition).getName();
    }
    
    /**
     * Set the final store the lookup occurred in.
     * @param store The store to set.
     */
    public void setFinalStore(AVMStore store)
    {
        fFinalStore = store;
    }
    
    /**
     * Get the final store traversed during lookup.
     * @return The final store traversed.
     */
    public AVMStore getFinalStore()
    {
        return fFinalStore;
    }
    
    /**
     * Get whether the node looked up is directly contained from the
     * original root.
     * @return Whether the node looked up is directly contained.
     */
    public boolean getDirectlyContained()
    {
        return fDirectlyContained;
    }
}
