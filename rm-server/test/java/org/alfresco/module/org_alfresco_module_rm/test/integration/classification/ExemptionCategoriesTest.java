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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.ExemptionCategory;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.springframework.context.ApplicationEvent;

/**
 * Tests of exemption category loading.
 *
 * @author tpage
 * @since 3.0.a
 */
public class ExemptionCategoriesTest extends BaseRMTestCase
{
    /**
     * Initial exemption category loading.
     * <p>
     * <a href="https://issues.alfresco.com/jira/browse/RM-2321">RM-2321</a><pre>
     * Given that I have a clean system
     * When I boot it for the first time
     * Then the default set of exemption categories are loaded
     * And are available throughout the application
     * </pre><p>
     * Note that this test requires a clean db, as otherwise the classification scheme service will use
     * the persisted exemption categories in preference to those given on the classpath (see the logic in
     * {@link ClassificationServiceBootstrap#onBootstrap(ApplicationEvent)}).
     */
    public void testLoadBootstrappedExemptionCategories() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given() throws Exception
            {
                // NOOP: I have a clean system.
            }

            public void when() throws Exception
            {
                // NOOP: I boot it for the first time.
            }

            public void then() throws Exception
            {
                // Check the classification scheme service exposes the classification reasons.
                List<ExemptionCategory> exemptionCategories = classificationSchemeService.getExemptionCategories();
                assertNotNull(exemptionCategories);
                assertEquals("The default exemption categories in test/resources/alfresco/module/"
                            + "org_alfresco_module_rm/classification/rm-exemption-categories.json "
                            + "contains three categories.", 3, exemptionCategories.size());
                // Check a couple of fields in the loaded data.
                assertEquals("Unexpected id for the first test category.", "Test Category 1", exemptionCategories.get(0).getId());
                assertEquals("Unexpected displayLabelKey for the third test category.",
                             "rm.test.exemption-category.three", exemptionCategories.get(2).getDisplayLabel());
            }
        });
    }
}
