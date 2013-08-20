/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.admin.patch.OptionalPatchApplicationCheckBootstrapBean;
import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Andy
 *
 */
public class DbAftsQueryLanguage  extends AbstractAlfrescoFtsQueryLanguage
{
    NamespaceService namespaceService;
    
    DictionaryService dictionaryService;
    
    OptionalPatchApplicationCheckBootstrapBean optionalPatchApplicationCheckBootstrapBean;
    
    /**
     * @param optionalPatchApplicationCheckBootstrapBean the optionalPatchApplicationCheckBootstrapBean to set
     */
    public void setOptionalPatchApplicationCheckBootstrapBean(OptionalPatchApplicationCheckBootstrapBean optionalPatchApplicationCheckBootstrapBean)
    {
        this.optionalPatchApplicationCheckBootstrapBean = optionalPatchApplicationCheckBootstrapBean;
    }

    /**
     * @param namespacePrefixResolver the namespacePrefixResolver to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage#getNamespacePrefixResolver(org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    protected NamespacePrefixResolver getNamespacePrefixResolver(ADMLuceneSearcherImpl admLuceneSearcher)
    {
        return namespaceService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage#getDictionaryService(org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    protected DictionaryService getDictionaryService(ADMLuceneSearcherImpl admLuceneSearcher)
    {
        return dictionaryService;
    }

    public DbAftsQueryLanguage()
    {
        this.setName("db-afts");
    }

    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        if(optionalPatchApplicationCheckBootstrapBean.getPatchApplied())
        {
            return super.executeQuery(searchParameters, admLuceneSearcher);
        }
        else
        {
            throw new QueryModelException("The patch to add the indexes to support in-transactional metadata queries has not been applied");
        }
    }
}
