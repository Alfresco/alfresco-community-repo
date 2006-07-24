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

import java.util.Set;

import org.alfresco.repo.search.IndexerSPI;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Andy Hind
 */
public interface LuceneIndexer2 extends IndexerSPI
{

    public void commit();
    public void rollback();
    public int prepare();
    public boolean isModified();
    public void setNodeService(NodeService nodeService);
    public void setDictionaryService(DictionaryService dictionaryService);
    public void setLuceneFullTextSearchIndexer(FullTextSearchIndexer luceneFullTextSearchIndexer);
    
    public String getDeltaId();
    public  void flushPending() throws LuceneIndexException;
    public Set<NodeRef> getDeletions();
    public boolean getDeleteOnlyNodes();
    
    public <R> R doWithWriteLock(IndexInfo.LockWork <R> lockWork);
}
