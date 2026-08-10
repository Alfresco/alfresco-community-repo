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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for {@link SearchEngineInfo}.
 */
public class SearchEngineInfoTest
{
    @Test
    public void shouldExposeNameAndVersionProvidedToConstructor()
    {
        SearchEngineInfo info = new SearchEngineInfo("opensearch", "2.17.0");

        assertThat(info.getSearchEngineName()).isEqualTo("opensearch");
        assertThat(info.getSearchEngineVersion()).isEqualTo("2.17.0");
    }

    @Test
    public void shouldAllowNullValues()
    {
        SearchEngineInfo info = new SearchEngineInfo(null, null);

        assertThat(info.getSearchEngineName()).isNull();
        assertThat(info.getSearchEngineVersion()).isNull();
    }
}
