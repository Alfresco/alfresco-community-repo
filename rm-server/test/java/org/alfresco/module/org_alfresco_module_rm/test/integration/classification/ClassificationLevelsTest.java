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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification;

import java.io.InputStream;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Classification level integration test
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassificationLevelsTest extends BaseRMTestCase
{
    private static final String CLASSIFICATION_LEVELS_FILE_PATH = "/alfresco/module/org_alfresco_module_rm/classification/rm-classification-levels.json";
    private static final String LEVEL1_ID = "level1";
    private static final String LEVEL1_DISPLAY_LABEL = "Level 1";
    private static final String LEVEL2_ID = "level2";
    private static final String LEVEL2_DISPLAY_LABEL_KEY = "rm.classification.level2";
    private static final String LEVEL3_ID = "level3";
    private static final String LEVEL3_DISPLAY_LABEL_KEY = "rm.classification.level3";
    private static final String LEVEL4_ID = "level4";
    private static final String LEVEL4_DISPLAY_LABEL = "Level 4";

    public void testClassificationLevels() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given() throws Exception
            {
                try (final InputStream in = getClass().getResourceAsStream(CLASSIFICATION_LEVELS_FILE_PATH))
                {
                    assertNotNull(in);
                }
            }

            public void when() throws Exception
            {
                // Server is up and running
            }

            public void then() throws Exception
            {
                List<ClassificationLevel> levels = classificationService.getClassificationLevels();
                assertNotNull(levels);
                assertEquals(5, levels.size());

                ClassificationLevel level1 = levels.get(0);
                ClassificationLevel level2 = levels.get(1);
                ClassificationLevel level3 = levels.get(2);
                ClassificationLevel level4 = levels.get(3);

                assertEquals(level4.getDisplayLabel(), LEVEL4_DISPLAY_LABEL);
                assertEquals(level3.getDisplayLabel(), LEVEL3_DISPLAY_LABEL_KEY);
                assertEquals(level2.getDisplayLabel(), LEVEL2_DISPLAY_LABEL_KEY);
                assertEquals(level1.getDisplayLabel(), LEVEL1_DISPLAY_LABEL);

                assertEquals(level1.getId(), LEVEL1_ID);
                assertEquals(level2.getId(), LEVEL2_ID);
                assertEquals(level3.getId(), LEVEL3_ID);
                assertEquals(level4.getId(), LEVEL4_ID);
                
                assertEquals(ClassificationLevelManager.UNCLASSIFIED, levels.get(4));
            }
        });
    }
}
