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
package org.alfresco.repo.jscript.app;

import static org.alfresco.repo.jscript.app.ClassificationReasonsPropertyDecorator.DISPLAY_LABEL;
import static org.alfresco.repo.jscript.app.ClassificationReasonsPropertyDecorator.FULL_REASON;
import static org.alfresco.repo.jscript.app.ClassificationReasonsPropertyDecorator.ID;
import static org.alfresco.repo.jscript.app.ClassificationReasonsPropertyDecorator.LABEL;
import static org.alfresco.repo.jscript.app.ClassificationReasonsPropertyDecorator.VALUE;
import static org.alfresco.util.GUID.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationReason;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Classification reasons property decorator unit test
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassificationReasonsPropertyDecoratorUnitTest
{
    /** Classification reasons property decorator */
    private @InjectMocks ClassificationReasonsPropertyDecorator decorator = new ClassificationReasonsPropertyDecorator();

    /** Mocked classification scheme service */
    private @Mock ClassificationSchemeService mockedClassificationSchemeService;

    /**
     * Given that no classification reason id is supplied
     * When decorated
     * Then an {@link IllegalArgumentException} is thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecoratorWithoutClassificationId()
    {
        decorator.decorate(null, null, null);
    }

    /**
     * Given that a classification reason id in an invalid format is supplied
     * When decorated
     * Then an {@link JsonSyntaxException} is thrown
     */
    @Test(expected = JsonSyntaxException.class)
    public void testDecoratorWithInvalidClassificationReasonId()
    {
        String classificationReasonId = generate();
        String classificationReasonDisplayLabel = generate();

        ClassificationReason classificationReason = new ClassificationReason(classificationReasonId, classificationReasonDisplayLabel);
        doReturn(classificationReason).when(mockedClassificationSchemeService).getClassificationReasonById(classificationReasonId);

        decorator.decorate(null, null, classificationReasonId);
    }

    /**
     * Given that a classification reason id is supplied
     * When decorated
     * Then the result is a {@link JSONArray} containing the classification reason id, label, value, display label and full reason
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDecoratorWithClassificationReasonId()
    {
        String classificationReasonId = generate();
        String classificationReasonDisplayLabel = generate();
        String classificationFullReason = classificationReasonId + ": " + classificationReasonDisplayLabel;

        ClassificationReason classificationReason = new ClassificationReason(classificationReasonId, classificationReasonDisplayLabel);
        doReturn(classificationReason).when(mockedClassificationSchemeService).getClassificationReasonById(classificationReasonId);

        JSONArray classificationReasonIds = new JSONArray();
        classificationReasonIds.add(classificationReasonId);

        JSONArray decoratedProperty = (JSONArray) decorator.decorate(null, null, new Gson().toJson(classificationReasonIds));
        assertNotNull(decoratedProperty);
        assertEquals(1, decoratedProperty.size());

        JSONObject jsonObject = (JSONObject) decoratedProperty.get(0);
        assertNotNull(jsonObject);

        assertEquals(classificationReasonId, jsonObject.get(ID));
        assertEquals(classificationFullReason, jsonObject.get(LABEL));
        assertEquals(classificationReasonId, jsonObject.get(VALUE));
        assertEquals(classificationReasonDisplayLabel, jsonObject.get(DISPLAY_LABEL));
        assertEquals(classificationFullReason, jsonObject.get(FULL_REASON));
    }

    /**
     * Given that two classification reason id are supplied
     * When decorated
     * Then the result is a {@link JSONArray} containing the classification reason ids,
     * labels, values, display labels and full reasons of both classification reasons
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDecorateWithMultipleClassificationReasonIds()
    {
        String classificationReasonId1 = generate();
        String classificationReasonDisplayLabel1 = generate();
        String classificationFullReason1 = classificationReasonId1 + ": " + classificationReasonDisplayLabel1;

        String classificationReasonId2 = generate();
        String classificationReasonDisplayLabel2 = generate();
        String classificationFullReason2 = classificationReasonId2 + ": " + classificationReasonDisplayLabel2;

        ClassificationReason classificationReason1 = new ClassificationReason(classificationReasonId1, classificationReasonDisplayLabel1);
        ClassificationReason classificationReason2 = new ClassificationReason(classificationReasonId2, classificationReasonDisplayLabel2);
        doReturn(classificationReason1).when(mockedClassificationSchemeService).getClassificationReasonById(classificationReasonId1);
        doReturn(classificationReason2).when(mockedClassificationSchemeService).getClassificationReasonById(classificationReasonId2);

        JSONArray classificationReasonIds = new JSONArray();
        classificationReasonIds.add(classificationReasonId1);
        classificationReasonIds.add(classificationReasonId2);

        JSONArray decoratedProperty = (JSONArray) decorator.decorate(null, null, new Gson().toJson(classificationReasonIds));
        assertNotNull(decoratedProperty);
        assertEquals(2, decoratedProperty.size());

        JSONObject jsonObject1 = (JSONObject) decoratedProperty.get(0);
        assertNotNull(jsonObject1);

        assertEquals(classificationReasonId1, jsonObject1.get(ID));
        assertEquals(classificationFullReason1, jsonObject1.get(LABEL));
        assertEquals(classificationReasonId1, jsonObject1.get(VALUE));
        assertEquals(classificationReasonDisplayLabel1, jsonObject1.get(DISPLAY_LABEL));
        assertEquals(classificationFullReason1, jsonObject1.get(FULL_REASON));

        JSONObject jsonObject2 = (JSONObject) decoratedProperty.get(1);
        assertNotNull(jsonObject2);

        assertEquals(classificationReasonId2, jsonObject2.get(ID));
        assertEquals(classificationFullReason2, jsonObject2.get(LABEL));
        assertEquals(classificationReasonId2, jsonObject2.get(VALUE));
        assertEquals(classificationReasonDisplayLabel2, jsonObject2.get(DISPLAY_LABEL));
        assertEquals(classificationFullReason2, jsonObject2.get(FULL_REASON));
    }
}