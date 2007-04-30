/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * Layered nodes share these methods.
 * @author britt
 */
interface Layered
{
    /**
     * Get the indirection, or underlying path that this 
     * node points to.
     * @param lookup The lookup path.  Needed for most nodes to determine
     * underlying path.
     * @return The underlying indirection.
     */
    public String getUnderlying(Lookup lookup);
    
    /**
     * Get the indirection version.
     * @param lookup The lookup path.
     * @return The underlying indirection version.
     */
    public int getUnderlyingVersion(Lookup lookup);

    /**
     * Get the raw indirection of a layered node.
     * @return The raw indirection, which will be null for
     * LayeredDirectoryNodes that are not primary indirections.
     */
    public String getIndirection();
    
    /**
     * Set the indirection version for this layered node.
     * @param version The indirection version to set.
     */
    public void setIndirectionVersion(int version);
}
