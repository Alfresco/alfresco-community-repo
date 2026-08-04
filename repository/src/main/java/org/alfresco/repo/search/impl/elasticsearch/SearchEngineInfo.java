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
package org.alfresco.repo.search.impl.elasticsearch;

/**
 * Holds information about the search engine backing the Elasticsearch search subsystem, namely which provider
 * (OpenSearch or Elasticsearch) and which version is in use.
 * <p>
 * The information is detected at startup by querying the engine and is then available to any component that needs it.
 * Detection re-runs on every server (re)start, so the values always reflect the currently connected engine.
 */
public interface SearchEngineInfo
{
    /**
     * @return the detected search engine provider, or {@link SearchEngineType#UNKNOWN} when detection has not run or failed
     */
    SearchEngineType getType();

    /**
     * @return the detected engine version (for example {@code "2.13.0"}), or {@code null} when unknown
     */
    String getVersion();

    /**
     * @return the raw {@code distribution} value reported by the engine (for example {@code "opensearch"}), or {@code null}
     *         when none was reported (as is the case for Elasticsearch) or detection has not run
     */
    String getDistribution();

    /**
     * @return {@code true} once the provider and version have been successfully detected
     */
    boolean isDetected();

    /**
     * Queries the search engine and (re)populates the provider and version information. Implementations must never throw:
     * a detection failure leaves the type as {@link SearchEngineType#UNKNOWN} and {@link #isDetected()} as {@code false}.
     */
    void detect();
}
