package org.alfresco.service.cmr.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Common exception thrown when an operation fails because of a name clash.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class FileExistsException extends AlfrescoRuntimeException
{
    private static final String MESSAGE_ID = "file_folder_service.file_exists_message";

    private static final long serialVersionUID = -4133713912784624118L;
    
    private NodeRef parentNodeRef;
    private String name;

    public FileExistsException(NodeRef parentNodeRef, String name)
    {
        super(MESSAGE_ID, new Object[] { name });
        this.parentNodeRef = parentNodeRef;
        this.name = name;
    }

    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }

    public String getName()
    {
        return name;
    }
}
