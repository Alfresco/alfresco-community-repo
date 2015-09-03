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

import static org.alfresco.repo.jscript.app.CurrentClassificationPropertyDecorator.ID;
import static org.alfresco.repo.jscript.app.CurrentClassificationPropertyDecorator.LABEL;
import static org.alfresco.util.GUID.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Current classification property decorator unit test
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
@RunWith(MockitoJUnitRunner.class)
public class CurrentClassificationPropertyDecoratorUnitTest
{
    /** Current classification property decorator */
    private @InjectMocks CurrentClassificationPropertyDecorator decorator = new CurrentClassificationPropertyDecorator();

    /** Mocked classification scheme service */
    private @Mock ClassificationSchemeService mockedClassificationSchemeService;

    /**
     * Given that no classification id is supplied
     * When decorated
     * Then an {@link IllegalArgumentException} is thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecoratorWithoutClassificationId()
    {
        decorator.decorate(null, null, null);
    }

    /**
     * Given that a classification id is supplied
     * When decorated
     * Then the result is a {@link JSONObject} containing the classification id and display label
     */
    @Test
    public void testDecoratorWithClassificationId()
    {
        String classificationLevelId = generate();
        String classificationDisplayLabel = generate();
        ClassificationLevel classificationLevel = new ClassificationLevel(classificationLevelId, classificationDisplayLabel);
        doReturn(classificationLevel).when(mockedClassificationSchemeService).getClassificationLevelById(classificationLevelId);

        JSONObject decoratedProperty = (JSONObject) decorator.decorate(null, null, classificationLevelId);
        assertNotNull(decoratedProperty);
        assertEquals(classificationLevelId, decoratedProperty.get(ID));
        assertEquals(classificationDisplayLabel, decoratedProperty.get(LABEL));
    }
}
