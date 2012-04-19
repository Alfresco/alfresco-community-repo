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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class defines policy behaviour for avmlayeredfolder type
 *
 * @author Ivan Rybnikov
 * @since 4.0.2
 */
public class LayeredFolderType implements NodeServicePolicies.OnMoveNodePolicy
{

    private static Log logger = LogFactory.getLog(LayeredFolderType.class);

    /** The policy component */
    private PolicyComponent policyComponent;

    private FileFolderService fileFolderService;

    transient private AVMService avmService;

    /** Used to determine was the policy executed by Web-Client operation  */
    private static ThreadLocal<Boolean> issuedByWebClient = new ThreadLocal<Boolean>();

    /** Default max time allowed for the external issuers (CIFS, FTP) */
    private static long EXTERNAL_MAX_TIME = 2000;

    /** Max time allowed for the external issuers (CIFS, FTP).
     *  can be overridden by bean definition */
    private long maxTime = EXTERNAL_MAX_TIME;

    /**
     * Initialize the avmlayeredfolder type policies
     */

    public void init()
    {
        if (maxTime < 0)
        {
            logger.warn(
                    "Unable to set 'wcm.rename.max.time.milliseconds' property value: '" + maxTime + " ms', " +
                    "set default value: '" + EXTERNAL_MAX_TIME + " ms'");
            setMaxTime(EXTERNAL_MAX_TIME);
        }
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
        PropertyCheck.mandatory(this, "avmService", avmService);

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                WCMModel.TYPE_AVM_LAYERED_FOLDER,
                new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public synchronized void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        // Storing start time
        long startTime = System.currentTimeMillis();

        NodeRef oldNodeRef = oldChildAssocRef.getChildRef();
        NodeRef newNodeRef = newChildAssocRef.getChildRef();
        String name = newChildAssocRef.getQName().getLocalName();

        String[] splittedPath = AVMUtil.splitPath(AVMNodeConverter.ToAVMVersionPath(oldNodeRef).getSecond());
        // Get AVM store name
        String avmStore = splittedPath[0];
        // Get stale path
        String oldPathToReplace = splittedPath[1];
        // Create new path
        String newPathToInsert = oldPathToReplace.substring(0, oldPathToReplace.lastIndexOf(AVMUtil.AVM_PATH_SEPARATOR_CHAR) + 1) + name;

        AVMNodeDescriptor desc = avmService.lookup(-1, AVMNodeConverter.ToAVMVersionPath(newNodeRef).getSecond());

        if (desc.isDirectory())
        {
            // Create the initial list of descriptors
            List<AVMNodeDescriptor> descriptors = new ArrayList<AVMNodeDescriptor>(10);
            descriptors.add(desc);

            // List of next level of descriptors
            List<AVMNodeDescriptor> nextDescriptors = new ArrayList<AVMNodeDescriptor>(10);

            while (descriptors.size() != 0)
            {
                if (nextDescriptors != null)
                {
                    nextDescriptors.clear();
                }

                for (AVMNodeDescriptor curDescriptor : descriptors)
                {
                    Map<String, AVMNodeDescriptor> listing = avmService.getDirectoryListing(curDescriptor);
                    if (listing.size() == 0)
                    {
                        continue;
                    }

                    for (AVMNodeDescriptor descriptor : listing.values())
                    {
                        if (descriptor.isDirectory())
                        {
                            nextDescriptors.add(descriptor);
                        }
                        else if (descriptor.isFile())
                        {
                            refreshPath(descriptor, avmStore, oldPathToReplace, newPathToInsert, startTime);
                        }
                    }
                }

                descriptors.clear();
                if(nextDescriptors != null)
                {
                    descriptors.addAll(nextDescriptors);
                }
            }
        }
    }

