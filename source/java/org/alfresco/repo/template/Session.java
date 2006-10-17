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
package org.alfresco.repo.template;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Support session information in free marker templates.
 * 
 * @author Andy Hind
 */
public class Session
{

    private ServiceRegistry services;

    @SuppressWarnings("unused")
    private TemplateImageResolver imageResolver;

    public Session(ServiceRegistry services, TemplateImageResolver imageResolver)
    {
        this.services = services;
        this.imageResolver = imageResolver;
    }

    /**
     * Get the current authentication ticket.
     * 
     * @return
     */
    public String getTicket()
    {
        return services.getAuthenticationService().getCurrentTicket();
    }
}
