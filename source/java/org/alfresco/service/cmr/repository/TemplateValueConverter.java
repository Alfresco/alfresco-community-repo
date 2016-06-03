package org.alfresco.service.cmr.repository;

/**
 * A service capable of converting Java objects for use within a template.
 * 
 * @author dward
 */
public interface TemplateValueConverter
{

    /**
     * Converts a Java object (e.g. one produced by a method call) to one suitable for use within a template.
     * 
     * @param value
     *            the Java object to convert
     * @param imageResolver
     *            the image resolver
     * @return the converted object
     */
    public Object convertValue(Object value, TemplateImageResolver imageResolver);
}
