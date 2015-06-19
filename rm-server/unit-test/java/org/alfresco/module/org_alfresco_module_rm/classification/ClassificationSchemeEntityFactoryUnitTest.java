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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


/**
 * Unit tests for the {@link ClassificationSchemeEntityFactory}.
 *
 * @author tpage
 * @since 3.0
 */
public class ClassificationSchemeEntityFactoryUnitTest
{
    ClassificationSchemeEntityFactory classificationSchemeEntityFactory = new ClassificationSchemeEntityFactory();
    @Mock JSONObject mockJsonObject;

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    @Test
    public void testCreateClassificationLevel() throws JSONException
    {
        when(mockJsonObject.getString("name")).thenReturn("name1");
        when(mockJsonObject.getString("displayLabel")).thenReturn("displayLabelKey2");

        ClassificationLevel classificationLevel = classificationSchemeEntityFactory.create(ClassificationLevel.class, mockJsonObject);

        assertEquals("name1", classificationLevel.getId());
        assertEquals("displayLabelKey2", classificationLevel.getDisplayLabelKey());
    }

    @Test
    public void testCreateClassificationReason() throws JSONException
    {
        when(mockJsonObject.getString("id")).thenReturn("id1");
        when(mockJsonObject.getString("displayLabel")).thenReturn("displayLabelKey2");

        ClassificationReason classificationReason = classificationSchemeEntityFactory.create(ClassificationReason.class, mockJsonObject);

        assertEquals("id1", classificationReason.getId());
        assertEquals("displayLabelKey2", classificationReason.getDisplayLabelKey());
    }

    @Test
    public void testCreateExemptionCategory() throws JSONException
    {
        when(mockJsonObject.getString("id")).thenReturn("id1");
        when(mockJsonObject.getString("displayLabel")).thenReturn("displayLabelKey2");

        ExemptionCategory exemptionCategory = classificationSchemeEntityFactory.create(ExemptionCategory.class, mockJsonObject);

        assertEquals("id1", exemptionCategory.getId());
        assertEquals("displayLabelKey2", exemptionCategory.getDisplayLabelKey());
    }
}
