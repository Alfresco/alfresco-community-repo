package org.alfresco.filesys.repo;

import java.io.File;
import java.io.InputStream;

import org.alfresco.service.cmr.repository.ContentReader;

public interface ContentComparator
{
    /**
     * Are the two content items equal?
     * <p>
     * For most cases a simple binary comparison is sufficient but some mimetypes 
     * trivial changes need to be discarded.
     * <p>
     * @param existingContent
     * @param newFile
     * @return true content is equal, false content is different.
     */
    boolean isContentEqual(ContentReader existingContent, File file);
}
