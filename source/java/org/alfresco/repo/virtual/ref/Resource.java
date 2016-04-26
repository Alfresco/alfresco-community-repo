
package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;

/**
 * A {@link Reference} element that identifies the main or a parameter content
 * location. <br>
 * The semantics of the resource is given by {@link Reference} protocol.
 * 
 * @author Bogdan Horje
 */
public interface Resource
{
    /** 
     * Returns the String representation of the resource.
     * 
     * @param stringifier
     * @return 
     * @throws ReferenceEncodingException
     */
    String stringify(Stringifier stringifier) throws ReferenceEncodingException;

    /**
     * Processes the Resource with a {@link ResourceProcessor}. This method has
     * the role of the accept method in the Visitor pattern, in this case the
     * Visitor being the {@link ResourceProcessor} and the Element - the
     * Resource.
     * 
     * @param processor 
     * @return
     * @throws ResourceProcessingError
     */
    <R> R processWith(ResourceProcessor<R> processor) throws ResourceProcessingError;

    InputStream asStream(ActualEnvironment environment) throws ActualEnvironmentException;
}
