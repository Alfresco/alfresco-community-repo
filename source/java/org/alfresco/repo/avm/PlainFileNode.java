package org.alfresco.repo.avm;

import org.alfresco.service.cmr.repository.ContentData;

/**
 * Interface for Plain file nodes.
 * @author britt
 */
public interface PlainFileNode extends FileNode
{
    public ContentData getContentData();
    public void setContentData(ContentData contentData);
    
    public boolean isLegacyContentData();
    public Long getContentDataId();
    
    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public String getContentURL();
    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public String getMimeType();
    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public String getEncoding();
    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public long getLength();
}
