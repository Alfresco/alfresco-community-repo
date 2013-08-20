/**
 * Provides the public facing interfaces of the Services of the Alfresco Repository.
 * <p>
 * The PublicService is a marker interface for those services which are intended to be 
 * public entry points to the Alfresco Repository.   Those interfaces marked as PublicService 
 * are audited.
 * <p>
 * The ServiceRegistry provides access to the Alfresco Repository Services for the cases where the spring context is not available.
 */
@PackageMarker
package org.alfresco.service;
import org.alfresco.util.PackageMarker;
