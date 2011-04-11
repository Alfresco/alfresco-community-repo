/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.service.cmr.admin;

import java.io.InputStream;
import java.util.List;

import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
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
    @Auditable(parameters = {"modelStream", "modelFileName"}, recordable = {false, true})
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

    //
    // Usage
    //
    
    /**
     * Get the currently-active restrictions to the repository usage
     * 
     * @since 3.5
     */
    @Auditable
    public RepoUsage getRestrictions();
    
    /**
     * Get the repository usage, where known
     * 
     * @return          the currently-known repository usage
     * 
     * @since 3.5
     */
    public RepoUsage getUsage();
    
    /**
     * Force an update of the usages, providing a hint on the specific updates required.
     * If another client is already performing the update, then the calling code will need
     * to determine the severity i.e. is an updated value <b>really</b> needed.  Generally
     * clients should accept that the data might be slightly stale, especially since there
     * is no way to guarantee visibility of data being put into the database by other
     * transactions.
     * 
     * @param usageType         the type of usage update to perform
     * @return                  <tt>true</tt> if the update succeeded or <tt>false</tt> if
     *                          some other client was already performing the same update
     */
    public boolean updateUsage(UsageType usageType);
    
    /**
     * Get full information on the state of the usage limits, including errors and warnings
     * about limits in play.
     * 
     * @return                  the object containing all the information
     */
    public RepoUsageStatus getUsageStatus();
}
