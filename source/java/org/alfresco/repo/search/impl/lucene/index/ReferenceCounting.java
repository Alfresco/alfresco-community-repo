package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;
import java.util.Deque;

/**
 * Reference counting and caching for read only index access.
 * 
 * When this object is invalid for reuse and all referees have gone the implementation should release all 
 * resources held (release the caches, close the index readers etc)
 * 
 * @author andyh
 *
 */
public interface ReferenceCounting
{
    public long getCreationTime();

    public Deque<Throwable> getReferences();

    /**
     * Get the number of references
     * @return int
     */
    public int getReferenceCount();

    /**
     * Mark is invalid for reuse. 
     * @throws IOException
     */
    public void setInvalidForReuse() throws IOException;
    
    /**
     * Determine if valid for reuse
     * @return boolean
     */
    public boolean isInvalidForReuse();
    
    /**
     * Get the id for this reader.
     * @return String
     */
    public String getId();
}