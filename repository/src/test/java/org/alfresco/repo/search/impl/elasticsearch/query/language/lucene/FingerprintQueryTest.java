/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_FINGERPRINT;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.query.fingerprint.FingerprintResolver;

/**
 * Tests the {@code FINGERPRINT:} query support of {@link LuceneQueryParser} when a {@link FingerprintResolver} is wired.
 */
public class FingerprintQueryTest extends BaseQueryTest
{
    private static final String HASH = "46b5f1fc07ba950cdbf478fc0403668d2ad91b3b8084739e5681895a194f4e54"; // pragma: allowlist secret
    private static final String EXPECTED = "FINGERPRINT:" + HASH;

    private FingerprintResolver fingerprintResolver;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        fingerprintResolver = mock(FingerprintResolver.class);
        when(fingerprintResolver.isEnabled()).thenReturn(true);
        parser = new LuceneQueryParser(resolver, dictionaryService, siteService1, parameters, null, fingerprintResolver);
    }

    @Test
    public void uuidResolvesToHashTerm()
    {
        when(fingerprintResolver.resolve(AN_ID)).thenReturn(Optional.of(HASH));

        assertQueryEquals(EXPECTED, parser.getFieldQuery(FIELD_FINGERPRINT, AN_ID, false));
    }

    @Test
    public void quotedNodeRefResolvesToHashTerm()
    {
        when(fingerprintResolver.resolve(A_NODEREF)).thenReturn(Optional.of(HASH));

        assertQueryEquals(EXPECTED, parser.getFieldQuery(FIELD_FINGERPRINT, "'" + A_NODEREF + "'", true));
    }

    @Test
    public void aftsParseOfUuid()
    {
        when(fingerprintResolver.resolve(AN_ID)).thenReturn(Optional.of(HASH));

        assertQueryEquals(EXPECTED, parseAndBuildQuery("FINGERPRINT:" + AN_ID));
    }

    @Test
    public void thresholdSuffixIsExactWithWarning()
    {
        when(fingerprintResolver.resolve(AN_ID)).thenReturn(Optional.of(HASH));

        assertQueryEquals(EXPECTED, parser.getFieldQuery(FIELD_FINGERPRINT, AN_ID + "_100", false));
        assertQueryEquals(EXPECTED, parser.getFieldQuery(FIELD_FINGERPRINT, AN_ID + "_60", false));
        assertQueryEquals(EXPECTED, parser.getFieldQuery(FIELD_FINGERPRINT, AN_ID + "_60_80", false));
        verify(fingerprintResolver, times(3)).resolve(AN_ID);
    }

    @Test
    public void hashIsUsedDirectly()
    {
        assertQueryEquals(EXPECTED, parser.getFieldQuery(FIELD_FINGERPRINT, HASH.toUpperCase(), false));
        verify(fingerprintResolver, never()).resolve(any());
    }

    @Test
    public void unresolvedMatchesNothing()
    {
        when(fingerprintResolver.resolve(any())).thenReturn(Optional.empty());

        assertQueryEquals("FINGERPRINT:" + LuceneQueryParser.UNKNOWN, parser.getFieldQuery(FIELD_FINGERPRINT, AN_ID, false));
    }

    @Test
    public void disabledKeepsOldBehaviour()
    {
        when(fingerprintResolver.isEnabled()).thenReturn(false);

        assertNull(parser.getFieldQuery(FIELD_FINGERPRINT, AN_ID, false));
        verify(fingerprintResolver, never()).resolve(any());
    }

    @Test
    public void prefixStillUnsupported()
    {
        assertNull(parser.getPrefixQuery(FIELD_FINGERPRINT, "46b5"));
    }

    @Override
    protected Stream<String> unsupportedFields()
    {
        return Stream.empty();
    }

    @Override
    protected Stream<Function<String, Query>> queryBuilderFunctions()
    {
        return Stream.empty();
    }

    @Override
    protected String queryTypeUnderTest()
    {
        return "Fingerprint";
    }
}
