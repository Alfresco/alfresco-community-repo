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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.Test;

import org.alfresco.util.log4j.Log4jAppenderUtil;

public class IdentityServiceConfigUnitTest
{
    private final IdentityServiceConfig config = new IdentityServiceConfig();

    @Test
    public void shouldNotConfigureAdditionalParametersByDefault()
    {
        assertThat(config.getAuthorizationAdditionalParams()).isEmpty();

        config.setAuthorizationAdditionalParams(null);
        assertThat(config.getAuthorizationAdditionalParams()).isEmpty();

        config.setAuthorizationAdditionalParams("  ");
        assertThat(config.getAuthorizationAdditionalParams()).isEmpty();
    }

    @Test
    public void shouldParseAdditionalAuthorizationParameters()
    {
        config.setAuthorizationAdditionalParams(
                "resourceServer=Identity Provider unencrypted, foo = bar,expression=a=b,empty=,claims='{\"userinfo\":{\"given_name\":null,\"email\":null}}'");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("resourceServer", "Identity Provider unencrypted"),
                Map.entry("foo", "bar"),
                Map.entry("expression", "a=b"),
                Map.entry("empty", ""),
                Map.entry("claims", "{\"userinfo\":{\"given_name\":null,\"email\":null}}"));
    }

    @Test
    public void shouldIgnoreMalformedAdditionalAuthorizationParameters()
    {
        config.setAuthorizationAdditionalParams("valid=one,missingSeparator,=missingName,   ,alsoValid=two,unterminated='one,two");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("valid", "one"),
                Map.entry("alsoValid", "two"));
    }

    @Test
    public void shouldIgnoreEmptyEntriesAndSurroundingWhitespace()
    {
        config.setAuthorizationAdditionalParams(" , foo = bar , ,baz =  '  spaced  ' ,unicode =\u2003'kept'\u2003,");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("foo", "bar"),
                Map.entry("baz", "  spaced  "),
                Map.entry("unicode", "kept"));
    }

    @Test
    public void shouldSupportEscapedQuotesInQuotedValues()
    {
        config.setAuthorizationAdditionalParams("quoted='it''s, quoted, again',plain=it's fine,onlyQuote='''',emptyQuoted=''");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("quoted", "it's, quoted, again"),
                Map.entry("plain", "it's fine"),
                Map.entry("onlyQuote", "'"),
                Map.entry("emptyQuoted", ""));
    }

    @Test
    public void shouldNotTreatAnApostropheInAnUnquotedValueAsAQuote()
    {
        config.setAuthorizationAdditionalParams("surname=O'Brien,next=value,quoted='value'  ");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("surname", "O'Brien"),
                Map.entry("next", "value"),
                Map.entry("quoted", "value"));
    }

    @Test
    public void shouldIgnoreEntriesWithUnexpectedCharactersAfterAQuotedValue()
    {
        config.setAuthorizationAdditionalParams("broken='one' two,valid=three");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(Map.entry("valid", "three"));
    }

    @Test
    public void shouldUseLastValueWhenParameterIsConfiguredMoreThanOnce()
    {
        config.setAuthorizationAdditionalParams("duplicate=first,duplicate=last");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(Map.entry("duplicate", "last"));
    }

    @Test
    public void shouldNameRepositoryPropertyInMalformedEntryWarningsByDefault()
    {
        final List<String> warnings = captureParserWarnings(() -> config.setAuthorizationAdditionalParams("missingSeparator,valid=one"));

        assertThat(warnings).containsExactly(
                "Ignoring malformed entry 1 in identity-service.authorization.additional-params: missing '=' separator");
        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(Map.entry("valid", "one"));
    }

    @Test
    public void shouldNameGivenPropertyInMalformedEntryWarnings()
    {
        final String propertyName = "aos.identity-service.authorization.additional-params";

        final List<String> warnings = captureParserWarnings(() -> config.setAuthorizationAdditionalParams(
                "missingSeparator,=missingName,broken='one' two,valid=one,unterminated='one", propertyName));

        assertThat(warnings).containsExactly(
                "Ignoring malformed entry 1 in " + propertyName + ": missing '=' separator",
                "Ignoring malformed entry 2 in " + propertyName + ": empty parameter name",
                "Ignoring malformed entry 3 in " + propertyName + ": unexpected characters after quoted value",
                "Ignoring malformed entry 5 in " + propertyName + ": unterminated quoted value");
        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(Map.entry("valid", "one"));
    }

    private static List<String> captureParserWarnings(Runnable action)
    {
        final List<String> warnings = new CopyOnWriteArrayList<>();
        final AbstractAppender appender = new AbstractAppender("AdditionalParamsWarnings", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event)
            {
                if (event.getLevel() == Level.WARN)
                {
                    warnings.add(event.getMessage().getFormattedMessage());
                }
            }
        };
        final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AdditionalAuthorizationParametersParser.class);
        Log4jAppenderUtil.addAbstractAppenderToLogger(appender, logger);
        try
        {
            action.run();
        }
        finally
        {
            Log4jAppenderUtil.removeAbstractAppenderFromLogger(appender, logger);
        }
        return warnings;
    }
}
