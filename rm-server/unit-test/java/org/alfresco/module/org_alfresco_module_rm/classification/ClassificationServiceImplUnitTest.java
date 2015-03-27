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
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
import static java.util.Arrays.asList;

/**
 * Unit tests for {@link ClassificationServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceImplUnitTest
{
    private static final List<ClassificationLevel> DEFAULT_CLASSIFICATION_LEVELS = asLevelList("Top Secret",   "rm.classification.topSecret",
                                                                                               "Secret",       "rm.classification.secret",
                                                                                               "Confidential", "rm.classification.confidential",
                                                                                               "No Clearance", "rm.classification.noClearance");
    private static final List<ClassificationLevel> ALT_CLASSIFICATION_LEVELS = asLevelList("Board",                "B",
                                                                                           "Executive Management", "EM",
                                                                                           "Employee",             "E",
                                                                                           "Public",               "P");
    private static final List<ClassificationReason> PLACEHOLDER_CLASSIFICATION_REASONS = asList(new ClassificationReason("r1", "l1"),
                                                                                                new ClassificationReason("r2", "l2"));
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

    private ClassificationServiceImpl classificationService;

    private AttributeService   mockedAttributeService   = mock(AttributeService.class);
    private AuthenticationUtil mockedAuthenticationUtil = mock(AuthenticationUtil.class);
    private Configuration      mockConfig               = mock(Configuration.class);

    @Before public void setUp()
    {
        reset(mockConfig, mockedAttributeService);

        // FIXME This should be out of here (and BaseUnitTest) and into a common utility class.
        // We don't care about authentication here.
        doAnswer(new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork work
                        = (org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork)invocation.getArguments()[0];
                return work.doWork();
            }

        }).when(mockedAuthenticationUtil).<Object>runAs(any(org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork.class), anyString());

        // Use the admin user
        doReturn("admin").when(mockedAuthenticationUtil).getAdminUserName();
        doReturn("admin").when(mockedAuthenticationUtil).getFullyAuthenticatedUser();

        classificationService = new ClassificationServiceImpl(mockConfig);
        classificationService.setAttributeService(mockedAttributeService);
        classificationService.setAuthenticationUtil(mockedAuthenticationUtil);
    }

    @Test public void defaultLevelsConfigurationVanillaSystem()
    {
        when(mockConfig.getConfiguredLevels()).thenReturn(DEFAULT_CLASSIFICATION_LEVELS);
        when(mockedAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(null);

        classificationService.initConfiguredClassificationLevels();

        verify(mockedAttributeService).setAttribute(eq((Serializable) DEFAULT_CLASSIFICATION_LEVELS),
                anyString(), anyString(), anyString());
    }

    @Test public void alternativeLevelsConfigurationPreviouslyStartedSystem()
    {
        when(mockConfig.getConfiguredLevels()).thenReturn(ALT_CLASSIFICATION_LEVELS);
        when(mockedAttributeService.getAttribute(anyString(), anyString(), anyString()))
                                   .thenReturn((Serializable) DEFAULT_CLASSIFICATION_LEVELS);

        classificationService.initConfiguredClassificationLevels();

        verify(mockedAttributeService).setAttribute(eq((Serializable) ALT_CLASSIFICATION_LEVELS),
                anyString(), anyString(), anyString());
    }

    @Test (expected=MissingConfiguration.class)
    public void missingLevelsConfigurationVanillaSystemShouldFail() throws Exception
    {
        when(mockedAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(null);

        classificationService.initConfiguredClassificationLevels();
    }

    @Test public void pristineSystemShouldBootstrapReasonsConfiguration()
    {
        // There are no classification reasons stored in the AttributeService.
        when(mockedAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn(null);

        // We'll use a small set of placeholder classification reasons.
        when(mockConfig.getConfiguredReasons()).thenReturn(PLACEHOLDER_CLASSIFICATION_REASONS);

        classificationService.initConfiguredClassificationReasons();

        verify(mockedAttributeService).setAttribute(eq((Serializable)PLACEHOLDER_CLASSIFICATION_REASONS),
                anyString(), anyString(), anyString());
    }

    @Ignore ("This test is currently failing. Needs to be fixed.") // FIXME
    @Test public void previouslyStartedSystemShouldProceedNormallyIfConfiguredReasonsHaveNotChanged()
    {
        // There are existing classification reasons stored in the AttributeService.
        when(mockedAttributeService.getAttribute(anyString(), anyString(), anyString())).thenReturn((Serializable)PLACEHOLDER_CLASSIFICATION_REASONS);

        // We'll use a small set of placeholder classification reasons.
        when(mockConfig.getConfiguredReasons()).thenReturn(PLACEHOLDER_CLASSIFICATION_REASONS);

        classificationService.initConfiguredClassificationReasons();

        // This line added to try and work out what the interaction *is*.
        verifyZeroInteractions(mockedAttributeService);

        verify(mockedAttributeService, never()).setAttribute(any(Serializable.class),
                anyString(), anyString(), anyString());
    }

    @Ignore ("To be implemented") // TODO
    @Test public void previouslyStartedSystemShouldWarnIfConfiguredReasonsHaveChanged()
    {
        fail("TODO");
    }
}
