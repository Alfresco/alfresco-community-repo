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
