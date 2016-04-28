/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp.exception;

import org.alfresco.update.tool.dircomp.FileTreeCompare;

/**
 * Exception class representing failures during file tree comparison.
 * 
 * @see FileTreeCompare
 * @author Matt Ward
 */
public class FileTreeCompareException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public FileTreeCompareException(String message)
    {
        super(message);
    }
    
    public FileTreeCompareException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
