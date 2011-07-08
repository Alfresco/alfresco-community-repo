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
package org.alfresco.repo.admin.patch.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.FileCopyUtils;

/**
 * Patch to update the activities email templates. Current templates become
 * versions of the new templates.
 * 
 * @author Florian Mueller
 */
public class ActivitiesTemplatesUpdatePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.activitiesTemplatesUpdate.result";

    protected FileFolderService fileFolderService;
    protected VersionService versionService;

    protected String newTemplatesFile;
    protected String newTemplatesName;

    protected ZipFile zipFile;

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public void setNewTemplatesFile(String newTemplatesFile)
    {
        this.newTemplatesFile = newTemplatesFile;

        int x = newTemplatesFile.lastIndexOf("/");
        if (x < 0)
        {
            newTemplatesName = newTemplatesFile;
        } else
        {
            newTemplatesName = newTemplatesFile.substring(x + 1);
        }

        x = newTemplatesName.lastIndexOf(".");
        if (x > 0)
        {
            newTemplatesName = newTemplatesName.substring(0, x);
        }
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // get templates folder
        NodeRef templateFolder = getTemplateFolder();

        // get ACP file
        ZipFile zipFile = getZipFile();

        // iterate over all templates in the ACP file and apply version
        @SuppressWarnings("unchecked")
        Enumeration<ZipArchiveEntry> zae = (Enumeration<ZipArchiveEntry>) zipFile.getEntries();
        int count = 0;
        while (zae.hasMoreElements())
        {
            ZipArchiveEntry entry = zae.nextElement();
            if (!(entry.getName().startsWith(newTemplatesName + "/") && entry.getName().endsWith(".ftl")))
            {
                // ignore non-template files
                continue;
            }

            // find matching node and add version
            NodeRef nodeRef = findMatchingNode(templateFolder, entry);
            if (nodeRef != null)
            {
                addNewVersion(nodeRef, zipFile, entry);
                count++;
            }
        }

        return I18NUtil.getMessage(MSG_SUCCESS, count);
    }

    protected NodeRef getTemplateFolder()
    {
        String xpath = "app:company_home/app:dictionary/app:email_templates/cm:activities";
        try
        {
            NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, xpath, null, namespaceService, false);

            if (nodeRefs.size() < 1)
            {
                throw new PatchException("patch.activitiesTemplatesUpdate.err.template_folder_not_found");
            }

            return nodeRefs.get(0);
        } catch (Exception e)
        {
            throw new PatchException("patch.activitiesTemplatesUpdate.err.template_folder_not_found", e);
        }
    }

    protected ZipFile getZipFile()
    {
        InputStream templateFileStream = ActivitiesTemplatesUpdatePatch.class.getClassLoader().getResourceAsStream(
                newTemplatesFile);
        if (templateFileStream == null)
        {
            throw new PatchException("patch.activitiesTemplatesUpdate.err.source_not_found");
        }

        try
        {
            File tempFile = TempFileProvider.createTempFile("templateFile", ".tmp");
            FileOutputStream os = new FileOutputStream(tempFile);
            FileCopyUtils.copy(templateFileStream, os);

            return new ZipFile(tempFile, "Cp437");
        } catch (IOException e)
        {
            throw new PatchException("patch.activitiesTemplatesUpdate.err.source_not_found", e);
        }
    }

    protected NodeRef findMatchingNode(NodeRef parentNodeRef, ZipArchiveEntry entry)
    {
        String name = entry.getName();
        int x = name.lastIndexOf("/");
        if (x > 0)
        {
            name = name.substring(x + 1);
        }

        return nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);
    }

    protected void addNewVersion(NodeRef nodeRef, ZipFile zipFile, ZipArchiveEntry entry)
    {
        try
        {
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_INITIAL_VERSION, true);
                props.put(ContentModel.PROP_AUTO_VERSION, false);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
            }

            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

            versionService.createVersion(nodeRef, versionProperties);

            ContentWriter writer = fileFolderService.getWriter(nodeRef);
            writer.setMimetype("text/plain");
            writer.setEncoding("UTF-8");
            writer.putContent(zipFile.getInputStream(entry));
        } catch (Exception e)
        {
            throw new PatchException("patch.activitiesTemplatesUpdate.err.update_failed", e);
        }
    }
}
