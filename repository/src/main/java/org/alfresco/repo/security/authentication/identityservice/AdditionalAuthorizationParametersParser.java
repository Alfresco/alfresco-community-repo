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
package org.alfresco.repo.security.authentication.identityservice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Parses additional OIDC authorization parameters. */
final class AdditionalAuthorizationParametersParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AdditionalAuthorizationParametersParser.class);
    private static final char PARAM_DELIMITER = ',';
    private static final char PARAM_SEPARATOR = '=';
    private static final char QUOTE = '\'';

    private AdditionalAuthorizationParametersParser()
    {}

    /** Parses all configured entries into an insertion-ordered parameter map. */
    static Map<String, String> parse(String additionalParams)
    {
        final Map<String, String> parsedParams = new LinkedHashMap<>();
        final List<String> entries = split(additionalParams);
        for (int index = 0; index < entries.size(); index++)
        {
            parseEntry(entries.get(index), index + 1, parsedParams);
        }
        return parsedParams;
    }

    /** Splits on delimiters outside quoted values. */
    private static List<String> split(String additionalParams)
    {
        final List<String> entries = new ArrayList<>();
        int entryStart = 0;

        int delimiter = findEntryDelimiter(additionalParams, entryStart);
        while (delimiter >= 0)
        {
            entries.add(additionalParams.substring(entryStart, delimiter));
            entryStart = delimiter + 1;
            delimiter = findEntryDelimiter(additionalParams, entryStart);
        }
        entries.add(additionalParams.substring(entryStart));
        return entries;
    }

    /** Finds the next delimiter, ignoring delimiters inside a quoted value. */
    private static int findEntryDelimiter(String additionalParams, int entryStart)
    {
        final int delimiter = additionalParams.indexOf(PARAM_DELIMITER, entryStart);
        final int separator = additionalParams.indexOf(PARAM_SEPARATOR, entryStart);
        if (separator < 0 || delimiter >= 0 && delimiter < separator)
        {
            return delimiter;
        }

        final int valueStart = skipWhitespace(additionalParams, separator + 1);
        if (valueStart < additionalParams.length() && additionalParams.charAt(valueStart) == QUOTE)
        {
            final int closingQuote = findClosingQuote(additionalParams, valueStart + 1);
            return closingQuote < 0 ? -1 : additionalParams.indexOf(PARAM_DELIMITER, closingQuote + 1);
        }
        return delimiter;
    }

    /** Returns the position of the first non-whitespace character. */
    private static int skipWhitespace(String value, int position)
    {
        while (position < value.length() && Character.isWhitespace(value.charAt(position)))
        {
            position++;
        }
        return position;
    }

    /** Finds a closing quote while skipping escaped quote pairs. */
    private static int findClosingQuote(String value, int position)
    {
        int quote = value.indexOf(QUOTE, position);
        while (quote >= 0 && quote + 1 < value.length() && value.charAt(quote + 1) == QUOTE)
        {
            quote = value.indexOf(QUOTE, quote + 2);
        }
        return quote;
    }

    /** Validates and adds one configured parameter entry to the result map. */
    private static void parseEntry(String entry, int entryNumber, Map<String, String> parsedParams)
    {
        final int separator = entry.indexOf(PARAM_SEPARATOR);
        if (separator < 0)
        {
            // Tolerate empty entries, e.g. a trailing or doubled delimiter.
            if (!entry.isBlank())
            {
                logMalformedEntry(entryNumber, "missing '" + PARAM_SEPARATOR + "' separator");
            }
            return;
        }

        final String name = entry.substring(0, separator).strip();
        if (name.isEmpty())
        {
            logMalformedEntry(entryNumber, "empty parameter name");
            return;
        }

        parseValue(entry.substring(separator + 1), entryNumber)
                .ifPresent(value -> parsedParams.put(name, value));
    }

    /** Parses an unquoted or single-quoted parameter value. */
    private static Optional<String> parseValue(String configuredValue, int entryNumber)
    {
        final String value = configuredValue.strip();
        if (value.isEmpty() || value.charAt(0) != QUOTE)
        {
            return Optional.of(value);
        }

        final StringBuilder parsedValue = new StringBuilder();
        int position = 1;
        while (position < value.length())
        {
            final char current = value.charAt(position);
            if (current != QUOTE)
            {
                parsedValue.append(current);
            }
            else if (position + 1 < value.length() && value.charAt(position + 1) == QUOTE)
            {
                parsedValue.append(QUOTE);
                position++;
            }
            else if (value.substring(position + 1).isBlank())
            {
                return Optional.of(parsedValue.toString());
            }
            else
            {
                logMalformedEntry(entryNumber, "unexpected characters after quoted value");
                return Optional.empty();
            }
            position++;
        }

        logMalformedEntry(entryNumber, "unterminated quoted value");
        return Optional.empty();
    }

    private static void logMalformedEntry(int entryNumber, String reason)
    {
        LOGGER.warn("Ignoring malformed entry {} in identity-service.authorization.additional-params: {}", entryNumber, reason);
    }
}
