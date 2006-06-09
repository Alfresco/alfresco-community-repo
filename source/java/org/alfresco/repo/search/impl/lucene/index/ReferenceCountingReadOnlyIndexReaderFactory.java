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
package org.alfresco.repo.search.impl.lucene.index;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;

public class ReferenceCountingReadOnlyIndexReaderFactory
{
    public static IndexReader createReader(String id, IndexReader indexReader)
    {
        return new ReferenceCountingReadOnlyIndexReader(id, indexReader);
    }

    public static class ReferenceCountingReadOnlyIndexReader extends FilterIndexReader implements ReferenceCounting
    {
        private static Logger s_logger = Logger.getLogger(ReferenceCountingReadOnlyIndexReader.class);
        
        
        private static final long serialVersionUID = 7693185658022810428L;

        String id;
     
        int refCount = 0;

        boolean invalidForReuse = false;

        ReferenceCountingReadOnlyIndexReader(String id, IndexReader indexReader)
        {
            super(indexReader);
            this.id = id;
        }

        public synchronized void incrementReferenceCount()
        {
            refCount++;
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName()+ ": Reader "+id+ " - increment - ref count is "+refCount);
            }
        }

        public synchronized void decrementReferenceCount() throws IOException
        {
            refCount--;
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName()+ ": Reader "+id+ " - decrement - ref count is "+refCount);
            }
            closeIfRequired();
        }

        private void closeIfRequired() throws IOException
        {
            if ((refCount == 0) && invalidForReuse)
            {
                if(s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName()+ ": Reader "+id+ " closed.");
                }
                in.close();
            }
            else
            {
                if(s_logger.isDebugEnabled())
                {
                    s_logger.debug(Thread.currentThread().getName()+ ": Reader "+id+ " still open .... ref = "+refCount+" invalidForReuse = "+invalidForReuse);
                }
            }
        }

        public synchronized int getReferenceCount()
        {
            return refCount;
        }

        public synchronized void setInvalidForReuse() throws IOException
        {
            invalidForReuse = true;
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName()+ ": Reader "+id+ " set invalid for reuse");
            }
            closeIfRequired();
        }

        @Override
        protected void doClose() throws IOException
        {
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug(Thread.currentThread().getName()+ ": Reader "+id+ " closing");
            }
            decrementReferenceCount();
        }

        @Override
        protected void doDelete(int n) throws IOException
        {
            throw new UnsupportedOperationException("Delete is not supported by read only index readers");
        }
        
        
    }
}
