/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.copy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * This is a base class for all the copy services. Introduces general methods for collecting all the properties, which are required to perform a copying
 * 
 * @author Dmitry Velichkevich
 * @since 4.1.5
 */
public class AbstractBaseCopyService
{
    private Set<String> systemNamespaces;

    public AbstractBaseCopyService()
    {
        systemNamespaces = new HashSet<String>(5);
    }

    /**
     * Set the namespaces that should be treated as 'system' namespaces.
     * <p>
     * When files or folders are renamed, the association path (QName) is normally modified to follow the name of the node.
     * If, however, the namespace of the patch QName is in this list, the association path is left alone. This allows parts
     * of the application to use well-known paths even if the end-user is able to modify the objects <b>cm:name</b> value.
     * 
     * @param systemNamespaces a list of system namespaces
     */
    public void setSystemNamespaces(List<String> systemNamespaces)
    {
        this.systemNamespaces.addAll(systemNamespaces);
    }

    public List<String> getSystemNamespaces()
    {
        return new LinkedList<String>(systemNamespaces);
    }

    /**
     * Calculates {@link QName} type of target association, which will be created after copying
     * 
     * @param sourceNodeRef             the node that will be copied (never <tt>null</tt>)
     * @param sourceParentRef           the parent of the node being copied (may be <tt>null</tt>)
     * @param newName                   the planned new name of the node
     * @param nameChanged               <tt>true</tt> if the name of the node is being changed
     * @return                          Returns the path part for a new association and the effective
     *                                  primary parent association that was used
     */
    protected AssociationCopyInfo getAssociationCopyInfo(
            NodeService nodeService,
            NodeRef sourceNodeRef,
            NodeRef sourceParentRef,
            String newName, boolean nameChanged)
    {
        // we need the current association type
        ChildAssociationRef primaryAssocRef = nodeService.getPrimaryParent(sourceNodeRef);

        // Attempt to find a template association reference for the new association
        ChildAssociationRef sourceParentAssocRef = primaryAssocRef;
        if (sourceParentRef != null)
        {
            // We have been given a source parent node
            boolean copyingFromPrimaryParent = sourceParentRef.equals(primaryAssocRef.getParentRef());
            if (!copyingFromPrimaryParent)
            {
                // We are not copying from the primary parent.
                // Find a random association to the source parent to use as a template
                List<ChildAssociationRef> assocList = nodeService.getParentAssocs(sourceNodeRef);
                for (ChildAssociationRef assocListEntry : assocList)
                {
                    if (sourceParentRef.equals(assocListEntry.getParentRef()))
                    {
                        sourceParentAssocRef = assocListEntry;
                        break;
                    }
                }
            }
        }

        QName targetAssocQName = null;
        QName existingQName = sourceParentAssocRef.getQName();
        if (nameChanged && !systemNamespaces.contains(existingQName.getNamespaceURI()))
        {
            // Change the localname to match the new name
            targetAssocQName = QName.createQName(sourceParentAssocRef.getQName().getNamespaceURI(), QName.createValidLocalName(newName));
        }
        else
        {
            // Keep the localname
            targetAssocQName = existingQName;
        }

        return new AssociationCopyInfo(targetAssocQName, sourceParentAssocRef);
    }

    /**
     * Simple container for storing data required to copy a node, including the parent association that will be copied along with
     * the new path part of the association that will be created by the copy.
     * <p/>
     * This container is immutable.
     * 
     * @author Dmitry Velichkevich
     * @since 4.1.5
     * 
     * @see AbstractBaseCopyService#getAssociationCopyInfo(NodeService, NodeRef, NodeRef, String, boolean)
     */
    public static class AssociationCopyInfo
    {
        private final QName targetAssocQName;
        private final ChildAssociationRef sourceParentAssoc;

        public AssociationCopyInfo(QName targetAssocQName, ChildAssociationRef sourceParentAssoc)
        {
            this.targetAssocQName = targetAssocQName;
            this.sourceParentAssoc = sourceParentAssoc;
        }

        /**
         * Get the path part of the association that should be created for the copied node
         */
        public QName getTargetAssocQName()
        {
            return targetAssocQName;
        }

        /**
         * Get the association that will be copied.
         */
        public ChildAssociationRef getSourceParentAssoc()
        {
            return sourceParentAssoc;
        }
    }
}
