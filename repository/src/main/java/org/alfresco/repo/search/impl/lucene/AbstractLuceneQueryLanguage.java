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
package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Andy
 *
 */
public abstract class AbstractLuceneQueryLanguage implements LuceneQueryLanguageSPI, InitializingBean
{
    private String name;
    
    private List<IndexerAndSearcher> factories;
    
    @Override
    final public void setFactories(List<IndexerAndSearcher> factories)
    {
        this.factories = factories;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        for (IndexerAndSearcher factory : factories)
        {
            factory.registerQueryLanguage(this);
        }
    }

    public final String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<IndexerAndSearcher> getFactories()
    {
        return factories;
    }
}
