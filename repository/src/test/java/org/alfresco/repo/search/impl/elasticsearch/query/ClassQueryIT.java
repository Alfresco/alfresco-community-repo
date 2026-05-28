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

import static org.alfresco.repo.search.impl.elasticsearch.AssertionUtils.assertHasIgnoredFields;
import static org.alfresco.repo.search.impl.elasticsearch.AssertionUtils.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public class ClassQueryIT extends LuceneOrAFTSQueryIT
{
    private NodeRef bigYellowBanana;
    private NodeRef yellowBananaDescendant;
    private NodeRef yellowBananaSecondDescendant;

    private NodeRef yellowTaxi;
    private NodeRef anotherWhiteTaxi;

    public ClassQueryIT(String language)
    {
        super(language);
    }

    @Before
    public void initDocuments()
    {
        bigYellowBanana = indexDocument(new IndexDocumentSourceBuilder()
                .withName("big yellow banana")
                .withType("cm:content", "cm:person")
                .withAspects("rn:rendition", "audio:audio"));

        yellowBananaDescendant = indexDocument(new IndexDocumentSourceBuilder()
                .withName("big yellow banana")
                .withType("cm:dictionaryModel")
                .withAspects("rn:hiddenRendition"));

        yellowBananaSecondDescendant = indexDocument(new IndexDocumentSourceBuilder()
                .withName("big yellow banana")
                .withType("fm:post")
                .withAspects("rn:visibleRendition"));

        yellowTaxi = indexDocument(new IndexDocumentSourceBuilder()
                .withName("yellow taxi")
                .withType("cm:person")
                .withAspects("audio:audio"));

        anotherWhiteTaxi = indexDocument(new IndexDocumentSourceBuilder()
                .withName("another white taxi")
                .withType("cm:person")
                .withAspects("audio:audio"));
    }

    @Test
    public void fieldQuery_CLASSshortName_shouldSearchForDescendants()
    {
        // Aspects
        assertContainsOnly(searchFor(language, "CLASS:\"audio:audio\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "CLASS:\"rn:rendition\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "CLASS:\"audio:audio\" AND ASPECT:\"rn:rendition\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "CLASS:\"audio:audio\" OR ASPECT:\"rn:rendition\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);

        // Types
        assertContainsOnly(searchFor(language, "CLASS:\"cm:person\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "CLASS:\"cm:content\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "CLASS:\"cm:person\" AND TYPE:\"cm:content\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "CLASS:\"cm:person\" OR TYPE:\"cm:content\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);
    }

    @Test
    public void fieldQuery_CLASSlongName_shouldSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/audio/1.0}audio\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/audio/1.0}audio\" AND ASPECT:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/audio/1.0}audio\" OR ASPECT:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);

        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/content/1.0}person\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/content/1.0}person\" AND TYPE:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "CLASS:\"{http://www.alfresco.org/model/content/1.0}person\" OR TYPE:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);
    }

    @Test
    public void prefixQueriesAreNotSupported()
    {
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("audio:aud*"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("rn:rend*"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("audio:audio") + " AND CLASS:" + escape("rn:rend*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("rn:rendition") + " OR CLASS:" + escape("rn:hidden*")), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant));

        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audi*"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}rend*"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio") + " AND CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}re*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio") + " OR CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}rendi*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));
    }

    @Test
    public void wildcardQueriesAreNotSupported()
    {
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("audio:aud*o"))));

        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("rn:ren*ti?n"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("audio:audio") + " AND CLASS:" + escape("rn:ren*ti?n")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("audio:audio") + " OR CLASS:" + escape("rn:ren*ti?n")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));

        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}aud*o"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}ren*ti?n"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio") + " AND CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}ren*ti?n")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio") + " OR CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}ren*ti?n")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi));
    }

    /**
     * We expect a {@link RuntimeException} instead of {@link UnsupportedOperationException} because the FTS query parser throws a {@link org.alfresco.repo.search.impl.parsers.FTSQueryException}.
     */
    @Test
    public void fuzzyQueriesAreNotSupported()
    {
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("audio:audeo~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("audio:audeo~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("rn:renditian~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("audio:audio") + " AND CLASS:" + escape("rn:renditian~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("audio:audio") + " OR CLASS:" + escape("rn:rendizion~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}renditian~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio") + " AND CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}renditian~")), RuntimeException.class);
        assertThatThrownBy(() -> searchFor(language, "CLASS:" + escape("{http://www.alfresco.org/model/audio/1.0}audio") + " OR CLASS:" + escape("{http://www.alfresco.org/model/rendition/1.0}rendizion~")), RuntimeException.class);
    }

    @Test
    public void rangeQueriesAreNotSupported()
    {
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("[audio:something TO audio:somethingElse]"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("[audio:something TO *]"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("[* TO audio:somethingElse]"))));
        assertHasIgnoredFields(() -> assertContainsOnly(searchFor(language, "CLASS:" + escape("{* TO audio:somethingElse}"))));
    }

}
