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
package org.alfresco.repo.search.impl.elasticsearch.query.language;

import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.LuceneQueryParser.PRIMARY_HIERARCHY_FIELD;

import java.util.Locale;

import org.alfresco.repo.search.adaptor.QueryConstants;

public interface FieldQueryTransformer
{
    FieldQueryTransformer DEFAULT = new DefaultFieldQueryTransformer();

    String transformTerm(String field, String term);
}

class DefaultFieldQueryTransformer implements FieldQueryTransformer
{
    @Override
    public String transformTerm(String field, String term)
    {
        if (isKeyword(field))
        {
            return toKeywordTerm(term);
        }
        return term;
    }

    private boolean isKeyword(String field)
    {
        return QueryConstants.FIELD_TAG.equals(field) || PRIMARY_HIERARCHY_FIELD.equals(field);
    }

    private String toKeywordTerm(String term)
    {
        return term == null ? null : term.toLowerCase(Locale.ROOT);
    }
}
