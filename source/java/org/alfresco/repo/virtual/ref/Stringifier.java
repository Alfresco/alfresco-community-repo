
package org.alfresco.repo.virtual.ref;

import java.io.Serializable;
import java.util.List;

/**
 * A {@link Reference} abstract tree visitor designed to produce custom string
 * representations.
 * 
 * @author Bogdan Horje
 */
public interface Stringifier extends Serializable
{
    String stringify(Reference reference) throws ReferenceEncodingException;

    String stringify(Resource resource) throws ReferenceEncodingException;

    String stringifyResource(Resource resource) throws ReferenceEncodingException;

    String stringifyResource(RepositoryResource resource) throws ReferenceEncodingException;

    String stringifyResource(ClasspathResource resource) throws ReferenceEncodingException;

    String stringify(RepositoryLocation repositoryLocation) throws ReferenceEncodingException;

    String stringifyRepositoryLocation(RepositoryLocation repositoryLocation) throws ReferenceEncodingException;

    String stringifyRepositoryLocation(RepositoryNodeRef repositoryNodeRef) throws ReferenceEncodingException;

    String stringifyRepositoryLocation(RepositoryPath repositoryPath) throws ReferenceEncodingException;

    String stringify(List<Parameter> parameters) throws ReferenceEncodingException;

    String stringify(Parameter parameter) throws ReferenceEncodingException;

    String stringifyParameter(Parameter parameter) throws ReferenceEncodingException;

    String stringifyParameter(ResourceParameter resourceParameter) throws ReferenceEncodingException;

    String stringifyParameter(StringParameter stringParameter) throws ReferenceEncodingException;

    String stringifyParameter(ReferenceParameter referenceParameter) throws ReferenceEncodingException;
}
