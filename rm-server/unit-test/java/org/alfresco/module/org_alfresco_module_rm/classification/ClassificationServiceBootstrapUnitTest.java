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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the {@link ClassificationServiceBootstrap}.
 *
 * @author tpage
 */
public class ClassificationServiceBootstrapUnitTest
{
    private static final List<ClassificationLevel> DEFAULT_CLASSIFICATION_LEVELS = asLevelList("Top Secret",   "rm.classification.topSecret",
                                                                                               "Secret",       "rm.classification.secret",
                                                                                               "Confidential", "rm.classification.confidential",
                                                                                               "No Clearance", "rm.classification.noClearance");
    private static final List<ClassificationLevel> ALT_CLASSIFICATION_LEVELS = asLevelList("Board",                "B",
                                                                                           "Executive Management", "EM",
                                                                                           "Employee",             "E",
                                                                                           "Public",               "P");
    private static final List<ClassificationReason> PLACEHOLDER_CLASSIFICATION_REASONS = asList(new ClassificationReason("id1", "label1"),
                                                                                                new ClassificationReason("id2", "label2"));
    private static final List<ClassificationReason> ALTERNATIVE_CLASSIFICATION_REASONS = asList(new ClassificationReason("id8", "label8"),
                                                                                                new ClassificationReason("id9", "label9"));

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

