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
package org.alfresco.repo.importer;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;

/**
 * Collection of views to import
 * 
 * @author David Caruana
 */
public class ImporterBootstrapViews implements InitializingBean
{
    // Dependencies
    private ImporterBootstrap importer;
    private List<Properties> bootstrapViews;
    

    /**
     * Sets the importer
     * 
     * @param importer
     */
    public void setImporter(ImporterBootstrap importer)
    {
        this.importer = importer;
    }
    
    /**
     * Sets the bootstrap views
     * 
     * @param bootstrapViews
     */
    public void setBootstrapViews(List<Properties> bootstrapViews)
    {
        this.bootstrapViews = bootstrapViews;
    }

    
    public void afterPropertiesSet() throws Exception
    {
        importer.addBootstrapViews(bootstrapViews);
    }

}
