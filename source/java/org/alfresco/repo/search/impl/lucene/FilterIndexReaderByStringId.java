/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import java.io.IOException;
import java.util.BitSet;
import java.util.Set;

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


/**
 * An index reader that filters documents from another.
 * 
 * @author andyh
 *
 */        
public class FilterIndexReaderByStringId extends FilterIndexReader
{
    private static Log s_logger = LogFactory.getLog(FilterIndexReaderByStringId.class);

    BitSet deletedDocuments;
    
    private String id;

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
        this.id = id;
        
        deletedDocuments = new BitSet(reader.maxDoc());

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Applying deletions FOR "+id +" (the index ito which these are applied is the previous one ...)");
        }

        try
        {
            if (!deleteNodesOnly)
            {
                for (String stringRef : deletions)
                {
                    TermDocs td = reader.termDocs(new Term("ID", stringRef));
                    while (td.next())
                    {
                        deletedDocuments.set(td.doc(), true);
                    }
                }
            }
            else
            {

                Searcher searcher = new IndexSearcher(reader);
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
                                deletedDocuments.set(hits.id(i), true);
                                // There should only be one thing to delete
                                // break;
                            }
                        }
                    }

                }
            }
        }
        catch (IOException e)
        {
        	s_logger.error("Error initialising "+id);
            throw new AlfrescoRuntimeException("Failed to construct filtering index reader", e);
        }
    }

    /**
     * Filter implementation
     * 
     * @author andyh
     *
     */
    public static class FilterTermDocs implements TermDocs
    {
        BitSet deletedDocuments;

        protected TermDocs in;
        
        String id;

        /**
         * @param id
         * @param in
         * @param deletedDocuments
         */
        public FilterTermDocs(String id, TermDocs in, BitSet deletedDocuments)
        {
            this.in = in;
            this.deletedDocuments = deletedDocuments;
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
                while (in.next())
                {
                    if (!deletedDocuments.get(in.doc()))
                    {
                        // Not masked
                        return true;
                    }
                }
                return false;
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

            if (allDeleted(innerDocs, count))
            {
                // Did not find anything - try again
                return read(docs, freqs);
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

        private boolean allDeleted(int[] docs, int fillSize)
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
            boolean result = in.skipTo(i);
            if (result == false)
            {
                return false;
            }

            if (deletedDocuments.get(in.doc()))
            {
                return skipTo(i);
            }
            else
            {
                return true;
            }
        }

        public void close() throws IOException
        {
            // Leave to internal implementation
            in.close();
        }
    }

    /** Base class for filtering {@link TermPositions} implementations. */
    public static class FilterTermPositions extends FilterTermDocs implements TermPositions
    {

        /**
         * @param id
         * @param in
         * @param deletedDocuements
         */
        public FilterTermPositions(String id, TermPositions in, BitSet deletedDocuements)
        {
            super(id, in, deletedDocuements);
        }

        public int nextPosition() throws IOException
        {
            return ((TermPositions) this.in).nextPosition();
        }
    }

    @Override
    public int numDocs()
    {
        return super.numDocs() - deletedDocuments.cardinality();
    }

    @Override
    public TermDocs termDocs() throws IOException
    {
        return new FilterTermDocs(id, super.termDocs(), deletedDocuments);
    }

    @Override
    public TermPositions termPositions() throws IOException
    {
        return new FilterTermPositions(id, super.termPositions(), deletedDocuments);
    }
}
