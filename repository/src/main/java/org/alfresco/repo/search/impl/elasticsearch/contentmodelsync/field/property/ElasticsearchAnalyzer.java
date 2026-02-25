/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.AnalyzerPurpose.SYMMETRIC_INDEX_ANALYZER;

import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Property;

public class ElasticsearchAnalyzer implements Property
{
    public static final ElasticsearchAnalyzer DEFAULT_ANALYZER = new ElasticsearchAnalyzer("locale_content", SYMMETRIC_INDEX_ANALYZER);
    public static final ElasticsearchAnalyzer DEFAULT_EXACT_TERM_SEARCH_ANALYZER = new ElasticsearchAnalyzer("standard", SYMMETRIC_INDEX_ANALYZER);

    private final String name;
    private final String purpose;

    private ElasticsearchAnalyzer(String name, AnalyzerPurpose purpose)
    {
        this.name = name;
        this.purpose = purpose.purpose;
    }

    public static ElasticsearchAnalyzer of(TextType textType, AnalyzerType type, AnalyzerPurpose purpose)
    {
        String name = type.type + textType.name + purpose.suffix;

        return new ElasticsearchAnalyzer(name, purpose);
    }

    @Override
    public Entry<String, ?> toEntry()
    {
        return Map.entry(purpose, name);
    }

    public String getName()
    {
        return name;
    }

    public enum TextType
    {
        CONTENT("content"), TEXT("text");

        private final String name;

        TextType(String name)
        {
            this.name = name;
        }
    }

    public enum AnalyzerType
    {
        LOCALE_ANALYZER("locale_"), LOCALE_CROSS_ANALYZER("locale_cross_");

        private final String type;

        AnalyzerType(String prefix)
        {
            this.type = prefix;
        }
    }

    public enum AnalyzerPurpose
    {
        SYMMETRIC_INDEX_ANALYZER("analyzer", ""), ASYMMETRIC_INDEX_ANALYZER("analyzer", "_index"), SEARCH_ANALYZER("search_analyzer", "_query"), QUOTE_ANALYZER("search_quote_analyzer", "_quote");

        private final String purpose;
        private final String suffix;

        AnalyzerPurpose(String purpose, String suffix)
        {
            this.purpose = purpose;
            this.suffix = suffix;
        }
    }

}
