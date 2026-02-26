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
package org.alfresco.repo.search.impl.elasticsearch.shared.translator;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AlfrescoQualifiedNameTranslator
{
    /**
     * From the URLEncoder official JavaDoc: "Data characters that are allowed in a URI but do not have a reserved purpose are called unreserved. These include upper and lower case letters, decimal digits, and a limited set of punctuation marks and symbols.
     *
     * <p>
     * unreserved = alphanum | mark
     *
     * <p>
     * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     *
     * <p>
     * Unreserved characters can be escaped without changing the semantics of the URI, but this should not be done unless the URI is being used in a context that does not allow the unescaped character to appear."
     *
     * <p>
     * In our context there are some special characters that are reserved in Elasticsearch and not encoded out of the box from the URLEncoder: mark = "-" | "+" | "." | "*" | " "
     *
     * <p>
     * They are replaced explicitly to be consistent with the rest of the encoding. N.B. it's not mandatory to encode "-" | "+" | "*" , but they can cause troubles at query time
     *
     * @param qualifiedName
     * @return
     */
    public static String encode(String qualifiedName)
    {
        return URLEncoder
                .encode(qualifiedName, StandardCharsets.UTF_8)
                .replaceAll("\\.", "%2E")
                .replaceAll("\\-", "%2D")
                .replaceAll("\\*", "%2A")
                .replaceAll("\\+", "%20");
    }

    public static String decode(String elasticsearchFieldName)
    {
        return URLDecoder.decode(elasticsearchFieldName, StandardCharsets.UTF_8);
    }
}
