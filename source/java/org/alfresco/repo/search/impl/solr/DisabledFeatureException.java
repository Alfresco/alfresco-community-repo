package org.alfresco.repo.search.impl.solr;

/**
 * Identifies an attempt to use a disabled feature.
 * 
 * @author Matt Ward
 */
public class DisabledFeatureException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    DisabledFeatureException(String message)
    {
        super(message);
    }
}