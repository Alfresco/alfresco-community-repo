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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.elasticsearch.shared.ElasticsearchConstants;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;

public class StoreRefStripper
{
    private static final Pattern STOREREF_REGEX_PATTERN = Pattern.compile("([^\"]*:\\/\\/[^\"]*)");

    private final AlfrescoFunctionEvaluationContext functionContext;

    public StoreRefStripper(NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService,
            String namespace)
    {
        this.functionContext = new AlfrescoFunctionEvaluationContext(namespacePrefixResolver, dictionaryService,
                namespace);
    }

    public String stripIfNeeded(String fieldName, String phrase)
    {
        if (requiresStrippingStoreRef(fieldName))
        {
            return stripStoreRefFromNodeRef(phrase);
        }
        return phrase;
    }

    private boolean requiresStrippingStoreRef(String fieldName)
    {
        return fieldName != null && fieldName.equals(
                functionContext.getLuceneFieldName(ElasticsearchConstants.CM_CATEGORIES));
    }

    private String stripStoreRefFromNodeRef(String phrase)
    {
        if (phrase == null)
        {
            return phrase;
        }
        Matcher matcher = STOREREF_REGEX_PATTERN
                .matcher(phrase);
        if (matcher.find())
        {
            NodeRef nodeRef = new NodeRef(matcher.group(1));
            return nodeRef.getId();
        }
        return phrase;
    }
}
