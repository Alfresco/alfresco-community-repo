/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2.filter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.event2.shared.CSVStringToListParser;
import org.alfresco.repo.event2.shared.QNameMatcher;
import org.alfresco.repo.event2.shared.TypeDefExpander;
import org.alfresco.service.namespace.QName;

/**
 * Abstract {@link EventFilter} implementation, containing common event filtering functionality for the {@link QName} type.
 *
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AbstractNodeEventFilter implements EventFilter<QName>
{
    protected TypeDefExpander typeDefExpander;

    private QNameMatcher qNameMatcher;

    public final void init()
    {
        qNameMatcher = new QNameMatcher(getExcludedTypes());
    }

    public void setTypeDefExpander(TypeDefExpander typeDefExpander)
    {
        this.typeDefExpander = typeDefExpander;
    }

    @Override
    public boolean isExcluded(QName qName)
    {
        return qNameMatcher.isMatching(qName);
    }

    protected abstract Set<QName> getExcludedTypes();

    protected List<String> parseFilterList(String unparsedFilterList)
    {
        return CSVStringToListParser.parse(unparsedFilterList);
    }

    protected Collection<QName> expandTypeDef(String typeDef)
    {
        return typeDefExpander.expand(typeDef);
    }
}
