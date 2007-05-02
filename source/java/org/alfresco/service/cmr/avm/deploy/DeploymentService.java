/**
 * 
 */
package org.alfresco.service.cmr.avm.deploy;

/**
 * A service to handle AVM repository to remote AVM repository deployment.
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
    public DeploymentReport deployDifference(int version, String srcPath,
                                             String hostName, int port,
                                             String userName, String password,
                                             String dstPath,
                                             boolean createDst,
                                             boolean dontDelete,
                                             boolean dontDo,
                                             DeploymentCallback callback);

    /**
     * Deploy to a filesystem on another machine.
     * @param version The version to deploy from.
     * @param srcPath The path to deploy from.
     * @param hostName The hostname of the filesystem receiver.
     * @param port The port to connect to.
     * @param userName The username for authentication 
     * @param password The password for authentication
     * @param dstTarget The target on the deployment receiver.
     * @param createDst Flag for whether a missing destination should be created.
     * @param dontDelete Don't delete deleted nodes from destination.
     * @param dontDo If this is set, this is a dry run.
     * @param callback A possibly null callback.
     */
    public DeploymentReport deployDifferenceFS(int version, String srcPath,
                                               String hostName, int port,
                                               String userName, String password,
                                               String dstTarget,
                                               boolean createDst,
                                               boolean dontDelete, 
                                               boolean dontDo,
                                               DeploymentCallback callback);
}
