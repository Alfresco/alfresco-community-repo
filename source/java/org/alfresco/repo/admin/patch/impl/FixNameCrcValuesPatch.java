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
package org.alfresco.repo.admin.patch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.hibernate.ChildAssocImpl;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Fixes <a href=https://issues.alfresco.com/jira/browse/ETWOTWO-1133>ETWOTWO-1133</a>.
 * Checks all CRC values for <b>alf_child_assoc.child_node_name_crc and alf_child_assoc.qname_crc</b>.
 * 
 * @author Derek Hulley
 * @since V2.2SP4
 */
public class FixNameCrcValuesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixNameCrcValues.result";
    private static final String MSG_REWRITTEN = "patch.fixNameCrcValues.fixed";
    private static final String MSG_UNABLE_TO_CHANGE = "patch.fixNameCrcValues.unableToChange";
    
    private SessionFactory sessionFactory;
    private NodeDaoService nodeDaoService;
    private QNameDAO qnameDAO;
    private RuleService ruleService;
    
    public FixNameCrcValuesPatch()
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

    /**
     * @param qnameDAO          resolved QNames
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    /**
     * @param ruleService the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(sessionFactory, "sessionFactory");
        checkPropertyNotNull(nodeDaoService, "nodeDaoService");
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(applicationEventPublisher, "applicationEventPublisher");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // initialise the helper
        HibernateHelper helper = new HibernateHelper();
        helper.setSessionFactory(sessionFactory);
        
        try
        {
            String msg = helper.fixCrcValues();
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
            logFile = new File("./FixNameCrcValuesPatch.log");
            // open the file for appending
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();
            // move to the end of the file
            channel.position(channel.size());
            // add a newline and it's ready
            writeLine("").writeLine("");
            writeLine("FixNameCrcValuesPatch executing on " + new Date());
        }
        
        private HibernateHelper write(Object obj) throws IOException
        {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes("UTF-8")));
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

        public String fixCrcValues() throws Exception
        {
            // get the association types to check
            BatchProcessor<Long> batchProcessor = new BatchProcessor<Long>(
                    "FixNameCrcValuesPatch",
                    transactionService.getRetryingTransactionHelper(),
                    findMismatchedCrcs(),
                    2, 20,
                    applicationEventPublisher,
                    logger, 1000);

            // Precautionary flush and clear so that we have an empty session
            getSession().flush();
            getSession().clear();

            int updated = batchProcessor.process(new BatchProcessWorker<Long>()
            {
                public String getIdentifier(Long entry)
                {
                    return entry.toString();
                }
                
                public void beforeProcess() throws Throwable
                {
                    // Switch rules off
                    ruleService.disableRules();
                    // Authenticate as system
                    String systemUsername = AuthenticationUtil.getSystemUserName();
                    AuthenticationUtil.setFullyAuthenticatedUser(systemUsername);
                }

                public void process(Long childAssocId) throws Throwable
                {
                    ChildAssoc assoc = (ChildAssoc) getHibernateTemplate().get(ChildAssocImpl.class, childAssocId);
                    if (assoc == null)
                    {
                        // Missing now ...
                        return;
                    }
                    // Get the old CRCs
                    long oldChildCrc = assoc.getChildNodeNameCrc();
                    long oldQNameCrc = assoc.getQnameCrc();
                    
                    // Get the child node
                    Node childNode = assoc.getChild();
                    // Get the name
                    String childName = (String) nodeDaoService.getNodeProperty(childNode.getId(), ContentModel.PROP_NAME);
                    if (childName == null)
                    {
                        childName = childNode.getUuid();
                    }
                    // Update the CRCs
                    long childCrc = getCrc(childName);
                    QName qname = assoc.getQName(qnameDAO);
                    long qnameCrc = ChildAssocImpl.getCrc(qname);
                    
                    // Update the assoc
                    assoc.setChildNodeNameCrc(childCrc);
                    assoc.setQnameCrc(qnameCrc);
                    // Persist
                    try
                    {
                        getSession().flush();
                    }
                    catch (Throwable e)
                    {
                        String msg = I18NUtil.getMessage(MSG_UNABLE_TO_CHANGE, childNode.getId(), childName, oldChildCrc,
                                childCrc, qname, oldQNameCrc, qnameCrc, e.getMessage());
                        // We just log this and add details to the message file
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(msg, e);
                        }
                        else
                        {
                            logger.warn(msg);
                        }
                        writeLine(msg);
                    }
                    getSession().clear();
                    // Record
                    writeLine(I18NUtil.getMessage(MSG_REWRITTEN, childNode.getId(), childName, oldChildCrc, childCrc,
                            qname, oldQNameCrc, qnameCrc));
                }
                
                public void afterProcess() throws Throwable
                {
                    ruleService.enableRules();
                }
            }, true);

            
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
        
        private List<Long> findMismatchedCrcs() throws Exception
        {
            final Long qnameId = qnameDAO.getOrCreateQName(ContentModel.PROP_NAME).getFirst();
            
            final List<Long> childAssocIds = new ArrayList<Long>(1000);
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    SQLQuery query = session
                            .createSQLQuery(
                                    " SELECT " +
                                    "    ca.id AS child_assoc_id," +
                                    "    ca.child_node_name_crc AS child_assoc_crc," +
                                    "    np.string_value AS node_name," +
                                    "    n.uuid as node_uuid," +
                                    "    ca.qname_crc AS qname_crc," +
                                    "    ca.qname_ns_id AS qname_ns_id," +
                                    "    ca.qname_localname AS qname_localname" +
                                    " FROM" +
                                    "    alf_child_assoc ca" +
                                    "    JOIN alf_node n ON (ca.child_node_id = n.id)" +
                                    "    LEFT OUTER JOIN alf_node_properties np on (np.node_id = n.id AND np.qname_id = :qnameId)" +
                                    "");
                    query.setLong("qnameId", qnameId);
                    query.addScalar("child_assoc_id", new LongType());
                    query.addScalar("child_assoc_crc", new LongType());
                    query.addScalar("node_name", new StringType());
                    query.addScalar("node_uuid", new StringType());
                    query.addScalar("qname_crc", new LongType());
                    query.addScalar("qname_ns_id", new LongType());
                    query.addScalar("qname_localname", new StringType());
                    return query.scroll(ScrollMode.FORWARD_ONLY);
                }
            };
            ScrollableResults rs = null;
            try
            {
                rs = (ScrollableResults) getHibernateTemplate().execute(callback);
                while (rs.next())
                {
                    // Compute child name crc
                    Long assocId = (Long) rs.get(0);
                    Long dbChildCrc = (Long) rs.get(1);
                    String name = (String) rs.get(2);
                    String uuid = (String) rs.get(3);
                    long utf8ChildCrc;
                    if (name != null)
                    {
                        utf8ChildCrc = getCrc(name);
                    }
                    else
                    {
                        utf8ChildCrc = getCrc(uuid);
                    }

                    // Compute qname crc
                    Long dbQNameCrc = (Long) rs.get(4);
                    Long namespaceId = (Long) rs.get(5);
                    String namespace = qnameDAO.getNamespace(namespaceId).getSecond();
                    String localName = (String) rs.get(6);
                    QName qname = QName.createQName(namespace, localName);
                    long utf8QNameCrc = ChildAssocImpl.getCrc(qname);

                    // Check
                    if (dbChildCrc != null && utf8ChildCrc == dbChildCrc.longValue() && dbQNameCrc != null && utf8QNameCrc == dbQNameCrc.longValue())
                    {
                        // It is a match, so ignore
                        continue;
                    }
                    childAssocIds.add(assocId);
                }
            }
            catch (Throwable e)
            {
                logger.error("Failed to query for child name CRCs", e);
                writeLine("Failed to query for child name CRCs: " + e.getMessage());
                throw new PatchException("Failed to query for child name CRCs", e);
            }
            finally
            {
                if (rs != null)
                {
                    try { rs.close(); } catch (Throwable e) { writeLine("Failed to close resultset: " + e.getMessage()); }
                }
            }
            return childAssocIds;
        }
        
        /**
         * @param str           the name that will be converted to lowercase
         * @return              the CRC32 calcualted on the lowercase version of the string
         */
        private long getCrc(String str)
        {
            CRC32 crc = new CRC32();
            try
            {
                crc.update(str.toLowerCase().getBytes("UTF-8"));              // https://issues.alfresco.com/jira/browse/ALFCOM-1335
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("UTF-8 encoding is not supported");
            }
            return crc.getValue();
        }
    }
}
