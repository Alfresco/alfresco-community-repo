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
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;

public class DeltaReader extends MultiReader
{
    int[][] deletions;

    Boolean hasExclusions = null;

    private IndexReader[] subReaders;

    private int maxDoc = 0;

    private int[] starts;

    public DeltaReader(IndexReader[] readers, int[][] deletions) throws IOException
    {
        super(readers);
        this.deletions = deletions;
        initialize(readers);
    }

    private void initialize(IndexReader[] subReaders) throws IOException
    {
        this.subReaders = subReaders;
        starts = new int[subReaders.length + 1]; // build starts array
        for (int i = 0; i < subReaders.length; i++)
        {
            starts[i] = maxDoc;
            maxDoc += subReaders[i].maxDoc(); // compute maxDocs
        }
        starts[subReaders.length] = maxDoc;
    }

    protected void doCommit() throws IOException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    protected void doDelete(int arg0) throws IOException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    protected void doUndeleteAll() throws IOException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean hasDeletions()
    {
        return super.hasDeletions() || hasSearchExclusions();
    }

    private boolean hasSearchExclusions()
    {
        if (hasExclusions == null)
        {
            for (int i = 0; i < deletions.length; i++)
            {
                if (deletions[i].length > 0)
                {
                    hasExclusions = new Boolean(true);
                    break;
                }
            }
            hasExclusions = new Boolean(false);
        }
        return hasExclusions.booleanValue();
    }

    public boolean isDeleted(int docNumber)
    {
        int i = readerIndex(docNumber);
        return super.isDeleted(docNumber) || (Arrays.binarySearch(deletions[i], docNumber - starts[i]) != -1);
    }

    private int readerIndex(int n)
    { // find reader for doc n:
        int lo = 0; // search starts array
        int hi = subReaders.length - 1; // for first element less

        while (hi >= lo)
        {
            int mid = (lo + hi) >> 1;
            int midValue = starts[mid];
            if (n < midValue)
                hi = mid - 1;
            else if (n > midValue)
                lo = mid + 1;
            else
            { // found a match
                while (mid + 1 < subReaders.length && starts[mid + 1] == midValue)
                {
                    mid++; // scan to last match
                }
                return mid;
            }
        }
        return hi;
    }

    public TermDocs termDocs() throws IOException
    {
        return new DeletingTermDocs(super.termDocs());
    }

    public TermPositions termPositions() throws IOException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    private class DeletingTermDocs implements TermDocs
    {
        TermDocs delegate;

        DeletingTermDocs(TermDocs delegate)
        {
            super();
            this.delegate = delegate;
        }

        public void seek(Term term) throws IOException
        {
            delegate.seek(term);
        }

        public void seek(TermEnum termEnum) throws IOException
        {
            delegate.seek(termEnum);
        }

        public int doc()
        {
            return delegate.doc();
        }

        public int freq()
        {
            return delegate.freq();
        }

        public boolean next() throws IOException
        {
            while (delegate.next())
            {
                if (!isDeleted(doc()))
                {
                    return true;
                }
            }
            return false;
        }

        public int read(int[] docs, int[] freqs) throws IOException
        {
            int end;
            int deletedCount;
            do
            {
                end = delegate.read(docs, freqs);
                if (end == 0)
                {
                    return end;
                }
                deletedCount = 0;
                for (int i = 0; i < end; i++)
                {
                    if (!isDeleted(docs[i]))
                    {
                        deletedCount++;
                    }
                }
            }
            while (end == deletedCount);
            // fix up for deleted
            int position = 0;
            for(int i = 0; i < end; i++)
            {
                if(!isDeleted(i))
                {
                    docs[position] = docs[i];
                    freqs[position] = freqs[i];
                    position++;
                }                
            }
            return position;
        }

        public boolean skipTo(int docNumber) throws IOException
        {
            delegate.skipTo(docNumber);
            if (!isDeleted(doc()))
            {
                return true;
            }
            else
            {
                return next();
            }
        }

        public void close() throws IOException
        {
            delegate.close();
        }

    }
}
