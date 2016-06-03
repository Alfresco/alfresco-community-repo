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

public class AspectIndexFilter extends AbstractIndexFilter
{

    private boolean ignorePathsForSpecificAspects = false;
    private Set<QName> aspectsForIgnoringPaths = new HashSet<QName>();
    private List<String> aspectsForIgnoringPathsString;

    public boolean isIgnorePathsForSpecificAspects()
    {
        return ignorePathsForSpecificAspects;
    }

    public void setIgnorePathsForSpecificAspects(boolean ignorePathsForSpecificAspects)
    {
        this.ignorePathsForSpecificAspects = ignorePathsForSpecificAspects;
    }

    public void setAspectsForIgnoringPaths(List<String> aspectsForIgnoringPaths)
    {
        this.aspectsForIgnoringPathsString = aspectsForIgnoringPaths;
    }

    public void init()
    {
        super.init();
        initIgnoringPathsByCriterion(aspectsForIgnoringPathsString, aspectsForIgnoringPaths, new DefinitionExistChecker()
        {
            @Override
            public boolean isDefinitionExists(QName qName)
            {
                return (null != dictionaryService.getAspect(qName));
            }
        });
    }

    public boolean shouldBeIgnored(Set<QName> aspects)
    {
        if (!ignorePathsForSpecificAspects)
        {
            return false;
        }
        
        if ((null != aspects) && !aspects.isEmpty())
        {
            for (QName aspectForIgnoringPaths : aspectsForIgnoringPaths)
            {
                if (aspects.contains(aspectForIgnoringPaths))
                {
                    return true;
                }
                for (QName nodeAspect : aspects)
                {
                    if (dictionaryService.isSubClass(nodeAspect, aspectForIgnoringPaths))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
