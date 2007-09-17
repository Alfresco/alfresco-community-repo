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
package org.alfresco.service.cmr.admin;

import java.io.InputStream;
import java.util.List;

import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.namespace.QName;


/**
 * Repository Admin Service.
 *
 * Client facing API for interacting with Alfresco Repository Admin services.
 *
 */
@PublicService
public interface RepoAdminService
{
    //
    // Custom Model Management
    //

    /**
     * Get list of deployed custom model.
     */  
    @Auditable
    public List<RepoModelDefinition> getModels();

    /**
     * Deploy custom model (to the 'Models' space). 
     * Allows creation of new models and incremental update of existing models.
     * 
     */
    @Auditable(parameters = {"modelStream, modelFileName"})
    public void deployModel(InputStream modelStream, String modelFileName);

    /**
     * Undeploy custom model (from the 'Models' space). 
     * Allows delete of existing models, if not used. 
     * Permanently removes the model definition from the repository (all versions).
     */
    @Auditable(parameters = {"modelFileName"})
    public QName undeployModel(String modelFileName);

    /**
     * Activate custom model.
     */
    @Auditable(parameters = {"modelFileName"})
    public QName activateModel(String modelFileName);
    
    /**
     * Deactivate custom model.
     */
    @Auditable(parameters = {"modelFileName"})
    public QName deactivateModel(String modelFileName);

    //
    // Custom Message Management
    //

    /**
     * Get deployed custom messages resource bundles.
     */
    @Auditable
    public List<String> getMessageBundles();

    /**
     * Deploy custom message resource bundle (to the 'Messages' space).
     * 
     */
    @Auditable(parameters = {"resourceClasspath"})
    public String deployMessageBundle(String resourceClasspath);

    /**
     * Undeploy custom message resource bundle (from the 'Messages' space).
     */
    @Auditable(parameters = {"bundleBaseName"})
    public void undeployMessageBundle(String bundleBaseName);
 
    /**
     * Reload custom message resource bundle.
     */
    @Auditable(parameters = {"bundleBaseName"})
    public void reloadMessageBundle(String bundleBaseName);

}
