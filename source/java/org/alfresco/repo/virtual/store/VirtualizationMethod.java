
package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implementors define virtualization rules.<br>
 * Virtualization is the process of converting {@link NodeRef}s into
 * {@link Reference}s in the context of given {@link ActualEnvironment} .
 * 
 * @author Bogdan Horje
 */
public interface VirtualizationMethod
{
    /**
     * Determines if a given {@link NodeRef} can be virtualized by this
     * virtualization method.
     * 
     * @param env the environment in which the virtualization should take place
     * @param nodeRef the {@link NodeRef} that should be virtualized
     * @return <code>true</code> if the given {@link NodeRef} can be virtualized
     *         by this virtualization method<br>
     *         <code>false</code> otherwise
     * @throws VirtualizationException
     */
    boolean canVirtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException;

    /**
     * Applies this virtualizatio rule on a given {@link NodeRef}.
     * 
     * @param env the environment in which the virtualization takes place
     * @param nodeRef nodeRef the {@link NodeRef} that will be virtualized
     * @return a {@link Reference} correspondent of the given {@link NodeRef}
     * @throws VirtualizationException
     */
    Reference virtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException;
}
