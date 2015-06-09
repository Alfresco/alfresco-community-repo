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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;

/**
 * Container for the configured {@link ClassificationLevel} objects.
 *
 * @author tpage
 */
public class ClassificationLevelManager
{
    /** Unclassified classification level */
    public static final String UNCLASSIFIED_ID = "U";
    private static final String UNCLASSIFIED_MSG = "rm.classification.unclassified";
    public static final ClassificationLevel UNCLASSIFIED = new ClassificationLevel(UNCLASSIFIED_ID, UNCLASSIFIED_MSG);

    /** An immutable list of classification levels ordered from most to least secure. */
    private ImmutableList<ClassificationLevel> classificationLevels;

    /**
     * Store an immutable copy of the given classification levels.
     *
     * @param classificationLevels A list of classification levels ordered from most to least secure.
     */
    public void setClassificationLevels(List<ClassificationLevel> classificationLevels)
    {
        List<ClassificationLevel> temp = new ArrayList<>(classificationLevels);
        temp.add(temp.size(), UNCLASSIFIED);
        this.classificationLevels = ImmutableList.copyOf(temp);
    }

    /** @return the highest security classification level. */
    public ClassificationLevel getMostSecureLevel()
    {
        return classificationLevels.get(0);
    }

    /** @return An immutable list of classification levels ordered from most to least secure. */
    public ImmutableList<ClassificationLevel> getClassificationLevels()
    {
        return classificationLevels;
    }

    /**
     * Get a <code>ClassificationLevel</code> using its id.
     *
     * @param id The id of a classification level.
     * @return The classification level.
     * @throws LevelIdNotFound If the classification level cannot be found.
     */
    public ClassificationLevel findLevelById(String id) throws LevelIdNotFound
    {
        for (ClassificationLevel classificationLevel : classificationLevels)
        {
            if (classificationLevel.getId().equals(id))
            {
                return classificationLevel;
            }
        }
        throw new LevelIdNotFound(id);
    }
}
