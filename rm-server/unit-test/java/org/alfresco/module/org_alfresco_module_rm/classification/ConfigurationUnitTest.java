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

import static org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceImplUnitTest.asLevelList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.MalformedConfiguration;
import org.junit.Test;

/**
 * Unit tests for {@link Configuration}.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ConfigurationUnitTest
{
    private static final List<ClassificationLevel> DEFAULT_CLASSIFICATION_LEVELS = asLevelList("TopSecret",    "TS",
                                                                                               "Secret",       "S",
                                                                                               "Confidential", "C",
                                                                                               "NoClearance",  "NC");

    @Test public void getConfiguredLevels_readingDefaultConfigurationShouldWork()
    {
        Configuration c = new Configuration(ClassificationServiceImpl.DEFAULT_LEVELS_FILE, ClassificationServiceImpl.DEFAULT_REASONS_FILE);
        List<ClassificationLevel> config = c.getConfiguredLevels();
        assertEquals(DEFAULT_CLASSIFICATION_LEVELS, config);
    }

    @Test public void getConfiguredLevels_readingMissingConfigurationShouldProduceEmptyConfig() throws Exception
    {
        Configuration c = new Configuration("/no/such/resource", "/no/such/resource");
        assertTrue(c.getConfiguredLevels().isEmpty());
    }

    @Test (expected = MalformedConfiguration.class)
    public void getConfiguredLevels_readingMalformedConfigurationShouldFail()
    {
		Configuration c = new Configuration(
				"/alfresco/classification/rm-classification-levels-malformed.json",
				"/alfresco/classification/rm-classification-levels-malformed.json");	
			c.getConfiguredLevels();
    }

    @Test public void getConfiguredReasons_readingDefaultConfigurationShouldWork()
    {
        Configuration c = new Configuration(ClassificationServiceImpl.DEFAULT_LEVELS_FILE, 
        		ClassificationServiceImpl.DEFAULT_REASONS_FILE);
        List<ClassificationReason> config = c.getConfiguredReasons();
        assertFalse(config.isEmpty());
    }

    @Test public void getConfiguredReasons_readingMissingConfigurationShouldProduceEmptyConfig() throws Exception
    {
        Configuration c = new Configuration("/no/such/resource", "/no/such/resource");
        assertTrue(c.getConfiguredReasons().isEmpty());
    }
    
    @Test (expected = MalformedConfiguration.class)
    public void getConfiguredReasons_readingMalformedConfigurationShouldFail()
    {
		Configuration c = new Configuration(
				"/alfresco/classification/rm-classification-levels-malformed.json",
				"/alfresco/classification/rm-classification-levels-malformed.json");
    	c.getConfiguredReasons();
    }
}
