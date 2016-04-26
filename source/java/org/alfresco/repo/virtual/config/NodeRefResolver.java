
package org.alfresco.repo.virtual.config;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Dependency inversion of the Provision of Repository Context.<br>
 *
 * @see Repository
 */
public interface NodeRefResolver
{

    /**
     * Path type reference create if absent. Fail substitute for
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @param reference path element names array
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef createNamePath(String[] reference);

    /**
     * QName type reference create if absent.Fail safe substitute for
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @param reference path element qnames array
     * @param names names to be used when creating the given path. If less than
     *            reference elements they will be matched from the end of the
     *            reference path.
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef createQNamePath(String[] reference, String[] names);

    /**
     * Node type explicit inversion of
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef resolveNodeReference(String[] reference);

    /**
     * Path type explicit inversion of
     * {@link Repository#findNodeRef(String, String[])}.
     * 
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef resolvePathReference(String[] reference);

    /**
     * QName type explicit inversion of
     * {@link Repository#findNodeRef(String, String[])}.<br>
     * Unlike {@link Repository} {@link NodeRefResolver} implementors must
     * provide an adequate implementation.
     * 
     * @return reference array of reference segments
     * @throws AlfrescoRuntimeException if an unimplemented or invalid reference
     *             type is provided
     * @see Repository#findNodeRef(String, String[])
     */
    NodeRef resolveQNameReference(String[] reference);

    /**
     * Gets the Company Home. Note this is tenant-aware if the correct Cache is
     * supplied.
     * 
     * @return company home node ref
     */
    NodeRef getCompanyHome();

    /**
     * Gets the root home of the company home store
     * 
     * @return root node ref
     */
    NodeRef getRootHome();

    /**
     * Gets the Shared Home. Note this is tenant-aware if the correct Cache is
     * supplied.
     * 
     * @return shared home node ref
     */
    NodeRef getSharedHome();

    /**
     * Gets the user home of the currently authenticated person
     * 
     * @param person person
     * @return user home of person
     */
    NodeRef getUserHome(NodeRef person);

}
