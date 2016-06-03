
package org.alfresco.repo.forms.processor.node;

/**
 * Interface definition for an object that retrieves a transient filed value.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public interface TransientValueGetter
{
    Object getTransientValue(String name);
}
