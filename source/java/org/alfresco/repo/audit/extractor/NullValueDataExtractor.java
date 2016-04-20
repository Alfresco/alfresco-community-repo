package org.alfresco.repo.audit.extractor;

import java.io.Serializable;

/**
 * An extractor that merely records a null value.  This enables configuration such
 * that the <i>presence</i> of a data path can be recorded when the actual value
 * is of no interest.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class NullValueDataExtractor extends AbstractDataExtractor
{
    /**
     * @return          Returns <tt>true</tt> always
     */
    public boolean isSupported(Serializable data)
    {
        return true;
    }

    /**
     * @return          Returns <tt>null</tt> always
     */
    public Serializable extractData(Serializable in) throws Throwable
    {
        return null;
    }
}
