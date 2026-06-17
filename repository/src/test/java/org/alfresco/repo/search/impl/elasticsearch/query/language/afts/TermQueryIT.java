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

import org.alfresco.repo.search.impl.elasticsearch.query.BaseTermQueryIT;

public class TermQueryIT extends BaseTermQueryIT
{
    @Override
    public void whenSearchForNameContainingDotHyphenUnderscore()
    {
        assertContainsOnly(aftsSearch("cm:name:\"DummyFile1f62783c-bb84-4c1c-ba76-123b391bcfdd.txt\""), dummy);
        assertContainsOnly(aftsSearch("cm:name:MyTest-file___is_beautiful.txt"), dotTest);
        assertContainsOnly(aftsSearch("cm:name:\"Certificate of Completion\""), certificateOfCompletion);
        assertContainsOnly(aftsSearch("cm:name:tèst.docx"), testdocx);
        assertContainsOnly(aftsSearch("cm:name:document"), certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:name:test"), dotTest, certificateOfCompletionInName, testdocx);
        assertContainsOnly(aftsSearch("cm:name:My"), dotTest);
        assertContainsOnly(aftsSearch("cm:name:Test"), dotTest, certificateOfCompletionInName, testdocx);
        assertContainsOnly(aftsSearch("cm:name:file"), dotTest, dummy);
        assertContainsOnly(aftsSearch("cm:name:is"), dotTest);
        assertContainsOnly(aftsSearch("cm:name:beautiful"), dotTest);
        assertContainsOnly(aftsSearch("cm:name:.txt"), dotTest, dummy);
        assertContainsOnly(aftsSearch("cm:name:txt"), dotTest, dummy);
        assertContainsOnly(aftsSearch("cm:name:file and cm:name:beautiful"), dotTest);
    }

    @Override
    public void whenSearchForNameContainingCamelcase()
    {
        assertContainsOnly(aftsSearch("cm:name:MyTest"), dotTest);
        assertContainsOnly(aftsSearch("cm:name:my and cm:name:test"), dotTest);
    }

    @Override
    public void whenSearchUsingCatenate()
    {
        // words
        assertContainsOnly(aftsSearch("cm:name:mytestfileisbeautifultxt"), dotTest);
        assertContainsOnly(aftsSearch("cm:name:MyTest"), dotTest);
        // numbers
        assertContainsOnly(aftsSearch("cm:name:400"), certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:name:40032"), certificateOfCompletionInName);
        // all
        assertContainsOnly(aftsSearch("cm:name:32a"), certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:name:document400"), certificateOfCompletionInName);
    }

    @Override
    public void whenSearchUsingAsciiFolding()
    {
        assertContainsOnly(aftsSearch("cm:name:document"), certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:name:doçument"), certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:name:doçumènt"), certificateOfCompletionInName);
    }

    @Override
    public void whenSearchUsingSimpleTermQuery()
    {
        assertContainsOnly(aftsSearch("completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("Completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("CoMPleTion"), certificateOfCompletion, certificateOfCompletionInName);
    }

    @Override
    public void whenSearchUsingTermQueryWithPrefix()
    {
        assertContainsOnly(aftsSearch("cm:name:completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:name:Completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("TEXT:Completion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("TEXT:CoMPleTion"), certificateOfCompletion, certificateOfCompletionInName);
        assertContainsOnly(aftsSearch("cm:content:Completion"), certificateOfCompletion);
        assertContainsOnly(aftsSearch("cm:content:CoMPleTion"), certificateOfCompletion);
    }

    @Override
    public void whenSearchUsingTermQueryOnNumericField()
    {
        assertContainsOnly(aftsSearch("cm:sizeCurrent:1000"), dotTest);
        assertContainsOnly(aftsSearch("cm:sizeCurrent:100"));
    }

    @Override
    public void whenSearchUsingTermQueryOnBooleanField()
    {
        assertContainsOnly(aftsSearch("cm:autoVersion:false"), dotTest);
        assertContainsOnly(aftsSearch("cm:autoVersion:true"));
    }

    @Override
    public void whenSearchUsingReservedWordOnTokenisedField()
    {
        assertContainsOnly(aftsSearch("cm:title:\"OR\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("cm:title:\"or\""), tokenizedFieldDoc);
    }

    @Override
    public void whenSearchUsingReservedWordOnUntokenisedField()
    {
        assertContainsOnly(aftsSearch("cm:creator:\"OR\""), untokenizedFieldDoc);
    }
}
