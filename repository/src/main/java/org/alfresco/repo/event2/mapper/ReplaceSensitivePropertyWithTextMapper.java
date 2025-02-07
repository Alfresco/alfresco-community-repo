/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2.mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event2.shared.QNameMatcher;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.namespace.QName;

public class ReplaceSensitivePropertyWithTextMapper implements PropertyMapper
{
    private static final Set<QName> DEFAULT_SENSITIVE_PROPERTIES = Set.of(
            ContentModel.PROP_PASSWORD,
            ContentModel.PROP_SALT,
            ContentModel.PROP_PASSWORD_HASH,
            TransferModel.PROP_PASSWORD);
    private static final String DEFAULT_REPLACEMENT_TEXT = "SENSITIVE_DATA_REMOVED";

    private final QNameMatcher qNameMatcher;
    private final String replacementText;

    public ReplaceSensitivePropertyWithTextMapper(Set<QName> userConfiguredSensitiveProperties, String userConfiguredReplacementText)
    {
        Set<QName> sensitiveProperties = Optional.ofNullable(userConfiguredSensitiveProperties)
                .filter(Predicate.not(Collection::isEmpty))
                .orElse(DEFAULT_SENSITIVE_PROPERTIES);
        qNameMatcher = new QNameMatcher(sensitiveProperties);
        replacementText = Optional.ofNullable(userConfiguredReplacementText)
                .filter(Predicate.not(String::isEmpty))
                .filter(userInput -> !userInput.contains("${"))
                .orElse(DEFAULT_REPLACEMENT_TEXT);
    }

    @Override
    public Serializable map(QName propertyQName, Serializable value)
    {
        if (qNameMatcher.isMatching(propertyQName))
        {
            return replacementText;
        }
        return value;
    }
}
