package org.alfresco.repo.audit.generator;

import java.io.Serializable;
import java.util.Date;

/**
 * Gives back the currently time.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class SystemTimeDataGenerator extends AbstractDataGenerator
{
    /**
     * @return              Returns the current time
     */
    public Serializable getData() throws Throwable
    {
        return new Date();
    }
}
