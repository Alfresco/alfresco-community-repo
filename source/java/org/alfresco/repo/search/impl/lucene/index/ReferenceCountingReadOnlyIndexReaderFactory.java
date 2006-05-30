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

import org.aopalliance.intercept.MethodInvocation;
import org.apache.lucene.index.IndexReader;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.target.SingletonTargetSource;

public class ReferenceCountingReadOnlyIndexReaderFactory
{
    public static IndexReader createReader(IndexReader indexReader)
    {
        AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisorAdapterRegistry.wrap(new Interceptor(indexReader)));
        proxyFactory.setTargetSource(new SingletonTargetSource(indexReader));
        IndexReader proxy = (IndexReader) proxyFactory.getProxy();
        return proxy;
    }

    public static class Interceptor extends DelegatingIntroductionInterceptor implements ReferenceCounting
    {
        
        private static final long serialVersionUID = 7693185658022810428L;

        IndexReader indexReader;

        int refCount = 0;

        boolean invalidForReuse = false;

        Interceptor(IndexReader indexReader)
        {
            this.indexReader = indexReader;
        }

        public Object invoke(MethodInvocation mi) throws Throwable
        {
            // Read only
            String methodName = mi.getMethod().getName();
            if (methodName.equals("delete") || methodName.equals("doDelete"))
            {
                throw new UnsupportedOperationException("Delete is not supported by read only index readers");
            }
            // Close
            else if (methodName.equals("close"))
            {
                decrementReferenceCount();
                return null;
            }
            else
            {
                return super.invoke(mi);
            }
        }

        public synchronized void incrementReferenceCount()
        {
            refCount++;
        }

        public synchronized void decrementReferenceCount() throws IOException
        {
            refCount--;
            closeIfRequired();
        }

        private void closeIfRequired() throws IOException
        {
            if ((refCount == 0) && invalidForReuse)
            {
                indexReader.close();
            }
        }

        public synchronized boolean isUsed()
        {
            return (refCount > 0);
        }

        public synchronized void setInvalidForReuse() throws IOException
        {
            invalidForReuse = true;
            closeIfRequired();
        }
    }
}
