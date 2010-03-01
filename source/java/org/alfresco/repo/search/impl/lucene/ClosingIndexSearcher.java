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
