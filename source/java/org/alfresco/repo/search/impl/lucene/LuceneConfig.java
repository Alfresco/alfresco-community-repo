/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
