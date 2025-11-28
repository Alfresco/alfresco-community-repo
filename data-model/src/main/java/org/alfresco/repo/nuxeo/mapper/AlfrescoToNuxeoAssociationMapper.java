/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.nuxeo.mapper;

import org.alfresco.repo.dictionary.M2ChildAssociation;
import org.alfresco.repo.dictionary.M2Association;
import org.alfresco.repo.nuxeo.config.MappingContext;

/**
 * Maps Alfresco associations to Nuxeo relations and containment.
 * Handles both child associations (parent-child) and peer associations (references).
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoAssociationMapper
{
    /**
     * Maps an Alfresco child association to Nuxeo parent-child containment.
     *
     * @param childAssociation Alfresco child association
     * @param context mapping context
     * @return description of the mapping
     */
    public String mapChildAssociation(M2ChildAssociation childAssociation, MappingContext context)
    {
        if (childAssociation == null)
        {
            return null;
        }

        StringBuilder mapping = new StringBuilder();
        mapping.append("Child Association: ").append(childAssociation.getName()).append("\n");
        mapping.append("  Mapped to: Nuxeo parent-child containment\n");
        mapping.append("  Target Type: ").append(childAssociation.getTargetClassName()).append("\n");
        
        // Determine if this is a primary or secondary child
        // Note: M2ChildAssociation doesn't have a direct isPrimary() method
        // This would need to be determined from the association configuration
        mapping.append("  Containment Type: Parent-child hierarchy\n");
        
        // Check cardinality
        if (childAssociation.isTargetMany() != null && childAssociation.isTargetMany())
        {
            mapping.append("  Cardinality: One parent, many children\n");
        }
        else
        {
            mapping.append("  Cardinality: One parent, one child\n");
        }
        
        // Add warning about association metadata
        context.addWarning(String.format(
            "Child association '%s': Nuxeo containment may not support all association metadata. " +
            "Consider using document properties for metadata.", childAssociation.getName()));
        
        return mapping.toString();
    }

    /**
     * Maps an Alfresco peer association to a Nuxeo relation.
     *
     * @param association Alfresco peer association
     * @param context mapping context
     * @return description of the mapping
     */
    public String mapPeerAssociation(M2Association association, MappingContext context)
    {
        if (association == null)
        {
            return null;
        }

        StringBuilder mapping = new StringBuilder();
        mapping.append("Peer Association: ").append(association.getName()).append("\n");
        mapping.append("  Mapped to: Nuxeo Relation\n");
        mapping.append("  Source: Current document\n");
        mapping.append("  Target Type: ").append(association.getTargetClassName()).append("\n");
        
        // Determine cardinality
        String cardinality = determineCardinality(association);
        mapping.append("  Cardinality: ").append(cardinality).append("\n");
        
        // Determine directionality
        boolean isSourceMany = association.isSourceMany() != null && association.isSourceMany();
        boolean isTargetMany = association.isTargetMany() != null && association.isTargetMany();
        
        if (isSourceMany || isTargetMany)
        {
            mapping.append("  Directionality: Bidirectional (may require)\n");
        }
        else
        {
            mapping.append("  Directionality: Unidirectional\n");
        }
        
        // Add warning about relation implementation
        context.addWarning(String.format(
            "Peer association '%s': Nuxeo relations are implemented separately from document hierarchy. " +
            "Querying may differ from Alfresco.", association.getName()));
        
        return mapping.toString();
    }

    /**
     * Determines the cardinality of an association.
     *
     * @param association Alfresco association
     * @return cardinality description
     */
    private String determineCardinality(M2Association association)
    {
        boolean isSourceMany = association.isSourceMany() != null && association.isSourceMany();
        boolean isTargetMany = association.isTargetMany() != null && association.isTargetMany();
        
        if (isSourceMany && isTargetMany)
        {
            return "Many-to-Many (N:M)";
        }
        else if (isSourceMany)
        {
            return "Many-to-One (N:1)";
        }
        else if (isTargetMany)
        {
            return "One-to-Many (1:N)";
        }
        else
        {
            return "One-to-One (1:1)";
        }
    }

    /**
     * Generates a Nuxeo relation name from an Alfresco association name.
     *
     * @param associationName Alfresco association name
     * @return Nuxeo relation name
     */
    public String generateRelationName(String associationName)
    {
        if (associationName == null)
        {
            return "custom_relation";
        }

        // Extract local name and prefix
        String localName = extractLocalName(associationName);
        String prefix = extractPrefix(associationName);
        
        // Nuxeo relation name format
        return prefix + "_" + localName + "_relation";
    }

    /**
     * Extracts the prefix from a qualified name.
     *
     * @param qualifiedName qualified name
     * @return prefix
     */
    private String extractPrefix(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "custom";
        }

        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0)
        {
            return qualifiedName.substring(0, colonIndex);
        }

        return "custom";
    }

    /**
     * Extracts the local name from a qualified name.
     *
     * @param qualifiedName qualified name
     * @return local name
     */
    private String extractLocalName(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "relation";
        }

        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0 && colonIndex < qualifiedName.length() - 1)
        {
            return qualifiedName.substring(colonIndex + 1);
        }

        return qualifiedName;
    }
}
