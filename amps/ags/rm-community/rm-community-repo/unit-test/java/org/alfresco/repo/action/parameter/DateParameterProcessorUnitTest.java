/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.action.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DateParameterProcessor
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class DateParameterProcessorUnitTest
{
    private DateParameterProcessor dateParameterProcessor;

    @Before
    public void setUp() throws Exception
    {
        this.dateParameterProcessor = new DateParameterProcessor();
        this.dateParameterProcessor.setName("date");
    }

    @Test
    public void testGetSubstitutionSuggestions_01()
    {
        List<String> suggestions = this.dateParameterProcessor.getSubstitutionSuggestions("date");
        assertTrue(suggestions.contains("date.day.short"));
        assertTrue(suggestions.contains("date.day"));
        assertTrue(suggestions.contains("date.day.long"));
        assertTrue(suggestions.contains("date.day.number"));
        assertTrue(suggestions.contains("date.day.month"));
        assertTrue(suggestions.contains("date.day.year"));
        assertTrue(suggestions.contains("date.month.short"));
        assertTrue(suggestions.contains("date.month"));
        assertTrue(suggestions.contains("date.month.long"));
        assertTrue(suggestions.contains("date.month.number"));
        assertEquals(10, suggestions.size());
    }

    @Test
    public void testGetSubstitutionSuggestions_02()
    {
        List<String> suggestions = this.dateParameterProcessor.getSubstitutionSuggestions("dat");
        assertTrue(suggestions.contains("date.day.short"));
        assertTrue(suggestions.contains("date.day"));
        assertTrue(suggestions.contains("date.day.long"));
        assertTrue(suggestions.contains("date.day.number"));
        assertTrue(suggestions.contains("date.day.month"));
        assertTrue(suggestions.contains("date.day.year"));
        assertTrue(suggestions.contains("date.month.short"));
        assertTrue(suggestions.contains("date.month"));
        assertTrue(suggestions.contains("date.month.long"));
        assertTrue(suggestions.contains("date.month.number"));
        assertEquals(10, suggestions.size());
    }

    @Test
    public void testGetSubstitutionSuggestions_03()
    {
        List<String> suggestions = this.dateParameterProcessor.getSubstitutionSuggestions("at");
        assertTrue(suggestions.contains("date.day.short"));
        assertTrue(suggestions.contains("date.day"));
        assertTrue(suggestions.contains("date.day.long"));
        assertTrue(suggestions.contains("date.day.number"));
        assertTrue(suggestions.contains("date.day.month"));
        assertTrue(suggestions.contains("date.day.year"));
        assertTrue(suggestions.contains("date.month.short"));
        assertTrue(suggestions.contains("date.month"));
        assertTrue(suggestions.contains("date.month.long"));
        assertTrue(suggestions.contains("date.month.number"));
        assertEquals(10, suggestions.size());
    }

    @Test
    public void testGetSubstitutionSuggestions_05()
    {
        List<String> suggestions = this.dateParameterProcessor.getSubstitutionSuggestions("ay");
        assertTrue(suggestions.contains("date.day.short"));
        assertTrue(suggestions.contains("date.day"));
        assertTrue(suggestions.contains("date.day.long"));
        assertTrue(suggestions.contains("date.day.number"));
        assertTrue(suggestions.contains("date.day.month"));
        assertTrue(suggestions.contains("date.day.year"));
        assertEquals(6, suggestions.size());
    }

    @Test
    public void testGetSubstitutionSuggestions_06()
    {
        List<String> suggestions = this.dateParameterProcessor.getSubstitutionSuggestions("on");
        assertTrue(suggestions.contains("date.day.long"));
        assertTrue(suggestions.contains("date.month.short"));
        assertTrue(suggestions.contains("date.month"));
        assertTrue(suggestions.contains("date.month.long"));
        assertTrue(suggestions.contains("date.month.number"));
        assertTrue(suggestions.contains("date.year.long"));
        assertTrue(suggestions.contains("date.day.month"));
        assertEquals(7, suggestions.size());
    }
}
