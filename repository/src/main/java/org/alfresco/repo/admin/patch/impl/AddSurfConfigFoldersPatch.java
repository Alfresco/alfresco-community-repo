/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.admin.patch.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A patch to add folders into
 * {@literal /app:company_home/st:sites/cm:surf-config} folder and optionally
 * sets their permission.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class AddSurfConfigFoldersPatch extends AbstractPatch
{

    private static final String MSG_START = "patch.addSurfConfigFolders.start";
    private static final String MSG_RESULT = "patch.addSurfConfigFolders.result";
    private static final String MSG_EXIST = "patch.addSurfConfigFolders.exist";
    private static final String MSG_MISSING_SURFCONFIG = "patch.addSurfConfigFolders.missingSurfConfig";

    private SiteService siteService;
    private FileFolderService fileFolderService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private HiddenAspect hiddenAspect;

    private List<FolderDetails> folderDetailsList;

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }

    public void setFolderDetailsList(List<FolderDetails> folderDetailsList)
    {
        this.folderDetailsList = folderDetailsList;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        StringBuilder result = new StringBuilder(I18NUtil.getMessage(MSG_START));

        // /app:company_home/st:sites/
        NodeRef siteRoot = siteService.getSiteRoot();
        NodeRef surfConfigNodeRef = nodeService.getChildByName(siteRoot, ContentModel.ASSOC_CONTAINS, "surf-config");
        if (surfConfigNodeRef == null)
        {
            result.append(I18NUtil.getMessage(MSG_MISSING_SURFCONFIG));
            return result.toString();
        }

        Map<String, Boolean> createdFolders = new LinkedHashMap<>();
        Map<String, Boolean> skippedFolders = new LinkedHashMap<>();
        for (FolderDetails fd : folderDetailsList)
        {
            boolean appliedPermission = false;
            NodeRef childFolder = nodeService.getChildByName(surfConfigNodeRef, ContentModel.ASSOC_CONTAINS, fd.folderName);
            if (childFolder == null)
            {
                childFolder = fileFolderService.create(surfConfigNodeRef, fd.folderName, ContentModel.TYPE_FOLDER).getNodeRef();
                // apply index control aspect as part of the hidden aspect
                hiddenAspect.hideNode(childFolder, false, false, false);

                appliedPermission = setPermission(childFolder, fd);
                createdFolders.put(fd.folderName, appliedPermission);
            }
            else
            {
                if (fd.applyPermissionIfFolderExist)
                {
                    appliedPermission = setPermission(childFolder, fd);
                }
                skippedFolders.put(fd.folderName, appliedPermission);
            }
        }
        if (createdFolders.size() > 0)
        {
            result.append(I18NUtil.getMessage(MSG_RESULT, toString(createdFolders, false), toString(createdFolders, true)));
        }
        if (skippedFolders.size() > 0)
        {
            result.append(I18NUtil.getMessage(MSG_EXIST, toString(skippedFolders, false), toString(skippedFolders, true)));
        }
        return result.toString();
    }

    private boolean setPermission(NodeRef nodeRef, FolderDetails details)
    {
        if (details.authority != null && details.permission != null)
        {
            if (authorityService.getAuthorityNodeRef(details.authority) == null)
            {
                throw new PatchException("The [" + details.authority + "] is not a valid authority name");
            }
            permissionService.setPermission(nodeRef, details.authority, details.permission, true);

            return true;
        }
        return false;
    }

    private String toString(Map<String, Boolean> map, boolean onlyAppliedPermission)
    {
        StringBuilder sb = new StringBuilder(map.size() * 2);
        for (Entry<String, Boolean> entry : map.entrySet())
        {
            if (onlyAppliedPermission)
            {
                if (entry.getValue())
                {
                    sb.append(entry.getKey()).append(", ");
                }
            }
            else
            {
                sb.append(entry.getKey()).append(", ");
            }
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    public static class FolderDetails
    {
        private String folderName;
        private String authority;
        private String permission;
        // Whether to set the given permission even though the folder does exist
        private boolean applyPermissionIfFolderExist;

        public void setFolderName(String folderName)
        {
            this.folderName = folderName;
        }

        public void setAuthority(String authority)
        {
            this.authority = authority;
        }

        public void setPermission(String permission)
        {
            this.permission = permission;
        }

        public void setApplyPermissionIfFolderExist(boolean applyPermissionIfFolderExist)
        {
            this.applyPermissionIfFolderExist = applyPermissionIfFolderExist;
        }
    }
}
