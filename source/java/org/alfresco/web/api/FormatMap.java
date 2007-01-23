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
package org.alfresco.web.api;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;


/**
 * A map of mimetypes indexed by format.
 * 
 * @author davidc
 */
public class FormatMap implements InitializingBean
{
    private FormatRegistry registry;
    private String agent;
    private Map<String, String> formats;
    

    /**
     * Sets the Format Registry
     * 
     * @param registry
     */
    public void setRegistry(FormatRegistry registry)
    {
        this.registry = registry;
    }
    
    /**
     * Sets the User Agent for which the formats apply
     * 
     * @param agent
     */
    public void setAgent(String agent)
    {
        this.agent = agent;
    }
    
    /**
     * Sets the formats
     * 
     * @param formats
     */
    public void setFormats(Map<String, String> formats)
    {
        this.formats = formats;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        // Add formats to format registry
        registry.addFormats(agent, formats);
    }
    
}
