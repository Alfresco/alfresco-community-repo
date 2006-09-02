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
package org.alfresco.repo.audit;

import java.io.InputStream;

import org.springframework.beans.factory.InitializingBean;

/**
 * A class to read the audit configuration from the class path
 * 
 * @author Andy Hind
 */
public class AuditConfigurationImpl implements InitializingBean, AuditConfiguration
{

    private String config;
    
    public AuditConfigurationImpl()
    {
        super();
    }
    
    public void setConfig(String config)
    {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.getInputStream#getInputStream()
     */
    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.AuditConfiguration#getInputStream()
     */
    public InputStream getInputStream()
    {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(config);
        return is;
    }

    public void afterPropertiesSet() throws Exception
    {
        // Read and set up the audit configuration
        
    }
    
}
