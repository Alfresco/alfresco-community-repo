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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Savepoint;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.permissions.AclCrudDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Fixes <a href=https://issues.alfresco.com/jira/browse/ALF-478>ALF-478</a>.
 * Checks all CRC values for <b>alf_authorities</b>.
 * 
 * @author Andrew Hind
 * @since V3.3
 */
public class FixAuthoritiesCrcValuesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixAuthoritiesCrcValues.result";
    private static final String MSG_REWRITTEN = "patch.fixAuthoritiesCrcValues.fixed";
    private static final String MSG_UNABLE_TO_CHANGE = "patch.fixAuthoritiesCrcValues.unableToChange";

    private final Log logger = LogFactory.getLog(getClass());
    
    private PatchDAO patchDAO;
    private AclCrudDAO aclCrudDAO;
    private ControlDAO controlDAO;
    
    public FixAuthoritiesCrcValuesPatch()
    {
    }

    /**
     * @param patchDAO          finds incorrect authorities
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param aclCrudDAO        does the actual fixing
     */
    public void setAclCrudDAO(AclCrudDAO aclCrudDAO)
    {
        this.aclCrudDAO = aclCrudDAO;
    }

    /**
     * @param controlDAO        used to create Savepoints
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(aclCrudDAO, "aclCrudDAO");
        checkPropertyNotNull(controlDAO, "controlDAO");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // initialise the helper
        FixAuthoritiesCrcValuesPatchHelper helper = new FixAuthoritiesCrcValuesPatchHelper();
        
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
    
    private class FixAuthoritiesCrcValuesPatchHelper
    {
        private File logFile;
        private FileChannel channel;
        
        private FixAuthoritiesCrcValuesPatchHelper() throws IOException
        {
            // put the log file into a long life temp directory
            File tempDir = TempFileProvider.getLongLifeTempDir("patches");
            logFile = new File(tempDir, "FixAuthorityCrcValuesPatch.log");
            
            // open the file for appending
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();
            // move to the end of the file
            channel.position(channel.size());
            // add a newline and it's ready
            writeLine("").writeLine("");
            writeLine("FixAuthorityCrcValuesPatch executing on " + new Date());
        }
        
        private FixAuthoritiesCrcValuesPatchHelper write(Object obj) throws IOException
        {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes("UTF-8")));
            return this;
        }
        private FixAuthoritiesCrcValuesPatchHelper writeLine(Object obj) throws IOException
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
            List<String> mismatchedAuthorities = patchDAO.getAuthoritiesWithNonUtf8Crcs();
            // get the association types to check
            BatchProcessor<String> batchProcessor = new BatchProcessor<String>(
                    "FixAuthorityCrcValuesPatch",
                    transactionHelper,
                    mismatchedAuthorities,
                    2, 20,
                    applicationEventPublisher,
                    logger, 1000);

            int updated = batchProcessor.process(new BatchProcessWorker<String>()
            {
                public String getIdentifier(String entry)
                {
                    return entry;
                }
                
                public void beforeProcess() throws Throwable
                {
                    // Authenticate as system
                    String systemUsername = AuthenticationUtil.getSystemUserName();
                    AuthenticationUtil.setFullyAuthenticatedUser(systemUsername);
                }

                public void process(String authority) throws Throwable
                {
                    // Persist
                    Savepoint savepoint = controlDAO.createSavepoint("FixAuthorityCrcValuesPatch");
                    try
                    {
                        aclCrudDAO.renameAuthority(authority, authority);
                        controlDAO.releaseSavepoint(savepoint);
                    }
                    catch (Throwable e)
                    {
                        controlDAO.rollbackToSavepoint(savepoint);
                        
                        String msg = I18NUtil.getMessage(MSG_UNABLE_TO_CHANGE, authority, e.getMessage());
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
                    // Record
                    writeLine(I18NUtil.getMessage(MSG_REWRITTEN, authority));
                }
                
                public void afterProcess() throws Throwable
                {
                }
            }, true);

            
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
// Keeping this for reference.  Actually, the query need only pull back the authority and crc     
//        private List<String> findMismatchedCrcs() throws Exception
//        {
//            final List<Long> authorityIds = new ArrayList<Long>(1000);
//            HibernateCallback callback = new HibernateCallback()
//            {
//                public Object doInHibernate(Session session)
//                {
//                    SQLQuery query = session
//                            .createSQLQuery(
//                                    " SELECT " +
//                                    "    au.id AS authority_id," +
//                                    "    au.authority AS authority," +
//                                    "    au.crc as crc" +
//                                    " FROM" +
//                                    "    alf_authority au");
//                    query.addScalar("authority_id", new LongType());
//                    query.addScalar("authority", new StringType());
//                    query.addScalar("crc", new LongType());
//                    return query.scroll(ScrollMode.FORWARD_ONLY);
//                }
//            };
    }
}
