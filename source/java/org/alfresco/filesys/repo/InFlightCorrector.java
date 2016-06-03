package org.alfresco.filesys.repo;

import java.util.Date;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The in flight corrector corrects search results that have not yet been committed to the
 * repository.
 * 
 * It substitutes the "in flight" valuses from the state cache in place of the values committed to 
 * the repo
 * 
 * @author mrogers
 */
public interface InFlightCorrector
{
    /**
     * Correct thr results with in flight details.
     * @param info
     * @param folderPath
     */
    public void correct(FileInfo info, String folderPath);
}
