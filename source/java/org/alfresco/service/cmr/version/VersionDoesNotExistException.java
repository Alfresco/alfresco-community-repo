package org.alfresco.service.cmr.version;

import java.text.MessageFormat;


/**
 * Version does not exist exception class.
 * 
 * @author Roy Wetherall
 */
public class VersionDoesNotExistException extends VersionServiceException
{
    private static final long serialVersionUID = 3258133548417233463L;
    private static final String ERROR_MESSAGE = "The version with label {0} does not exisit in the version store.";

    /**
     * Constructor
     */
    public VersionDoesNotExistException(String versionLabel)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{versionLabel}));
    }
}
