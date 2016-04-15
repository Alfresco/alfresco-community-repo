package org.alfresco.service.cmr.repository;

import java.text.MessageFormat;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Used to indicate that an aspect is missing from a node.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public class AspectMissingException extends RuntimeException
{
    private static final long serialVersionUID = 3257852099244210228L;
    
    private QName missingAspect;
    private NodeRef nodeRef;

    /**
     * Error message
     */
    private static final String ERROR_MESSAGE = "The {0} aspect is missing from this node (id: {1}).  " +
            "It is required for this operation.";
    
    /**
     * Constructor
     */
    public AspectMissingException(QName missingAspect, NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{missingAspect.toString(), nodeRef.getId()}));
        this.missingAspect = missingAspect;
        this.nodeRef = nodeRef;
    }

    public QName getMissingAspect()
    {
        return missingAspect;
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
}
