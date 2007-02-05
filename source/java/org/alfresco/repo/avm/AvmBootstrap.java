/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationEvent;

/**
 * This component ensures that the AVM system is properly bootstrapped
 * and that this is done in the correct order relative to other
 * bootstrap components.
 * 
 * @see #setIssuers(List)
 * @see org.alfresco.repo.avm.Issuer
 * 
 * @author Derek Hulley
 */
public class AvmBootstrap extends AbstractLifecycleBean
{
    private List<Issuer> issuers;
    
    public AvmBootstrap()
    {
        issuers = new ArrayList<Issuer>(0);
    }

    /**
     * Provide a list of {@link Issuer issuers} to bootstrap on context initialization.
     * 
     * @see #onBootstrap(ApplicationEvent)
     */
    public void setIssuers(List<Issuer> issuers)
    {
        this.issuers = issuers;
    }

    /**
     * Initialize the issuers.
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        for (Issuer issuer : issuers)
        {
            issuer.initialize();
        }
    }

    /** NO-OP */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing
    }
}
