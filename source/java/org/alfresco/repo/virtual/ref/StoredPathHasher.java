
package org.alfresco.repo.virtual.ref;

/**
 * Creates string-pair hashes based on stored path hashes.<br>
 * @see HierarchicalPathHasher 
 */
public class StoredPathHasher extends HierarchicalPathHasher
{

    private HashStore pathHashStore;

    public StoredPathHasher(HashStore pathHashStore)
    {
        super();
        this.pathHashStore = pathHashStore;
    }

    @Override
    protected String hashSubpath(String subpath)
    {
        return pathHashStore.hash(subpath);
    }

    @Override
    protected String lookupSubpathHash(String hash)
    {
        return pathHashStore.lookup(hash);
    }
}
