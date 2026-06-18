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

package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseTermQueryIT;

public class TermQueryIT extends BaseTermQueryIT
{
    @Override
    public void whenSearchForNameContainingDotHyphenUnderscore()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:\"DummyFile1f62783c-bb84-4c1c-ba76-123b391bcfdd.txt\""), dummy);
        assertContainsOnly(luceneSearch("@cm\\:name:MyTest-file___is_beautiful.txt"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:name:\"Certificate of Completion\""), certificateOfCompletion);
        assertContainsOnly(luceneSearch("@cm\\:name:tèst.docx"), testdocx);
        assertContainsOnly(luceneSearch("@cm\\:name:document"), certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:name:test"), dotTest, certificateOfCompletionInName, testdocx);
        assertContainsOnly(luceneSearch("@cm\\:name:My"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:name:Test"), dotTest, certificateOfCompletionInName, testdocx);
        assertContainsOnly(luceneSearch("@cm\\:name:file"), dotTest, dummy);
        assertContainsOnly(luceneSearch("@cm\\:name:is"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:name:beautiful"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:name:.txt"), dotTest, dummy);
        assertContainsOnly(luceneSearch("@cm\\:name:txt"), dotTest, dummy);
        assertContainsOnly(luceneSearch("@cm\\:name:file AND @cm\\:name:beautiful"), dotTest);
    }

    @Override
    public void whenSearchForNameContainingCamelcase()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:MyTest"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:name:my AND @cm\\:name:test"), dotTest);
    }

    @Override
    public void whenSearchUsingCatenate()
    {
        // words
        assertContainsOnly(luceneSearch("@cm\\:name:mytestfileisbeautifultxt"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:name:MyTest"), dotTest);
        // numbers
        assertContainsOnly(luceneSearch("@cm\\:name:400"), certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:name:40032"), certificateOfCompletionInName);
        // all
        // Completion test doçumént_400.32-a
        // query token: 32 a 32a -> 32 _
        assertContainsOnly(luceneSearch("@cm\\:name:32a"), certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:name:document400"), certificateOfCompletionInName);
    }

    @Override
    public void whenSearchUsingAsciiFolding()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:document"), certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:name:doçument"), certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:name:doçumènt"), certificateOfCompletionInName);
    }

    @Override
    public void whenSearchUsingSimpleTermQuery()
    {
        assertContainsOnly(luceneSearch("completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("Completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("CoMPleTion"), certificateOfCompletion, certificateOfCompletionInName);
    }

    @Override
    public void whenSearchUsingTermQueryWithPrefix()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:name:Completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("TEXT:Completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("TEXT:CoMPleTion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(luceneSearch("@cm\\:content:Completion"), certificateOfCompletion);
        assertContainsOnly(luceneSearch("@cm\\:content:CoMPleTion"), certificateOfCompletion);
    }

    @Override
    public void whenSearchUsingTermQueryOnNumericField()
    {
        assertContainsOnly(luceneSearch("@cm\\:sizeCurrent:1000"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:sizeCurrent:100"));
    }

    @Override
    public void whenSearchUsingTermQueryOnBooleanField()
    {
        assertContainsOnly(luceneSearch("@cm\\:autoVersion:false"), dotTest);
        assertContainsOnly(luceneSearch("@cm\\:autoVersion:true"));
    }

    @Override
    public void whenSearchUsingReservedWordOnTokenisedField()
    {
        assertContainsOnly(aftsSearch("@cm\\:title:\"OR\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("@cm\\:title:\"or\""), tokenizedFieldDoc);
    }

    @Override
    public void whenSearchUsingReservedWordOnUntokenisedField()
    {
        assertContainsOnly(aftsSearch("@cm\\:creator:\"OR\""), untokenizedFieldDoc);
    }
}
