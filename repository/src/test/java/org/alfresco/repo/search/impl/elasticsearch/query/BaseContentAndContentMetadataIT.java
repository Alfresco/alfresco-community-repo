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
package org.alfresco.repo.search.impl.elasticsearch.query;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BaseContentAndContentMetadataIT extends ElasticsearchBaseQueryIT
{

    protected NodeRef txtDocument;
    protected NodeRef pdfDocument;

    @Before
    public void initDocuments()
    {
        txtDocument = indexDocument("test.txt", 100, "text/plain", "UTF-8");
        pdfDocument = indexDocument("test.pdf", 1000, "application/pdf", "ISO-8859-5");
    }

    @Test
    public abstract void whenSearchUsingSize();

    @Test
    public abstract void whenSearchUsingMimetype();

    @Test
    public abstract void whenSearchUsingEncoding();

}
