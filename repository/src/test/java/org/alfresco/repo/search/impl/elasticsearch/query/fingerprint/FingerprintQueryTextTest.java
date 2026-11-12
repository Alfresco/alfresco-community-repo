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
package org.alfresco.repo.search.impl.elasticsearch.query.fingerprint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class FingerprintQueryTextTest
{
    private static final String UUID = "3a5b3a3c-2d8e-4e4f-9d4c-1f2e3d4c5b6a";
    private static final String NODE_REF = "workspace://SpacesStore/" + UUID;
    private static final String HASH = "a".repeat(64);

    private FingerprintResolver resolver;

    @Before
    public void setUp()
    {
        resolver = mock(FingerprintResolver.class);
    }

    @Test
    public void rawHashIsReturnedLowercasedWithoutResolving()
    {
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash(HASH.toUpperCase(), resolver));
        verifyNoInteractions(resolver);
    }

    @Test
    public void quotedHashIsUnquoted()
    {
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash("\"" + HASH + "\"", resolver));
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash("'" + HASH + "'", resolver));
    }

    @Test
    public void uuidIsResolved()
    {
        when(resolver.resolve(UUID)).thenReturn(Optional.of(HASH));
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash(UUID, resolver));
    }

    @Test
    public void nodeRefIsResolved()
    {
        when(resolver.resolve(NODE_REF)).thenReturn(Optional.of(HASH));
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash(NODE_REF, resolver));
    }

    @Test
    public void thresholdSuffixIsStrippedBeforeResolving()
    {
        when(resolver.resolve(UUID)).thenReturn(Optional.of(HASH));
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash(UUID + "_60_80", resolver));
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash(UUID + "_0.6", resolver));
        assertEquals(Optional.of(HASH), FingerprintQueryText.resolveHash(UUID + "_abc", resolver));
        verify(resolver, times(3)).resolve(UUID);
    }

    @Test
    public void unresolvedNodeGivesEmpty()
    {
        when(resolver.resolve(UUID)).thenReturn(Optional.empty());
        assertTrue(FingerprintQueryText.resolveHash(UUID, resolver).isEmpty());
    }
}
