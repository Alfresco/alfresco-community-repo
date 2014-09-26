/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.relationship;

import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * The relationship service implementation
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipServiceImpl implements RelationshipService
{
    /** Records management admin service */
    private RecordsManagementAdminService recordsManagementAdminService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Namespace prefix resolver */
    private NamespacePrefixResolver namespacePrefixResolver;

    /**
     * Gets the records management admin service instance
     *
     * @return The records management admin service instance
     */
    protected RecordsManagementAdminService getRecordsManagementAdminService()
    {
        return this.recordsManagementAdminService;
    }

    /**
     * Sets the records management admin service instance
     *
     * @param recordsManagementAdminService The records management admin service instance
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }

    /**
     * Gets the dictionary service instance
     *
     * @return The dictionary service instance
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * Sets the dictionary service instance
     *
     * @param dictionaryService The dictionary service instance
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Gets the namespace prefix resolver instance
     *
     * @return The namespace prefix resolver instance
     */
    protected NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return this.namespacePrefixResolver;
    }

    /**
     * Sets the namespace prefix resolver instance
     *
     * @param namespacePrefixResolver The namespace prefix resolver instance
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipDefinitions()
     */
    @Override
    public Set<RelationshipDefinition> getRelationshipDefinitions()
    {
        Set<RelationshipDefinition> relationshipDefinitions = new HashSet<RelationshipDefinition>();

        Map<QName, AssociationDefinition> customReferenceDefinitions = getRecordsManagementAdminService().getCustomReferenceDefinitions();
        for (Map.Entry<QName, AssociationDefinition> customReferenceDefinitionEntry : customReferenceDefinitions.entrySet())
        {
            AssociationDefinition associationDefinition = customReferenceDefinitionEntry.getValue();
            RelationshipDefinition relationshipDefinition = createRelationshipDefinition(associationDefinition);
            if (relationshipDefinition != null)
            {
                relationshipDefinitions.add(relationshipDefinition);
            }
        }

        return relationshipDefinitions;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipDefinition(java.lang.String)
     */
    @Override
    public RelationshipDefinition getRelationshipDefinition(String uniqueName)
    {
        mandatoryString("uniqueName", uniqueName);

        RelationshipDefinition relationshipDefinition = null;

        QName associationDefinitionQName = getRecordsManagementAdminService().getQNameForClientId(uniqueName);
        if (associationDefinitionQName != null)
        {
            AssociationDefinition associationDefinition = getRecordsManagementAdminService().getCustomReferenceDefinitions().get(associationDefinitionQName);
            relationshipDefinition = createRelationshipDefinition(associationDefinition);
        }

        return relationshipDefinition;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#createRelationshipDefinition(org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName)
     */
    @Override
    public RelationshipDefinition createRelationshipDefinition(RelationshipDisplayName displayName)
    {
        mandatory("displayName", displayName);

        RelationshipType type = determineRelationshipTypeFromDisplayName(displayName);

        QName relationshipDefinitionQName;

        switch (type)
        {
            case BIDIRECTIONAL:

                String labelText = displayName.getLabelText();
                relationshipDefinitionQName = getRecordsManagementAdminService().addCustomAssocDefinition(labelText);
                break;

            case PARENTCHILD:

                String sourceText = displayName.getSourceText();
                String targetText = displayName.getTargetText();
                relationshipDefinitionQName = getRecordsManagementAdminService().addCustomChildAssocDefinition(sourceText, targetText);
                break;

            default:

                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type: '")
                    .append(type.toString())
                    .append("'.");
                throw new AlfrescoRuntimeException(sb.toString());
        }

        String uniqueName = relationshipDefinitionQName.getLocalName();

        return new RelationshipDefinitionImpl(uniqueName, type, displayName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#updateReleationshipDefinition(java.lang.String)
     */
    @Override
    public RelationshipDefinition updateReleationshipDefinition(String uniqueName, RelationshipDisplayName displayName)
    {
        mandatoryString("uniqueName", uniqueName);

        QName associationDefinitionQName = getRecordsManagementAdminService().getQNameForClientId(uniqueName);
        if (associationDefinitionQName == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The qualified name for '")
                .append(uniqueName)
                .append("' was not found.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        Map<QName, AssociationDefinition> customReferenceDefinitions = getRecordsManagementAdminService().getCustomReferenceDefinitions();
        AssociationDefinition associationDefinition = customReferenceDefinitions.get(associationDefinitionQName);
        RelationshipType type = getRelationshipType(associationDefinition);
        QName updatedAssociationDefinitionQName;

        switch (type)
        {
            case BIDIRECTIONAL:

                String labelText = displayName.getLabelText();

                if (isBlank(labelText))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Label text '")
                        .append(labelText)
                        .append(" cannot be blank.");
                    throw new AlfrescoRuntimeException(sb.toString());
                }

                updatedAssociationDefinitionQName = getRecordsManagementAdminService().updateCustomAssocDefinition(associationDefinitionQName, labelText);
                break;

            case PARENTCHILD:

                String sourceText = displayName.getSourceText();
                String targetText = displayName.getTargetText();

                if (isBlank(sourceText) || isBlank(targetText))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Neither source text '")
                        .append(sourceText)
                        .append("' nor target text '")
                        .append(targetText)
                        .append(" can be blank.");
                    throw new AlfrescoRuntimeException(sb.toString());
                }

                updatedAssociationDefinitionQName = getRecordsManagementAdminService().updateCustomChildAssocDefinition(associationDefinitionQName, sourceText, targetText);
                break;

            default:

                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type: '")
                    .append(type.toString())
                    .append("'.");
                throw new AlfrescoRuntimeException(sb.toString());
        }

        customReferenceDefinitions = getRecordsManagementAdminService().getCustomReferenceDefinitions();
        AssociationDefinition updatedAssociationDefinition = customReferenceDefinitions.get(updatedAssociationDefinitionQName);
        RelationshipDefinition updatedRelationshipDefinition = createRelationshipDefinition(updatedAssociationDefinition);
        if (updatedRelationshipDefinition == null)
        {
            throw new AlfrescoRuntimeException("The relationship definition was not updated successfully.");
        }

        return updatedRelationshipDefinition;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#removeRelationshipDefinition(java.lang.String)
     */
    @Override
    public boolean removeRelationshipDefinition(String uniqueName)
    {
        mandatoryString("uniqueName", uniqueName);

        // FIXME!!! There is no method on the backend for this. Must be implemented.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#existsRelationshipDefinition(java.lang.String)
     */
    @Override
    public boolean existsRelationshipDefinition(String uniqueName)
    {
        mandatoryString("uniqueName", uniqueName);

        boolean exists = false;

        QName associationDefinitionQName = getRecordsManagementAdminService().getQNameForClientId(uniqueName);
        if (associationDefinitionQName != null)
        {
            Map<QName, AssociationDefinition> customReferenceDefinitions = getRecordsManagementAdminService().getCustomReferenceDefinitions();
            exists = customReferenceDefinitions.containsKey(associationDefinitionQName);
        }

        return exists;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipsFrom(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<Relationship> getRelationshipsFrom(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        Set<Relationship> relationships = new HashSet<Relationship>();

        List<AssociationRef> customReferencesFrom = getRecordsManagementAdminService().getCustomReferencesFrom(nodeRef);
        relationships.addAll(generateRelationshipFromAssociationRef(customReferencesFrom));

        List<ChildAssociationRef> customChildReferences = getRecordsManagementAdminService().getCustomChildReferences(nodeRef);
        relationships.addAll(generateRelationshipFromParentChildAssociationRef(customChildReferences));

        return relationships;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipsTo(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<Relationship> getRelationshipsTo(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        Set<Relationship> relationships = new HashSet<Relationship>();

        List<AssociationRef> customReferencesTo = getRecordsManagementAdminService().getCustomReferencesTo(nodeRef);
        relationships.addAll(generateRelationshipFromAssociationRef(customReferencesTo));

        List<ChildAssociationRef> customParentReferences = getRecordsManagementAdminService().getCustomParentReferences(nodeRef);
        relationships.addAll(generateRelationshipFromParentChildAssociationRef(customParentReferences));

        return relationships;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#addRelationship(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addRelationship(String uniqueName, NodeRef source, NodeRef target)
    {
        mandatoryString("uniqueName", uniqueName);
        mandatory("source", source);
        mandatory("target", target);

        QName associationDefinitionQName = getRecordsManagementAdminService().getQNameForClientId(uniqueName);
        getRecordsManagementAdminService().addCustomReference(source, target, associationDefinitionQName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#removeRelationship(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeRelationship(String uniqueName, NodeRef source, NodeRef target)
    {
        mandatoryString("uniqueName", uniqueName);
        mandatory("source", source);
        mandatory("target", target);

        QName associationDefinitionQName = getRecordsManagementAdminService().getQNameForClientId(uniqueName);
        getRecordsManagementAdminService().removeCustomReference(source, target, associationDefinitionQName);
    }

    /**
     * Creates the relationship definition from the association definition
     *
     * @param associationDefinition The association definition
     * @return The relationship definition if <code>associationDefinition</code> exists, <code>null</code> otherwise
     */
    private RelationshipDefinition createRelationshipDefinition(AssociationDefinition associationDefinition)
    {
        RelationshipDefinition relationshipDefinition = null;

        if (associationDefinition != null)
        {
            String uniqueName = associationDefinition.getName().getLocalName();

            RelationshipType type = getRelationshipType(associationDefinition);

            String title = associationDefinition.getTitle(getDictionaryService());
            RelationshipDisplayName displayName = getRelationshipDisplayName(type, title);

            relationshipDefinition = new RelationshipDefinitionImpl(uniqueName, type, displayName);
        }

        return relationshipDefinition;
    }

    /**
     * Gets the relationship type from the association definition
     *
     * @param associationDefinition The association definition
     * @return The type of the relationship definition
     */
    private RelationshipType getRelationshipType(AssociationDefinition associationDefinition)
    {
        RelationshipType type;

        if (associationDefinition instanceof ChildAssociationDefinition)
        {
            type = RelationshipType.PARENTCHILD;
        }
        else if (associationDefinition instanceof AssociationDefinition)
        {
            type = RelationshipType.BIDIRECTIONAL;
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unsupported association definition: '")
                .append(associationDefinition.getName().getLocalName())
                .append("'.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        return type;
    }

    /**
     * Gets the relationship display name of the relationship definition
     *
     * @param type The type of the relationship definition
     * @param title The title of the association definition
     * @return The relationship display name of the relationship definition
     */
    private RelationshipDisplayName getRelationshipDisplayName(RelationshipType type, String title)
    {
        String sourceText = null;
        String targetText = null;
        String labelText = null;

        switch (type)
        {
            case BIDIRECTIONAL:

                labelText = title;
                break;

            case PARENTCHILD:

                String[] sourceAndTarget = getRecordsManagementAdminService().splitSourceTargetId(title);
                sourceText = sourceAndTarget[0];
                targetText = sourceAndTarget[1];
                break;

            default:

                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type: '")
                    .append(type.toString())
                    .append("'.");
                throw new AlfrescoRuntimeException(sb.toString());
        }

        return new RelationshipDisplayName(sourceText, targetText, labelText);
    }

    /**
     * Generates relationships from the given association references
     *
     * @param associationRefs Association references
     * @return Relationships generated from the given association references
     */
    private Set<Relationship> generateRelationshipFromAssociationRef(List<AssociationRef> associationRefs)
    {
        Set<Relationship> relationships = new HashSet<Relationship>();

        for (AssociationRef associationRef : associationRefs)
        {
            String uniqueName = associationRef.getTypeQName().getLocalName();
            NodeRef from = associationRef.getSourceRef();
            NodeRef to = associationRef.getTargetRef();
            relationships.add(new RelationshipImpl(uniqueName, from, to));
        }

        return relationships;
    }

    /**
     * Generates relationships from the given child association references
     *
     * @param childAssociationRefs Child association references
     * @return Relationships generated from the given child association references
     */
    private Set<Relationship> generateRelationshipFromParentChildAssociationRef(List<ChildAssociationRef> childAssociationRefs)
    {
        Set<Relationship> relationships = new HashSet<Relationship>();

        for (ChildAssociationRef childAssociationRef : childAssociationRefs)
        {
            String uniqueName = childAssociationRef.getQName().getLocalName();
            NodeRef from = childAssociationRef.getParentRef();
            NodeRef to = childAssociationRef.getChildRef();
            relationships.add(new RelationshipImpl(uniqueName, from, to));
        }

        return relationships;
    }

    /**
     * Determines the relationship type from the display name
     *
     * @param displayName The display name of the relationship
     * @return The relationship type from the display name
     */
    private RelationshipType determineRelationshipTypeFromDisplayName(RelationshipDisplayName displayName)
    {
        RelationshipType relationshipType;

        String labelText = displayName.getLabelText();
        String sourceText = displayName.getSourceText();
        String targetText = displayName.getTargetText();

        String errorMsg = "Relationship type could not be determined from the display name. It is neither biderectional nor parent/child relationship";

        if (isBlank(labelText))
        {
            if (isBlank(sourceText) || isBlank(targetText))
            {
                throw new AlfrescoRuntimeException(errorMsg);
            }
            relationshipType = RelationshipType.PARENTCHILD;
        }
        else
        {
            if (isNotBlank(sourceText) || isNotBlank(targetText))
            {
                throw new AlfrescoRuntimeException(errorMsg);
            }
            relationshipType = RelationshipType.BIDIRECTIONAL;
        }

        return relationshipType;
    }
}
