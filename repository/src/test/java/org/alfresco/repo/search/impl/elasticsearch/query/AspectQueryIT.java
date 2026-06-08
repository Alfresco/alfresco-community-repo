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

/**
 * The test uses the following aspect hierarchy:
 * 
 * <pre>
 * 1. rn:rendition
 *      |--- rn:visibleRendition
 *      |--- rn:hiddenRendition
 * 2. audio:audio
 * </pre>
 *
 * And the following nodes:
 *
 * <br/>
 * <br/>
 *
 * <ul>
 * <li>#1 aspects = rn:rendition, audio:audio</li>
 * <li>#2 aspects = rm:hiddenRendition</li>
 * <li>#3 aspects = rm:visibleRendition</li>
 * <li>#4 aspects = audio:audio</li>
 * <li>#5 aspects = audio:audio</li>
 * </ul>
 */
public class AspectQueryIT extends LuceneOrAFTSQueryIT
{
    private NodeRef bigYellowBanana;
    private NodeRef yellowBananaDescendant;
    private NodeRef yellowBananaSecondDescendant;

    private NodeRef yellowTaxi;
    private NodeRef anotherWhiteTaxi;

    public AspectQueryIT(String language)
    {
        super(language);
    }

    @Before
    public void initDocuments()
    {
        bigYellowBanana = indexDocument(new IndexDocumentSourceBuilder().withName("big yellow banana").withAspects("rn:rendition", "audio:audio"));
        yellowBananaDescendant = indexDocument(new IndexDocumentSourceBuilder().withName("big yellow banana").withAspects("rn:hiddenRendition"));
        yellowBananaSecondDescendant = indexDocument(new IndexDocumentSourceBuilder().withName("big yellow banana").withAspects("rn:visibleRendition"));

        yellowTaxi = indexDocument(new IndexDocumentSourceBuilder().withName("yellow taxi").withAspects("audio:audio"));
        anotherWhiteTaxi = indexDocument(new IndexDocumentSourceBuilder().withName("another white taxi").withAspects("audio:audio"));
    }

    @Test
    public void fieldQuery_ASPECTshortName_unknownAspectShouldMatchNothing()
    {
        assertZeroResults(searchFor(language, "ASPECT:\"audio:somethingUnknown\""));
    }

    @Test
    public void fieldQuery_ASPECTfullName_unknownAspectShouldMatchNothing()
    {
        assertZeroResults(searchFor(language, "ASPECT:\"{http://www.alfresco.org/model/audio/1.0}somethingUnknown\""));
    }

    @Test
    public void fieldQuery_ASPECTshortName_shouldSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "ASPECT:\"audio:audio\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "ASPECT:\"rn:rendition\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "ASPECT:\"audio:audio\" AND ASPECT:\"rn:rendition\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:\"audio:audio\" OR ASPECT:\"rn:rendition\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);
    }

    @Test
    public void fieldQuery_ASPECTlongName_shouldSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "ASPECT:\"{http://www.alfresco.org/model/audio/1.0}audio\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "ASPECT:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "ASPECT:\"{http://www.alfresco.org/model/audio/1.0}audio\" AND ASPECT:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:\"{http://www.alfresco.org/model/audio/1.0}audio\" OR ASPECT:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);
    }

    @Test
    public void fieldQuery_EXACTASPECTshortName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTASPECT:\"rn:rendition\""), bigYellowBanana);
    }

    @Test
    public void fieldQuery_EXACTASPECTlongName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTASPECT:\"{http://www.alfresco.org/model/rendition/1.0}rendition\""), bigYellowBanana);

    }

    @Test
    public void prefixQuery_ASPECTshortName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("audio:aud*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("rn:rend*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("audio:aud*") + " AND ASPECT:" + escape("rn:rendi*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("audio:aud*") + " OR ASPECT:" + escape("rn:rendi*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    @Test
    public void prefixQuery_ASPECTlongName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/audio/1.0}audi*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}ren*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/audio/1.0}aud*") + " AND ASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}re*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/audio/1.0}audi*") + " OR ASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}rendi*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    @Test
    public void prefixQuery_EXACTASPECTshortName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTASPECT:" + escape("rn:rend*")), bigYellowBanana);
    }

    @Test
    public void prefixQuery_EXACTASPECTlongName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}rendi*")), bigYellowBanana);
    }

    @Test
    public void wildcardQuery_ASPECTshortName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("audio:au*io")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("rn:ren*ti?n")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("audio:a*io") + " AND ASPECT:" + escape("rn:ren*ti?n")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("audio:aud?o") + " OR ASPECT:" + escape("rn:ren*ti?n")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    @Test
    public void wildcardQuery_ASPECTlongName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/audio/1.0}au*io")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}rend?ti?n")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/audio/1.0}a*io") + " AND ASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}rend?ti?n")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "ASPECT:" + escape("{http://www.alfresco.org/model/audio/1.0}aud?o") + " OR ASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}ren*ti?n")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    @Test
    public void wildcardQuery_EXACTASPECTshortName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTASPECT:" + escape("rn:ren*ti?n")), bigYellowBanana);
    }

    @Test
    public void wildcardQuery_EXACTASPECTlongName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTASPECT:" + escape("{http://www.alfresco.org/model/rendition/1.0}ren*ti?n")), bigYellowBanana);
    }
}
