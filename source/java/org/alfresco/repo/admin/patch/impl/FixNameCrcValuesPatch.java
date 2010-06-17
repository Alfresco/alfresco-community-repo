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
import java.sql.Savepoint;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.CRC32;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.hibernate.ChildAssocImpl;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.type.LongType;
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
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private ControlDAO controlDAO;
    private RuleService ruleService;
    private Dialect dialect;
    
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);
    
    public FixNameCrcValuesPatch()
    {
    }
    
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param qnameDAO          resolved QNames
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param controlDAO        used to create Savepoints
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }

    /**
     * @param ruleService the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    public void setDialect(Dialect dialect)
    {
        this.dialect = dialect;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(sessionFactory, "sessionFactory");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
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
            // put the log file into a long life temp directory
            File tempDir = TempFileProvider.getLongLifeTempDir("patches");
            logFile = new File(tempDir, "FixNameCrcValuesPatch.log");
            
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
                    getChildAssocIdCollection(),
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
                    String childName = (String) nodeDAO.getNodeProperty(childNode.getId(), ContentModel.PROP_NAME);
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
                    Savepoint savepoint = controlDAO.createSavepoint("FixNameCrcValuesPatch");
                    try
                    {
                        getSession().flush();
                        controlDAO.releaseSavepoint(savepoint);
                    }
                    catch (Throwable e)
                    {
                        controlDAO.rollbackToSavepoint(savepoint);
                        
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
        
        private Collection<Long> getChildAssocIdCollection() throws Exception
        {
            HibernateCallback<ScrollableResults> callback = new HibernateCallback<ScrollableResults>()
            {
                public ScrollableResults doInHibernate(Session session)
                {
                    SQLQuery query = session
                            .createSQLQuery(
                                    "SELECT ca.id AS child_assoc_id FROM alf_child_assoc ca");

                    // For MySQL databases we must set this unusual fetch size to force result set paging. See
                    // http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-implementation-notes.html
                    if (dialect instanceof MySQLDialect)
                    {
                        query.setFetchSize(Integer.MIN_VALUE);
                    }
                    query.addScalar("child_assoc_id", new LongType());
                    return query.scroll(ScrollMode.FORWARD_ONLY);
                }
            };
            final ScrollableResults rs;
            try
            {
                final int sizeEstimate = getHibernateTemplate().execute(new HibernateCallback<Integer>()
                {
                    public Integer doInHibernate(Session session)
                    {
                        SQLQuery query = session.createSQLQuery("SELECT COUNT(*) FROM alf_child_assoc");
                        return ((Number) query.uniqueResult()).intValue();
                    }
                });
                
                rs = getHibernateTemplate().execute(callback);                
                return new AbstractCollection<Long>()
                {
                    @Override
                    public Iterator<Long> iterator()
                    {
                        return new Iterator<Long>(){

                            private Long next = fetchNext();

                            private Long fetchNext()
                            {
                                Long next;
                                if (rs.next())
                                {
                                    next = rs.getLong(0);
                                }
                                else
                                {
                                    next = null;
                                    rs.close();
                                }
                                return next;
                            }

                            public boolean hasNext()
                            {
                                return next != null;
                            }

                            public Long next()
                            {
                                if (!hasNext())
                                {
                                    throw new IllegalStateException();
                                }
                                Long oldNext = next;
                                next = fetchNext();
                                return oldNext;
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }};
                    }

                    @Override
                    public int size()
                    {
                        return sizeEstimate;
                    }
                    
                };
            }
            catch (Throwable e)
            {
                logger.error("Failed to query for child association IDs", e);
                writeLine("Failed to query for child association IDs: " + e.getMessage());
                throw new PatchException("Failed to query for child association IDs", e);
            }
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
