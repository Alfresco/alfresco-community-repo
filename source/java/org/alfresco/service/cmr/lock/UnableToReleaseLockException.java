package org.alfresco.service.cmr.lock;

import java.text.MessageFormat;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Runtime exception class
 * 
 * @author Roy Wetherall
 */
public class UnableToReleaseLockException extends RuntimeException
{
    /**
     * Serial verison UID
     */
    private static final long serialVersionUID = 3257565088071432244L;
    
    /**
     * Error message
     */
    private static final String ERROR_MESSAGE_1 = I18NUtil.getMessage("lock_service.insufficent_privileges");
    private static final String ERROR_MESSAGE_2 = I18NUtil.getMessage("lock_service.unlock_checkedout");

    /**
     * Constructor
     */
    public UnableToReleaseLockException(NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE_1, new Object[]{nodeRef.getId()}));
    }
    
    public enum CAUSE { INSUFFICIENT, CHECKED_OUT };
    
    private static String createMessage(NodeRef nodeRef, CAUSE cause)
    {
        if (cause == null)
        {
            return MessageFormat.format(ERROR_MESSAGE_1, new Object[] { nodeRef.getId() });
        }

        switch (cause)
        {
        case INSUFFICIENT:
            return MessageFormat.format(ERROR_MESSAGE_1, new Object[] { nodeRef.getId() });
        case CHECKED_OUT:
            return MessageFormat.format(ERROR_MESSAGE_2, new Object[] { nodeRef.getId() });
        default:
            return MessageFormat.format(ERROR_MESSAGE_1, new Object[] { nodeRef.getId() });
        }
    }
    
    public UnableToReleaseLockException(NodeRef nodeRef, CAUSE cause)
    {
        super(createMessage(nodeRef, cause));
    }
    
}
