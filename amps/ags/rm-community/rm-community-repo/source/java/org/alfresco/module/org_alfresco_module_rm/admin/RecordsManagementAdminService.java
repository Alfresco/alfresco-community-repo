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

package org.alfresco.module.org_alfresco_module_rm.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * Records management custom model service interface. Implementations of this class are responsible
 * for the creation and maintenance of RM-related custom properties and custom associations.
 *
 * @author Neil McErlean, janv
 * @since 2.1
 * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService
 */
// Not @AlfrescoPublicApi at the moment as it requires MatchLogic which is not public API.
public interface RecordsManagementAdminService
{
    /**
     * Get a list of all registered customisable types and aspects.
     *
     * @return Set of &lt;{@link QName}&gt;s of customisable types and aspects
     */
    Set<QName> getCustomisable();

    /**
     * Get a list of all the registered customisable types and aspects present on a given
     * node reference.
     *
     * @param nodeRef  node reference
     * @return Set of &lt;{@link QName}&gt;s of customisable types and aspects, empty if none
     */
    Set<QName> getCustomisable(NodeRef nodeRef);

    /**
     * Indicates whether a type (or aspect) is customisable.
     *
     * @param type	customisable type {@link QName}
     * @return boolean	true if type customisable, false otherwise
     */
    boolean isCustomisable(QName type);

    /**
     * Makes a type customisable.
     *
     * @param type	type {@link QName} to make customisable
     */
    void makeCustomisable(QName type);

    /**
     * Assuming the custom properties are not in use, makes a type no longer customisable.
     *
     * @param type	type {@link QName} to make customisable
     */
    void unmakeCustomisable(QName type);

    /**
     * Indicates whether the custom property exists.
     *
     * @param property	properties {@link QName}
     * @return boolean	true if property exists, false otherwise
     */
    boolean existsCustomProperty(QName property);

    /**
     * This method returns the custom properties that have been defined for the specified
     * customisable RM element.
     * <p>
     * Note: the custom property definitions are retrieved from the dictionaryService
     * which is notified of any newly created definitions on transaction commit.
     * Therefore custom properties created in the current transaction will not appear
     * in the result of this method.
     *
     * </p>
     * @param customisableType
     * @return Map of &lt;{@link QName}, {@link PropertyDefinition}&gt;s of custom properties definitions
     */
    Map<QName, PropertyDefinition> getCustomPropertyDefinitions(QName customisableType);

    /**
     * This method returns the custom properties that have been defined for all of
     * the specified customisable RM elements.
     * <p>
     * Note: the custom property definitions are retrieved from the dictionaryService
     * which is notified of any newly created definitions on transaction commit.
     * Therefore custom properties created in the current transaction will not appear
     * in the result of this method.
     * </p>
     *
     * @return Map of &lt;{@link QName}, {@link PropertyDefinition}&gt;s of custom properties definitions
     */
    Map<QName, PropertyDefinition> getCustomPropertyDefinitions();

    /**
     * Add custom property definition
     *
     * Note: no default value, single valued, optional, not system protected, no constraints
     *
     * @param propId - If a value for propId is provided it will be used to identify property definitions
     *                 within URLs and in QNames. Therefore it must contain URL/QName-valid characters
     *                 only. It must also be unique.
     *                 If a null value is passed, an id will be generated.
     * @param typeName - mandatory. The aspect within which the property is to be defined.
     *                   This must be one of the CustomisableRmElements.
     * @param label - mandatory
     * @param dataType - mandatory
     * @param title - optional
     * @param description - optional
     *
     * @return the propId, whether supplied as a parameter or generated.
     */
    QName addCustomPropertyDefinition(QName propId, QName typeName, String label, QName dataType, String title, String description) throws CustomMetadataException;

    /**
     * Add custom property definition with one optional constraint reference
     *
     * @param propId - If a value for propId is provided it will be used to identify property definitions
     *                 within URLs and in QNames. Therefore it must contain URL/QName-valid characters
     *                 only. It must also be unique.
     *                 If a null value is passed, an id will be generated.
     * @param typeName - mandatory. The aspect within which the property is to be defined.
     *                   This must be one of the CustomisableRmElements.
     * @param label - mandatory
     * @param dataType - mandatory
     * @param title - optional
     * @param description - optional
     * @param defaultValue - optional
     * @param multiValued - TRUE if multi-valued property
     * @param mandatory - TRUE if mandatory property
     * @param isProtected - TRUE if protected property
     * @param lovConstraintQName - optional custom constraint
     *
     * @return the propId, whether supplied as a parameter or generated.
     */

