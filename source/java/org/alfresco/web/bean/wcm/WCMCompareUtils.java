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
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.util.NameMatcher;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.sandbox.SandboxVersion;
import org.alfresco.web.app.Application;

/**
 * AVMCompare Utils
 *
 * @author ValerySh
 */
public class WCMCompareUtils
{
	
    /**
     * Get a difference map between two corresponding node trees.
     * 
     * @param avmSyncService AVMSyncService
     * @param srcVersion The version id for the source tree.
     * @param srcPath The avm path to the source tree.
     * @param dstVersion The version id for the destination tree.
     * @param dstPath The avm path to the destination tree.
     * @param excluder A NameMatcher used to exclude files from consideration.
     * @return list of compared objects
     */
    public static List<Map<String, String>> getComparedNodes(AVMSyncService avmSyncService, int srcVersion, String srcPath, int dstVersion, String dstPath, NameMatcher excluder)
    {
        FacesContext context = FacesContext.getCurrentInstance();
        List<AVMDifference> compare = avmSyncService.compare(srcVersion, srcPath, dstVersion, dstPath, excluder);
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (AVMDifference diff : compare)
        {
            String path = diff.getSourcePath();
            Map<String, String> node = new HashMap<String, String>();
            String sandboxPath = AVMUtil.getSandboxPath(path);
            node.put("path", path.replaceFirst(sandboxPath, ""));
            node.put("name", path.substring(path.lastIndexOf("/") + 1));

            String status;
            switch (diff.getDifferenceCode())
            {
            case AVMDifference.OLDER:
                status = Application.getMessage(context, "avm_compare_older");
                break;
            case AVMDifference.NEWER:
                status = Application.getMessage(context, "avm_compare_newer");
                break;
            case AVMDifference.SAME:
                status = Application.getMessage(context, "avm_compare_same");
                break;
            case AVMDifference.DIRECTORY:
                status = Application.getMessage(context, "avm_compare_directory");
                break;
            case AVMDifference.CONFLICT:
                status = Application.getMessage(context, "avm_compare_conflict");
                break;
            default:
                status = "";
            }
            node.put("status", status);

            result.add(node);
        }
        return result;
    }

    /**
     * checks the version of the first is accessible for Store
     * 
     * @param versions versions of specified store.
     * @param item Version
     * @return true if version is first
     */
    public static boolean isFirstVersion(List<SandboxVersion> versions, SandboxVersion item)
    {
        boolean result = false;
        if (versions.size() > 0)
        {
            if (item.getVersion() == Collections.min(versions, new SandboxVersionComparator()).getVersion())
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * checks the version of the last is accessible for Store
     * 
     * @param versions versions of specified store.
     * @param item Version
     * @return true if version is latest
     */
    public static boolean isLatestVersion(List<SandboxVersion> versions, SandboxVersion item)
    {
        boolean result = false;
        if (versions.size() > 0)
        {
            if (item.getVersion() == Collections.max(versions, new SandboxVersionComparator()).getVersion())
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Get Previous Version Id
     * 
     * @param sandboxService SandboxService
     * @param name The name of the AVMStore
     * @param version Current version Id
     * @return Previous Version Id
     */
    public static int getPrevVersionID(SandboxService sandboxService, String name, int version)
    {
        List<Integer> allVersions = getAllVersionID(sandboxService, name);
        Collections.sort(allVersions);
        int index = allVersions.indexOf(version);
        if (index == 0)
        {
            return 0;
        }
        else
        {
            if (index == -1)
            {
                return -1;
            }
        }
        return allVersions.get(index - 1);
    }

    /**
     * Receive Stores List
     * 
     * @param avmService AVMService
     * @return List Stores name
     */
    public static List<String> receiveStoresList(AVMService avmService)
    {
        List<String> result = new ArrayList<String>();
        List<AVMStoreDescriptor> storeDescs = avmService.getStores();
        for (AVMStoreDescriptor storeDesc : storeDescs)
        {
            if (!storeDesc.getCreator().equalsIgnoreCase(AuthenticationUtil.SYSTEM_USER_NAME) && !AVMUtil.isPreviewStore(storeDesc.getName()))
            {
                result.add(storeDesc.getName());
            }
        }
        return result;
    }

    /**
     * Get the versions id in an AVMStore
     * 
     * @param sandboxService SandboxService
     * @param store The name of the AVMStore
     * @return List versions id
     */
    public static List<Integer> getAllVersionID(SandboxService sandboxService, String store)
    {
        List<SandboxVersion> allVersions = sandboxService.listSnapshots(store, false);
        List<Integer> result = new ArrayList<Integer>();
        for (SandboxVersion sandboxVersion : allVersions)
        {
            result.add(sandboxVersion.getVersion());
        }
        return result;
    }

    /**
     * Comparator for SandboxVersion class
     */
    private static class SandboxVersionComparator implements Comparator<SandboxVersion>
    {

        public int compare(SandboxVersion o1, SandboxVersion o2)
        {
            return ((Integer) o1.getVersion()).compareTo((Integer) o2.getVersion());
        }

    }
}
