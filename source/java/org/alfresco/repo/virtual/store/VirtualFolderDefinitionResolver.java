
package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.template.VirtualFolderDefinition;

/**
 * A {@link Reference} based {@link VirtualFolderDefinition} resolver.<br>
 *
 * @author Bogdan Horje
 */
public interface VirtualFolderDefinitionResolver
{
    /**
     * @param reference
     * @return the {@link VirtualFolderDefinition} of the given
     *         {@link Reference} considering inner paths
     * @throws VirtualizationException
     */
    VirtualFolderDefinition resolveVirtualFolderDefinition(Reference reference) throws VirtualizationException;
}
