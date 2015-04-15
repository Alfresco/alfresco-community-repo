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
     * Classify a document.
     * 
     * @param classificationLevel The security clearance needed to access the document.
     * @param classificationAuthority The name of the authority responsible for the classification of this document.
     * @param classificationReasons A non-empty set of reasons for classifying the document in this way.
     * @param document The node to classify.
     */
    void addClassificationToDocument(ClassificationLevel classificationLevel, String classificationAuthority,
                Set<ClassificationReason> classificationReasons, NodeRef document);
}
