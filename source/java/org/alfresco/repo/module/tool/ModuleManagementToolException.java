package org.alfresco.repo.module.tool;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Module Management Tool Exception class
 * 
 * @author Roy Wetherall
 */
public class ModuleManagementToolException extends AlfrescoRuntimeException 
{
    /**
	 * Serial version UID 
	 */
    private static final long serialVersionUID = -4329693103965834085L;

    public ModuleManagementToolException(String msgId)
    {
        super(msgId);
    }

    public ModuleManagementToolException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public ModuleManagementToolException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public ModuleManagementToolException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
