package org.alfresco.repo.avm;

import org.alfresco.service.cmr.repository.ContentData;

/**
 * Interface for Plain file nodes.
 * @author britt
 */
public interface PlainFileNode extends FileNode
{
    /**
     * Set the encoding of this file.
     * @param encoding
     */
    public void setEncoding(String encoding);
    
    /**
     * Set the mime type of this file.
     * @param mimeType
     */
    public void setMimeType(String mimeType);

    /**
     * Special case.
     * @return
     */
    public ContentData getContentData();
}
