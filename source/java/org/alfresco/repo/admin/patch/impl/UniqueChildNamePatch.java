/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
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

        String msg = helper.assignCrc();
        
        // done
        return msg;
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

        public String assignCrc() throws Exception
        {
            // get the association types to check
            @SuppressWarnings("unused")
            List<QName> assocTypeQNames = getUsedAssocQNames();
            
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
                        while(true)
                        {
                            try
                            {
                                // push the name back to the node
                                nodeService.setProperty(childNodeRef, ContentModel.PROP_NAME, usedChildName);
                                break;      // no issues - no duplicate
                            }
                            catch (DuplicateChildNodeNameException e)
                            {
                                // there was a duplicate, so adjust the name and change the node property
                                duplicate = true;
                                // assign a new name
                                usedChildName = childName + I18NUtil.getMessage(MSG_COPY_OF, processed);
                                // try again
                            }
                        }
                        // if duplicated, report it
                        if (duplicate)
                        {
                            fixed++;
                            // get the node path
                            NodeRef parentNodeRef = childAssoc.getParent().getNodeRef();
                            Path path = nodeService.getPath(parentNodeRef);
                            writeLine("  Changed duplicated child name:");
                            writeLine("     Parent:         " + parentNodeRef);
                            writeLine("     Parent path:    " + path);
                            writeLine("     Duplicate name: " + childName);
                            writeLine("     Replaced with:  " + usedChildName);
                        }
                    }
                }
            }
            
            
            // build the result message
            String msg = I18NUtil.getMessage(MSG_SUCCESS, processed, fixed, logFile);
            return msg;
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
