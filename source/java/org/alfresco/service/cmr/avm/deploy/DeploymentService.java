/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.avm.deploy;

import java.util.Set;
import java.util.List;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.NameMatcher;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;

/**
 * A service to handle WCM AVM repository to remote AVM repository deployment.
 * @author britt
 */
public interface DeploymentService
{
    /**
     * Deploys the differences between what is is the local source path
     * and a destination machine's path.
     * @param version The local version.
     * @param srcPath The local path.
     * @param hostName The remote hostname.
     * @param port The remote rmi registry port.
     * @param userName The username for authentication.
     * @param password The password for authentication.
     * @param dstPath The destination path corresponding to source path.
     * @param createDst Flag for whether a missing destination should be created.
     * @param dontDelete Don't delete assets at the destination.
     * @param dontDo If this is set then this is a dry run.
     * @param callback A possibly null callback.
     */
    @Auditable
    public void deployDifference(int version, String srcPath,
                                             String hostName, 
                                             int port,
                                             String userName, 
                                             String password,
                                             String dstPath,
                                             NameMatcher matcher,
                                             boolean createDst,
                                             boolean dontDelete,
                                             boolean dontDo,
                                             List<DeploymentCallback> callback);

    /**
     * Get A reference to an ActionService instance on a remote Alfresco Server.
     * @param hostName
     * @param port
     * @param userName
     * @param password
     * @return
     */
    @NotAuditable
    public ActionService getRemoteActionService(String hostName, int port,
                                                String userName, String password);

    /**
     * Deploy to a filesystem on another machine.
     * @param version The version to deploy from.
     * @param srcPath The path to deploy from.
     * @param adapterName The name of the transport adapter to connect to the remote system.  
     * The value "default" means use the traditional RMI used for versions of Alfresco prior to 3.0
     * @param hostName The hostname of the filesystem receiver.
     * @param port The port to connect to.
     * @param userName The username for authentication of the target
     * @param password The password for authentication of the target
     * @param dstTarget The target on the deployment receiver.
     * @param createDst Flag for whether a missing destination should be created.
     * @param dontDelete Don't delete deleted nodes from destination.
     * @param dontDo If this is set, this is a dry run.
     * @param callback A possibly null callback.
     */
    @Auditable
    public void deployDifferenceFS(int version, 
    		String srcPath,
            String adapterName,
            String hostName, 
            int port,
            String userName, 
            String password,
            String dstTarget,
            NameMatcher matcher,
            boolean createDst,
            boolean dontDelete,
            boolean dontDo,
            List<DeploymentCallback> callback);
    
    /**
     * Get the names of the transport adapters.
     * 
     * @return the adapters
     */
    @NotAuditable
    public Set<String> getAdapterNames(); 

    /*
    * @param webProjectRef Web project reference.
    * */
    @NotAuditable
    public List<NodeRef> findLiveDeploymentServers(NodeRef webProjectRef);


    /*
    * @param webProjectRef Web project reference.
    * @param availableOnly find available servers only.
    * */
    @NotAuditable
    public List<NodeRef> findTestDeploymentServers(NodeRef webProjectRef, boolean availableOnly);
}
