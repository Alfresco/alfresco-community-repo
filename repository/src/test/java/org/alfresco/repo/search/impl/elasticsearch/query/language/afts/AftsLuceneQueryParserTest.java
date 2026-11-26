/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.elasticsearch.util.MockNamespaceService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class AftsLuceneQueryParserTest
{
    private static final String CM_CREATED = "{http://www.alfresco.org/model/content/1.0}created";
    private static final String CM_EXPIRY_DATE = "{http://www.alfresco.org/model/content/1.0}expiryDate";
    private static final String CM_NAME = "{http://www.alfresco.org/model/content/1.0}name";

    private MockNamespaceService resolver;
    private DictionaryService dictionaryService;
    private AftsLuceneQueryParser parser;

    @Before
    public void setUp()
    {
        resolver = new MockNamespaceService();
        resolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        var dataTypes = Map.of(ContentModel.PROP_CREATED, dataType(DataTypeDefinition.DATETIME), ContentModel.PROP_EXPIRY_DATE, dataType(DataTypeDefinition.DATE),
                ContentModel.PROP_NAME, dataType(DataTypeDefinition.TEXT));
        dictionaryService = mock(DictionaryService.class);
        when(dictionaryService.getProperty(any())).thenAnswer(invocation -> {
            QName name = invocation.getArgument(0);
            var property = mock(PropertyDefinition.class);
            when(property.getName()).thenReturn(name);
            when(property.getDataType()).thenReturn(dataTypes.get(name));
            return property;
        });
        parser = new AftsLuceneQueryParser(resolver, dictionaryService, mock(SiteService.class), new SearchParameters(),
                mock(ElasticsearchExactTermSearchConfig.class));
    }

    private static DataTypeDefinition dataType(QName name)
    {
        var dataType = mock(DataTypeDefinition.class);
        when(dataType.getName()).thenReturn(name);
        return dataType;
    }

    @Test
    public void shouldConvertMonthNameInDateTerm()
    {
        assertEquals("cm%3Acreated:2025-08-05T", parser.getFieldQuery(CM_CREATED, "2025-Aug-05T", false).toString());
        assertEquals("cm%3Acreated:2025-08-05T", parser.getFieldQuery(CM_CREATED, "2025-Aug-05T", true).toString());
    }

    @Test
    public void shouldMatchWholeYearOrMonthOfDateTerm()
    {
        assertEquals("cm%3Acreated:[2025||/y TO 2025||/y]", parser.getFieldQuery(CM_CREATED, "2025", false).toString());
        assertEquals("cm%3Acreated:[2025-08||/M TO 2025-08||/M]", parser.getFieldQuery(CM_CREATED, "2025-08", false).toString());
        assertEquals("cm%3Acreated:[2025-08||/M TO 2025-08||/M]", parser.getFieldQuery(CM_CREATED, "2025-Aug", true).toString());
    }

    @Test
    public void shouldConvertDateOnlyProperty()
    {
        assertEquals("cm%3AexpiryDate:[2025-08||/M TO 2025-08||/M]", parser.getFieldQuery(CM_EXPIRY_DATE, "2025-Aug", false).toString());
    }

    @Test
    public void shouldConvertDateRangeBounds()
    {
        assertEquals("cm%3Acreated:{2025-07||/M TO 2025-09||/M}", parser.getRangeQuery(CM_CREATED, "2025-07", "2025-Sep", false, false).toString());
        assertEquals("cm%3Acreated:[2025-08-01 TO 2025-08-05T]", parser.getRangeQuery(CM_CREATED, "2025-Aug-01", "2025-Aug-05T", true, true).toString());
        assertEquals("cm%3Acreated:[\\* TO 2025||/y]", parser.getRangeQuery(CM_CREATED, "*", "2025", true, true).toString());
    }

    @Test
    public void shouldKeepValuesOfOtherProperties()
    {
        assertEquals("cm%3Aname:2025-Aug", parser.getFieldQuery(CM_NAME, "2025-Aug", false).toString());
        assertEquals("cm%3Aname:2025", parser.getFieldQuery(CM_NAME, "2025", false).toString());
        assertEquals("cm%3Aname:[2025 TO 2026]", parser.getRangeQuery(CM_NAME, "2025", "2026", true, true).toString());
    }

    /** The Lucene language uses the base parser and keeps its behaviour. */
    @Test
    public void shouldLeaveLuceneLanguageUnchanged() throws ParseException
    {
        var luceneParser = new LuceneQueryParser(resolver, dictionaryService, mock(SiteService.class), new SearchParameters());

        assertEquals("cm%3Acreated:2025-Aug", luceneParser.getFieldQuery(CM_CREATED, "2025-Aug", false).toString());
        assertEquals("cm%3Acreated:2025", luceneParser.parse("@cm\\:created:2025").toString());
    }
}
