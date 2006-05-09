package org.alfresco.repo.avm.hibernate;

import java.util.Map;
import java.util.Set;


public interface LayeredDirectoryNodeBean extends DirectoryNodeBean
{
    /**
     * Set the layer id.
     * @param id The id to set.
     */
    public void setLayerID(long id);

    /**
     * Get the layer id.
     * @return The layer id.
     */
    public long getLayerID();

    /**
     * Set the indirection.
     * @param indirection The indirection to set.
     */
    public void setIndirection(String indirection);

    /**
     * Get the indirection.
     * @return The indirection.
     */
    public String getIndirection();

    /**
     * Set the added map.
     * @param added The added children.
     */
    public void setAdded(Map<String, DirectoryEntry> added);

    /**
     * Get the added map.
     * @return The map of added children.
     */
    public Map<String, DirectoryEntry> getAdded();

    /**
     * Set the Set of deleted names.
     * @param deleted The deleted names.
     */
    public void setDeleted(Set<String> deleted);

    /**
     * Get the Set of deleted names.
     * @return The Set of deleted names.
     */
    public Set<String> getDeleted();
    
    /**
     * Set the primary indirection-ness of this.
     * @param primary Whether this is a primary indirection node.
     */
    public void setPrimaryIndirection(boolean primary);
    
    /**
     * Get the primary indirection-ness of this.
     * @return Whether this is a primary indirection node.
     */
    public boolean getPrimaryIndirection();
}