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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Classification level integration test
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassificationLevelsTest extends BaseRMTestCase
{
    private static final String ALFRESCO_GLOBAL_PROPERTIES_FILE_LOCATION = "/alfresco/module/org_alfresco_module_rm/alfresco-global.properties";
    private static final String CLASSIFICATION_LEVELS_PROPERTY_KEY = "rm.classification.levelsFile";
    private static final String TOP_SECRET_ID = "TopSecret";
    private static final String TOP_SECRET_DISPLAY_LABEL_KEY = "rm.classification.topSecret";
    private static final String SECRET_ID = "Secret";
    private static final String SECRET_DISPLAY_LABEL_KEY = "rm.classification.secret";
    private static final String CONFIDENTIAL_ID = "Confidential";
    private static final String CONFIDENTIAL_DISPLAY_LABEL_KEY = "rm.classification.confidential";
    private static final String NO_CLEARANCE_ID = "NoClearance";
    private static final String NO_CLEARANCE_DISPLAY_LABEL_KEY = "rm.classification.noClearance";

    public void testClassificationLevels() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given() throws Exception
            {
                String classificationLevelsConfigFilePath = getClassificationLevelsConfigFilePath();
                assertNotNull(classificationLevelsConfigFilePath);

                try (final InputStream in = getClass().getResourceAsStream(classificationLevelsConfigFilePath))
                {
                    assertNotNull(in);
                }
            }

            public void when() throws Exception
            {
                List<ClassificationLevel> levels = classificationService.getClassificationLevels();
                assertNotNull(levels);
                assertEquals(4, levels.size());

                String topSecretDisplayLabel = getDisplayLabel(TOP_SECRET_DISPLAY_LABEL_KEY);
                String secretDisplayLabel = getDisplayLabel(SECRET_DISPLAY_LABEL_KEY);
                String confidentialDisplayLabel = getDisplayLabel(CONFIDENTIAL_DISPLAY_LABEL_KEY);
                String noClearanceDisplayLabel = getDisplayLabel(NO_CLEARANCE_DISPLAY_LABEL_KEY);

                assertTrue(containsId(levels, CONFIDENTIAL_ID));
                assertTrue(containsId(levels, TOP_SECRET_ID));
                assertTrue(containsId(levels, NO_CLEARANCE_ID));
                assertTrue(containsId(levels, SECRET_ID));

                assertTrue(containsDisplayLabel(levels, noClearanceDisplayLabel));
                assertTrue(containsDisplayLabel(levels, secretDisplayLabel));
                assertTrue(containsDisplayLabel(levels, topSecretDisplayLabel));
                assertTrue(containsDisplayLabel(levels, confidentialDisplayLabel));

                ClassificationLevel level0 = levels.get(0);
                ClassificationLevel level1 = levels.get(1);
                ClassificationLevel level2 = levels.get(2);
                ClassificationLevel level3 = levels.get(3);

                assertEquals(level3.getDisplayLabel(), noClearanceDisplayLabel);
                assertEquals(level2.getDisplayLabel(), confidentialDisplayLabel);
                assertEquals(level1.getDisplayLabel(), secretDisplayLabel);
                assertEquals(level0.getDisplayLabel(), topSecretDisplayLabel);

                assertEquals(level0.getId(), TOP_SECRET_ID);
                assertEquals(level1.getId(), SECRET_ID);
                assertEquals(level2.getId(), CONFIDENTIAL_ID);
                assertEquals(level3.getId(), NO_CLEARANCE_ID);
            }
        });
    }

    private String getClassificationLevelsConfigFilePath() throws IOException
    {
        Properties properties = new Properties();

        try (final InputStream in = getClass().getResourceAsStream(ALFRESCO_GLOBAL_PROPERTIES_FILE_LOCATION))
        {
            assertNotNull(in);
            properties.load(in);
        }

        return properties.getProperty(CLASSIFICATION_LEVELS_PROPERTY_KEY);
    }

    private String getDisplayLabel(String displayLabelKey)
    {
        String message = I18NUtil.getMessage(displayLabelKey);
        return (isNotBlank(message) ? message : displayLabelKey);
    }

    private boolean containsId(List<ClassificationLevel> levels, String id)
    {
        boolean contains = false;
        for (ClassificationLevel level : levels)
        {
            if (level.getId().equals(id))
            {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private boolean containsDisplayLabel(List<ClassificationLevel> levels, String displayLabel)
    {
        boolean contains = false;
        for (ClassificationLevel level : levels)
        {
            if (level.getDisplayLabel().equals(displayLabel))
            {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
