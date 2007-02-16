/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.admin.patch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
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
    
    private SessionFactory sessionFactory;
    private NodeDaoService nodeDaoService;

    /**
     * Default constructor
     *
     */
    public LinkNodeFileExtensionPatch()
    {
    }

    /**
     * Set the session factory
     * 
     * @param sessionFactory SessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param nodeDaoService The service that generates the CRC values
     */
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(sessionFactory, "sessionFactory");
        checkPropertyNotNull(nodeDaoService, "nodeDaoService");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Initialise the helper
    	
        HibernateHelper helper = new HibernateHelper();
        helper.setSessionFactory(sessionFactory);

        try
        {
        	// Fix the link node file names
        	
            return helper.fixNames();
        }
        finally
        {
            helper.closeWriter();
        }
    }
    
    private class HibernateHelper extends HibernateDaoSupport
    {
        private File logFile;
        private FileChannel channel;
        
        private HibernateHelper() throws IOException
        {
        	// Open a log file
        	
            logFile = new File("./LinkNodeExtensionPatch.log");
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();

            // Append to the end of the file

            channel.position(channel.size());

            writeLine("").writeLine("");
            writeLine("LinkNodeExtensionPatch executing on " + new Date());
        }
        
        private HibernateHelper write(Object obj) throws IOException
        {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes()));
            return this;
        }
        private HibernateHelper writeLine(Object obj) throws IOException
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
            // Get the list of nodes to be updated
        	
            @SuppressWarnings("unused")
            List<NodeImpl> nodes = getInvalidNames();

            int updated = 0;
            for (NodeImpl node : nodes)
            {
            	// Check that the node is a link node
            	
                NodeRef nodeRef = node.getNodeRef();
                
                if ( nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION) != null)
                {
                	// Get the current file name
                	
	                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	                if (name != null && name.length() >= 4 && name.endsWith(".lnk"))
	                {
	                	// Update the name string, replace '.lnk' with '.url'
	                	
	                	String updatedName = name.substring(0, name.length() - 4) + ".url";
	                	
	                    int idx = 0;
	                    boolean applied = false;
	                    while (!applied)
	                    {
	                        try
	                        {
	                            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, updatedName);
	                            applied = true;
	                        }
	                        catch(DuplicateChildNodeNameException e)
	                        {
	                            idx++;
	                            if (idx > 10)
	                            {
	                                writeLine(I18NUtil.getMessage(ERR_UNABLE_TO_FIX, name, updatedName));
	                                throw new PatchException(ERR_UNABLE_TO_FIX, logFile);
	                            }
	                            updatedName += "_" + idx;
	                        }
	                    }
	                    writeLine(I18NUtil.getMessage(MSG_REWRITTEN, name ,updatedName));
	                    updated++;
	                    getSession().flush();
	                    getSession().clear();
	                }
                }
            }
            
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
        
        @SuppressWarnings("unchecked")
        private List<NodeImpl> getInvalidNames()
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                            .createQuery(
                                    "select node from org.alfresco.repo.domain.hibernate.NodeImpl as node " + 
                                    "join node.properties prop where " +
                                    " prop.stringValue like '%.lnk' ");                    
                    return query.list();
                }
            };
            List<NodeImpl> results = (List<NodeImpl>) getHibernateTemplate().execute(callback);
            return results;
        }
        
    }
}
