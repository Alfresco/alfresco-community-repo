package org.alfresco.service.cmr.lock;

import java.text.MessageFormat;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Node locked exception class
 * 
 * @author Roy Wetherall
 */
public class NodeLockedException extends AlfrescoRuntimeException
{    
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3762254149525582646L;
    
    /**
     * Error message
     */
    private static final String ERROR_MESSAGE = I18NUtil.getMessage("lock_service.no_op");
    private static final String ERROR_MESSAGE_2 = I18NUtil.getMessage("lock_service.no_op2");

    /**
     * Constructor for tests
     */
    public NodeLockedException()
    {
        super("TEST CONSTRUCTOR INVOKED FOR NodeLockedException");
    }
    
    /**
     * @param nodeRef NodeRef
     */
    public NodeLockedException(NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{nodeRef.getId()}));
    }   
    
    public NodeLockedException(NodeRef nodeRef, String operation)
    {
        super(MessageFormat.format(ERROR_MESSAGE_2, new Object[]{operation, nodeRef.getId()}));
    }
}
