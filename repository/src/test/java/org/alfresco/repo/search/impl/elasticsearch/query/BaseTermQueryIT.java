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

package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BaseTermQueryIT extends ElasticsearchBaseQueryIT
{
    protected NodeRef certificateOfCompletion;
    protected NodeRef certificateOfCompletionInName;
    protected NodeRef dotTest;
    protected NodeRef testdocx;
    protected NodeRef dummy;
    protected NodeRef tokenizedFieldDoc;
    protected NodeRef untokenizedFieldDoc;

    @Before
    public void initDocuments()
    {
        Map<String, Object> additionalProperties = Map.of("cm:autoVersion", false, "cm:sizeCurrent", 1000);
        dummy = indexDocument("DummyFile1f62783c-bb84-4c1c-ba76-123b391bcfdd.txt");
        dotTest = indexDocument("MyTest-file___is_beautiful.txt", "content MyFile", additionalProperties);
        certificateOfCompletion = indexDocument("Certificate of Completion");
        testdocx = indexDocument("tèst.docx", "Word document");
        certificateOfCompletionInName = indexDocument("Completion test doçumént_400.32-a", "This is a test");

        Map<String, Object> tokenisedProps = Map.of("cm:title", "OR");
        Map<String, Object> untokenisedProps = Map.of("cm:creator", "OR");

        tokenizedFieldDoc = indexDocument(new IndexDocumentSourceBuilder().withName("tokenisedReservedWord")
                .withContent("content").withAdditionalProperties(tokenisedProps));

        untokenizedFieldDoc = indexDocument(new IndexDocumentSourceBuilder().withName("untokenisedReservedWord")
                .withContent("content").withAdditionalProperties(untokenisedProps));
    }

    /**
     * Verify how word_delimiter_graph analyzer split for ".", "-", and "_"
     */
    @Test
    public abstract void whenSearchForNameContainingDotHyphenUnderscore();

    @Test
    public abstract void whenSearchForNameContainingCamelcase();

    @Test
    public abstract void whenSearchUsingCatenate();

    @Test
    public abstract void whenSearchUsingAsciiFolding();

    @Test
    public abstract void whenSearchUsingSimpleTermQuery();

    @Test
    public abstract void whenSearchUsingTermQueryWithPrefix();

    @Test
    public abstract void whenSearchUsingTermQueryOnNumericField();

    @Test
    public abstract void whenSearchUsingTermQueryOnBooleanField();

    @Test
    public abstract void whenSearchUsingReservedWordOnTokenisedField();

    @Test
    public abstract void whenSearchUsingReservedWordOnUntokenisedField();

}
