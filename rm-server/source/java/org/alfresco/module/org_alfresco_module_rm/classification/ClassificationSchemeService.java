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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.ReasonIdNotFound;

/**
 * The Classification Scheme Service supports the 'Classified Records' feature, whereby Alfresco content can be given a
 * {@link ClassificationLevel}. This restricts access to that content to users having the appropriate security
 * clearance.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public interface ClassificationSchemeService
{
    /**
     * Returns an immutable list of the defined classification levels visible to the current user.
     *
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
     * Gets the unclassified {@link ClassificationLevel}.
     * @return the unclassified classification level
     */
    ClassificationLevel getUnclassifiedClassificationLevel();

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

    /**
     * Returns an immutable list of the defined exemption categories.
     *
     * @return The exemption categories in the order that they are defined.
     */
    List<ExemptionCategory> getExemptionCategories();

    /**
     * Identifies the reclassification type for the provided pair of {@link ClassificationLevel levels}.
     *
     * @param from the first classification level.
     * @param to   the second classification level.
     * @return the reclassification represented by this change, or {@code null} if it is not a change.
     */
    Reclassification getReclassification(ClassificationLevel from, ClassificationLevel to);

    Set<String> getReclassificationValues();

    /** Types of reclassification. */
    enum Reclassification
    {
        UPGRADE, DOWNGRADE, DECLASSIFY;

        /** Returns the name of this enum value in a format suitable for storage in the Alfresco repo. */
        public String toModelString()
        {
            final String name = toString();
            final StringBuilder result = new StringBuilder(name.length());
            result.append(name.charAt(0))
                  .append(name.substring(1).toLowerCase());
            return result.toString();
        }
    }
}
