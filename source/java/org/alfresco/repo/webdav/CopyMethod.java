package org.alfresco.repo.webdav;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implements the WebDAV COPY method
 * 
 * @author Derek Hulley
 */
public class CopyMethod extends MoveMethod
{
    /**
     * Default constructor
     */
    public CopyMethod()
    {
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.webdav.MoveMethod#isMove()
     */
    @Override
    protected boolean isMove()
    {
        return false;
    }
}
