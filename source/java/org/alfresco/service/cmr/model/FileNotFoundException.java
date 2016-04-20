package org.alfresco.service.cmr.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Common, checked exception thrown when a file or folder could not be found
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class FileNotFoundException extends Exception
{
    private static final long serialVersionUID = 2558540174977806285L;

    public FileNotFoundException(NodeRef nodeRef)
    {
        super("No file or folder found for node reference: " + nodeRef);
    }
    
    public FileNotFoundException(String msg)
    {
        super(msg);
    }
}
