package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Version history interface.
 * 
 * Collects the versions that make-up a version history.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface VersionHistory extends Serializable
{
    /**
     * Gets the root (initial / least recent) version of the version history.
     * 
     * @return  the root version
     */
    public Version getRootVersion();
    
    /**
     * Gets the head (current / most recent) version of the version history.
     * 
     * @return  the root version
     */
    public Version getHeadVersion();
    
    /**
     * Gets a collection containing all the versions within the
     * version history.
     * <p>
     * Versions are returned in descending create date order (most recent first).
     * 
     * @return  collection containing all the versions
     */
    public Collection<Version> getAllVersions();

    /**
     * Gets the predecessor of a specified version
     * 
     * @param version  the version object
     * @return         the predeceeding version, null if root version
     */
    public Version getPredecessor(Version version);

    /**
     * Gets the succeeding versions of a specified version.
     * 
     * @param version  the version object
     * @return         a collection containing the succeeding version, empty is none
     */
    public Collection<Version> getSuccessors(Version version);
    
    /**
     * Gets a version with a specified version label.  The version label is guarenteed 
     * unique within the version history.
     * 
     * @param versionLabel                   the version label
     * @return                               the version object
     * @throws VersionDoesNotExistException  indicates requested version does not exisit
     */
    public Version getVersion(String versionLabel);

}
