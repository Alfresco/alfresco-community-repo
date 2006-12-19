/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.domain.schema;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Bootstraps the schema and schema update.  The schema is considered missing if the applied patch table
 * is not present, and the schema is considered empty if the applied patch table is empty.
 * 
 * @author Derek Hulley
 */
public class SchemaBootstrap extends AbstractLifecycleBean
{
    /** The placeholder for the configured <code>Dialect</code> class name: <b>${db.script.dialect}</b> */
    private static final String PLACEHOLDER_SCRIPT_DIALECT = "\\$\\{db\\.script\\.dialect\\}";

    private static final String MSG_EXECUTING_SCRIPT = "schema.update.msg.executing_script";
    private static final String MSG_OPTIONAL_STATEMENT_FAILED = "schema.update.msg.optional_statement_failed";
    private static final String ERR_STATEMENT_FAILED = "schema.update.err.statement_failed";
    private static final String ERR_UPDATE_FAILED = "schema.update.err.update_failed";
    private static final String ERR_VALIDATION_FAILED = "schema.update.err.validation_failed";
    private static final String ERR_SCRIPT_NOT_RUN = "schema.update.err.update_script_not_run";
    private static final String ERR_SCRIPT_NOT_FOUND = "schema.update.err.script_not_found";
    private static final String ERR_STATEMENT_TERMINATOR = "schema.update.err.statement_terminator";
    
    private static Log logger = LogFactory.getLog(SchemaBootstrap.class);
    
    private LocalSessionFactoryBean localSessionFactory;
    private String schemaOuputFilename;
    private boolean updateSchema;
    private List<String> postCreateScriptUrls;
    private List<SchemaUpgradeScriptPatch> validateUpdateScriptPatches;
    private List<SchemaUpgradeScriptPatch> applyUpdateScriptPatches;

