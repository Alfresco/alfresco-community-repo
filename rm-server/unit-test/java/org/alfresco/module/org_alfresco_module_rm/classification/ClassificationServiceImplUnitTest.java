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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ClassificationServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceImplUnitTest extends BaseUnitTest
{
    private static final List<ClassificationLevel> DEFAULT_CLASSIFICATION_LEVELS = asLevelList("Top Secret",   "rm.classification.topSecret",
                                                                                               "Secret",       "rm.classification.secret",
                                                                                               "Confidential", "rm.classification.confidential",
                                                                                               "No Clearance", "rm.classification.noClearance");
    private static final List<ClassificationLevel> ALT_CLASSIFICATION_LEVELS = asLevelList("Board",                "B",
                                                                                           "Executive Management", "EM",
                                                                                           "Employee",             "E",
                                                                                           "Public",               "P");
    /**
     * A convenience method for turning lists of level id Strings into lists
     * of {@code ClassificationLevel} objects.
     *
     * @param idsAndLabels A varargs/array of Strings like so: [ id0, label0, id1, label1... ]
     * @throws IllegalArgumentException if {@code idsAndLabels} has a non-even length.
     */
    public static List<ClassificationLevel> asLevelList(String ... idsAndLabels)
    {
        if (idsAndLabels.length % 2 != 0)
        {
            throw new IllegalArgumentException(String.format("Cannot create %s objects with %d args.",
                                                       ClassificationLevel.class.getSimpleName(), idsAndLabels.length));
        }

        final List<ClassificationLevel> levels = new ArrayList<>(idsAndLabels.length / 2);

        for (int i = 0; i < idsAndLabels.length; i += 2)
        {
            levels.add(new ClassificationLevel(idsAndLabels[i], idsAndLabels[i+1]));
        }

        return levels;
    }

    @Mock(name="attributeService") protected AttributeService mockedAttributeService;

    private ClassificationServiceImpl classificationService;

    @Test public void defaultConfigurationVanillaSystem()
    {
        classificationService = new TestClassificationService(null, DEFAULT_CLASSIFICATION_LEVELS);
        classificationService.setAttributeService(mockedAttributeService);

        classificationService.initConfiguredClassificationLevels();

        assertEquals(DEFAULT_CLASSIFICATION_LEVELS, classificationService.getClassificationLevels());
    }

    @Test public void alternativeConfigurationPreviouslyStartedSystem()
    {
        classificationService = new TestClassificationService(DEFAULT_CLASSIFICATION_LEVELS, ALT_CLASSIFICATION_LEVELS);
        classificationService.setAttributeService(mockedAttributeService);

        classificationService.initConfiguredClassificationLevels();

        assertEquals(ALT_CLASSIFICATION_LEVELS, classificationService.getClassificationLevels());
    }

    @Test (expected=MissingConfiguration.class)
    public void missingConfigurationVanillaSystemShouldFail() throws Exception
    {
        classificationService = new TestClassificationService(null, null);
        classificationService.setAttributeService(mockedAttributeService);

        classificationService.initConfiguredClassificationLevels();
    }

    /**
     * Helper class for test purposes that allows us to replace the persisted
     * and configured lists of {@link ClassificationLevel}s.
     */
    private static class TestClassificationService extends ClassificationServiceImpl
    {
        private final List<ClassificationLevel> persisted;
        private final List<ClassificationLevel> configured;
        public TestClassificationService(List<ClassificationLevel> persisted, List<ClassificationLevel> configured)
        {
            this.persisted  = persisted;
            this.configured = configured;
        }

        @Override List<ClassificationLevel> getPersistedLevels()  { return persisted; }
        @Override List<ClassificationLevel> getConfigurationLevels() { return configured; }
    }
}
