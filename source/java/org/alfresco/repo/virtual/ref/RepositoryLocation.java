
package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An Alfresco repository content abstraction.
 * 
 * @author Bogdan Horje
 */
public interface RepositoryLocation
{
    String stringify(Stringifier stringifier) throws ReferenceEncodingException;

    InputStream openContentStream(ActualEnvironment environment) throws ActualEnvironmentException;

    NodeRef asNodeRef(ActualEnvironment environment) throws ActualEnvironmentException;
}
