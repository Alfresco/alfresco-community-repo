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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationReason;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Tests of classification reason handling.
 *
 * @author tpage
 * @since 3.0
 */
public class ClassificationReasonsTest extends BaseRMTestCase
{
    /**
     * Given the default classification reasons config file is on the classpath When the system has finished starting up
     * Then the classification service exposes the classification reasons.
     * <p>
     * Note that this test requires a clean db, as otherwise the classification scheme service will use the persisted
     * classification reasons in preference to those given on the classpath (see the logic in
     * {@link ClassificationSchemeServiceImpl#initConfiguredClassificationReasons()}).
     */
    public void testLoadBootstrappedClassificationReasons() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given() throws Exception
            {
                // NOOP: The default classification reasons config file is on the classpath.
            }

            public void when() throws Exception
            {
                // NOOP: The system has finished starting up.
            }

            public void then() throws Exception
            {
                // Check the classification scheme service exposes the classification reasons.
                List<ClassificationReason> reasons = classificationSchemeService.getClassificationReasons();
                assertNotNull(reasons);
                assertEquals("The default classification reasons in test/resources/alfresco/module/"
                            + "org_alfresco_module_rm/classification/rm-classification-reasons.json "
                            + "contains four reasons.", 4, reasons.size());
                // Check a couple of fields in the loaded data.
                assertEquals("Unexpected id for the first test reason.", "Test Reason 1", reasons.get(0).getId());
                assertEquals("Unexpected displayLabelKey for the fourth test reason.",
                             "rm.test.classification-reason.foreignRelations", reasons.get(3).getDisplayLabel());
            }
        });
    }
}
