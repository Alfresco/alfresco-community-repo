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

import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.InvalidNode;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.ReasonIdNotFound;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Classification Service supports the 'Classified Records' feature, whereby Alfresco
 * content can be given a {@link ClassificationLevel}. This restricts access to that
 * content to users having the appropriate security clearance.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public interface ClassificationService
{
    /**
     * Returns an immutable list of the defined classification levels.
     * @return classification levels in descending order from highest to lowest
     *         (where fewer users have access to the highest classification levels
     *         and therefore access to the most restricted documents).
     */
    List<ClassificationLevel> getClassificationLevels();

    /**
     * Returns an immutable list of the defined classification reasons.
     * @return classification reasons in the order that they are defined.
     */
    List<ClassificationReason> getClassificationReasons();

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
    void classifyContent(String classificationLevelId, String classificationAuthority,
                Set<String> classificationReasonIds, NodeRef content) throws LevelIdNotFound, ReasonIdNotFound,
                InvalidNodeRefException, InvalidNode;

    /**
     * Gets the default {@link ClassificationLevel}, which will usually be the level with the lowest security clearance.
     * @return the default classification level, or {@code null} if no security levels are configured.
     */
    ClassificationLevel getDefaultClassificationLevel();

    /**
     * Gets the classification level for the given classification level id
     *
     * @param classificationLevelId {@link String} The classification level id for which the classification level should be retrieved.
     * @return The classification level for the given classification level id
     * @throws LevelIdNotFound If the given classification level id is not found
     */
    ClassificationLevel getClassificationLevelById(String classificationLevelId) throws LevelIdNotFound;

    /**
     * Gets the classification reason for the given classification reason id
     *
     * @param classificationReasonId {@link String} The classification reason id for which the classification reason should be retrieved.
     * @return The classification reason for the given classification reason id
     * @throws ReasonIdNotFound If the given classification reason id is not found
     */
    ClassificationReason getClassificationReasonById(String classificationReasonId) throws ReasonIdNotFound;
}
