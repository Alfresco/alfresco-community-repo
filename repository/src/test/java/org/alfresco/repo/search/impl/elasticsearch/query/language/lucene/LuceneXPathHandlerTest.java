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

import static org.junit.Assert.assertEquals;

import org.apache.lucene.queries.spans.SpanQuery;
import org.jaxen.saxpath.Axis;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for {@link LuceneXPathHandler}. */
public class LuceneXPathHandlerTest
{
    private static final String FIELD = "PATH";

    LuceneXPathHandler luceneXPathHandler;

    @Before
    public void setUp()
    {
        luceneXPathHandler = new LuceneXPathHandler(FIELD);
    }

    /** Test for xpath: / */
    @Test
    public void rootXPath()
    {
        luceneXPathHandler.startXPath();
        luceneXPathHandler.startAbsoluteLocationPath();
        luceneXPathHandler.endAbsoluteLocationPath();

        SpanQuery query = luceneXPathHandler.getQuery();

        assertEquals("Unexpected query returned.", "spanNear([PATH:^, PATH:$], 0, true)", query.toString());
    }

    /** Test for xpath: /app:company_home */
    @Test
    public void absoluteXPath()
    {
        luceneXPathHandler.startXPath();
        luceneXPathHandler.startAbsoluteLocationPath();
        luceneXPathHandler.startNameStep(Axis.CHILD, "app", "company_home");
        luceneXPathHandler.endAbsoluteLocationPath();

        SpanQuery query = luceneXPathHandler.getQuery();

        assertEquals("Unexpected query returned.", "spanNear([PATH:^, PATH:app, PATH:company_home, PATH:$], 0, true)", query.toString());
    }

    /** Test for xpath: //st:sites */
    @Test
    public void relativeXPath()
    {
        luceneXPathHandler.startXPath();
        luceneXPathHandler.startRelativeLocationPath();
        luceneXPathHandler.startNameStep(Axis.CHILD, "st", "sites");
        luceneXPathHandler.endRelativeLocationPath();

        SpanQuery query = luceneXPathHandler.getQuery();

        assertEquals("Unexpected query returned.", "spanNear([PATH:st, PATH:sites, PATH:$], 0, true)", query.toString());
    }

    /** Test for xpath: //st:sites/* */
    @Test
    public void childrenOfRelativeXPath()
    {
        luceneXPathHandler.startXPath();
        luceneXPathHandler.startRelativeLocationPath();
        luceneXPathHandler.startNameStep(Axis.CHILD, "st", "sites");
        luceneXPathHandler.startNameStep(Axis.CHILD, "", "");
        luceneXPathHandler.endRelativeLocationPath();

        SpanQuery query = luceneXPathHandler.getQuery();

        assertEquals("Unexpected query returned.", "spanNear([PATH:st, PATH:sites, PATH:*, PATH:*, PATH:$], 0, true)", query.toString());
    }

    /** Test for xpath: /app:company_home//cm:folder1 */
    @Test
    public void descendentXPath()
    {
        luceneXPathHandler.startXPath();
        luceneXPathHandler.startAbsoluteLocationPath();
        luceneXPathHandler.startNameStep(Axis.CHILD, "app", "company_home");
        luceneXPathHandler.startAllNodeStep(Axis.CHILD);
        luceneXPathHandler.endAllNodeStep();
        luceneXPathHandler.startNameStep(Axis.CHILD, "cm", "folder1");
        luceneXPathHandler.endAbsoluteLocationPath();

        SpanQuery query = luceneXPathHandler.getQuery();

        assertEquals("Unexpected query returned.",
                "spanNear([spanNear([PATH:^, PATH:app, PATH:company_home], 0, true), spanNear([PATH:cm, PATH:folder1, PATH:$], 0, true)], 1000, true)",
                query.toString());
    }

    /** Test for xpath: //st:sites//cm:folder1//cm:file1 */
    @Test
    public void relativeDescendentXPath()
    {
        luceneXPathHandler.startXPath();
        luceneXPathHandler.startRelativeLocationPath();
        luceneXPathHandler.startNameStep(Axis.CHILD, "st", "sites");
        luceneXPathHandler.startAllNodeStep(Axis.CHILD);
        luceneXPathHandler.endAllNodeStep();
        luceneXPathHandler.startNameStep(Axis.CHILD, "cm", "folder1");
        luceneXPathHandler.startAllNodeStep(Axis.CHILD);
        luceneXPathHandler.endAllNodeStep();
        luceneXPathHandler.startNameStep(Axis.CHILD, "cm", "file1");
        luceneXPathHandler.endRelativeLocationPath();

        SpanQuery query = luceneXPathHandler.getQuery();

        assertEquals("Unexpected query returned.",
                "spanNear([spanNear([PATH:st, PATH:sites], 0, true), spanNear([spanNear([PATH:cm, PATH:folder1], 0, true), spanNear([PATH:cm, PATH:file1, PATH:$], 0, true)], 1000, true)], 1000, true)",
                query.toString());
    }
}