    public SchemaBootstrap()
    {
        postCreateScriptUrls = new ArrayList<String>(1);
        validateUpdateScriptPatches = new ArrayList<SchemaUpgradeScriptPatch>(4);
        applyUpdateScriptPatches = new ArrayList<SchemaUpgradeScriptPatch>(4);
    }
    
    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory) throws BeansException
    {
        this.localSessionFactory = localSessionFactory;
    }

    /**
     * Set this to output the full database creation script
     * 
     * @param schemaOuputFilename the name of a file to dump the schema to, or null to ignore
     */
    public void setSchemaOuputFilename(String schemaOuputFilename)
    {
        this.schemaOuputFilename = schemaOuputFilename;
    }

    /**
     * Set whether to modify the schema or not.  Either way, the schema will be validated.
     * 
     * @param updateSchema true to update and validate the schema, otherwise false to just
     *      validate the schema.  Default is <b>true</b>.
     */
    public void setUpdateSchema(boolean updateSchema)
    {
        this.updateSchema = updateSchema;
    }

    /**
     * Set the scripts that must be executed after the schema has been created.
     * 
     * @param postCreateScriptUrls file URLs
     * 
     * @see #PLACEHOLDER_SCRIPT_DIALECT
     */
    public void setPostCreateScriptUrls(List<String> postUpdateScriptUrls)
    {
        this.postCreateScriptUrls = postUpdateScriptUrls;
    }

    /**
     * Set the schema script patches that must have been applied.  These will not be
     * applied to the database.  These can be used where the script <u>cannot</u> be
     * applied automatically or where a particular upgrade path is no longer supported.
     * For example, at version 3.0, the upgrade scripts for version 1.4 may be considered
     * unsupported - this doesn't prevent the manual application of the scripts, though.
     * 
     * @param applyUpdateScriptPatches a list of schema patches to check
     */
    public void setValidateUpdateScriptPatches(List<SchemaUpgradeScriptPatch> scriptPatches)
    {
        this.validateUpdateScriptPatches = scriptPatches;
    }

    /**
     * Set the schema script patches that may be executed during an update.
     * 
     * @param applyUpdateScriptPatches a list of schema patches to check
     */
    public void setApplyUpdateScriptPatches(List<SchemaUpgradeScriptPatch> scriptPatches)
    {
        this.applyUpdateScriptPatches = scriptPatches;
    }

    private void dumpSchemaCreate(Configuration cfg, File schemaOutputFile)
    {
        // if the file exists, delete it
        if (schemaOutputFile.exists())
        {
            schemaOutputFile.delete();
        }
        SchemaExport schemaExport = new SchemaExport(cfg)
                .setFormat(true)
                .setHaltOnError(true)
                .setOutputFile(schemaOutputFile.getAbsolutePath())
                .setDelimiter(";");
        schemaExport.execute(false, false, false, true);
    }
    
    private SessionFactory getLocalSessionFactory()
    {
        return (SessionFactory) localSessionFactory.getObject();
    }
    
    private static class NoSchemaException extends Exception
    {
        private static final long serialVersionUID = 5574280159910824660L;
    }
    
    /**
     * @return Returns the number of applied patches
     */
    private int countAppliedPatches(Connection connection) throws Exception
    {
        DatabaseMetaData dbMetadata = connection.getMetaData();
        
        ResultSet tableRs = dbMetadata.getTables(null, null, "%", null);
        boolean newPatchTable = false;
        boolean oldPatchTable = false;
        try
        {
            while (tableRs.next())
            {
                String tableName = tableRs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase("applied_patch"))
                {
                    oldPatchTable = true;
                    break;
                }
                else if (tableName.equalsIgnoreCase("alf_applied_patch"))
                {
                    newPatchTable = true;
                    break;
                }
            }
        }
        finally
        {
            try { tableRs.close(); } catch (Throwable e) {e.printStackTrace(); }
        }
        
        if (newPatchTable)
        {
            Statement stmt = connection.createStatement();
            try
            {
                ResultSet rs = stmt.executeQuery("select count(id) from alf_applied_patch");
                rs.next();
                int count = rs.getInt(1);
                return count;
            }
            finally
            {
                try { stmt.close(); } catch (Throwable e) {}
            }
        }
        else if (oldPatchTable)
        {
            // found the old style table name
            Statement stmt = connection.createStatement();
            try
            {
                ResultSet rs = stmt.executeQuery("select count(id) from applied_patch");
                rs.next();
                int count = rs.getInt(1);
                return count;
            }
            finally
            {
                try { stmt.close(); } catch (Throwable e) {}
            }
        }
        else
        {
            // The applied patches table is not present
            throw new NoSchemaException();
        }
    }
    
    /**
     * @return Returns the number of applied patches
     */
    private boolean didPatchSucceed(Connection connection, String patchId) throws Exception
    {
        Statement stmt = connection.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery("select succeeded from alf_applied_patch where id = '" + patchId + "'");
            if (!rs.next())
            {
                return false;
            }
            boolean succeeded = rs.getBoolean(1);
            return succeeded;
        }
        catch (Throwable e)
        {
            // we'll try another table name
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
        // for pre-1.4 databases, the table was named differently
        stmt = connection.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery("select succeeded from applied_patch where id = '" + patchId + "'");
            if (!rs.next())
            {
                return false;
            }
            boolean succeeded = rs.getBoolean(1);
            return succeeded;
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Builds the schema from scratch or applies the necessary patches to the schema.
     */
    private void updateSchema(Configuration cfg, Session session, Connection connection) throws Exception
    {
        boolean create = false;
        try
        {
            countAppliedPatches(connection);
        }
        catch (NoSchemaException e)
        {
            create = true;
        }
        // Get the dialect
        final Dialect dialect = Dialect.getDialect(cfg.getProperties());
        String dialectStr = dialect.getClass().getName();

        if (create)
        {
            // the applied patch table is missing - we assume that all other tables are missing
            // perform a full update using Hibernate-generated statements
            File tempFile = TempFileProvider.createTempFile("AlfrescoSchemaCreate-" + dialectStr + "-", ".sql");
            dumpSchemaCreate(cfg, tempFile);
            executeScriptFile(cfg, connection, tempFile, tempFile.getPath());
            // execute post-create scripts (not patches)
            for (String scriptUrl : this.postCreateScriptUrls)
            {
                executeScriptUrl(cfg, connection, scriptUrl);
            }
        }
        else
        {
            // we have a database, so just run the update scripts
            checkSchemaPatchScripts(cfg, session, connection, validateUpdateScriptPatches, false);   // check for scripts that must have been run
            checkSchemaPatchScripts(cfg, session, connection, applyUpdateScriptPatches, true);       // execute scripts as required
            // let Hibernate do any required updates
            File tempFile = null;
            Writer writer = null;
            try
            {
                DatabaseMetadata metadata = new DatabaseMetadata(connection, dialect);
                String[] sqls = cfg.generateSchemaUpdateScript(dialect, metadata);
                if (sqls.length > 0)
                {
                    tempFile = TempFileProvider.createTempFile("AlfrescoSchemaUpdate-" + dialectStr + "-", ".sql");
                    writer = new BufferedWriter(new FileWriter(tempFile));
                    for (String sql : sqls)
                    {
                        writer.append(sql);
                        writer.append(";\n");
                    }
                }
            }
            finally
            {
                if (writer != null)
                {
                    try {writer.close();} catch (Throwable e) {}
                }
            }
            // execute if there were changes raised by Hibernate
            if (tempFile != null)
            {
                executeScriptFile(cfg, connection, tempFile, tempFile.getPath());
            }
        }
    }
    
    /**
     * Check that the necessary scripts have been executed against the database
     */
    private void checkSchemaPatchScripts(
            Configuration cfg,
            Session session,
            Connection connection,
            List<SchemaUpgradeScriptPatch> scriptPatches,
            boolean apply) throws Exception
    {
        // first check if there have been any applied patches
        int appliedPatchCount = countAppliedPatches(connection);
        if (appliedPatchCount == 0)
        {
            // This is a new schema, so upgrade scripts are irrelevant
            // and patches will not have been applied yet
            return;
        }
        
        for (SchemaUpgradeScriptPatch patch : scriptPatches)
        {
            final String patchId = patch.getId();
            final String scriptUrl = patch.getScriptUrl();

            // check if the script was successfully executed
            boolean wasSuccessfullyApplied = didPatchSucceed(connection, patchId);
            if (wasSuccessfullyApplied)
            {
                // nothing to do - it has been done before
                continue;
            }
            else if (!apply)
            {
                // the script was not run and may not be run automatically
                throw AlfrescoRuntimeException.create(ERR_SCRIPT_NOT_RUN, scriptUrl);
            }
            // it wasn't run and it can be run now
            executeScriptUrl(cfg, connection, scriptUrl);
        }
    }
    
    private void executeScriptUrl(Configuration cfg, Connection connection, String scriptUrl) throws Exception
    {
        Dialect dialect = Dialect.getDialect(cfg.getProperties());
        String dialectStr = dialect.getClass().getName();
        InputStream scriptInputStream = getScriptInputStream(dialect.getClass(), scriptUrl);
        // check that it exists
        if (scriptInputStream == null)
        {
            throw AlfrescoRuntimeException.create(ERR_SCRIPT_NOT_FOUND, scriptUrl);
        }
        // write the script to a temp location for future and failure reference
        File tempFile = null;
        try
        {
            tempFile = TempFileProvider.createTempFile("AlfrescoSchemaUpdate-" + dialectStr + "-", ".sql");
            ContentWriter writer = new FileContentWriter(tempFile);
            writer.putContent(scriptInputStream);
        }
        finally
        {
            try { scriptInputStream.close(); } catch (Throwable e) {}  // usually a duplicate close
        }
        // now execute it
        executeScriptFile(cfg, connection, tempFile, scriptUrl);
    }
    
    /**
     * Replaces the dialect placeholder in the script URL and attempts to find a file for
     * it.  If not found, the dialect hierarchy will be walked until a compatible script is
     * found.  This makes it possible to have scripts that are generic to all dialects.
     * 
     * @return Returns an input stream onto the script, otherwise null
     */
    private InputStream getScriptInputStream(Class dialectClazz, String scriptUrl) throws Exception
    {
        // replace the dialect placeholder
        String dialectScriptUrl = scriptUrl.replaceAll(PLACEHOLDER_SCRIPT_DIALECT, dialectClazz.getName());
        // get a handle on the resource
        ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
        Resource resource = rpr.getResource(dialectScriptUrl);
        if (!resource.exists())
        {
            // it wasn't found.  Get the superclass of the dialect and try again
            Class superClazz = dialectClazz.getSuperclass();
            if (Dialect.class.isAssignableFrom(superClazz))
            {
                // we still have a Dialect - try again
                return getScriptInputStream(superClazz, scriptUrl);
            }
            else
            {
                // we have exhausted all options
                return null;
            }
        }
        else
        {
            // we have a handle to it
            return resource.getInputStream();
        }
    }
    
    private void executeScriptFile(
            Configuration cfg,
            Connection connection,
            File scriptFile,
            String scriptUrl) throws Exception
    {
        logger.info(I18NUtil.getMessage(MSG_EXECUTING_SCRIPT, scriptUrl));
        
        InputStream scriptInputStream = new FileInputStream(scriptFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(scriptInputStream, "UTF8"));
        try
        {
            int line = 0;
            // loop through all statements
            StringBuilder sb = new StringBuilder(1024);
            while(true)
            {
                String sql = reader.readLine();
                line++;
                
                if (sql == null)
                {
                    // nothing left in the file
                    break;
                }
                
                // trim it
                sql = sql.trim();
                if (sql.length() == 0 ||
                    sql.startsWith( "--" ) ||
                    sql.startsWith( "//" ) ||
                    sql.startsWith( "/*" ) )
                {
                    if (sb.length() > 0)
                    {
                        // we have an unterminated statement
                        throw AlfrescoRuntimeException.create(ERR_STATEMENT_TERMINATOR, (line - 1), scriptUrl);
                    }
                    // there has not been anything to execute - it's just a comment line
                    continue;
                }
                // have we reached the end of a statement?
                boolean execute = false;
                boolean optional = false;
                if (sql.endsWith(";"))
                {
                    sql = sql.substring(0, sql.length() - 1);
                    execute = true;
                    optional = false;
                }
                else if (sql.endsWith(";(optional)"))
                {
                    sql = sql.substring(0, sql.length() - 11);
                    execute = true;
                    optional = true;
                }
                // append to the statement being built up
                sb.append(" ").append(sql);
                // execute, if required
                if (execute)
                {
                    sql = sb.toString();
                    executeStatement(connection, sql, optional, line, scriptFile);
                    sb = new StringBuilder(1024);
                }
            }
        }
        finally
        {
            try { reader.close(); } catch (Throwable e) {}
            try { scriptInputStream.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Execute the given SQL statement, absorbing exceptions that we expect during
     * schema creation or upgrade.
     */
    private void executeStatement(Connection connection, String sql, boolean optional, int line, File file) throws Exception
    {
        Statement stmt = connection.createStatement();
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing statment: " + sql);
            }
            stmt.execute(sql);
        }
        catch (SQLException e)
        {
            if (optional)
            {
                // it was marked as optional, so we just ignore it
                String msg = I18NUtil.getMessage(MSG_OPTIONAL_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
                logger.debug(msg);
            }
            else
            {
                String err = I18NUtil.getMessage(ERR_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
                logger.error(err);
                throw e;
            }
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // do everything in a transaction
        Session session = getLocalSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try
        {
            // make sure that we don't autocommit
            Connection connection = session.connection();
            connection.setAutoCommit(false);
            
            Configuration cfg = localSessionFactory.getConfiguration();
            // dump the schema, if required
            if (schemaOuputFilename != null)
            {
                File schemaOutputFile = new File(schemaOuputFilename);
                dumpSchemaCreate(cfg, schemaOutputFile);
            }
            
            // update the schema, if required
            if (updateSchema)
            {
                updateSchema(cfg, session, connection);
            }
            
            // verify that all patches have been applied correctly 
            checkSchemaPatchScripts(cfg, session, connection, validateUpdateScriptPatches, false);   // check scripts
            checkSchemaPatchScripts(cfg, session, connection, applyUpdateScriptPatches, false);      // check scripts

            // all done successfully
            transaction.commit();
        }
        catch (Throwable e)
        {
            try { transaction.rollback(); } catch (Throwable ee) {}
            if (updateSchema)
            {
                throw new AlfrescoRuntimeException(ERR_UPDATE_FAILED, e);
            }
            else
            {
                throw new AlfrescoRuntimeException(ERR_VALIDATION_FAILED, e);
            }
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}
