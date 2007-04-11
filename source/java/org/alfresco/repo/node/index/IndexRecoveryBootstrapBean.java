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
package org.alfresco.repo.node.index;

import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

public class IndexRecoveryBootstrapBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(IndexRecoveryBootstrapBean.class);

    IndexRecovery indexRecoveryComponent;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // reindex
        log.info("Checking/Recovering indexes ...");
        indexRecoveryComponent.reindex();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

    public IndexRecovery getIndexRecoveryComponent()
    {
        return indexRecoveryComponent;
    }

    public void setIndexRecoveryComponent(IndexRecovery indexRecoveryComponent)
    {
        this.indexRecoveryComponent = indexRecoveryComponent;
    }

}
