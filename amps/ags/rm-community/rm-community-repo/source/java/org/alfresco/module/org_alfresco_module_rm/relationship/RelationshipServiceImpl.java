/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.relationship;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_FROZEN;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeCreateReference;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRemoveReference;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRemoveReference;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminBase;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.PoliciesUtil;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2ClassAssociation;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;

/**
 * The relationship service implementation
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipServiceImpl extends RecordsManagementAdminBase implements RelationshipService
{
    /** Policy component */
    private PolicyComponent policyComponent;

    /**
     * Gets the policy component instance
     *
     * @return The policy component instance
     */
    private PolicyComponent getPolicyComponent()
    {
        return this.policyComponent;
    }

    /**
     * Sets the policy component instance
     *
     * @param policyComponent The policy component instance
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /** Policy delegates */
    private ClassPolicyDelegate<BeforeCreateReference> beforeCreateReferenceDelegate;
    private ClassPolicyDelegate<OnCreateReference> onCreateReferenceDelegate;
    private ClassPolicyDelegate<BeforeRemoveReference> beforeRemoveReferenceDelegate;
    private ClassPolicyDelegate<OnRemoveReference> onRemoveReferenceDelegate;

    /**
     * Initialisation method
     */
    public void init()
    {
        // Register the various policies
        beforeCreateReferenceDelegate = getPolicyComponent().registerClassPolicy(BeforeCreateReference.class);
        onCreateReferenceDelegate = getPolicyComponent().registerClassPolicy(OnCreateReference.class);
        beforeRemoveReferenceDelegate = getPolicyComponent().registerClassPolicy(BeforeRemoveReference.class);
        onRemoveReferenceDelegate = getPolicyComponent().registerClassPolicy(OnRemoveReference.class);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipDefinitions()
     */
    @Override
    public Set<RelationshipDefinition> getRelationshipDefinitions()
    {
        Set<RelationshipDefinition> relationshipDefinitions = new HashSet<RelationshipDefinition>();

        Set<Entry<QName, AssociationDefinition>> associationsEntrySet = getCustomAssociations().entrySet();
        for (Map.Entry<QName, AssociationDefinition> associationEntry : associationsEntrySet)
        {
            AssociationDefinition associationDefinition = associationEntry.getValue();
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

        AssociationDefinition associationDefinition = getAssociationDefinition(uniqueName);
        if (associationDefinition != null)
        {
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

        String title;
        RelationshipType type = determineRelationshipTypeFromDisplayName(displayName);

        switch (type)
        {
            case BIDIRECTIONAL:

                title = displayName.getSourceText();
                break;

            case PARENTCHILD:

                String sourceText = displayName.getSourceText();
                String targetText = displayName.getTargetText();
                title = composeAssociationDefinitionTitle(sourceText, targetText);
                break;

            default:

                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type: '")
                    .append(type.toString())
                    .append("'.");
                throw new AlfrescoRuntimeException(sb.toString());
        }

        // If this title is already taken...
        if (existsTitle(title))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot create a relationship definition for the display name: '")
                .append(displayName.toString())
                .append("' as there is already a relationship definition with this display name.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        // Defaults to RM_CUSTOM_URI
        NodeRef modelRef = getCustomModelRef("");
        M2Model deserializedModel = readCustomContentModel(modelRef);
        String customAspectName = ASPECT_CUSTOM_ASSOCIATIONS.toPrefixString(getNamespaceService());
        M2Aspect customAssocsAspect = deserializedModel.getAspect(customAspectName);

        if (customAssocsAspect == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The aspect: '")
                .append(customAspectName)
                .append("' is undefined.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        QName relationshipDefinitionQName = generateRelationshipDefinitionQNameFor(title);
        String generatedShortQName = relationshipDefinitionQName.toPrefixString(getNamespaceService());

        M2ClassAssociation customAssoc = customAssocsAspect.getAssociation(generatedShortQName);
        if (customAssoc != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The association: '")
                .append(customAssoc.getName())
                .append("' already exists.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        M2ClassAssociation newAssoc;

        switch (type)
        {
            case BIDIRECTIONAL:

                newAssoc = customAssocsAspect.createAssociation(generatedShortQName);
                break;

            case PARENTCHILD:

                newAssoc = customAssocsAspect.createChildAssociation(generatedShortQName);
                break;

            default:

                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type: '")
                    .append(type.toString())
                    .append("'.");
                throw new AlfrescoRuntimeException(sb.toString());
        }

        newAssoc.setSourceMandatory(false);
        newAssoc.setTargetMandatory(false);

        // MOB-1573
        newAssoc.setSourceMany(true);
        newAssoc.setTargetMany(true);

        newAssoc.setTitle(title);
        newAssoc.setTargetClassName(RecordsManagementModel.ASPECT_RECORD.toPrefixString(getNamespaceService()));
        writeCustomContentModel(modelRef, deserializedModel);

        return new RelationshipDefinitionImpl(relationshipDefinitionQName.getLocalName(), type, displayName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#updateRelationshipDefinition(java.lang.String, RelationshipDisplayName)
     */
    @Override
    public RelationshipDefinition updateRelationshipDefinition(String uniqueName, RelationshipDisplayName displayName)
    {
        mandatoryString("uniqueName", uniqueName);

        RelationshipDefinition relationshipDefinition = getRelationshipDefinition(uniqueName);
        if (relationshipDefinition == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The relationship definition for the unique name '")
                .append(uniqueName)
                .append("' was not found.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        String title;
        RelationshipType type = relationshipDefinition.getType();

        switch (type)
        {
            case BIDIRECTIONAL:

                title = displayName.getSourceText();

                if (isBlank(title))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Label text '")
                        .append(title)
                        .append(" cannot be blank.");
                    throw new AlfrescoRuntimeException(sb.toString());
                }

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

                title = composeAssociationDefinitionTitle(sourceText, targetText);

                break;

            default:

                StringBuilder sb = new StringBuilder();
                sb.append("Unsupported relationship type: '")
                    .append(type.toString())
                    .append("'.");
                throw new AlfrescoRuntimeException(sb.toString());
        }

        if (existsTitle(title))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot update the relationship definition as '")
                .append(title)
                .append("' already exists.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        QName associationDefinitionQName = getAssociationDefinitionName(uniqueName);
        QName updatedAssociationDefinitionQName = persistUpdatedAssocTitle(associationDefinitionQName, title);
        RelationshipDefinition updatedRelationshipDefinition = getRelationshipDefinition(updatedAssociationDefinitionQName.getLocalName());

        if (updatedRelationshipDefinition == null)
        {
            throw new AlfrescoRuntimeException("The relationship definition could not be updated successfully.");
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

        throw new UnsupportedOperationException("It is not possible to remove a relationship.");
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#existsRelationshipDefinition(java.lang.String)
     */
    @Override
    public boolean existsRelationshipDefinition(String uniqueName)
    {
        mandatoryString("uniqueName", uniqueName);

        boolean exists = false;

        RelationshipDefinition relationshipDefinition = getRelationshipDefinition(uniqueName);
        if (relationshipDefinition != null)
        {
            exists = true;
        }

        return exists;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipsFrom(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<Relationship> getRelationshipsFrom(NodeRef nodeRef)
    {
        return getRelationshipsFrom(nodeRef, null);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipsFrom(org.alfresco.service.cmr.repository.NodeRef, String)
     */
    @Override
    public Set<Relationship> getRelationshipsFrom(NodeRef nodeRef, String nameFilter)
    {
        mandatory("nodeRef", nodeRef);

        Set<Relationship> relationships = new HashSet<Relationship>();

        List<AssociationRef> customReferencesFrom = getNodeService().getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        relationships.addAll(generateRelationshipFromAssociationRef(customReferencesFrom, nameFilter));

        List<ChildAssociationRef> customChildReferences = getNodeService().getChildAssocs(nodeRef);
        relationships.addAll(generateRelationshipFromParentChildAssociationRef(customChildReferences, nameFilter));

        return relationships;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipsTo(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<Relationship> getRelationshipsTo(NodeRef nodeRef)
    {
        return getRelationshipsTo(nodeRef, null);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService#getRelationshipsTo(org.alfresco.service.cmr.repository.NodeRef, String)
     */
    @Override
    public Set<Relationship> getRelationshipsTo(NodeRef nodeRef, String nameFilter)
    {
        mandatory("nodeRef", nodeRef);

        Set<Relationship> relationships = new HashSet<Relationship>();

        List<AssociationRef> customReferencesTo = getNodeService().getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        relationships.addAll(generateRelationshipFromAssociationRef(customReferencesTo, nameFilter));

        List<ChildAssociationRef> customParentReferences = getNodeService().getParentAssocs(nodeRef);
        relationships.addAll(generateRelationshipFromParentChildAssociationRef(customParentReferences, nameFilter));

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
        
        // check the source node exists
        if (!getNodeService().exists(source))
        {
            throw new AlfrescoRuntimeException("Can't create relationship '" + uniqueName + "', because source node doesn't exist.");
        }
        
        // check the target node exists
        if (!getNodeService().exists(target))
        {
            throw new AlfrescoRuntimeException("Can't create relationship " + uniqueName + ", because target node doesn't exist.");
        }
        
        if (getNodeService().hasAspect(target, ASPECT_FROZEN))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Relationship cannot be created as the target '").
                append(getNodeService().getProperty(target, ContentModel.PROP_NAME)).
                append("' is in a hold.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        // Check that the association definition for the given unique name exists.
        AssociationDefinition associationDefinition = getAssociationDefinition(uniqueName);
        if (associationDefinition == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("No association definition found for '").
                append(uniqueName).
                append("'.");
            throw new IllegalArgumentException(sb.toString());
        }

        // Get the association definition name
        QName associationDefinitionName = associationDefinition.getName();

        // Check if an instance of this association already exists in the same direction
        boolean associationAlreadyExists = associationExists(associationDefinition, source, target);

        if (associationAlreadyExists)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Association '").
                append(associationDefinitionName.getLocalName()).
                append("' already exists from '").
                append(source).
                append("' to '").
                append(target).
                append("'.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        // Invoke before create reference policy
        invokeBeforeCreateReference(source, target, associationDefinitionName);

        if (associationDefinition.isChild())
        {
            getNodeService().addChild(source, target, associationDefinitionName, associationDefinitionName);
        }
        else
        {
            getNodeService().createAssociation(source, target, associationDefinitionName);
        }

        // Invoke on create reference policy
        invokeOnCreateReference(source, target, associationDefinitionName);
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

        // Check that the association definition for the given unique name exists.
        AssociationDefinition associationDefinition = getAssociationDefinition(uniqueName);
        if (associationDefinition == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("No association definition found for '").
                append(uniqueName).
                append("'.");
            throw new IllegalArgumentException(sb.toString());
        }

        // Get the association definition name
        final QName associationDefinitionName = associationDefinition.getName();
        final NodeRef targetNode = target;
        final NodeRef sourceNode = source;

        invokeBeforeRemoveReference(sourceNode, targetNode, associationDefinitionName);

        if (associationDefinition.isChild())
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    List<ChildAssociationRef> children = getNodeService().getChildAssocs(sourceNode);
                    for (ChildAssociationRef chRef : children)
                    {
                        if (associationDefinitionName.equals(chRef.getTypeQName()) && chRef.getChildRef().equals(targetNode))
                        {
                            getNodeService().removeChildAssociation(chRef);
                        }
                    }

                    return null;
                }
            });
        }
        else
        {
            getNodeService().removeAssociation(source, targetNode, associationDefinitionName);
        }

        invokeOnRemoveReference(source, targetNode, associationDefinitionName);
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
        else
        {
            type = RelationshipType.BIDIRECTIONAL;
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

        switch (type)
        {
            case BIDIRECTIONAL:

                sourceText = title;
                targetText = title;
                break;

            case PARENTCHILD:

                String[] sourceAndTarget = splitAssociationDefinitionTitle(title);
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

        return new RelationshipDisplayName(sourceText, targetText);
    }

    /**
     * Generates relationships from the given association references
     *
     * @param associationRefs Association references
     * @return Relationships generated from the given association references
     */
    private Set<Relationship> generateRelationshipFromAssociationRef(List<AssociationRef> associationRefs, String nameFilter)
    {
        Set<Relationship> relationships = new HashSet<Relationship>();

        for (AssociationRef associationRef : associationRefs)
        {
            String uniqueName = associationRef.getTypeQName().getLocalName();
            if (existsRelationshipDefinition(uniqueName) &&
                (nameFilter == null || uniqueName.equals(nameFilter)))
            {
                NodeRef from = associationRef.getSourceRef();
                NodeRef to = associationRef.getTargetRef();
                relationships.add(new RelationshipImpl(uniqueName, from, to));
            }
        }

        return relationships;
    }

    /**
     * Generates relationships from the given child association references
     *
     * @param childAssociationRefs Child association references
     * @return Relationships generated from the given child association references
     */
    private Set<Relationship> generateRelationshipFromParentChildAssociationRef(List<ChildAssociationRef> childAssociationRefs, String nameFilter)
    {
        Set<Relationship> relationships = new HashSet<Relationship>();

        for (ChildAssociationRef childAssociationRef : childAssociationRefs)
        {
            String uniqueName = childAssociationRef.getQName().getLocalName();
            if (existsRelationshipDefinition(uniqueName)&&
                (nameFilter == null || uniqueName.equals(nameFilter)))
            {
                NodeRef from = childAssociationRef.getParentRef();
                NodeRef to = childAssociationRef.getChildRef();
                relationships.add(new RelationshipImpl(uniqueName, from, to));
            }
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

        String sourceText = displayName.getSourceText();
        String targetText = displayName.getTargetText();

        String errorMsg = "Relationship type could not be determined from the display name. It is neither biderectional nor parent/child relationship";

        if (isBlank(sourceText) || isBlank(targetText))
        {
            throw new AlfrescoRuntimeException(errorMsg);
        }

        if (sourceText.equals(targetText))
        {
            relationshipType = RelationshipType.BIDIRECTIONAL;
        }
        else
        {
            relationshipType = RelationshipType.PARENTCHILD;
        }

        return relationshipType;
    }

    /**
     * Invoke before create reference policy
     *
     * @param source The source node reference
     * @param target The target node reference
     * @param associationDefinitionName The association definition name
     */
    private void invokeBeforeCreateReference(NodeRef source, NodeRef target, QName associationDefinitionName)
    {
        // Get QNames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(getNodeService(), source);
        // Execute policy for node type and aspects
        BeforeCreateReference policy = beforeCreateReferenceDelegate.get(qnames);
        policy.beforeCreateReference(source, target, associationDefinitionName);
    }

    /**
     * Invoke on create reference policy
     *
     * @param source The source node reference
     * @param target The target node reference
     * @param associationDefinitionName The association definition name
     */
    private void invokeOnCreateReference(NodeRef source, NodeRef target, QName associationDefinitionName)
    {
        // Get QNames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(getNodeService(), source);
        // Execute policy for node type and aspects
        OnCreateReference policy = onCreateReferenceDelegate.get(qnames);
        policy.onCreateReference(source, target, associationDefinitionName);
    }

    /**
     * Invoke before remove reference policy
     *
     * @param source The source node reference
     * @param target The target node reference
     * @param associationDefinitionName The association definition name
     */
    private void invokeBeforeRemoveReference(NodeRef source, NodeRef target, QName associationDefinitionName)
    {
        // Get QNames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(getNodeService(), source);
        // Execute policy for node type and aspects
        BeforeRemoveReference policy = beforeRemoveReferenceDelegate.get(qnames);
        policy.beforeRemoveReference(source, target, associationDefinitionName);
    }

    /**
     * Invoke on remove reference policy
     *
     * @param source The source node reference
     * @param target The target node reference
     * @param associationDefinitionName The association definition name
     */
    private void invokeOnRemoveReference(NodeRef source, NodeRef target, QName associationDefinitionName)
    {
        // Get QNames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(getNodeService(), source);
        // Execute policy for node type and aspects
        OnRemoveReference policy = onRemoveReferenceDelegate.get(qnames);
        policy.onRemoveReference(source, target, associationDefinitionName);
    }

    /**
     * Check if an instance of the association already exists from the given
     * source node reference to the given target node reference
     *
     * @param associationDefinition The association definition
     * @param source The source node reference
     * @param target The target node reference
     * @return <code>true</code> if an association already exists, <code>false</code> otherwise
     */
    private boolean associationExists(AssociationDefinition associationDefinition, NodeRef source, NodeRef target)
    {
        boolean associationAlreadyExists = false;

        QName associationDefinitionName = associationDefinition.getName();
        if (associationDefinition.isChild())
        {
            List<ChildAssociationRef> childAssocs = getNodeService().getChildAssocs(source, associationDefinitionName, associationDefinitionName);
            for (ChildAssociationRef chAssRef : childAssocs)
            {
                if (chAssRef.getChildRef().equals(target))
                {
                    associationAlreadyExists = true;
                }
            }
        }
        else
        {
            List<AssociationRef> assocs = getNodeService().getTargetAssocs(source, associationDefinitionName);
            for (AssociationRef assRef : assocs)
            {
                if (assRef.getTargetRef().equals(target))
                {
                    associationAlreadyExists = true;
                }
            }
        }

        return associationAlreadyExists;
    }

    /**
     * Gets the association definition for the given unique name
     *
     * @param uniqueName The unique name
     * @return The association definition for the given unique name if exists, <code>null</code> otherwise
     */
    private AssociationDefinition getAssociationDefinition(String uniqueName)
    {
        AssociationDefinition associationDefinition = null;

        Set<Entry<QName, AssociationDefinition>> associationsEntrySet = getCustomAssociations().entrySet();
        for (Map.Entry<QName, AssociationDefinition> associationEntry : associationsEntrySet)
        {
            String localName = associationEntry.getKey().getLocalName();
            if (uniqueName.equals(localName))
            {
                associationDefinition = associationEntry.getValue();
                break;
            }
        }

        return associationDefinition;
    }

    /**
     * Gets the qualified name of the association definition for the given unique name
     *
     * @param uniqueName The unique name
     * @return The qualified name of the association definition for the given unique name
     */
    private QName getAssociationDefinitionName(String uniqueName)
    {
        AssociationDefinition associationDefinition = getAssociationDefinition(uniqueName);

        if (associationDefinition == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The qualified name for '")
                .append(uniqueName)
                .append("' was not found.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        return associationDefinition.getName();
    }

    /**
     * This method writes the specified String into the association's title property.
     * For RM custom properties and references, Title is used to store the identifier.
     *
     * NOTE: Currently RMC custom associations only
     * @param associationDefinitionQName Qualified name for the association definition
     * @param newTitle The new title
     * @return Qualified name for the association definition
     */
    private QName persistUpdatedAssocTitle(QName associationDefinitionQName, String newTitle)
    {
        mandatory("associationDefinitionQName", associationDefinitionQName);

        AssociationDefinition assocDefn = getDictionaryService().getAssociation(associationDefinitionQName);
        if (assocDefn == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot find the association definiton for '").
                append(associationDefinitionQName.getLocalName()).
                append("'.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        // defaults to RM_CUSTOM_URI
        NodeRef modelRef = getCustomModelRef("");
        M2Model deserializedModel = readCustomContentModel(modelRef);

        String customAspectName = ASPECT_CUSTOM_ASSOCIATIONS.toPrefixString(getNamespaceService());
        M2Aspect customAssocsAspect = deserializedModel.getAspect(customAspectName);

        for (M2ClassAssociation assoc : customAssocsAspect.getAssociations())
        {
            if (associationDefinitionQName.toPrefixString(getNamespaceService()).equals(assoc.getName()) && newTitle != null)
            {
                assoc.setTitle(newTitle);
            }
        }
        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("persistUpdatedAssocTitle: " + associationDefinitionQName + "=" + newTitle + " to aspect: " + customAspectName);
        }

        return associationDefinitionQName;
    }

    /**
     * Generates a qualified name for the given relationship definition unique name
     *
     * @param uniqueName The unique name of the relationship definition
     * @return The qualified name of relationship definition
     */
    private QName generateRelationshipDefinitionQNameFor(String uniqueName)
    {
        mandatoryString("uniqueName", uniqueName);

        QName existingQName = null;

        Set<QName> customAssociationsQNames = getCustomAssociations().keySet();
        for (QName customAssociationsQName : customAssociationsQNames)
        {
            if (uniqueName.equals(customAssociationsQName.getLocalName()))
            {
                existingQName = customAssociationsQName;
            }
        }

        if (existingQName != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot create qualified name for given unique name '").
                append(uniqueName).
                append("' as it already exists.");
            throw new AlfrescoRuntimeException(sb.toString());
        }

        return QName.createQName(RM_CUSTOM_PREFIX, GUID.generate(), getNamespaceService());
    }
}
