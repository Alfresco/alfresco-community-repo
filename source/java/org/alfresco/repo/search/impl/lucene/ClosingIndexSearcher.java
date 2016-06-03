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
