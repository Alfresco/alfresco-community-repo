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

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.ReasonIdNotFound;

/**
 * Container for the configured {@link ClassificationReason} objects.
 *
 * @author tpage
 */
public class ClassificationReasonManager
{
    /** An immutable list of classification reasons. */
    private ImmutableList<ClassificationReason> classificationReasons;

    /**
     * Store an immutable copy of the given reasons.
     *
     * @param classificationReasons The classification reasons.
     */
    public void setClassificationReasons(Collection<ClassificationReason> classificationReasons)
    {
        this.classificationReasons = ImmutableList.copyOf(classificationReasons);
    }

    /** @return An immutable list of classification reasons. */
    public ImmutableList<ClassificationReason> getClassificationReasons()
    {
        return classificationReasons;
    }

    /**
     * Get a <code>ClassificationReason</code> using its id.
     *
     * @param id The id of a classification reason.
     * @return The classification reason.
     * @throws ReasonIdNotFound If the classification reason cannot be found.
     */
    public ClassificationReason findReasonById(String id) throws ReasonIdNotFound
    {
        for (ClassificationReason classificationReason : classificationReasons)
        {
            if (classificationReason.getId().equals(id))
            {
                return classificationReason;
            }
        }
        throw new ReasonIdNotFound(id);
    }
}
