
package org.alfresco.repo.virtual.ref;

/**
 * Generic {@link Resource} visitor. It ensures the processing of different
 * types of resources. <br>
 * 
 * @author Bogdan Horje
 * @param <R>
 */
public interface ResourceProcessor<R>
{
    /**
     * Processes a resource of type {@link Resource}.
     * 
     * @param resource a {@link Resource} to be processed.
     * @return generic parameter R that implementors are parameterised with.
     * @throws ResourceProcessingError
     */
    R process(Resource resource) throws ResourceProcessingError;

    /**
     * Processes a resource of type {@link ClasspathResource}.
     * 
     * @param classpath the {@link ClasspathResource} to be processed.
     * @return generic parameter R that implementors are parameterised with.
     * @throws ResourceProcessingError
     */
    R process(ClasspathResource classpath) throws ResourceProcessingError;

    /**
     * Processes a resource of type {@link RepositoryResource}.
     * 
     * @param repository a {@link RepositoryResource} to be processed.
     * @return generic parameter R that implementors are parameterised with.
     * @throws ResourceProcessingError
     */
    R process(RepositoryResource repository) throws ResourceProcessingError;
}
