package org.alfresco.repo.search.impl.lucene.index;

/**
 * The type of an entry in this index. 
 * 
 * @author Andy Hind
 */
public enum IndexType
{
    /**
     * Identifies the main index. This is always a fully optimised index.
     */
    INDEX,
    
    /**
     * An overlay. This is an optimised index with a deletion list. To commit an overlay requires no deletions against other indexes. Deletions are done when an overlay turns
     * into or is merged into a index. Overlays are periodically merged into an index. An overlay can require or have background properties indexed.
     */
    DELTA,
    
    /**
     * A long running overlay definition against the index. Not yet supported.
     * This, itself, may have transactional additions. 
     */
    OVERLAY,
 
    OVERLAY_DELTA;
    
    
}