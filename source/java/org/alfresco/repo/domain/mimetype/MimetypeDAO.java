package org.alfresco.repo.domain.mimetype;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;

/**
 * DAO services for <b>alf_mimetype</b> table
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface MimetypeDAO
{
    /**
     * @param id            the unique ID of the entity
     * @return              the Mimetype pair (id, mimetype) (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    Pair<Long, String> getMimetype(Long id);

    /**
     * @param mimetype      the Mimetype to query for
     * @return              the Mimetype pair (id, mimetype) or <tt>null</tt> if it doesn't exist
     */
    Pair<Long, String> getMimetype(String mimetype);
    
    /**
     * Retrieve an existing mimetype or create a new one if it doesn't exist.
     * 
     * @param mimetype      the Mimetype
     * @return              the Mimetype pair (id, mimetype) (never null)
     */
    Pair<Long, String> getOrCreateMimetype(String mimetype);
    
    /**
     * Update a mimetype if it exists.  This method does not do any conflict resolution
     * i.e. it will only succeed if the new mimetype does not exist already.  Higher-level
     * logic is required to handle updates to dependent rows, etc.
     * 
     * @param oldMimetype   the old Mimetype
     * @param newMimetype   the new Mimetype
     * @return              the number of rows modified
     */
    int updateMimetype(String oldMimetype, String newMimetype);
}
