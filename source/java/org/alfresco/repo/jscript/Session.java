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
package org.alfresco.repo.jscript;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Scriptable;

/**
 * Support object for session level properties etc.
 * <p>
 * Provides access to the user's authentication ticket.
 * 
 * @author Andy Hind
 */
public class Session implements Scopeable
{

    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(Session.class);
    
    @SuppressWarnings("unused")
    private Scriptable scope;

    private ServiceRegistry services;

    @SuppressWarnings("unused")
    private TemplateImageResolver imageResolver;

    public Session(ServiceRegistry services,  TemplateImageResolver imageResolver)
    {
        this.services = services;
        this.imageResolver = imageResolver;
    }
    
    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    /**
     * Get the user's authentication ticket.
     * 
     * @return
     */
    public String getTicket()
    {
        return services.getAuthenticationService().getCurrentTicket();
    }
    
    /**
     * Expose the user's authentication ticket as JavaScipt property.
     * 
     * @return
     */
    public String jsGet_ticket()
    {
        return getTicket();
    }
}
