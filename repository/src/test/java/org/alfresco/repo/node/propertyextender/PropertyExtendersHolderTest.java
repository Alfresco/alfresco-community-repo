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
package org.alfresco.repo.node.propertyextender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

public class PropertyExtendersHolderTest
{
    private static final PropertyExtender EXT_1 = new TestExtender("ext-1");
    private static final PropertyExtender EXT_2 = new TestExtender("ext-2");

    private PropertyExtendersHolder sut;

    @Before
    public void setUp()
    {
        sut = new PropertyExtendersHolder();
    }

    @Test
    public void testRegisterExtender_shouldAddExtender()
    {
        // when
        sut.registerExtender(EXT_1);

        // then
        assertThat(sut.getExtenders()).containsExactly(EXT_1);
    }

    @Test
    public void testRegisterExtender_multipleDifferentExtenders_shouldAddAll()
    {
        // when
        sut.registerExtender(EXT_1);
        sut.registerExtender(EXT_2);

        // then
        assertThat(sut.getExtenders()).containsExactlyInAnyOrder(EXT_1, EXT_2);
    }

    @Test
    public void testRegisterExtender_duplicateExtender_shouldNotAddTwice()
    {
        // given
        var ext1Duplicate = new TestExtender("ext-1");

        // when
        sut.registerExtender(EXT_1);
        sut.registerExtender(ext1Duplicate);

        // then
        assertThat(sut.getExtenders()).hasSameElementsAs(Set.of(EXT_1));
    }

    @Test
    public void testRegisterExtender_sameInstanceTwice_shouldNotAddTwice()
    {
        // when
        sut.registerExtender(EXT_1);
        sut.registerExtender(EXT_1);

        // then
        assertThat(sut.getExtenders()).containsExactly(EXT_1);
    }

    @Test
    public void testGetExtenders_shouldReturnEmptySetInitially()
    {
        assertThat(sut.getExtenders()).isEmpty();
    }

    @Test
    public void testRegisterExtender_concurrentRegistration_shouldBeThreadSafe()
    {
        // given
        int threadCount = 5;
        int extendersCount = 100;
        var extenders = IntStream.range(0, extendersCount)
                .mapToObj(i -> new TestExtender("ext-" + i))
                .collect(Collectors.toList());

        try (var executor = Executors.newFixedThreadPool(threadCount))
        {
            var futures = extenders.stream()
                    .map(extender -> CompletableFuture.runAsync(() -> sut.registerExtender(extender), executor))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        }

        // then
        assertThat(sut.getExtenders()).hasSize(extendersCount);
        assertThat(sut.getExtenders()).containsExactlyInAnyOrderElementsOf(extenders);
    }

    private record TestExtender(String id) implements PropertyExtender
    {
        @Override
        public CalculationResult calculate(CalculationContext context)
        {
            return CalculationResult.NO_OP;
        }
    }
}
