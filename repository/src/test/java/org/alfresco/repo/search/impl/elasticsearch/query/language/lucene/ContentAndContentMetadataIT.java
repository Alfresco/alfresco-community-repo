/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseContentAndContentMetadataIT;

public class ContentAndContentMetadataIT extends BaseContentAndContentMetadataIT
{
    @Override
    public void whenSearchUsingSize()
    {
        assertContainsOnly(luceneSearch("@cm\\:content.size:100"), txtDocument);
        assertContainsOnly(luceneSearch("test AND @cm\\:content.size:100"), txtDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.size:1000"), pdfDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.size:2000"));
    }

    @Override
    public void whenSearchUsingMimetype()
    {
        assertContainsOnly(luceneSearch("@cm\\:content.mimetype:text\\/plain"), txtDocument);
        assertContainsOnly(luceneSearch("test AND @cm\\:content.mimetype:text\\/plain"), txtDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.mimetype:application\\/pdf"), pdfDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.mimetype:text\\/xml"));

        assertContainsOnly(luceneSearch("@cm\\:content.mimetype:\"text/plain\""), txtDocument);
        assertContainsOnly(luceneSearch("test AND @cm\\:content.mimetype:\"text/plain\""), txtDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.mimetype:\"application/pdf\""), pdfDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.mimetype:\"text/xml\""));
    }

    @Override
    public void whenSearchUsingEncoding()
    {
        assertContainsOnly(luceneSearch("@cm\\:content.encoding:UTF-8"), txtDocument);
        assertContainsOnly(luceneSearch("test AND @cm\\:content.encoding:UTF-8"), txtDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.encoding:ISO-8859-5"), pdfDocument);
        assertContainsOnly(luceneSearch("@cm\\:content.encoding:UTF-16"));
    }
}
