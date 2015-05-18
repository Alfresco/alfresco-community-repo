/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.InvalidNode;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.ReasonIdNotFound;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A service to handle the classification of content.
 *
 * @author tpage
 */
public interface ContentClassificationService
{
    /**
     * Returns the current classification level of a given node.
     *
     * @param  nodeRef                      node reference
     * @return {@link ClassificationLevel}  classification level, unclassified if none
     */
    ClassificationLevel getCurrentClassification(NodeRef nodeRef);

    /**
     * Classify a piece of content.
     *
     * @param classificationLevelId The security clearance needed to access the content.
     * @param classificationAuthority The name of the authority responsible for the classification of this content.
     * @param classificationReasonIds A non-empty set of ids of reasons for classifying the content in this way.
     * @param content The node to classify.
     * @throws LevelIdNotFound If the supplied level id is not found.
     * @throws ReasonIdNotFound If any of the supplied reason ids are not found.
     * @throws InvalidNodeRefException If the node could not be found.
     * @throws InvalidNode If the supplied node is not a content node.
     */
    void classifyContent(String classificationLevelId, String classificationAuthority, Set<String> classificationReasonIds, NodeRef content)
            throws LevelIdNotFound, ReasonIdNotFound, InvalidNodeRefException, InvalidNode;

    /**
     * Indicates whether the currently authenticated user has clearance to see the
     * provided node.
     * <p>
     * Note that users, regardless of their clearance level, are always cleared to see a node that has no classification
     * applied.
     *
     * @param  nodeRef  node reference
     * @return boolean  true if cleared to see node, false otherwise
     */
    boolean hasClearance(NodeRef nodeRef);
}
