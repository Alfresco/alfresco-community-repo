/**
 * 
 */
package org.alfresco.repo.avm;

import org.alfresco.repo.avm.util.SimplePath;

/**
 * This is the key by which Lookup's are retrieved from the cache.
 * @author britt
 */
public class LookupKey 
{
    /**
     * The name of the store.
     */
    private String fStoreName;
    
    /**
     * The path being looked up.
     */
    private SimplePath fPath;
    
    /**
     * The version being looked up.
     */
    private int fVersion;
    
    /**
     * Whether the lookup is a write lookup.
     */
    private boolean fWrite;
    
    /**
     * Whether the lookup includes deleted nodes.
     */
    private boolean fIncludeDeleted;

    /**
     * Create one from whole cloth.
     * @param version The version we're looking under.
     * @param path The path.
     * @param storeName The name of the store.
     * @param write Whether this is a write lookup.
     * @param includeDeleted Whether this lookup should include deleted items.
     */
    public LookupKey(int version, 
                     SimplePath path,
                     String storeName,
                     boolean write,
                     boolean includeDeleted)
    {
        fVersion = version;
        fPath = path;
        fStoreName = storeName;
        fWrite = write;
        fIncludeDeleted = includeDeleted;
    }
    
    public LookupKey(LookupKey other)
    {
        fVersion = other.fVersion;
        fPath = other.fPath;
        fStoreName = other.fStoreName;
        fWrite = other.fWrite;
        fIncludeDeleted = other.fIncludeDeleted;
    }
    
    /**
     * Set the writeness of this key.
     */
    public void setWrite(boolean write)
    {
        fWrite = write;
    }

    /**
     * Get the store name for this key.
     * @return The store name.
     */
    public String getStoreName()
    {
        return fStoreName;
    }
    
    /**
     * Is this a write lookup.
     * @return Whether this is a write lookup.
     */
    public boolean isWrite()
    {
        return fWrite;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof LookupKey))
        {
            return false;
        }
        LookupKey o = (LookupKey)obj;
        return fStoreName.equals(o.fStoreName) &&
               fVersion == o.fVersion &&
               fPath.equals(o.fPath) &&
               fWrite == o.fWrite &&
               fIncludeDeleted == o.fIncludeDeleted;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() 
    {
        int hash = fStoreName.hashCode();
        hash += fPath.hashCode();
        hash += fVersion;
        hash += fWrite ? 1 : 0;
        hash += fIncludeDeleted ? 1 : 0;
        return hash;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        return fStoreName + ":" + fPath + "-" + fVersion + "-" + fWrite + "-" + fIncludeDeleted;
    }   
}
