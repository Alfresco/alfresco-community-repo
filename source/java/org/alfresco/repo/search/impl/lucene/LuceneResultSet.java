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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;

import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.ResultSetRowIterator;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Searcher;

/**
 * Implementation of a ResultSet on top of Lucene Hits class.
 * 
 * @author andyh
 * 
 */
public class LuceneResultSet extends AbstractResultSet
{
    /**
     * The underlying hits
     */
    Hits hits;

    private Searcher searcher;
    
    private NodeService nodeService;

    SearchParameters searchParameters;
    
    /**
     * Wrap a lucene seach result with node support
     * 
     * @param storeRef
     * @param hits
     */
    public LuceneResultSet(Hits hits, Searcher searcher, NodeService nodeService, Path[]propertyPaths, SearchParameters searchParameters)
    {
        super(propertyPaths);
        this.hits = hits;
        this.searcher = searcher;
        this.nodeService = nodeService;
        this.searchParameters = searchParameters;
    }

    /*
     * ResultSet implementation
     */

    public ResultSetRowIterator iterator()
    {
        return new LuceneResultSetRowIterator(this);
    }

    public int length()
    {
        return hits.length();
    }

    public NodeRef getNodeRef(int n)
    {
        try
        {
            // We have to get the document to resolve this
            // It is possible the store ref is also stored in the index
            Document doc = hits.doc(n);
            String id = doc.get("ID");
            return new NodeRef(id);
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading reading node ref from the result set", e);
        }
    }

    public float getScore(int n) throws SearcherException
    {
        try
        {
            return hits.score(n);
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading score from the result set", e);
        }
    }

    public Document getDocument(int n)
    {
        try
        {
            Document doc = hits.doc(n);
            return doc;
        }
        catch (IOException e)
        {
            throw new SearcherException("IO Error reading reading document from the result set", e);
        }
    }

    public void close()
    {
        try
        {
            searcher.close();
        }
        catch (IOException e)
        {
            throw new SearcherException(e);
        }
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public ResultSetRow getRow(int i)
    {
        if(i < length())
        {
           return new LuceneResultSetRow(this, i);
        }
        else
        {
            throw new SearcherException("Invalid row");
        }
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
       return getRow(n).getChildAssocRef();
    }

    
    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, searchParameters);
    }
}