    private void refreshPath(AVMNodeDescriptor descriptor, String avmStore, String oldPathToReplace, String newPathToInsert, long startTime)
    {
        checkDuration(startTime);
        String path = descriptor.getPath();

        // If file is a form instance data refresh its paths to renditions
        if (avmService.hasAspect(-1, path, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
        {
            refreshFormInstanceData(avmStore, oldPathToReplace, newPathToInsert, -1, path);
        }
        // If file is a rendition find its form instance data and refresh its paths to renditions
        else if (avmService.hasAspect(-1, path, WCMAppModel.ASPECT_RENDITION))
        {
            refreshRendition(avmStore, oldPathToReplace, newPathToInsert, -1, path);
        }
    }

    private void refreshFormInstanceData(String avmStore, String pathToReplace, String pathToInsert, int nodeVersion, String nodePath)
    {
        // Get all renditions
        final PropertyValue pv = avmService.getNodeProperty(nodeVersion, nodePath, WCMAppModel.PROP_RENDITIONS);

        // Skip if there is no renditions.
        if (pv == null)
            return;

        final Collection<Serializable> renditionPaths = pv.getCollection(DataTypeDefinition.TEXT);
        final List<String> newRenditionPaths = new ArrayList<String>(renditionPaths.size());

        // Refresh all renditions paths
        for (Serializable renditionPath : renditionPaths)
        {
            // Get old path which become stale
            String oldPath = (String) renditionPath;

            // Refresh path to make it actual
            String newPath = oldPath.replaceFirst(pathToReplace, pathToInsert);
            newRenditionPaths.add(newPath);

            // Refresh property WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA of the rendition
            // which is the path from rendition to the form instance data
            String fullRenditionPath = avmStore + ":" + newPath;
            String primaryFormInstanceData = avmService.getNodeProperty(-1, fullRenditionPath, WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA).getStringValue();
            primaryFormInstanceData = primaryFormInstanceData.replaceFirst(pathToReplace, pathToInsert);
            avmService.setNodeProperty(fullRenditionPath, WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA, new PropertyValue(DataTypeDefinition.TEXT,
                    primaryFormInstanceData));
        }
        avmService.setNodeProperty(nodePath, WCMAppModel.PROP_RENDITIONS, new PropertyValue(DataTypeDefinition.TEXT, (Serializable) newRenditionPaths));

    }

    private void refreshRendition(String avmStore, String pathToReplace, String pathToInsert, int nodeVersion, String nodePath)
    {
        // Get form instance data
        String primaryFormInstanceData = avmService.getNodeProperty(nodeVersion, nodePath, WCMAppModel.PROP_PRIMARY_FORM_INSTANCE_DATA).getStringValue();

        // If form instance data is under the same folder skip it because it will be processed by refreshFormInstanceData() method
        if (primaryFormInstanceData.startsWith(pathToReplace + AVMUtil.AVM_PATH_SEPARATOR_CHAR))
            return;

        // If form instance data contains new path skip it because it have already been processed
        if (primaryFormInstanceData.startsWith(pathToInsert + AVMUtil.AVM_PATH_SEPARATOR_CHAR))
            return;

        // Get paths to renditions
        final PropertyValue pv = avmService.getNodeProperty(-1, avmStore + ":" + primaryFormInstanceData, WCMAppModel.PROP_RENDITIONS);

        // Return if renditions list is empty
        if (pv == null)
            return;

        final Collection<Serializable> renditionPaths = pv.getCollection(DataTypeDefinition.TEXT);
        final List<String> newRenditionPaths = new ArrayList<String>(renditionPaths.size());

        // Refresh all paths
        for (Serializable renditionPath : renditionPaths)
        {
            // Form instance data have already been refreshed
            if (((String) renditionPath).startsWith(pathToInsert))
                return;

            String newPath = ((String) renditionPath).replaceFirst(pathToReplace, pathToInsert);
            newRenditionPaths.add(newPath);
        }

        avmService.setNodeProperty(
                avmStore + ":" + primaryFormInstanceData,
                WCMAppModel.PROP_RENDITIONS,
                new PropertyValue(DataTypeDefinition.TEXT, (Serializable) newRenditionPaths));
    }

    /**
     * Set the policy component
     *
     * @param policyComponent the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the fileFolderService
     *
     * @param fileFolderService
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Set the avmService
     *
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * Checks policy duration.
     * If policy was fired by the external calls (FTP, CIFS) 
     * and exceeds value set in wcm.rename.max.time.milliseconds exception is thrown.
     * Default time is set to 2 seconds.
     *
     * @throws AlfrescoRuntimeException if operation exceeds max time duration
     */
    private void checkDuration(long startTime)
    {
        if (!isIssuedByWebClient() && ((System.currentTimeMillis() - startTime) > maxTime))
        {
            logger.warn("Operation exceeds max time duration and was aborted");
            throw new AlfrescoRuntimeException("Operation exceeds max time duration and was aborted");
        }
    }

    /**
     * Thread variable used to determine whether policy 
     * fired by Web-Client or by external issuers (FTP, CIFS)
     *
     * @param isWebClient
     */
    public static void setIssuedByWebClient(Boolean isWebClient)
    {
        issuedByWebClient.set(isWebClient);
    }

    /**
     * Get the issuedByWebClient
     *
     * @return issuedByWebClient
     */
    public static Boolean isIssuedByWebClient()
    {
        Boolean result = issuedByWebClient.get();
        return result == null ? Boolean.FALSE : result;
    }

    /**
     * Set the max time
     *
     * @param maxTime
     */
    public void setMaxTime(long maxTime)
    {
        this.maxTime = maxTime;
    }
}