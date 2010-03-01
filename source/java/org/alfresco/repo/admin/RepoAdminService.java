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
package org.alfresco.repo.admin;

import java.io.InputStream;
import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.namespace.QName;


/**
 * Repository Admin Service interface.
 * <p>
 * This interface provides certain repository administrative methods to:
 *
 * - deploy/undeploy custom content models to/from repository
 * - deploy/undeploy custom messages resources to/from repository
 *
 * Initially, this will support models and messages used by workflow process definitions.
 */

public interface RepoAdminService
{
    /* Custom models managed in the repository */
    
    public List<RepoModelDefinition> getModels();
     
    public void deployModel(InputStream modelStream, String modelFileName);
    
    public QName undeployModel(String modelFileName);
    
    public QName activateModel(String modelFileName);
    
    public QName deactivateModel(String modelFileName);
    
    /* Custom message/resource bundles managed in the repository */
    
    public List<String> getMessageBundles();
    
    public String deployMessageBundle(String resourceClasspath); 
    
    public void undeployMessageBundle(String bundleBaseName);
    
    public void reloadMessageBundle(String bundleBaseName);
    
}
