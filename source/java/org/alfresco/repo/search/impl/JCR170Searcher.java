/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl;

import org.alfresco.repo.search.AbstractSearcherComponent;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;

/**
 * Simple searcher against another store using the JSR 170 API.
 * <p>
 * This class is not fully implemented and hence still abstract.
 */
public abstract class JCR170Searcher extends AbstractSearcherComponent
{
    public ResultSet query(StoreRef store, String language, String query, Path[] queryOptions,
            QueryParameter[] queryParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths, QueryParameterDefinition[] queryParameterDefinitions)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(SearchParameters searchParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }
}
