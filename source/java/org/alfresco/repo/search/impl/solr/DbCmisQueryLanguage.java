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
package org.alfresco.repo.search.impl.solr;

import java.util.List;
import java.util.Set;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.opencmis.search.CMISQueryParser;
import org.alfresco.opencmis.search.CmisFunctionEvaluationContext;
import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.admin.patch.OptionalPatchApplicationCheckBootstrapBean;
import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;

/**
 * @author Andy
 */
public class DbCmisQueryLanguage extends AbstractLuceneQueryLanguage
{
    QueryEngine queryEngine;

    private CMISDictionaryService cmisDictionaryService;

    OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck1;
    
    OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck2;
    
   

    /**
     * @param metadataIndexCheck1 the metadataIndexCheck1 to set
     */
    public void setMetadataIndexCheck1(OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck1)
    {
        this.metadataIndexCheck1 = metadataIndexCheck1;
    }


    /**
     * @param metadataIndexCheck2 the metadataIndexCheck2 to set
     */
    public void setMetadataIndexCheck2(OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck2)
    {
        this.metadataIndexCheck2 = metadataIndexCheck2;
    }


    /**
     * Set the query engine
     * 
     * @param queryEngine QueryEngine
     */
    public void setQueryEngine(QueryEngine queryEngine)
    {
        this.queryEngine = queryEngine;
    }

    
    /**
     * @param cmisDictionaryService the cmisDictionaryService to set
     */
    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }



    public DbCmisQueryLanguage()
    {
        this.setName("db-cmis");
    }

    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        if(metadataIndexCheck1.getPatchApplied())
        {
            return executeQueryImpl(searchParameters, admLuceneSearcher);
        }
        else
        {
            throw new QueryModelException("The patch to add the indexes to support in-transactional metadata queries has not been applied");
        }
    }
    
    
    private ResultSet executeQueryImpl(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        CMISQueryOptions options = CMISQueryOptions.create(searchParameters);
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        CapabilityJoin joinSupport = CapabilityJoin.INNERONLY;
        BaseTypeId[] validScopes = CmisFunctionEvaluationContext.ALFRESCO_SCOPES;
        CmisFunctionEvaluationContext functionContext = new CmisFunctionEvaluationContext();
        functionContext.setCmisDictionaryService(cmisDictionaryService);
        functionContext.setValidScopes(validScopes);

        CMISQueryParser parser = new CMISQueryParser(options, cmisDictionaryService, joinSupport);
        org.alfresco.repo.search.impl.querymodel.Query queryModelQuery = parser.parse(new DBQueryModelFactory(), functionContext);

        // build lucene query
        Set<String> selectorGroup = null;
        if (queryModelQuery.getSource() != null)
        {
            List<Set<String>> selectorGroups = queryModelQuery.getSource().getSelectorGroups(functionContext);
            if (selectorGroups.size() == 0)
            {
                throw new UnsupportedOperationException("No selectors");
            }
            if (selectorGroups.size() > 1)
            {
                throw new UnsupportedOperationException("Advanced join is not supported");
            }
            selectorGroup = selectorGroups.get(0);
        }

        QueryEngineResults results = queryEngine.executeQuery(queryModelQuery, options, functionContext);
        ResultSet resultSet = results.getResults().values().iterator().next();
        return resultSet;
    }
}
