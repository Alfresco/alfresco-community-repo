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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;
import java.util.HashMap;

import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;

public class ReferenceCountingReadOnlyIndexReaderFactory
{
    private static Log s_logger = LogFactory.getLog(ReferenceCountingReadOnlyIndexReaderFactory.class);

    private static HashMap<String, ReferenceCountingReadOnlyIndexReader> log = new HashMap<String, ReferenceCountingReadOnlyIndexReader>();

    public static IndexReader createReader(String id, IndexReader indexReader)
    {
        ReferenceCountingReadOnlyIndexReader rc = new ReferenceCountingReadOnlyIndexReader(id, indexReader);
        if (s_logger.isDebugEnabled())
        {
            if (log.containsKey(id))
            {
                s_logger.debug("Replacing ref counting reader for " + id );
            }
            s_logger.debug("Created ref counting reader for " + id +" "+rc.toString());
            log.put(id, rc);
        }
        return rc;
    }

    public static String getState(String id)
    {
        if (s_logger.isDebugEnabled())
        {
            ReferenceCountingReadOnlyIndexReader rc = log.get(id);
            if (rc != null)
            {
                StringBuilder builder = new StringBuilder();
                builder
                        .append("Id = "
                                + rc.getId() + " Invalid = " + rc.getReferenceCount() + " invalid = "
                                + rc.getInvalidForReuse());
                return builder.toString();
            }

        }
        return ("<UNKNOWN>");

    }

    public static class ReferenceCountingReadOnlyIndexReader extends FilterIndexReader implements ReferenceCounting
    {
        private static Logger s_logger = Logger.getLogger(ReferenceCountingReadOnlyIndexReader.class);

        private static final long serialVersionUID = 7693185658022810428L;

        String id;

        int refCount = 0;

        boolean invalidForReuse = false;

        boolean allowsDeletions;
        
        boolean closed = false;

        ReferenceCountingReadOnlyIndexReader(String id, IndexReader indexReader)
        {
            super(indexReader);
            this.id = id;
        }

        public synchronized void incrementReferenceCount()
        {
            if(closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() + "Indexer is closed "+id);
            }
            refCount++;
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName()
                        + ": Reader " + id + " - increment - ref count is " + refCount + "        ... "+super.toString());
            }
        }

        public synchronized void decrementReferenceCount() throws IOException
        {
            refCount--;
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName()
                        + ": Reader " + id + " - decrement - ref count is " + refCount + "        ... "+super.toString());
            }
            closeIfRequired();
            if (refCount < 0)
            {
                s_logger.error("Invalid reference count for Reader " + id + " is " + refCount + "        ... "+super.toString());
            }
        }

        private void closeIfRequired() throws IOException
        {
            if ((refCount == 0) && invalidForReuse)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " closed." + "        ... "+super.toString());
                }
                in.close();
                closed = true;
            }
            else
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName()
                            + ": Reader " + id + " still open .... ref = " + refCount + " invalidForReuse = "
                            + invalidForReuse + "        ... "+super.toString());
                }
            }
        }

        public synchronized int getReferenceCount()
        {
            return refCount;
        }

        public synchronized boolean getInvalidForReuse()
        {
            return invalidForReuse;
        }

        public synchronized void setInvalidForReuse() throws IOException
        {
            if(closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() +"Indexer is closed "+id);
            }
            invalidForReuse = true;
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " set invalid for reuse" + "        ... "+super.toString());
            }
            closeIfRequired();
        }

        @Override
        protected void doClose() throws IOException
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName() + ": Reader " + id + " closing" + "        ... "+super.toString());
            }
            if(closed)
            {
                throw new IllegalStateException(Thread.currentThread().getName() +"Indexer is closed "+id);
            }
            decrementReferenceCount();
        }

        @Override
        protected void doDelete(int n) throws IOException
        {
            throw new UnsupportedOperationException("Delete is not supported by read only index readers");
        }

        public String getId()
        {
            return id;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ReferenceCountingReadOnlyIndexReader))
            {
                return false;
            }
            ReferenceCountingReadOnlyIndexReader other = (ReferenceCountingReadOnlyIndexReader) o;
            return EqualsHelper.nullSafeEquals(this.getId(), other.getId());

        }

        @Override
        public int hashCode()
        {
            return getId().hashCode();
        }

    }
}
