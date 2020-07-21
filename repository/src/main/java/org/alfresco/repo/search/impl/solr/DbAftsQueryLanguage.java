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

import org.alfresco.repo.admin.patch.OptionalPatchApplicationCheckBootstrapBean;
import org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author Andy
 *
 */
public class DbAftsQueryLanguage  extends AbstractAlfrescoFtsQueryLanguage
{
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

    public DbAftsQueryLanguage()
    {
        this.setName("db-afts");
    }

    @Override
    public ResultSet executeQuery(SearchParameters searchParameters)
    {
        if(metadataIndexCheck1.getPatchApplied())
        {
            return super.executeQuery(searchParameters);
        }
        else
        {
            throw new QueryModelException("The patch to add the indexes to support in-transactional metadata queries has not been applied");
        }
    }
}
