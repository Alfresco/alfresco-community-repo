/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm.actions;

import java.util.List;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

/**
 * This action submits all the newer changes in the passed in NodeRef
 * to its corresponding staging area. It ignores conflicts and older nodes.
 * @author britt
 */
public class SimpleAVMSubmitAction extends ActionExecuterAbstractBase
{
    private static Logger fgLogger = Logger.getLogger(SimpleAVMSubmitAction.class);
    
    public static String NAME = "simple-avm-submit";
    
    /**
     * The AVMService instance.
     */
    private AVMService fAVMService;
    
    /**
     * The AVMSyncService instance.
     */
    private AVMSyncService fAVMSyncService;
    
    /**
     * The Excluding NameMatcher.
     */
    private NameMatcher fExcluder;
    
    /**
     * Default constructor.
     */
    public SimpleAVMSubmitAction()
    {
        super();
    }
    
    /**
     * Set the AVMService.
     * @param avmService The instance.
     */
    public void setAvmService(AVMService avmService)
    {
        fAVMService = avmService;
    }
    
    /**
     * Set the AVMSyncService.
     * @param avmSyncService The instance.
     */
    public void setAvmSyncService(AVMSyncService avmSyncService)
    {
        fAVMSyncService = avmSyncService;
    }
    
    // TODO This should be a parameter of the action execution really.
    /**
     * Set the excluder.
     * @param excluder
     */
    public void setExcluder(NameMatcher excluder)
    {
        fExcluder = excluder;
    }
    
    /**
     * Perform the action. The NodeRef must be an AVM NodeRef.
     * @param action Don't actually need anything from this here.
     * @param actionedUponNodeRef The AVM NodeRef.
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // Crack the NodeRef.
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
        // Get store name and path parts.
        String [] storePath = path.split(":");
        if (storePath.length != 2)
        {
            throw new AVMSyncException("Malformed source path " + path);
        }
        // Get the .website.name property.
        PropertyValue wsProp = 
            fAVMService.getStoreProperty(storePath[0], 
                    QName.createQName(null, ".website.name"));
        if (wsProp == null)
        {
            fgLogger.warn(".website.name property not found.");
            return;
        }
        // And the actual web-site name.
        String websiteName = wsProp.getStringValue();
        // Construct the submit destination path.
        String avmDest = websiteName + "-staging:" + storePath[1];
        // Get the difference between source and destination.
        List<AVMDifference> diffs = 
            fAVMSyncService.compare(version, path, -1, avmDest, fExcluder);
        // TODO fix update comments at some point.
        // Do the update.
        fAVMSyncService.update(diffs, fExcluder, false, false, true, true,
              "Submit of item: " + AVMNodeConverter.SplitBase(path)[1], null);
        // Cleanup by flattening the source relative to the destination.
        // This is an ugliness to prevent database write misorderings in flatten.
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        fAVMSyncService.flatten(storePath[0] + ":/" + JNDIConstants.DIR_DEFAULT_WWW, websiteName + "-staging:/" + JNDIConstants.DIR_DEFAULT_WWW);
    }

    /**
     * This action takes no parameters.
     * @param paramList The List to add nothing to.
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // No parameters for this action.
    }
}
