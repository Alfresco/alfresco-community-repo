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

import java.util.Arrays;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Classification level integration test
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
public class ClassificationLevelsTest extends BaseRMTestCase
{
    public void testClassificationLevels() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void when() throws Exception
            {
                // Server is up and running
            }

            public void then() throws Exception
            {
                List<ClassificationLevel> levels = classificationSchemeService.getClassificationLevels();
                List<ClassificationLevel> expectedLevels = Arrays.asList(
                            new ClassificationLevel("TS", "rm.caveat.classification.mark.ts.label.label"),
                            new ClassificationLevel("S", "rm.caveat.classification.mark.s.label.label"),
                            new ClassificationLevel("C", "rm.caveat.classification.mark.c.label.label"),
                            ClassificationLevelManager.UNCLASSIFIED);
                assertEquals(levels, expectedLevels);
            }
        });
    }
}
