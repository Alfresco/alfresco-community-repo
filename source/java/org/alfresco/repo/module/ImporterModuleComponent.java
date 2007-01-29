/*
 * Copyright (C) 2007 Alfresco, Inc.
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
package org.alfresco.repo.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.util.PropertyCheck;


/**
 * Generic module component that can be wired up to import data into the system.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
public class ImporterModuleComponent extends AbstractModuleComponent
{
    private ImporterBootstrap importer;
    private Properties bootstrapView;
    private List<Properties> bootstrapViews;

    /**
     * Set the helper that has details of the store to load the data into.
     * Alfresco has a set of predefined importers for all the common stores in use.
     * 
     * @param importer the bootstrap bean that performs the store bootstrap.
     */
    public void setImporter(ImporterBootstrap importer)
    {
        this.importer = importer;
    }

    /**
     * Set a list of bootstrap views to import.<br/>
     * This is an alternative to {@link #setBootstrapViews(List)}.
     * 
     * @param bootstrapView the bootstrap data location
     * 
     * @see ImporterBootstrap#setBootstrapViews(List)
     */
    public void setBootstrapView(Properties bootstrapView)
    {
        this.bootstrapView = bootstrapView;
    }

    /**
     * Set a list of bootstrap views to import.<br/>
     * This is an alternative to {@link #setBootstrapView(Properties)}.
     * 
     * @param bootstrapViews the bootstrap data locations
     * 
     * @see ImporterBootstrap#setBootstrapViews(List)
     */
    public void setBootstrapViews(List<Properties> bootstrapViews)
    {
        this.bootstrapViews = bootstrapViews;
    }

    @Override
    protected void checkProperties()
    {
        PropertyCheck.mandatory(this, "importerBootstrap", importer);
        if (bootstrapView == null && bootstrapViews == null)
        {
            PropertyCheck.mandatory(this, null, "bootstrapViews or bootstrapView");
        }
        // fulfil contract of override
        super.checkProperties();
    }

    @Override
    protected void executeInternal() throws Throwable
    {
        // Construct the bootstrap views
        List<Properties> views = new ArrayList<Properties>(1);
        if (bootstrapViews != null)
        {
            views.addAll(bootstrapViews);
        }
        if (bootstrapView != null)
        {
            views.add(bootstrapView);
        }
        // modify the bootstrapper
        importer.setBootstrapViews(views);
        importer.setUseExistingStore(true);              // allow import into existing store

        importer.bootstrap();
        // Done
    }
}
