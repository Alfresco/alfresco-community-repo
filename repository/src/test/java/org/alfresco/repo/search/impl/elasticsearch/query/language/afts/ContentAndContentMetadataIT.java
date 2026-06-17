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

import org.alfresco.repo.search.impl.elasticsearch.query.BaseContentAndContentMetadataIT;

public class ContentAndContentMetadataIT extends BaseContentAndContentMetadataIT
{

    @Override
    public void whenSearchUsingSize()
    {
        assertContainsOnly(aftsSearch("cm:content.size:100"), txtDocument);
        assertContainsOnly(aftsSearch("test AND cm:content.size:100"), txtDocument);
        assertContainsOnly(aftsSearch("cm:content.size:1000"), pdfDocument);
        assertContainsOnly(aftsSearch("cm:content.size:2000"));
    }

    @Override
    public void whenSearchUsingMimetype()
    {
        assertContainsOnly(aftsSearch("cm:content.mimetype:text/plain"), txtDocument);
        assertContainsOnly(aftsSearch("test AND cm:content.mimetype:text/plain"), txtDocument);
        assertContainsOnly(aftsSearch("cm:content.mimetype:application/pdf"), pdfDocument);
        assertContainsOnly(aftsSearch("cm:content.mimetype:text/xml"));
    }

    @Override
    public void whenSearchUsingEncoding()
    {
        assertContainsOnly(aftsSearch("cm:content.encoding:UTF-8"), txtDocument);
        assertContainsOnly(aftsSearch("test AND cm:content.encoding:UTF-8"), txtDocument);
        assertContainsOnly(aftsSearch("cm:content.encoding:ISO-8859-5"), pdfDocument);
        assertContainsOnly(aftsSearch("cm:content.encoding:UTF-16"));
    }
}
