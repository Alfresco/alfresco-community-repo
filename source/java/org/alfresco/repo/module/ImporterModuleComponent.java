/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
