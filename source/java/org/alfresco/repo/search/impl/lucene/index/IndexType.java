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
     * Identifies a subindex. This will also be optimised. Sub-indexes are periodically merged into the index.
     */
    SUBINDEX,
    
    /**
     * An overlay. This is an optimised index with a deletion list. To commit an overlay requires no deletions against other indexes. Deletions are done when an overlay turns
     * into or is merged into a subindex. Overlays are periodically merged into a sub index. An overlay can require or have background properties indexed
     */
    INDEX_OVERLAY,
    
    /**
     * A long running overlay defintion against the index. Not yet supported.
     * This, itself, may have transactional additions. 
     */
    OVERLAY,
    
    /**
     * A delta is a transactional change set. This commits to an overlay index.
     */
    DELTA,
    
    /**
     * A delta to an overlay
     */
    OVERLAY_DELTA;
}