    // TODO propId string (not QName) ?
    // TODO remove title (since it is ignored) (or remove label to title)

    QName addCustomPropertyDefinition(QName propId,
                                             QName typeName,
                                             String label,
                                             QName dataType,
                                             String title,
                                             String description,
                                             String defaultValue,
                                             boolean multiValued,
                                             boolean mandatory,
                                             boolean isProtected,
                                             QName lovConstraintQName) throws CustomMetadataException;

    /**
     * Update the custom property definition's label (title).
     *
     * @param propQName the qname of the property definition
     * @param newLabel the new value for the label.
     * @return the propId.
     */
    QName setCustomPropertyDefinitionLabel(QName propQName, String newLabel) throws PropertyAlreadyExistsMetadataException;

    /**
     * Update the name and label of the custom property definition.
     * @param propQName The qname of the existing property definition
     * @param newName THe new name for both the custom property and its label.
     * @return
     * @throws CustomMetadataException
     */
    QName updateCustomPropertyDefinitionName(QName propQName, String newName) throws CustomMetadataException;

    /**
     * Sets a new list of values constraint on the custom property definition.
     *
     * @param propQName the qname of the property definition
     * @param newLovConstraint the List-Of-Values constraintRef.
     * @return the propId.
     */
    QName setCustomPropertyDefinitionConstraint(QName propQName, QName newLovConstraint);

    /**
     * Removes all list of values constraints from the custom property definition.
     *
     * @param propQName the qname of the property definition
     * @return the propId.
     */
    QName removeCustomPropertyDefinitionConstraints(QName propQName);

    /**
     * Remove custom property definition
     *
     * @param propQName
     */
    void removeCustomPropertyDefinition(QName propQName);

    /**
     * This method returns the custom references that have been defined in the custom
     * model.
     * Note: the custom reference definitions are retrieved from the dictionaryService
     * which is notified of any newly created definitions on transaction commit.
     * Therefore custom references created in the current transaction will not appear
     * in the results.
     *
     * @return The Map of custom references (both parent-child and standard).
     * @deprecated as of RM 2.3, please use {@link RelationshipService#getRelationshipDefinitions()} instead.
     */
    Map<QName, AssociationDefinition> getCustomReferenceDefinitions();

    /**
     * Fetches all associations <i>from</i> the given source.
     *
     * @param node the node from which the associations start.
     * @return a List of associations.
     * @deprecated as of RM 2.3, please
     * use{@link NodeService#getTargetAssocs(NodeRef, QNamePattern)} with QNamePattern RegexQNamePattern.MATCH_ALL
     * instead
     */
    List<AssociationRef> getCustomReferencesFrom(NodeRef node);

    /**
     * Fetches all child associations of the given source. i.e. all associations where the
     * given node is the parent.
     *
     * @param node
     * @return
     * @deprecated as of RM 2.3, please use {@link NodeService#getChildAssocs(NodeRef)} instead.
     */
    List<ChildAssociationRef> getCustomChildReferences(NodeRef node);

    /**
     * Returns a List of all associations <i>to</i> the given node.
     *
     * @param node the node to which the associations point.
     * @return a List of associations.
     * @deprecated as of RM 2.3, please use
     * {@link NodeService#getSourceAssocs(NodeRef, QNamePattern)} with QNamePattern RegexQNamePattern.MATCH_ALL instead.
     */
    List<AssociationRef> getCustomReferencesTo(NodeRef node);

    /**
     * Fetches all child associations where the given node is the child.
     *
     * @param node
     * @return
     * @deprecated as of RM 2.3, please use {@link NodeService#getParentAssocs(NodeRef)} instead.
     */
    List<ChildAssociationRef> getCustomParentReferences(NodeRef node);

    /**
     * This method adds the specified custom reference instance between the specified nodes.
     * Only one instance of any custom reference type is allowed in a given direction
     * between two given records.
     *
     * @param fromNode
     * @param toNode
     * @param assocId the server-side qname e.g. {http://www.alfresco.org/model/rmcustom/1.0}abcd-12-efgh-4567
     * @throws AlfrescoRuntimeException if an instance of the specified reference type
     *                                  already exists from fromNode to toNode.
     * @deprecated as of RM 2.3, please use {@link RelationshipService#addRelationship(String, NodeRef, NodeRef)} instead.
     */
    void addCustomReference(NodeRef fromNode, NodeRef toNode, QName assocId);

