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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ClassificationLevelManager}.
 * 
 * @author tpage
 */
public class ClassificationLevelManagerUnitTest
{
    private static final ClassificationLevel LEVEL_1 = new ClassificationLevel("id1", "displayLabelKey1");
    private static final ClassificationLevel LEVEL_2 = new ClassificationLevel("id2", "displayLabelKey2");
    private static final ClassificationLevel LEVEL_3 = new ClassificationLevel("id3", "displayLabelKey3");
    private static final List<ClassificationLevel> LEVELS = Arrays.asList(LEVEL_1, LEVEL_2, LEVEL_3);

    private ClassificationLevelManager classificationLevelManager;

    @Before public void setup()
    {
        classificationLevelManager = new ClassificationLevelManager(LEVELS);          
    }

    @Test public void findClassificationById_found()
    {
        ClassificationLevel actual = classificationLevelManager.findLevelById("id2");
        assertEquals(LEVEL_2, actual);
    }

    @Test(expected=LevelIdNotFound.class) public void findClassificationById_notFound()
    {
        classificationLevelManager.findLevelById("id_unknown");
    }

    @Test public void getMostSecureLevel()
    {
        ClassificationLevel actual = classificationLevelManager.getMostSecureLevel();
        assertEquals(LEVEL_1, actual);
    }
    
    /**
     * Given that I have created the classification level manager with a list of classification levels
     * Then the unclassified level is available
     */
    @Test public void getUnclassifiedLevel()
    {
        assertEquals(LEVELS.size() + 1, classificationLevelManager.getClassificationLevels().size());         	
    	assertEquals(ClassificationLevelManager.UNCLASSIFIED, classificationLevelManager.findLevelById(ClassificationLevelManager.UNCLASSIFIED_ID));
    }     
}
