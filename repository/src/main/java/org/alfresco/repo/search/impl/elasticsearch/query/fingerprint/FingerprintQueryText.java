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

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns the value of a {@code FINGERPRINT:} query into the fingerprint hash to match exactly.
 */
public final class FingerprintQueryText
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FingerprintQueryText.class);
    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    private FingerprintQueryText()
    {}

    /**
     * @param queryText
     *            a SHA-256 hash, or Solr syntax {@code <nodeRef|uuid>[_<overlap>[_<probability>]]}
     * @return the hash to match, or empty if the reference node has no fingerprint
     */
    public static Optional<String> resolveHash(String queryText, FingerprintResolver resolver)
    {
        String text = stripQuotes(queryText.trim());
        String lower = text.toLowerCase(Locale.ROOT);
        if (SHA256_HEX.matcher(lower).matches())
        {
            return Optional.of(lower);
        }
        // node refs and UUIDs contain no '_'
        int separator = text.indexOf('_');
        String nodeId = separator < 0 ? text : text.substring(0, separator);
        if (separator >= 0)
        {
            warnIfSimilarityRequested(text.substring(separator + 1));
        }
        return resolver.resolve(nodeId);
    }

    private static void warnIfSimilarityRequested(String thresholds)
    {
        String overlap = thresholds.split("_")[0];
        try
        {
            float value = Float.parseFloat(overlap);
            float fraction = value > 1f ? value / 100f : value; // Solr rule: 60 and 0.6 both mean 60%
            if (fraction < 1f)
            {
                LOGGER.warn("FINGERPRINT similarity threshold '{}' is not supported by Elasticsearch search yet; returning exact duplicates only.", thresholds);
            }
        }
        catch (NumberFormatException e)
        {
            LOGGER.warn("Invalid FINGERPRINT threshold '{}'; returning exact duplicates only.", thresholds);
        }
    }

    private static String stripQuotes(String value)
    {
        if (value.length() >= 2
                && (value.startsWith("'") && value.endsWith("'") || value.startsWith("\"") && value.endsWith("\"")))
        {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
