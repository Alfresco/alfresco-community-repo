package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

/**
 * An extractor that supports all values and does not conversion.
 * This implementation can be used as a base class, although there is little
 * abstraction necessary for the converters in general.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class SimpleValueDataExtractor extends AbstractDataExtractor
{
    /**
     * @return          Returns <tt>true</tt> always
     */
    public boolean isSupported(Serializable data)
    {
        return true;
    }

    /**
     * Just returns the value unchanged
     */
    public Serializable extractData(Serializable in) throws Throwable
    {
        return in;
    }
}
