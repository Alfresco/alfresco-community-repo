package org.alfresco.repo.domain.encoding;

import org.alfresco.util.Pair;

/**
 * DAO services for <b>alf_encoding</b> and related tables
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface EncodingDAO
{
    /**
     * Get the encoding pair.
     * 
     * @param encoding              the encoding string
     * @return                      the ID-encoding pair or <tt>null</tt> if it doesn't exsit
     */
    Pair<Long, String> getEncoding(String encoding);
    
    Pair<Long, String> getEncoding(Long id);
    
    Pair<Long, String> getOrCreateEncoding(String encoding);
}
