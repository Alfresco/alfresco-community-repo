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

import java.util.Comparator;

/**
 * A class to compare classification levels. More secure classification levels are "higher" than less secure levels.
 *
 * @author tpage
 * @since 2.4.a
 */
public class ClassificationLevelComparator implements Comparator<ClassificationLevel>
{
    private ClassificationLevelManager classificationLevelManager;

    public ClassificationLevelComparator(ClassificationLevelManager classificationLevelManager)
    {
        this.classificationLevelManager = classificationLevelManager;
    }

    /**
     * Return a positive number if the first classification level is more secure than the second. {@inheritDoc}
     */
    @Override
    public int compare(ClassificationLevel oneLevel, ClassificationLevel otherLevel)
    {
        int oneIndex = classificationLevelManager.getClassificationLevels().indexOf(oneLevel);
        int otherIndex = classificationLevelManager.getClassificationLevels().indexOf(otherLevel);
        // Smaller indexes are more secure.
        return otherIndex - oneIndex;
    }
}