    @InjectMocks ClassificationServiceBootstrap classificationServiceBootstrap;
    @Mock ClassificationServiceDAO mockClassificationServiceDAO;
    @Mock AttributeService mockAttributeService;
    @Mock AuthenticationUtil mockAuthenticationUtil;
    @Mock ClearanceLevelManager mockClearanceLevelManager;
    /** Using a mock appender in the class logger so that we can verify some of the logging requirements. */
    @Mock Appender mockAppender;
    @Captor ArgumentCaptor<LoggingEvent> loggingEventCaptor;
    @Captor ArgumentCaptor<List<ClearanceLevel>> clearanceLevelCaptor;

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
        // This seems to be necessary because the ClearanceLevelManager isn't a constructor argument.
        classificationServiceBootstrap.setClearanceLevelManager(mockClearanceLevelManager);
    }

    @Test public void defaultLevelsConfigurationVanillaSystem()
    {
        when(mockClassificationServiceDAO.getConfiguredLevels()).thenReturn(DEFAULT_CLASSIFICATION_LEVELS);
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(null);

        classificationServiceBootstrap.initConfiguredClassificationLevels();

        verify(mockAttributeService).setAttribute(eq((Serializable) DEFAULT_CLASSIFICATION_LEVELS),
                anyString(), anyString(), anyString());
    }

    @Test public void alternativeLevelsConfigurationPreviouslyStartedSystem()
    {
        when(mockClassificationServiceDAO.getConfiguredLevels()).thenReturn(ALT_CLASSIFICATION_LEVELS);
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString()))
                                   .thenReturn((Serializable) DEFAULT_CLASSIFICATION_LEVELS);

        classificationServiceBootstrap.initConfiguredClassificationLevels();

        verify(mockAttributeService).setAttribute(eq((Serializable) ALT_CLASSIFICATION_LEVELS),
                anyString(), anyString(), anyString());
    }

    @Test (expected=MissingConfiguration.class)
    public void missingLevelsConfigurationVanillaSystemShouldFail() throws Exception
    {
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(null);

        classificationServiceBootstrap.initConfiguredClassificationLevels();
    }

    @Test public void pristineSystemShouldBootstrapReasonsConfiguration()
    {
        // There are no classification reasons stored in the AttributeService.
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(null);

        // We'll use a small set of placeholder classification reasons.
        when(mockClassificationServiceDAO.getConfiguredReasons()).thenReturn(PLACEHOLDER_CLASSIFICATION_REASONS);

        classificationServiceBootstrap.initConfiguredClassificationReasons();

        verify(mockAttributeService).setAttribute(eq((Serializable)PLACEHOLDER_CLASSIFICATION_REASONS),
                anyString(), anyString(), anyString());
    }

    @Test public void checkAttributesNotTouchedIfConfiguredReasonsHaveNotChanged()
    {
        // The classification reasons stored are the same values that are found on the classpath.
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn((Serializable)PLACEHOLDER_CLASSIFICATION_REASONS);
        when(mockClassificationServiceDAO.getConfiguredReasons()).thenReturn(PLACEHOLDER_CLASSIFICATION_REASONS);

        classificationServiceBootstrap.initConfiguredClassificationReasons();

        verify(mockAttributeService, never()).setAttribute(any(Serializable.class),
                anyString(), anyString(), anyString());
    }

    /**
     * Check that if the reasons supplied on the classpath differ from those already persisted then a warning is logged
     * and no change is made to the persisted reasons.
     * <p>
     * This test uses the underlying log4j implementation to insert a mock Appender, and tests this for the warning
     * message. If the underlying logging framework is changed then this unit test will fail, and it may not be
     * possible to/worth fixing.
     */
    @Test public void previouslyStartedSystemShouldWarnIfConfiguredReasonsHaveChanged()
    {
        // The classification reasons stored are different from those found on the classpath.
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(
                    (Serializable) PLACEHOLDER_CLASSIFICATION_REASONS);
        when(mockClassificationServiceDAO.getConfiguredReasons()).thenReturn(ALTERNATIVE_CLASSIFICATION_REASONS);

        // Put the mock Appender into the log4j logger and allow warning messages to be received.
        org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(ClassificationServiceBootstrap.class);
        log4jLogger.addAppender(mockAppender);
        Level normalLevel = log4jLogger.getLevel();
        log4jLogger.setLevel(Level.WARN);

        // Call the method under test.
        classificationServiceBootstrap.initConfiguredClassificationReasons();

        // Reset the logging level for other tests.
        log4jLogger.setLevel(normalLevel);

        // Check the persisted values weren't changed.
        verify(mockAttributeService, never()).setAttribute(any(Serializable.class),
                anyString(), anyString(), anyString());

        // Check that the warning message was logged.
        verify(mockAppender).doAppend(loggingEventCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventCaptor.getAllValues();
        Stream<String> messages = loggingEvents.stream().map(event -> event.getRenderedMessage());
        String expectedMessage = "Classification reasons configured in classpath do not match those stored in Alfresco. Alfresco will use the unchanged values stored in the database.";
        assertTrue("Warning message not found in log.", messages.anyMatch(message -> message == expectedMessage));
    }

    @Test(expected = MissingConfiguration.class)
    public void noReasonsFoundCausesException()
    {
        when(mockAttributeService.getAttribute(anyString(), anyString(), anyString()))
                    .thenReturn((Serializable) null);
        when(mockClassificationServiceDAO.getConfiguredReasons()).thenReturn(null);

        classificationServiceBootstrap.initConfiguredClassificationReasons();
    }

    /**
     * Check that the initialise method creates a clearance level corresponding to each classification level and that
     * the display label for the lowest clearance level is "No Clearance" (rather than "Unclassified").
     */
    @Test public void initConfiguredClearanceLevels()
    {
        ClassificationLevel topSecret = new ClassificationLevel("1", "TopSecret");
        ClassificationLevel secret = new ClassificationLevel("2", "Secret");
        ImmutableList<ClassificationLevel> classificationLevels = ImmutableList.of(topSecret, secret, ClassificationLevelManager.UNCLASSIFIED);
        doNothing().when(mockClearanceLevelManager).setClearanceLevels(clearanceLevelCaptor.capture());

        // Call the method under test.
        classificationServiceBootstrap.initConfiguredClearanceLevels(classificationLevels);

        List<ClearanceLevel> clearanceLevels = clearanceLevelCaptor.getValue();
        assertEquals("There should be one clearance level for each classification level.", classificationLevels.size(), clearanceLevels.size());
        assertEquals("TopSecret", clearanceLevels.get(0).getDisplayLabel());
        assertEquals("Secret", clearanceLevels.get(1).getDisplayLabel());
        assertEquals("rm.classification.noClearance", clearanceLevels.get(2).getDisplayLabel());
    }
}
