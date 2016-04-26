
package org.alfresco.repo.forms.processor;

/**
 * Interface definition for a helper class that handles persisting form data.
 * 
 * @since 3.4
 * @author Nick Smith
 *
 * @param <PersistType>
 */
public interface FormPersister<PersistType>
{
    PersistType persist();
}
