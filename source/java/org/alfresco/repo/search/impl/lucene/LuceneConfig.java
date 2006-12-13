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

import org.alfresco.repo.search.MLAnalysisMode;

public interface LuceneConfig
{
    /**
     * Set the lock dir - just to make sure - this should no longer be used.
     * 
     * @param lockDirectory
     */
    public void setLockDirectory(String lockDirectory);
    
    /**
     * The path to the index location 
     * @return
     */
    public String getIndexRootLocation();
    
    /**
     * The batch size in which to group flushes of the index.
     * 
     * @return
     */
    public int getIndexerBatchSize();

    /**
     * The maximum numbr of sub-queries the can be generated out of wild card expansion etc
     * @return
     */
    public int getQueryMaxClauses();
    
    /**
     * The default mode for analysing ML text during index.
     * 
     * @return
     */
    public MLAnalysisMode getDefaultMLIndexAnalysisMode();
    
    /**
     * The default mode for analysis of ML text during search.
     * 
     * @return
     */
    public MLAnalysisMode getDefaultMLSearchAnalysisMode();
   
}
