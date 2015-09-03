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

import static org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeServiceImplUnitTest.asLevelList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MalformedConfiguration;
import org.junit.Test;

/**
 * Unit tests for {@link ClassificationServiceDAO}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ClassificationServiceDAOUnitTest
{
    private static final List<ClassificationLevel> DEFAULT_CLASSIFICATION_LEVELS = asLevelList("level1",    "l1",
                                                                                               "level2",    "l2",
                                                                                               "level3",    "l3",
                                                                                               "level4",    "l4");

    @Test public void getConfiguredLevels_readingDefaultConfigurationShouldWork()
    {
        ClassificationServiceDAO c = new ClassificationServiceDAO();
        c.setLevelConfigLocation("/alfresco/module/org_alfresco_module_rm/classification/rm-classification-levels.json");
        List<ClassificationLevel> config = c.getConfiguredValues(ClassificationLevel.class);
        assertEquals(DEFAULT_CLASSIFICATION_LEVELS, config);
    }

    @Test public void getConfiguredLevels_readingMissingConfigurationShouldProduceEmptyConfig() throws Exception
    {
        ClassificationServiceDAO c = new ClassificationServiceDAO();
        c.setLevelConfigLocation("/no/such/resource");
        assertTrue(c.getConfiguredValues(ClassificationLevel.class).isEmpty());
    }

    @Test (expected = MalformedConfiguration.class)
    public void getConfiguredLevels_readingMalformedConfigurationShouldFail()
    {
        ClassificationServiceDAO c = new ClassificationServiceDAO();
        c.setLevelConfigLocation("/alfresco/classification/rm-classification-levels-malformed.json");
        c.getConfiguredValues(ClassificationLevel.class);
    }

    @Test public void getConfiguredReasons_readingDefaultConfigurationShouldWork()
    {
        ClassificationServiceDAO c = new ClassificationServiceDAO();
        c.setReasonConfigLocation("/alfresco/module/org_alfresco_module_rm/classification/rm-classification-reasons.json");
        List<ClassificationReason> config = c.getConfiguredValues(ClassificationReason.class);
        assertFalse(config.isEmpty());
    }

    @Test public void getConfiguredReasons_readingMissingConfigurationShouldProduceEmptyConfig() throws Exception
    {
        ClassificationServiceDAO c = new ClassificationServiceDAO();
        c.setReasonConfigLocation("/no/such/resource");
        assertTrue(c.getConfiguredValues(ClassificationReason.class).isEmpty());
    }

    @Test (expected = MalformedConfiguration.class)
    public void getConfiguredReasons_readingMalformedConfigurationShouldFail()
    {
        ClassificationServiceDAO c = new ClassificationServiceDAO();
        c.setReasonConfigLocation("/alfresco/classification/rm-classification-levels-malformed.json");
        c.getConfiguredValues(ClassificationReason.class);
    }
}
