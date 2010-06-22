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
package org.alfresco.repo.admin.patch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Savepoint;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Checks that all names do not end with ' ' or '.'
 * 
 * @author David Caruana
 */
public class LinkNodeFileExtensionPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.linkNodeExtension.result";
    private static final String MSG_REWRITTEN = "patch.linkNodeExtension.rewritten";
    private static final String ERR_UNABLE_TO_FIX = "patch.linkNodeExtension.err.unable_to_fix";
    
    private PatchDAO patchDAO;
    private ControlDAO controlDAO;
    private NodeService nodeService;

    /**
     * Default constructor
     */
    public LinkNodeFileExtensionPatch()
    {
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(nodeService, "nodeService");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        LinkNodeFileExtensionHelper helper = new LinkNodeFileExtensionHelper();

        try
        {
            return helper.fixNames();
        }
        finally
        {
            helper.closeWriter();
        }
    }
    
    private class LinkNodeFileExtensionHelper extends HibernateDaoSupport
    {
        private File logFile;
        private FileChannel channel;
        
        private LinkNodeFileExtensionHelper() throws IOException
        {
        	// Open a log file
            File tempDir = TempFileProvider.getLongLifeTempDir("patches");
            logFile = new File(tempDir, "LinkNodeExtensionPatch.log");
           
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();

            // Append to the end of the file

            channel.position(channel.size());

            writeLine("").writeLine("");
            writeLine("LinkNodeExtensionPatch executing on " + new Date());
        }
        
        private LinkNodeFileExtensionHelper write(Object obj) throws IOException
        {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes()));
            return this;
        }
        private LinkNodeFileExtensionHelper writeLine(Object obj) throws IOException
        {
            write(obj);
            write("\n");
            return this;
        }
        private void closeWriter()
        {
            try { channel.close(); } catch (Throwable e) {}
        }

        public String fixNames() throws Exception
        {
            List<Pair<NodeRef, String>> names = patchDAO.getNodesOfTypeWithNamePattern(ContentModel.TYPE_LINK, "%.lnk");

            int updated = 0;
            for (Pair<NodeRef, String> pair : names)
            {
                NodeRef nodeRef = pair.getFirst();
                String name = pair.getSecond();
                // Update the name string, replace '.lnk' with '.url'
                String updatedName = name.substring(0, name.length() - 4) + ".url";
                int idx = 0;
                boolean applied = false;
                while (!applied && idx < 10)
                {
                    Savepoint savepoint = controlDAO.createSavepoint("LinkNodeFileExtensionsFix");
                    try
                    {
                        nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, updatedName);
                        controlDAO.releaseSavepoint(savepoint);
                        applied = true;
                    }
                    catch(DuplicateChildNodeNameException e)
                    {
                        controlDAO.rollbackToSavepoint(savepoint);
                        idx++;
                        updatedName += "_" + idx;
                    }
                }
                if (applied)
                {
                    writeLine(I18NUtil.getMessage(MSG_REWRITTEN, name ,updatedName));
                }
                else
                {
                    writeLine(I18NUtil.getMessage(ERR_UNABLE_TO_FIX, name, updatedName));
                }
                updated++;
            }
            
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
    }
}