    /**
     * This method removes the specified custom reference instance from the specified node.
     *
     * @param fromNode
     * @param toNode
     * @param assocId the server-side qname e.g. {http://www.alfresco.org/model/rmcustom/1.0}abcd-12-efgh-4567
     * @deprecated as of RM 2.3, please use {@link RelationshipService#removeRelationship(String, NodeRef, NodeRef)} instead.
     */
    void removeCustomReference(NodeRef fromNode, NodeRef toNode, QName assocId);

    /**
     * This method creates a new custom association, using the given label as the title.
     *
     * @param label the title of the association definition
     * @return the QName of the newly-created association.
     * @deprecated as of RM 2.3, please use {@link RelationshipService#createRelationshipDefinition(org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName)} instead.
     */
    QName addCustomAssocDefinition(String label);

    /**
     * This method creates a new custom child association, combining the given source and
     * target and using the combined String  as the title.
     *
     * @param source
     * @param target
     * @return the QName of the newly-created association.
     * @deprecated as of RM 2.3, please use {@link RelationshipService#createRelationshipDefinition(org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName)} instead.
     */
    QName addCustomChildAssocDefinition(String source, String target);

    /**
     * This method updates the source and target values for the specified child association.
     * The source and target will be combined into a single string and stored in the title property.
     * Source and target are String metadata for RM parent/child custom references.
     *
     * @param refQName qname of the child association.
     * @param newSource the new value for the source field.
     * @param newTarget the new value for the target field.
     * @see #getCompoundIdFor(String, String)
     * @see #splitSourceTargetId(String)
     * @deprecated as of RM 2.3, please use {@link RelationshipService#updateRelationshipDefinition(String, org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName)} instead.
     */
    QName updateCustomChildAssocDefinition(QName refQName, String newSource, String newTarget);

    /**
     * This method updates the label value for the specified association.
     * The label will be stored in the title property.
     * Label is String metadata for bidirectional custom references.
     *
     * @param refQName qname of the child association.
     * @param newLabel the new value for the label field.
     * @deprecated as of RM 2.3, please use {@link RelationshipService#updateRelationshipDefinition(String, org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName)} instead.
     */
    QName updateCustomAssocDefinition(QName refQName, String newLabel);

    /**
     * This method returns ConstraintDefinition objects defined in the given model
     * (note: not property references or in-line defs)
     * The custom constraint definitions are retrieved from the dictionaryService
     * which is notified of any newly created definitions on transaction commit.
     * Therefore custom constraints created in the current transaction will not appear
     * in the results.
     */
    List<ConstraintDefinition> getCustomConstraintDefinitions(QName modelQName);

    /**
     * This method adds a Constraint definition to the custom model.
     * The implementation of this method would have to go into the M2Model and insert
     * the relevant M2Objects for this new constraint.
     *
     * param type not included as it would always be RMListOfValuesConstraint for RM.
     *
     * @param constraintName the name e.g. rmc:foo
     * @param title the human-readable title e.g. My foo list
     * @param caseSensitive
     * @param allowedValues the allowed values list
     * @param matchLogic AND (all values must match), OR (at least one values must match)
     */
    void addCustomConstraintDefinition(QName constraintName, String title, boolean caseSensitive, List<String> allowedValues, MatchLogic matchLogic);

    /**
     * Remove custom constraint definition - if not referenced (by any properties)
     *
     *
     * @param constraintName the name e.g. rmc:foo
     */
    void removeCustomConstraintDefinition(QName constraintName);

    /**
     * Update custom constraint definition with new list of values (replaces existing list, if any)
     *
     * @param constraintName the name e.g. rmc:foo
     * @param newValues
     */
    void changeCustomConstraintValues(QName constraintName, List<String> newValues);

    /**
     *
     * @param constraintName
     * @param title
     */
    void changeCustomConstraintTitle(QName constraintName, String title);

    /**
     * This method iterates over the custom properties, references looking for one whose id
     * exactly matches that specified.
     *
     * @param localName the localName part of the qname of the property or reference definition.
     * @return the QName of the property, association definition which matches, or null.
     */
    QName getQNameForClientId(String localName);

    /**
     * Given a compound id for source and target strings (as used with parent/child
     * custom references), this method splits the String and returns an array containing
     * the source and target IDs separately.
     *
     * @param sourceTargetId the compound ID.
     * @return a String array, where result[0] == sourceId and result[1] == targetId.
     */
    String[] splitSourceTargetId(String sourceTargetId);

    /**
     * This method retrieves a compound ID (client-side) for the specified
     * sourceId and targetId.
     *
     * @param sourceId
     * @param targetId
     * @return
     */
    String getCompoundIdFor(String sourceId, String targetId);
}
