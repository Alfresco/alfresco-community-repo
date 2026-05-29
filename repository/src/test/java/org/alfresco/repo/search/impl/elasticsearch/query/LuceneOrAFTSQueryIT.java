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

import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@RunWith(Parameterized.class)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.CallSuperInConstructor"})
public abstract class LuceneOrAFTSQueryIT extends ElasticsearchBaseQueryIT
{
    @ClassRule
    public static final SpringClassRule scr = new SpringClassRule();

    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    protected final String language;

    public LuceneOrAFTSQueryIT(String language)
    {
        this.language = language;
    }

    @Parameterized.Parameters(name = "language: {0}")
    public static Collection<String[]> languages()
    {
        return List.of(new String[]{"afts"}, new String[]{"lucene"});
    }

    @After
    public void cleanUp()
    {
        deleteIndex(TEST_INDEX_NAME);
    }

    protected boolean isLuceneSyntaxInUse()
    {
        return "lucene".equals(language);
    }

    protected String escape(String propertyName)
    {
        return propertyName.replace(":", "\\:")
                .replace("/", "\\/")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("{", "\\{");
    }
}
