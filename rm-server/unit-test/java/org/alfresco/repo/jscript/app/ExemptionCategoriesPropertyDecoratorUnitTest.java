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

import static org.alfresco.repo.jscript.app.ExemptionsCategoriesPropertyDecorator.DISPLAY_LABEL;
import static org.alfresco.repo.jscript.app.ExemptionsCategoriesPropertyDecorator.FULL_CATEGORY;
import static org.alfresco.repo.jscript.app.ExemptionsCategoriesPropertyDecorator.ID;
import static org.alfresco.repo.jscript.app.ExemptionsCategoriesPropertyDecorator.LABEL;
import static org.alfresco.repo.jscript.app.ExemptionsCategoriesPropertyDecorator.VALUE;
import static org.alfresco.util.GUID.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.alfresco.module.org_alfresco_module_rm.classification.ExemptionCategory;
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
 * Exemption categories property decorator unit test
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
@RunWith(MockitoJUnitRunner.class)
public class ExemptionCategoriesPropertyDecoratorUnitTest
{
    /** Exemption categories property decorator */
    private @InjectMocks ExemptionsCategoriesPropertyDecorator decorator = new ExemptionsCategoriesPropertyDecorator();

    /** Mocked classification scheme service */
    private @Mock ClassificationSchemeService mockedClassificationSchemeService;

    /**
     * Given that no exemption category id is supplied
     * When decorated
     * Then an {@link IllegalArgumentException} is thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecoratorWithoutExemptionCategoryId()
    {
        decorator.decorate(null, null, null);
    }

    /**
     * Given that an exemption category id in an invalid format is supplied
     * When decorated
     * Then an {@link JsonSyntaxException} is thrown
     */
    @Test(expected = JsonSyntaxException.class)
    public void testDecoratorWithInvalidExemptionCategoryId()
    {
        String exemptionCategoryId = generate();
        String exemptionCategoryDisplayLabel = generate();

        ExemptionCategory exemptionCategory = new ExemptionCategory(exemptionCategoryId, exemptionCategoryDisplayLabel);
        doReturn(exemptionCategory).when(mockedClassificationSchemeService).getExemptionCategoryById(exemptionCategoryId);

        decorator.decorate(null, null, exemptionCategoryId);
    }

    /**
     * Given that an exemption category id is supplied
     * When decorated
     * Then the result is a {@link JSONArray} containing the exemption category id, label, value, display label and full category
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDecoratorWithExemptionCategoryId()
    {
        String exemptionCategoryId = generate();
        String exemptionCategoryDisplayLabel = generate();
        String exemptionCategoryFullCategory = exemptionCategoryId + ": " + exemptionCategoryDisplayLabel;

        ExemptionCategory exemptionCategory = new ExemptionCategory(exemptionCategoryId, exemptionCategoryDisplayLabel);
        doReturn(exemptionCategory).when(mockedClassificationSchemeService).getExemptionCategoryById(exemptionCategoryId);

        JSONArray exemptionCategoryIds = new JSONArray();
        exemptionCategoryIds.add(exemptionCategoryId);

        JSONArray decoratedProperty = (JSONArray) decorator.decorate(null, null, new Gson().toJson(exemptionCategoryIds));
        assertNotNull(decoratedProperty);
        assertEquals(1, decoratedProperty.size());

        JSONObject jsonObject = (JSONObject) decoratedProperty.get(0);
        assertNotNull(jsonObject);

        assertEquals(exemptionCategoryId, jsonObject.get(ID));
        assertEquals(exemptionCategoryFullCategory, jsonObject.get(LABEL));
        assertEquals(exemptionCategoryId, jsonObject.get(VALUE));
        assertEquals(exemptionCategoryDisplayLabel, jsonObject.get(DISPLAY_LABEL));
        assertEquals(exemptionCategoryFullCategory, jsonObject.get(FULL_CATEGORY));
    }

    /**
     * Given that two exemption category ids are supplied
     * When decorated
     * Then the result is a {@link JSONArray} containing the exemption category ids,
     * labels, values, display labels and full category of both exemption categories
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDecorateWithMultipleExemptionCategoryIds()
    {
        String exemptionCategoryId1 = generate();
        String exemptionCategoryDisplayLabel1 = generate();
        String exemptionCategoryIdFullCategory1 = exemptionCategoryId1 + ": " + exemptionCategoryDisplayLabel1;

        String exemptionCategoryId2 = generate();
        String exemptionCategoryDisplayLabel2 = generate();
        String exemptionCategoryFullCategory2 = exemptionCategoryId2 + ": " + exemptionCategoryDisplayLabel2;

        ExemptionCategory exemptionCategory1 = new ExemptionCategory(exemptionCategoryId1, exemptionCategoryDisplayLabel1);
        ExemptionCategory exemptionCategory2 = new ExemptionCategory(exemptionCategoryId2, exemptionCategoryDisplayLabel2);
        doReturn(exemptionCategory1).when(mockedClassificationSchemeService).getExemptionCategoryById(exemptionCategoryId1);
        doReturn(exemptionCategory2).when(mockedClassificationSchemeService).getExemptionCategoryById(exemptionCategoryId2);

        JSONArray exemptionCategoryIds = new JSONArray();
        exemptionCategoryIds.add(exemptionCategoryId1);
        exemptionCategoryIds.add(exemptionCategoryId2);

        JSONArray decoratedProperty = (JSONArray) decorator.decorate(null, null, new Gson().toJson(exemptionCategoryIds));
        assertNotNull(decoratedProperty);
        assertEquals(2, decoratedProperty.size());

        JSONObject jsonObject1 = (JSONObject) decoratedProperty.get(0);
        assertNotNull(jsonObject1);

        assertEquals(exemptionCategoryId1, jsonObject1.get(ID));
        assertEquals(exemptionCategoryIdFullCategory1, jsonObject1.get(LABEL));
        assertEquals(exemptionCategoryId1, jsonObject1.get(VALUE));
        assertEquals(exemptionCategoryDisplayLabel1, jsonObject1.get(DISPLAY_LABEL));
        assertEquals(exemptionCategoryIdFullCategory1, jsonObject1.get(FULL_CATEGORY));

        JSONObject jsonObject2 = (JSONObject) decoratedProperty.get(1);
        assertNotNull(jsonObject2);

        assertEquals(exemptionCategoryId2, jsonObject2.get(ID));
        assertEquals(exemptionCategoryFullCategory2, jsonObject2.get(LABEL));
        assertEquals(exemptionCategoryId2, jsonObject2.get(VALUE));
        assertEquals(exemptionCategoryDisplayLabel2, jsonObject2.get(DISPLAY_LABEL));
        assertEquals(exemptionCategoryFullCategory2, jsonObject2.get(FULL_CATEGORY));
    }
}