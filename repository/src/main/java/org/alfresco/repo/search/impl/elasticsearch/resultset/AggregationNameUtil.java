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
package org.alfresco.repo.search.impl.elasticsearch.resultset;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Opensearch has a limitation. Aggregation names can contain any character except '[', ']', and '>'. Our default facetQueries labels contain range e.g. @{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]
 */
public final class AggregationNameUtil
{
    private AggregationNameUtil()
    {}

    public static String encode(String name)
    {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    public static String decode(String encodedName)
    {
        return URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
    }
}
