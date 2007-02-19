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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Checks that all child node names are unique for the associations that require it.
 * 
 * @author Derek Hulley
 */
public class UniqueChildNamePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.uniqueChildName.result";
    private static final String ERR_UNABLE_TO_FIX = "patch.uniqueChildName.err.unable_to_fix";
    private static final String MSG_COPY_OF = "patch.uniqueChildName.copyOf";
    /** the number of associations to process at a time */
    private static final int MAX_RESULTS = 1000;
    
    private SessionFactory sessionFactory;
    private DictionaryService dictionaryService;
    private NodeDaoService nodeDaoService;
    
    public UniqueChildNamePatch()
    {
    }
    
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param dictionaryService The service used to sort out the associations
     *      that require duplicate checks
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
        checkPropertyNotNull(dictionaryService, "dictionaryService");
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
            String msg = helper.assignCrc();
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
            logFile = new File("./UniqueChildNamePatch.log");
            // open the file for appending
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();
            // move to the end of the file
            channel.position(channel.size());
            // add a newline and it's ready
            writeLine("").writeLine("");
            writeLine("UniqueChildNamePatch executing on " + new Date());
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

        public String assignCrc() throws Exception
        {
            // get the association types to check
            @SuppressWarnings("unused")
            List<QName> assocTypeQNames = getUsedAssocQNames();
            
            boolean unableToFix = false;
            int fixed = 0;
            int processed = 0;
            // check loop through all associations, looking for duplicates
            for (QName assocTypeQName : assocTypeQNames)
            {
                AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
                if (!(assocDef instanceof ChildAssociationDefinition))
                {
                    String msg = "WARNING: Non-child association used to link a child node: " + assocTypeQName;
                    writeLine(msg);
                    logger.warn(msg);
                    continue;
                }
                ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
                if (childAssocDef.getDuplicateChildNamesAllowed())
                {
                    continue;
                }
                write("Checking for name duplicates on association type ").writeLine(assocTypeQName);
                
                // get all child associations until there are no more results
                long lastAssocId = Long.MIN_VALUE;
                int lastResultCount = 1;
                while(lastResultCount > 0)
                {
                    writeLine(String.format("...Processed %7d associations with %3d duplicates found...", processed, fixed));
                    
                    List<Object[]> results = getAssociations(assocTypeQName, lastAssocId) ;
                    lastResultCount = results.size();
                    for (Object[] objects : results)
                    {
                        ChildAssoc childAssoc = (ChildAssoc) objects[0];
                        Node childNode = (Node) objects[1];
                        NodeRef childNodeRef = childNode.getNodeRef();
                        
                        // get the current name
                        String childName = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);

                        lastAssocId = childAssoc.getId();
                        String usedChildName = childName;
                        processed++;
                        boolean duplicate = false;
                        int duplicateNumber = 0;
                        while(true)
                        {
                            duplicateNumber++;
                            try
                            {
                                // push the name back to the node
                                nodeService.setProperty(childNodeRef, ContentModel.PROP_NAME, usedChildName);
                                break;      // no issues - no duplicate
                            }
                            catch (DuplicateChildNodeNameException e)
                            {
                                if (duplicateNumber == 10)
                                {
                                    // Try removing the secondary parent associations
                                    writeLine("   Removing secondary parents of node " + childNode.getId());
                                    Collection<ChildAssoc> parentAssocs = childNode.getParentAssocs();
                                    for (ChildAssoc parentAssoc : parentAssocs)
                                    {
                                        if (!parentAssoc.getIsPrimary())
                                        {
                                            write("      - ").writeLine(parentAssoc);
                                            // remove it
                                            getSession().delete(parentAssoc);
                                        }
                                    }
                                    // flush to ensure the database gets the changes
                                    getSession().flush();
                                    // try again to be sure
                                    continue;
                                }
                                else if (duplicateNumber > 10)
                                {
                                    // after 10 attempts, we have to admit defeat.  Perhaps there is a larger issue.
                                    Collection<ChildAssoc> parentAssocs = childNode.getParentAssocs();
                                    write("   Unable to set child name '" + usedChildName + "' for node " + childNode.getId());
                                    writeLine(" with parent associations:");
                                    for (ChildAssoc parentAssoc : parentAssocs)
                                    {
                                        write("      - ").writeLine(parentAssoc);
                                    }
                                    duplicate = false;
                                    unableToFix = true;
                                    break;
                                }
                                else
                                {
                                    // there was a duplicate, so adjust the name and change the node property
                                    duplicate = true;
                                    // assign a new name
                                    usedChildName = childName + I18NUtil.getMessage(MSG_COPY_OF, processed, duplicateNumber);
                                }
                            }
                        }
                        // if duplicated, report it
                        if (duplicate)
                        {
                            fixed++;
                            // get the node path
                            NodeRef parentNodeRef = childAssoc.getParent().getNodeRef();
                            Path path = nodeService.getPath(parentNodeRef);
                            writeLine("   Changed duplicated child name:");
                            writeLine("      Parent:         " + parentNodeRef);
                            writeLine("      Parent path:    " + path);
                            writeLine("      Duplicate name: " + childName);
                            writeLine("      Replaced with:  " + usedChildName);
                        }
                    }
                    // clear the session to preserve memory
                    getSession().flush();
                    getSession().clear();
                }
            }
            
            // check if it was successful or not
            if (unableToFix)
            {
                throw new PatchException(ERR_UNABLE_TO_FIX, logFile);
            }
            else
            {
                // build the result message
                String msg = I18NUtil.getMessage(MSG_SUCCESS, processed, fixed, logFile);
                return msg;
            }
        }
        
        @SuppressWarnings("unchecked")
        private List<QName> getUsedAssocQNames()
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                            .createQuery(
                                    "select distinct assoc.typeQName " +
                                    "from org.alfresco.repo.domain.hibernate.ChildAssocImpl as assoc");
                    return query.list();
                }
            };
            List<QName> results = (List<QName>) getHibernateTemplate().execute(callback);
            return results;
        }
        
        /**
         * @return Returns a list of <tt>ChildAssoc</tt> and <tt>String</tt> instances
         */
        @SuppressWarnings("unchecked")
        private List<Object[]> getAssociations(final QName assocTypeQName, final long lastAssocId)
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                            .getNamedQuery("node.patch.GetAssocsAndChildNames")
                            .setLong("lastAssocId", lastAssocId)
                            .setParameter("assocTypeQName", assocTypeQName)
                            .setMaxResults(MAX_RESULTS);
                    return query.list();
                }
            };
            List<Object[]> results = (List<Object[]>) getHibernateTemplate().execute(callback);
            return results;
        }
    }
}
