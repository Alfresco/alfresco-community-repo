package org.alfresco.repo.avm;

/**
 * Interface for Plain file nodes.
 * @author britt
 */
interface PlainFileNode extends FileNode
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
}
