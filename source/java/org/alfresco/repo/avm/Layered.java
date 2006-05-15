/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * Layered nodes share this method.
 * @author britt
 */
public interface Layered
{
    /**
     * Get the indirection, or underlying path that this 
     * node points to.
     * @param lookup The lookup path.  Needed for most nodes to determine
     * underlying path.
     * @return
     */
    public String getUnderlying(Lookup lookup);
}
