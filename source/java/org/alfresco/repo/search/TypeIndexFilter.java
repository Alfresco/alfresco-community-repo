/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.QName;

public class TypeIndexFilter extends AbstractIndexFilter
{
    private boolean ignorePathsForSpecificTypes = false;
    private Set<QName> typesForIgnoringPaths = new HashSet<QName>();
    private List<String> typesForIgnoringPathsString;

    public boolean isIgnorePathsForSpecificTypes()
    {
        return ignorePathsForSpecificTypes;
    }

    public void setIgnorePathsForSpecificTypes(boolean ignorePersonAndConfigurationPaths)
    {
        this.ignorePathsForSpecificTypes = ignorePersonAndConfigurationPaths;
    }

    public void setTypesForIgnoringPaths(List<String> typesForIgnoringPaths)
    {
        typesForIgnoringPathsString = typesForIgnoringPaths;
    }

    public void init()
    {
        super.init();
        initIgnoringPathsByCriterion(typesForIgnoringPathsString, typesForIgnoringPaths, new DefinitionExistChecker()
        {
            @Override
            public boolean isDefinitionExists(QName qName)
            {
                return (null != dictionaryService.getType(qName));
            }
        });
    }

    public boolean shouldBeIgnored(QName nodeType)
    {
        if (!ignorePathsForSpecificTypes)
        {
            return false;
        }

        if (null != nodeType)
        {
            if (typesForIgnoringPaths.contains(nodeType))
            {
                return true;
            }

            for (QName type : typesForIgnoringPaths)
            {
                if (dictionaryService.isSubClass(nodeType, type))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
