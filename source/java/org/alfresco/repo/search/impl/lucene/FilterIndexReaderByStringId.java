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
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.OpenBitSet;


/**
 * An index reader that filters documents from another.
 * 
 * @author andyh
 *
 */        
public class FilterIndexReaderByStringId extends FilterIndexReader
{
    private static Log s_logger = LogFactory.getLog(FilterIndexReaderByStringId.class);

    private OpenBitSet deletedDocuments;
    private final Set<String> deletions;
    private final boolean deleteNodesOnly;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final String id;

    /**
     * Apply the filter
     * 
     * @param id
     * @param reader
     * @param deletions
     * @param deleteNodesOnly
     */
    public FilterIndexReaderByStringId(String id, IndexReader reader, Set<String> deletions, boolean deleteNodesOnly)
    {
        super(reader);
        reader.incRef();
        this.id = id;
        this.deletions = deletions;
        this.deleteNodesOnly = deleteNodesOnly;
        
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Applying deletions FOR "+id +" (the index ito which these are applied is the previous one ...)");
        }

    }
    
    public OpenBitSet getDeletedDocuments()
    {
        lock.readLock().lock();
        try
        {
            if (deletedDocuments != null)
            {
                return deletedDocuments;
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try
        {
            if (deletedDocuments != null)
            {
                return deletedDocuments;
            }
            deletedDocuments = new OpenBitSet(in.maxDoc());

            if (!deleteNodesOnly)
            {
                for (String stringRef : deletions)
                {
                    TermDocs td = in.termDocs(new Term("ID", stringRef));
                    while (td.next())
                    {
                        deletedDocuments.set(td.doc());
                    }
                    td.close();
                }
            }
            else
            {

                Searcher searcher = new IndexSearcher(in);
                for (String stringRef : deletions)
                {
                    TermQuery query = new TermQuery(new Term("ID", stringRef));
                    Hits hits = searcher.search(query);
                    if (hits.length() > 0)
                    {
                        for (int i = 0; i < hits.length(); i++)
                        {
                            Document doc = hits.doc(i);
                            if (doc.getField("ISCONTAINER") == null)
                            {
                                deletedDocuments.set(hits.id(i));
                                // There should only be one thing to delete
                                // break;
                            }
                        }
                    }
                }
                // searcher does not need to be closed, the reader is live 
            }
            return deletedDocuments;
        }
        catch (IOException e)
        {
            s_logger.error("Error initialising "+id, e);
            throw new AlfrescoRuntimeException("Failed to find deleted documents to filter", e);
        }
        finally
        {
            lock.writeLock().unlock();
        }
        
    }

    // Prevent from actually setting the closed flag
    @Override
    protected void doClose() throws IOException
    {
        this.in.decRef();
    }        

    /**
     * Filter implementation
     * 
     * @author andyh
     *
     */
    public class FilterTermDocs implements TermDocs
    {
        protected TermDocs in;
        
        String id;

        /**
         * @param id
         * @param in
         * @param deletedDocuments
         */
        public FilterTermDocs(String id, TermDocs in)
        {
            this.in = in;
        }

        public void seek(Term term) throws IOException
        {
            // Seek is left to the base implementation
            in.seek(term);
        }

        public void seek(TermEnum termEnum) throws IOException
        {
            // Seek is left to the base implementation
            in.seek(termEnum);
        }

        public int doc()
        {
            // The current document info is valid in the base implementation
            return in.doc();
        }

        public int freq()
        {
            // The frequency is valid in the base implementation
            return in.freq();
        }

        public boolean next() throws IOException
        {
        	try
        	{
        	    if (!in.next())
        	    {
        	        return false;
        	    }
        	    OpenBitSet deletedDocuments = getDeletedDocuments();
        	    while (deletedDocuments.get(in.doc()))
        	    {
                    if (!in.next())
                    {
                        return false;
                    }        	        
        	    }
                // Not masked
                return true;
        	}
        	catch(IOException ioe)
        	{
        		s_logger.error("Error reading docs for "+id);
        		throw ioe;
        	}
        }

        public int read(int[] docs, int[] freqs) throws IOException
        {
            int[] innerDocs = new int[docs.length];
            int[] innerFreq = new int[docs.length];
            int count = in.read(innerDocs, innerFreq);

            // Is the stream exhausted
            if (count == 0)
            {
                return 0;
            }

            OpenBitSet deletedDocuments = getDeletedDocuments();
            while (allDeleted(innerDocs, count, deletedDocuments))
            {

                count = in.read(innerDocs, innerFreq);

                // Is the stream exhausted
                if (count == 0)
                {
                    return 0;
                }
            }

            // Add non deleted

            int insertPosition = 0;
            for (int i = 0; i < count; i++)
            {
                if (!deletedDocuments.get(innerDocs[i]))
                {
                    docs[insertPosition] = innerDocs[i];
                    freqs[insertPosition] = innerFreq[i];
                    insertPosition++;
                }
            }

            return insertPosition;
        }

        private boolean allDeleted(int[] docs, int fillSize, OpenBitSet deletedDocuments)
        {
            for (int i = 0; i < fillSize; i++)
            {
                if (!deletedDocuments.get(docs[i]))
                {
                    return false;
                }
            }
            return true;
        }

        public boolean skipTo(int i) throws IOException
        {
            if (!in.skipTo(i))
            {
                return false;
            }

            OpenBitSet deletedDocuments = getDeletedDocuments();
            while (deletedDocuments.get(in.doc()))
            {
                if (!in.next())
                {
                    return false;
                }
            }
            return true;
        }

        public void close() throws IOException
        {
            // Leave to internal implementation
            in.close();
        }
    }

    /** Base class for filtering {@link TermPositions} implementations. */
    public class FilterTermPositions extends FilterTermDocs implements TermPositions
    {

        TermPositions tp;
        
        /**
         * @param id
         * @param in
         * @param deletedDocuements
         */
        public FilterTermPositions(String id, TermPositions in)
        {
            super(id, in);
            tp = in;
        }

        public int nextPosition() throws IOException
        {
            return tp.nextPosition();
        }

        public byte[] getPayload(byte[] data, int offset) throws IOException
        {
            return tp.getPayload(data, offset);
        }

        public int getPayloadLength()
        {
            return tp.getPayloadLength();
        }

        public boolean isPayloadAvailable()
        {
            return tp.isPayloadAvailable();
        }
    }

    @Override
    public int numDocs()
    {
        return super.numDocs() - (int)getDeletedDocuments().cardinality();
    }

    @Override
    public TermDocs termDocs() throws IOException
    {
        return new FilterTermDocs(id, super.termDocs());
    }

    @Override
    public TermPositions termPositions() throws IOException
    {
        return new FilterTermPositions(id, super.termPositions());
    }
}
