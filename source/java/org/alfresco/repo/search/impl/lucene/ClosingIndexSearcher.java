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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class ClosingIndexSearcher extends IndexSearcher
{
    IndexReader reader;

    public ClosingIndexSearcher(String path) throws IOException
    {
        super(path);
    }

    public ClosingIndexSearcher(Directory directory) throws IOException
    {
        super(directory);
    }

    public ClosingIndexSearcher(IndexReader r)
    {
        super(r);
        this.reader = r;
    }

    /*package*/ IndexReader getReader()
    {
        return reader;
    }
    
    @Override
    public void close() throws IOException
    {
        super.close();
        if(reader != null)
        {
            reader.close();
        }
    }

}
