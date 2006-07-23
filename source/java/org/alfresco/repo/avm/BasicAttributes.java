/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * Ownership, timestamps, later perhaps ACLs
 * @author britt
 */
interface BasicAttributes
{
    /**
     * Set the creator of the node.
     * @param creator The creator to set.
     */
    public void setCreator(String creator);
    
    /**
     * Get the creator of the node.
     * @return The creator.
     */
    public String getCreator();
    
    /**
     * Set the owner of the node.
     * @param owner The owner to set.
     */
    public void setOwner(String owner);
    
    /**
     * Get the owner of the node.
     * @return The owner.
     */
    public String getOwner();
    
    /**
     * Set the last modifier of the node.
     * @param lastModifier
     */
    public void setLastModifier(String lastModifier);
    
    /**
     * Get the last modifier of the node.
     * @return The last modifier.
     */
    public String getLastModifier();
    
    /**
     * Set the create date.
     * @param createDate The date to set.
     */
    public void setCreateDate(long createDate);
    
    /**
     * Get the create date.
     * @return The create date.
     */
    public long getCreateDate();
    
    /**
     * Set the modification date.
     * @param modDate The date to set.
     */
    public void setModDate(long modDate);
    
    /**
     * Get the modification date.
     * @return The modification date.
     */
    public long getModDate();
    
    /**
     * Set the access date of the node.
     * @param accessDate The access date.
     */
    public void setAccessDate(long accessDate);
    
    /**
     * Get the access date of the node.
     * @return The access date.
     */
    public long getAccessDate();
}
