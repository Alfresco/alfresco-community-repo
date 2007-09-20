/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
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
import org.alfresco.repo.domain.Node;
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
public class InvalidNameEndingPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.invalidNameEnding.result";
    private static final String MSG_REWRITTEN = "patch.invalidNameEnding.rewritten";
    private static final String ERR_UNABLE_TO_FIX = "patch.invalidNameEnding.err.unable_to_fix";
    
    private SessionFactory sessionFactory;
    private NodeDaoService nodeDaoService;
    
    
    public static void main(String[] args)
    {
        String name = "fred. ...   ";
        
        int i = (name.length() == 0) ? 0 : name.length() - 1;
        while (i >= 0 && (name.charAt(i) == '.' || name.charAt(i) == ' '))
        {
            i--;
        }

        String updatedName = (i == 0) ? "unnamed" : name.substring(0, i + 1);
        System.out.println(updatedName);
    }
    
    
    public InvalidNameEndingPatch()
    {
    }
    
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
        // initialise the helper
        HibernateHelper helper = new HibernateHelper();
        helper.setSessionFactory(sessionFactory);

        try
        {
            String msg = helper.fixNames();
            // done
            return msg;
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
            logFile = new File("./InvalidNameEndingPatch.log");
            // open the file for appending
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();
            // move to the end of the file
            channel.position(channel.size());
            // add a newline and it's ready
            writeLine("").writeLine("");
            writeLine("InvalidNameEndingPatch executing on " + new Date());
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
            // get the association types to check
            @SuppressWarnings("unused")
            List<Node> nodes = getInvalidNames();

            int updated = 0;
            for (Node node : nodes)
            {
                NodeRef nodeRef = node.getNodeRef();
                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                if (name != null && (name.endsWith(".") || name.endsWith(" ")))
                {
                    int i = (name.length() == 0) ? 0 : name.length() - 1;
                    while (i >= 0 && (name.charAt(i) == '.' || name.charAt(i) == ' '))
                    {
                        i--;
                    }

                    String updatedName = (i == 0) ? "unnamed" : name.substring(0, i + 1);
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
                                writeLine(I18NUtil.getMessage(ERR_UNABLE_TO_FIX, name ,updatedName));
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
            
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
        
        @SuppressWarnings("unchecked")
        private List<Node> getInvalidNames()
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                            .createQuery(
                                    "select node from org.alfresco.repo.domain.hibernate.NodeImpl as node " + 
                                    "join node.properties prop where " +
                                    " prop.stringValue like '%.' or " + 
                                    " prop.stringValue like '% ' ");                    
                    return query.list();
                }
            };
            List<Node> results = (List<Node>) getHibernateTemplate().execute(callback);
            return results;
        }
        
    }
}
