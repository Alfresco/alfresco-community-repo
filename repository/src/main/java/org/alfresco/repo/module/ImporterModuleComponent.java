/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.util.PropertyCheck;

/**
 * Generic module component that can be wired up to import data into the system.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
@AlfrescoPublicApi
public class ImporterModuleComponent extends AbstractModuleComponent
{
    private ImporterBootstrap importer;
    private Properties bootstrapView;
    private List<Properties> bootstrapViews;

    /**
     * Set the helper that has details of the store to load the data into. Alfresco has a set of predefined importers for all the common stores in use.
     * 
     * @param importer
     *            the bootstrap bean that performs the store bootstrap.
     */
    public void setImporter(ImporterBootstrap importer)
    {
        this.importer = importer;
    }

    /**
     * Set a list of bootstrap views to import.<br/>
     * This is an alternative to {@link #setBootstrapViews(List)}.
     * 
     * @param bootstrapView
     *            the bootstrap data location
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
     * @param bootstrapViews
     *            the bootstrap data locations
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
        importer.setUseExistingStore(true); // allow import into existing store

        importer.bootstrap();
        // Done
    }
}
