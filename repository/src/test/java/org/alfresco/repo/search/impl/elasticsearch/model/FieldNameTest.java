/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.search.impl.elasticsearch.model;

import static java.lang.String.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

@RunWith(TestParameterInjector.class)
public class FieldNameTest
{
    @Mock
    NamespaceDAO namespaceDAO = mock();

    @Test
    public void shouldReturnRawName()
    {
        String name = "cm:content";

        FieldName nameUnderTest = new FieldName(name);

        assertEquals("cm:content", nameUnderTest.raw());
    }

    @Test
    public void shouldEncodeName()
    {
        String name = "cm:content";
        FieldName nameUnderTest = new FieldName(name);

        assertEquals("cm%3Acontent", nameUnderTest.encoded());
    }

    @Test
    public void shouldGenerateUntokenizedName()
    {
        String name = "cm:content";
        FieldName nameUnderTest = new FieldName(name);

        assertEquals("cm%3Acontent_untokenized", nameUnderTest.untokenized());
    }

    @Test
    public void shouldCreateNameFromUntokenized()
    {
        String name = "cm%3Acontent_untokenized";
        FieldName nameUnderTest = FieldName.fromUntokenized(name);

        assertEquals("cm:content", nameUnderTest.raw());
    }

    @Test
    public void shouldThrowIfPassedNameIsNotInUntokenizedFormat(@TestParameter({"cm:content", "cm%3Acontent", "cm%3Acontent_exact", "PATH"}) String name)
    {
        assertThrows(IllegalArgumentException.class, () -> FieldName.fromUntokenized(name));
    }

    @Test
    public void shouldGenerateExactTermSearchName()
    {
        String name = "cm:content";
        FieldName nameUnderTest = new FieldName(name);

        assertEquals("cm%3Acontent_exact", nameUnderTest.exactTermSearch());
    }

    @Test
    public void shouldCreateNameFromExactTermSearch()
    {
        String name = "cm%3Acontent_exact";
        FieldName nameUnderTest = FieldName.fromExactTermSearch(name);

        assertEquals("cm:content", nameUnderTest.raw());
    }

    @Test
    public void shouldThrowIfPassedNameIsNotInExactTermSearchFormat(@TestParameter({"cm:content", "cm%3Acontent", "cm%3Acontent_untokenized", "PATH"}) String name)
    {
        assertThrows(IllegalArgumentException.class, () -> FieldName.fromExactTermSearch(name));
    }

    @Test
    public void shouldNotTruncateIfNotNeeded()
    {
        String name = "cm:content";
        FieldName nameUnderTest = new FieldName(name);

        assertEquals("cm:content", nameUnderTest.truncated());
    }

    @Test
    public void shouldCreateNameFromPropertyDefinition()
    {
        String name = "cm:content";

        QName qname = mock();
        when(qname.getPrefixString()).thenReturn(name);

        PropertyDefinition propertyDefinition = mock();
        when(propertyDefinition.getName()).thenReturn(qname);

        FieldName fieldName = new FieldName(propertyDefinition);

        assertEquals("cm:content", fieldName.raw());
    }

    @Test
    public void shouldTruncateIfNeeded()
    {
        String longName = "acme:property_with_insanely_long_name_for_testing_purposes";
        FieldName fieldName = new FieldName(longName);

        assertEquals("...ty_with_insanely_long_name_for_testing_purposes", fieldName.truncated());
    }

    @Test
    public void shouldDetectIfNameIsInExactTermSearchFormat()
    {
        String exactTermSearchName = "acme:prop_exact";

        assertTrue(format("Name %s is in exact term search format!", exactTermSearchName), FieldName.isExactTermSearch(exactTermSearchName));

        String rawName = "acme:prop";

        assertFalse(format("Name %s is not in exact term search format!", rawName), FieldName.isExactTermSearch(rawName));
    }

    @Test
    public void shouldDetectIfNameIsInUntokenizedFormat()
    {
        String untokenizedName = "acme:prop_untokenized";

        assertTrue(format("Name %s is in untokenized format!", untokenizedName), FieldName.isUntokenized(untokenizedName));

        String rawName = "acme:prop";

        assertFalse(format("Name %s is not in untokenized format!", rawName), FieldName.isUntokenized(rawName));
    }

    @Test
    public void shouldConvertLuceneNameToFieldName()
    {
        given(namespaceDAO.getPrefixes("http://www.alfresco.org/model/content/1.0")).willReturn(List.of("cm"));

        FieldName fieldName = FieldName.fromLucene("@{http://www.alfresco.org/model/content/1.0}name", namespaceDAO);

        assertEquals("Unexpected raw field name", "cm:name", fieldName.raw());
    }

    @Test(expected = NullPointerException.class)
    public void fromLuceneRejectsNullName()
    {
        FieldName.fromLucene(null, namespaceDAO);
    }

    @Test(expected = NullPointerException.class)
    public void fromLuceneRejectsNullNamespaceDAO()
    {
        FieldName.fromLucene("@{http://www.alfresco.org/model/content/1.0}name", null);
    }
}
