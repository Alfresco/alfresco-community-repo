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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.ExemptionCategoryIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.ReasonIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ClassificationSchemeServiceImpl}.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class ClassificationSchemeServiceImplUnitTest
{
    private static final List<ClassificationLevel> DEFAULT_CLASSIFICATION_LEVELS = asLevelList("Top Secret",   "rm.classification.topSecret",
                                                                                               "Secret",       "rm.classification.secret",
                                                                                               "Confidential", "rm.classification.confidential",
                                                                                               "No Clearance", "rm.classification.noClearance");

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

    @InjectMocks private ClassificationSchemeServiceImpl classificationSchemeServiceImpl;

    @Mock private NodeService                 mockNodeService;
    @Mock private DictionaryService           mockDictionaryService;
    @Mock private ClassificationLevelManager  mockLevelManager;
    @Mock private ClassificationReasonManager mockReasonManager;
    @Mock private ExemptionCategoryManager    mockExemptionCategoryManager;
    @Captor private ArgumentCaptor<Map<QName, Serializable>> propertiesCaptor;

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Check that restrictList returns the three lower security levels when supplied with "secret" (i.e. that it doesn't
     * return "top secret").
     */
    @Test public void restrictList_filter()
    {
        ClassificationLevel targetLevel = new ClassificationLevel("Secret", "rm.classification.secret");

        List<ClassificationLevel> actual = classificationSchemeServiceImpl.restrictList(DEFAULT_CLASSIFICATION_LEVELS, targetLevel);

        List<ClassificationLevel> expected = asLevelList("Secret",       "rm.classification.secret",
                                                         "Confidential", "rm.classification.confidential",
                                                         "No Clearance", "rm.classification.noClearance");
        assertEquals(expected, actual);
        // Check that the returned list can't be modified.
        ExceptionUtils.expectedException(UnsupportedOperationException.class, () -> actual.remove(0));
    }

    /**
     * Check that restrictList returns an empty list when the target is not contained in the list.
     */
    @Test public void restrictList_targetNotFound()
    {
        ClassificationLevel targetLevel = new ClassificationLevel("UnrecognisedLevel", "rm.classification.IMadeThisUp");

        List<ClassificationLevel> actual = classificationSchemeServiceImpl.restrictList(DEFAULT_CLASSIFICATION_LEVELS, targetLevel);

        assertEquals("Expected an empty list when the target level is not found.", 0, actual.size());
    }

    @Test
    public void getClassificationLevelById()
    {
        String levelId = "classificationLevelId1";
        ClassificationLevel classificationLevel = new ClassificationLevel(levelId, "displayLabelKey");
        when(mockLevelManager.findLevelById(levelId)).thenReturn(classificationLevel);
        ClassificationLevel classificationLevelById = classificationSchemeServiceImpl.getClassificationLevelById(levelId);
        assertEquals(classificationLevel, classificationLevelById);
    }

    @Test(expected = LevelIdNotFound.class)
    public void getClassificationLevelById_nonExisting()
    {
        String classificationLevelId = "aRandomId";
        doThrow(new LevelIdNotFound("Id not found!")).when(mockLevelManager).findLevelById(classificationLevelId);
        classificationSchemeServiceImpl.getClassificationLevelById(classificationLevelId);
    }

    @Test
    public void getClassificationReasonById()
    {
        String reasonId = "classificationReasonId1";
        ClassificationReason classificationReason = new ClassificationReason(reasonId, "displayLabelKey");
        when(mockReasonManager.findReasonById(reasonId)).thenReturn(classificationReason);
        ClassificationReason classificationReasonById = classificationSchemeServiceImpl.getClassificationReasonById(reasonId);
        assertEquals(classificationReason, classificationReasonById);
    }

    @Test(expected = ReasonIdNotFound.class)
    public void getClassificationReasonById_nonExisting()
    {
        String classificationReasonId = "aRandomId";
        doThrow(new ReasonIdNotFound("Id not found!")).when(mockReasonManager).findReasonById(classificationReasonId);
        classificationSchemeServiceImpl.getClassificationReasonById(classificationReasonId);
    }

    @Test
    public void getExemptionCategoryById()
    {
        String exemptionCategoryId = "exemptionCategoryId";
        ExemptionCategory exemptionCategory = new ExemptionCategory(exemptionCategoryId, "displayLabelKey");
        when(mockExemptionCategoryManager.findCategoryById(exemptionCategoryId)).thenReturn(exemptionCategory);
        classificationSchemeServiceImpl.getExemptionCategoryById(exemptionCategoryId);
    }

    @Test(expected = ExemptionCategoryIdNotFound.class)
    public void getExemptionCategoryById_nonExisting()
    {
        String exemptionCategoryId = "aRandomId";
        doThrow(new ExemptionCategoryIdNotFound("Id not found!")).when(mockExemptionCategoryManager).findCategoryById(exemptionCategoryId);
        classificationSchemeServiceImpl.getExemptionCategoryById(exemptionCategoryId);
    }
}
