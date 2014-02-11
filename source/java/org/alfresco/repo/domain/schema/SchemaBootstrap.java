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
package org.alfresco.repo.domain.schema;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.SerializableTypeHandler;
import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.admin.patch.Patch;
import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoOracle9Dialect;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoSQLServerDialect;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoSybaseAnywhereDialect;
import org.alfresco.repo.domain.patch.AppliedPatchDAO;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.DatabaseMetaDataHelper;
import org.alfresco.util.LogUtil;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.schemacomp.ExportDb;
import org.alfresco.util.schemacomp.MultiFileDumper;
import org.alfresco.util.schemacomp.MultiFileDumper.DbToXMLFactory;
import org.alfresco.util.schemacomp.Result;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.SchemaComparator;
import org.alfresco.util.schemacomp.XMLToSchema;
import org.alfresco.util.schemacomp.model.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.UserSuppliedConnectionProvider;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.MySQLInnoDBDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.ActionQueue;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;
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
    private static final String PLACEHOLDER_DIALECT = "\\$\\{db\\.script\\.dialect\\}";
    
    /** The global property containing the default batch size used by --FOREACH */
    private static final String PROPERTY_DEFAULT_BATCH_SIZE = "system.upgrade.default.batchsize";

    private static final String MSG_DIALECT_USED = "schema.update.msg.dialect_used";
    private static final String MSG_DATABASE_USED = "schema.update.msg.database_used";
    private static final String MSG_BYPASSING_SCHEMA_UPDATE = "schema.update.msg.bypassing";
    private static final String MSG_NORMALIZED_SCHEMA = "schema.update.msg.normalized_schema";
    private static final String MSG_NO_CHANGES = "schema.update.msg.no_changes";
    private static final String MSG_ALL_STATEMENTS = "schema.update.msg.all_statements";
    private static final String MSG_EXECUTING_GENERATED_SCRIPT = "schema.update.msg.executing_generated_script";
    private static final String MSG_EXECUTING_COPIED_SCRIPT = "schema.update.msg.executing_copied_script";
    private static final String MSG_EXECUTING_STATEMENT = "schema.update.msg.executing_statement";
    private static final String MSG_OPTIONAL_STATEMENT_FAILED = "schema.update.msg.optional_statement_failed";
    private static final String WARN_DIALECT_UNSUPPORTED = "schema.update.warn.dialect_unsupported";
    private static final String WARN_DIALECT_HSQL = "schema.update.warn.dialect_hsql";
    private static final String WARN_DIALECT_DERBY = "schema.update.warn.dialect_derby";
    private static final String ERR_FORCED_STOP = "schema.update.err.forced_stop";
    private static final String ERR_DIALECT_SHOULD_USE = "schema.update.err.dialect_should_use";
    private static final String ERR_MULTIPLE_SCHEMAS = "schema.update.err.found_multiple";
    private static final String ERR_PREVIOUS_FAILED_BOOTSTRAP = "schema.update.err.previous_failed";
    private static final String ERR_STATEMENT_FAILED = "schema.update.err.statement_failed";
    private static final String ERR_UPDATE_FAILED = "schema.update.err.update_failed";
    private static final String ERR_VALIDATION_FAILED = "schema.update.err.validation_failed";
    private static final String ERR_SCRIPT_NOT_RUN = "schema.update.err.update_script_not_run";
    private static final String ERR_SCRIPT_NOT_FOUND = "schema.update.err.script_not_found";
    private static final String ERR_STATEMENT_INCLUDE_BEFORE_SQL = "schema.update.err.statement_include_before_sql";
    private static final String ERR_STATEMENT_VAR_ASSIGNMENT_BEFORE_SQL = "schema.update.err.statement_var_assignment_before_sql";
    private static final String ERR_STATEMENT_VAR_ASSIGNMENT_FORMAT = "schema.update.err.statement_var_assignment_format";
    private static final String ERR_STATEMENT_TERMINATOR = "schema.update.err.statement_terminator";
    private static final String DEBUG_SCHEMA_COMP_NO_REF_FILE = "system.schema_comp.debug.no_ref_file";
    private static final String INFO_SCHEMA_COMP_ALL_OK = "system.schema_comp.info.all_ok";
    private static final String WARN_SCHEMA_COMP_PROBLEMS_FOUND = "system.schema_comp.warn.problems_found";
    private static final String WARN_SCHEMA_COMP_PROBLEMS_FOUND_NO_FILE = "system.schema_comp.warn.problems_found_no_file";
    private static final String DEBUG_SCHEMA_COMP_TIME_TAKEN = "system.schema_comp.debug.time_taken";
    
    public static final int DEFAULT_LOCK_RETRY_COUNT = 24;
    public static final int DEFAULT_LOCK_RETRY_WAIT_SECONDS = 5;
    
    public static final int DEFAULT_MAX_STRING_LENGTH = 1024;

    private static volatile int maxStringLength = DEFAULT_MAX_STRING_LENGTH;
    private Dialect dialect;
        
    private ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

    /**
     * @see PropertyValue#DEFAULT_MAX_STRING_LENGTH
     */
    private static final void setMaxStringLength(int length)
    {
        if (length < 1024)
        {
            throw new AlfrescoRuntimeException("The maximum string length must >= 1024 characters.");
        }
        SchemaBootstrap.maxStringLength = length;
    }

    /**
     * @return      Returns the maximum number of characters that a string field can be
     */
    public static final int getMaxStringLength()
    {
        return SchemaBootstrap.maxStringLength;
    }
    
    /**
     * Truncates or returns a string that will fit into the string columns in the schema.  Text fields can
     * either cope with arbitrarily long text fields or have the default limit, {@link #DEFAULT_MAX_STRING_LENGTH}.
     * 
     * @param value             the string to check
     * @return                  Returns a string that is short enough for {@link SchemaBootstrap#getMaxStringLength()}
     * 
     * @since 3.2
     */
    public static final String trimStringForTextFields(String value)
    {
        if (value != null && value.length() > maxStringLength)
        {
            return value.substring(0, maxStringLength);
        }
        else
        {
            return value;
        }
    }
    
    
    /**
     * Provide a reference to the DescriptorService, used to provide information
     * about the repository such as the database schema version number.
     * 
     * @param descriptorService the descriptorService to set
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * Sets the previously auto-detected Hibernate dialect.
     * 
     * @param dialect
     *            the dialect
     */
    public void setDialect(Dialect dialect)
    {
        this.dialect = dialect;
    }

    private static Log logger = LogFactory.getLog(SchemaBootstrap.class);
    
    private DescriptorService descriptorService;
    private DataSource dataSource;
    private AppliedPatchDAO appliedPatchDAO;
    private LocalSessionFactoryBean localSessionFactory;
    private String schemaOuputFilename;
    private boolean updateSchema;
    private boolean stopAfterSchemaBootstrap;
    private List<String> preCreateScriptUrls;
    private List<String> postCreateScriptUrls;
    private List<String> schemaReferenceUrls;
    private List<SchemaUpgradeScriptPatch> preUpdateScriptPatches;
    private List<SchemaUpgradeScriptPatch> postUpdateScriptPatches;
    private List<SchemaUpgradeScriptPatch> updateActivitiScriptPatches;
    private int schemaUpdateLockRetryCount = DEFAULT_LOCK_RETRY_COUNT;
    private int schemaUpdateLockRetryWaitSeconds = DEFAULT_LOCK_RETRY_WAIT_SECONDS;
    private int maximumStringLength;
    private Properties globalProperties;
    
    private ThreadLocal<StringBuilder> executedStatementsThreadLocal = new ThreadLocal<StringBuilder>();

    public SchemaBootstrap()
    {
        preCreateScriptUrls = new ArrayList<String>(1);
        postCreateScriptUrls = new ArrayList<String>(1);
        preUpdateScriptPatches = new ArrayList<SchemaUpgradeScriptPatch>(4);
        postUpdateScriptPatches = new ArrayList<SchemaUpgradeScriptPatch>(4);
        updateActivitiScriptPatches = new ArrayList<SchemaUpgradeScriptPatch>(4);
        maximumStringLength = -1;
        globalProperties = new Properties();
    }
    
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setAppliedPatchDAO(AppliedPatchDAO appliedPatchDAO)
    {
        this.appliedPatchDAO = appliedPatchDAO;
    }

    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory)
    {
        this.localSessionFactory = localSessionFactory;
    }
    
    public LocalSessionFactoryBean getLocalSessionFactory()
    {
        return localSessionFactory;
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
     * Set whether this component should terminate the bootstrap process after running all the
     * usual checks and scripts.  This has the additional effect of dumping a final schema
     * structure file just before exiting.
     * <p>
     * <b>WARNING: </b>USE FOR DEBUG AND UPGRADE TESTING ONLY
     * 
     * @param stopAfterSchemaBootstrap      <tt>true</tt> to terminate (with exception) after
     *                                      running all the usual schema updates and checks.
     */
    public void setStopAfterSchemaBootstrap(boolean stopAfterSchemaBootstrap)
    {
        this.stopAfterSchemaBootstrap = stopAfterSchemaBootstrap;
    }

    /**
     * Specifies the schema reference files that will be used to validate the repository
     * schema whenever changes have been made. The database dialect placeholder will be
     * resolved so that the correct reference files are loaded for the current database
     * type (e.g. PostgreSQL)
     * 
     * @param schemaReferenceUrls the schemaReferenceUrls to set
     * @see #PLACEHOLDER_DIALECT
     */
    public void setSchemaReferenceUrls(List<String> schemaReferenceUrls)
    {
        this.schemaReferenceUrls = schemaReferenceUrls;
    }
    
    /**
     * Set the number times that the DB must be checked for the presence of the table
     * indicating that a schema change is in progress.
     * 
     * @param schemaUpdateLockRetryCount        the number of times to retry (default 24)
     */
    public void setSchemaUpdateLockRetryCount(int schemaUpdateLockRetryCount)
    {
        this.schemaUpdateLockRetryCount = schemaUpdateLockRetryCount;
    }

    /**
     * Set the wait time (seconds) between checks for the schema update lock.
     * 
     * @param schemaUpdateLockRetryWaitSeconds  the number of seconds between checks (default 5 seconds)
     */
    public void setSchemaUpdateLockRetryWaitSeconds(int schemaUpdateLockRetryWaitSeconds)
    {
        this.schemaUpdateLockRetryWaitSeconds = schemaUpdateLockRetryWaitSeconds;
    }

    /**
     * Optionally override the system's default maximum string length.  Some databases have
     * limitations on how long the <b>string_value</b> columns can be while other do not.
     * Some parts of the persistence have alternatives when the string values exceed this
     * length while others do not.  Either way, it is possible to adjust the text column sizes
     * and adjust this value manually to override the default associated with the database
     * being used.
     * <p>
     * The system - as of V2.1.2 - will attempt to adjust the maximum string length size
     * automatically and therefore this method is not normally required.  But it is possible
     * to manually override the value if, for example, the system doesn't guess the correct
     * maximum length or if the dialect is not explicitly catered for.
     * <p>
     * All negative or zero values are ignored and the system defaults to its best guess based
     * on the dialect being used.
     * 
     * @param maximumStringLength       the maximum length of the <b>string_value</b> columns
     */
    public void setMaximumStringLength(int maximumStringLength)
    {
        if (maximumStringLength > 0)
        {
            this.maximumStringLength = maximumStringLength;
        }
    }

    /**
     * Get the limit for the hibernate executions queue
     */
    public int getHibernateMaxExecutions()
    {
        return ActionQueue.getMAX_EXECUTIONS_SIZE();
    }

    /**
     * Set the limit for the hibernate executions queue
     * Less than zero always uses event amalgamation 
     */
    public void setHibernateMaxExecutions(int hibernateMaxExecutions)
    {
        ActionQueue.setMAX_EXECUTIONS_SIZE(hibernateMaxExecutions);
    }
    
    
    /**
     * Sets the properties map from which we look up some configuration settings.
     * 
     * @param globalProperties
     *            the global properties
     */
    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }

    private SessionFactory getSessionFactory()
    {
        return (SessionFactory) localSessionFactory.getObject();
    }
    
    /**
     * Register a new script for execution when creating a clean schema.  The order of registration
     * determines the order of execution.
     * 
     * @param preCreateScriptUrl            the script URL, possibly containing the <b>${db.script.dialect}</b> placeholder
     */
    public void addPreCreateScriptUrl(String preCreateScriptUrl)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered create script URL (pre-Hibernate): " + preCreateScriptUrl);
        }
        this.preCreateScriptUrls.add(preCreateScriptUrl);
    }

    /**
     * Register a new script for execution after the Hibernate schema creation phase.  The order of registration
     * determines the order of execution.
     * 
     * @param postCreateScriptUrl           the script URL, possibly containing the <b>${db.script.dialect}</b> placeholder
     */
    public void addPostCreateScriptUrl(String postUpdateScriptUrl)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered create script URL (post-Hibernate): " + postUpdateScriptUrl);
        }
        this.postCreateScriptUrls.add(postUpdateScriptUrl);
    }

    /**
     * Register a new SQL-based patch for consideration against the instance (before Hibernate execution)
     *  
     * @param scriptPatch                   the patch that will be examined for execution
     */
    public void addPreUpdateScriptPatch(SchemaUpgradeScriptPatch scriptPatch)
    {
        if(false == scriptPatch.isIgnored())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Registered script patch (pre-Hibernate): " + scriptPatch.getId());
            }
            this.preUpdateScriptPatches.add(scriptPatch);
        }
        else
        {
            logger.info("Ignoring script patch (pre-Hibernate): " + scriptPatch.getId());
        }
        
    }

    /**
     * Register a new SQL-based patch for consideration against the instance (after Hibernate execution)
     *  
     * @param scriptPatch                   the patch that will be examined for execution
     */
    public void addPostUpdateScriptPatch(SchemaUpgradeScriptPatch scriptPatch)
    {
        if(false == scriptPatch.isIgnored())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Registered script patch (post-Hibernate): " + scriptPatch.getId());
            }
            this.postUpdateScriptPatches.add(scriptPatch);
        }
        else
        {
            logger.info("Ignoring script patch (post-Hibernate): " + scriptPatch.getId());
        }
    }

    /**
     * Register a new SQL-based patch for consideration against the Activiti instance
     *  
     * @param scriptPatch                   the patch that will be examined for execution
     */
    public void addUpdateActivitiScriptPatch(SchemaUpgradeScriptPatch scriptPatch)
    {
        if(false == scriptPatch.isIgnored())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Registered Activiti script patch: " + scriptPatch.getId());
            }
            this.updateActivitiScriptPatches.add(scriptPatch);
        }
        else
        {
            logger.info("Ignoring Activiti script patch: " + scriptPatch.getId());
        }
        
    }

    private static class NoSchemaException extends Exception
    {
        private static final long serialVersionUID = 5574280159910824660L;
    }

    /**
     * Used to indicate a forced stop of the bootstrap.
     * 
     * @see SchemaBootstrap#setStopAfterSchemaBootstrap(boolean)
     * 
     * @author Derek Hulley
     * @since 3.1.1
     */
    private static class BootstrapStopException extends RuntimeException
    {
        private static final long serialVersionUID = 4250016675538442181L;
        private BootstrapStopException()
        {
            super(I18NUtil.getMessage(ERR_FORCED_STOP));
        }
    }
    
    /**
     * Count applied patches.  This fails if multiple applied patch tables are found,
     * which normally indicates that the schema view needs to be limited.
     * 
     * @param cfg           The Hibernate config
     * @param connection    a valid database connection
     * @return Returns the number of applied patches
     * @throws NoSchemaException if the table of applied patches can't be found
     */
    private int countAppliedPatches(Configuration cfg, Connection connection) throws Exception
    {
        String defaultSchema = DatabaseMetaDataHelper.getSchema(connection);
        if (defaultSchema != null && defaultSchema.length() == 0)
        {
            defaultSchema = null;
        }
        String defaultCatalog = cfg.getProperty("hibernate.default_catalog");
        if (defaultCatalog != null && defaultCatalog.length() == 0)
        {
            defaultCatalog = null;
        }
        DatabaseMetaData dbMetadata = connection.getMetaData();
        
        ResultSet tableRs = dbMetadata.getTables(defaultCatalog, defaultSchema, "%", null);
        boolean newPatchTable = false;
        boolean oldPatchTable = false;
        try
        {
            boolean multipleSchemas = false;
            while (tableRs.next())
            {
                String tableName = tableRs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase("applied_patch"))
                {
                    if (oldPatchTable || newPatchTable)
                    {
                        // Found earlier
                        multipleSchemas = true;
                    }
                    oldPatchTable = true;
                }
                else if (tableName.equalsIgnoreCase("alf_applied_patch"))
                {
                    if (oldPatchTable || newPatchTable)
                    {
                        // Found earlier
                        multipleSchemas = true;
                    }
                    newPatchTable = true;
                }
            }
            // We go through all the tables so that multiple visible schemas are detected
            if (multipleSchemas)
            {
                throw new AlfrescoRuntimeException(ERR_MULTIPLE_SCHEMAS);
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
            catch (SQLException e)
            {
                // This should work at least and is probably an indication of the user viewing multiple schemas
                throw new AlfrescoRuntimeException(ERR_MULTIPLE_SCHEMAS);
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
     * Check whether Activiti tables already created in db.
     * 
     * @param connection    a valid database connection
     * @return <code>true</code> if Activiti tables already created in schema, otherwise <code>false</code>
     */
    private boolean checkActivitiTablesExist(Connection connection)
    {
        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            stmt.executeQuery("select min(id_) from ACT_RU_TASK");
            return true;
        }
        catch (SQLException e)
        {
            logger.debug("Did not find ACT_RU_TASK table.");
            return false;
        }
        finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            }
            catch (Throwable e) {}
        }
    }
    
    /**
     * @return  Returns the name of the applied patch table, or <tt>null</tt> if the table doesn't exist
     */
    private String getAppliedPatchTableName(Connection connection) throws Exception
    {
        Statement stmt = connection.createStatement();
        try
        {
            stmt.executeQuery("select * from alf_applied_patch");
            return "alf_applied_patch";
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
            stmt.executeQuery("select * from applied_patch");
            return "applied_patch";
        }
        catch (Throwable e)
        {
            // It is not there
            return null;
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * @return Returns the number of applied patches
     */
    private boolean didPatchSucceed(Connection connection, String patchId, boolean alternative) throws Exception
    {
        String patchTableName = getAppliedPatchTableName(connection);
        if (patchTableName == null)
        {
            // Table doesn't exist, yet
            return false;
        }
        Statement stmt = connection.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery("select succeeded, was_executed from " + patchTableName + " where id = '" + patchId + "'");
            if (!rs.next())
            {
                return false;
            }
            boolean succeeded = rs.getBoolean(1);
            boolean wasExecuted = rs.getBoolean(2);
            
            if (alternative)
            {
                return succeeded && wasExecuted;
            }
            else
            {
                return succeeded;
            }
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Finds the <b>version.properties</b> file and determines the installed <b>version.schema</b>.<br>
     * The only way to determine the original installed schema number is by quering the for the minimum value in
     * <b>alf_applied_patch.applied_to_schema</b>.  This might not work if an upgrade is attempted straight from
     * Alfresco v1.0!
     * 
     * @return          the installed schema number or <tt>-1</tt> if the installation is new.
     */
    private int getInstalledSchemaNumber(Connection connection) throws Exception
    {
        Statement stmt = connection.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery(
                    "select min(applied_to_schema) from alf_applied_patch where applied_to_schema > -1");
            if (!rs.next())
            {
                // Nothing in the table
                return -1;
            }
            if (rs.getObject(1) == null)
            {
                // Nothing in the table
                return -1;
            }
            int installedSchema = rs.getInt(1);
            return installedSchema;
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }
    
    private static class LockFailedException extends Exception
    {
        private static final long serialVersionUID = -6676398230191205456L;
    }
    
    
    /**
     * Records that the bootstrap process has started
     */
    private synchronized void setBootstrapStarted(Connection connection) throws Exception
    {
        // Create the marker table
        Statement stmt = connection.createStatement();
        try
        {
            stmt.executeUpdate("create table alf_bootstrap_lock (charval CHAR(1) NOT NULL)");
            // Success
            return;
        }
        catch (Throwable e)
        {
            // We throw a well-known exception to be handled by retrying code if required
            throw new LockFailedException();
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Records that the bootstrap process has finished
     */
    private void setBootstrapCompleted(Connection connection) throws Exception
    {
        // Create the marker table
        Statement stmt = connection.createStatement();
        try
        {
            stmt.executeUpdate("drop table alf_bootstrap_lock");

            // from Thor
            executedStatementsThreadLocal.set(null);
        }
        catch (Throwable e)
        {
            // Table exists
            throw AlfrescoRuntimeException.create(ERR_PREVIOUS_FAILED_BOOTSTRAP);
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Builds the schema from scratch or applies the necessary patches to the schema.
     */
    private boolean updateSchema(Configuration cfg, Session session, Connection connection) throws Exception
    {
        boolean create = false;
        try
        {
            countAppliedPatches(cfg, connection);
        }
        catch (NoSchemaException e)
        {
            create = true;
        }
        // Get the dialect
        final Dialect dialect = Dialect.getDialect(cfg.getProperties());
        String dialectStr = dialect.getClass().getSimpleName();

        if (create)
        {
            long start = System.currentTimeMillis();
            
            // execute pre-create scripts (not patches)
            for (String scriptUrl : this.preCreateScriptUrls)
            {
                executeScriptUrl(cfg, connection, scriptUrl);
            }
            // Build and execute changes generated by Hibernate
            File tempFile = null;
            Writer writer = null;
            try
            {
                DatabaseMetadata metadata = new DatabaseMetadata(connection, dialect);
                String[] sqls = cfg.generateSchemaUpdateScript(dialect, metadata);
                if (sqls.length > 0)
                {
                    tempFile = TempFileProvider.createTempFile("AlfrescoSchema-" + dialectStr + "-Update-", ".sql");
                    writer = new BufferedWriter(new FileWriter(tempFile));
                    for (String sql : sqls)
                    {
                        writer.append(sql);
                        writer.append(";\n");
                    }
                    try {writer.close();} catch (Throwable e) {}
                    executeScriptFile(cfg, connection, tempFile, null);
                }
            }
            finally
            {
                if (writer != null)
                {
                    try {writer.close();} catch (Throwable e) {}
                }
            }
            // execute post-create scripts (not patches)
            for (String scriptUrl : this.postCreateScriptUrls)
            {
                executeScriptUrl(cfg, connection, scriptUrl);
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Create scripts executed in "+(System.currentTimeMillis()-start)+" ms");
            }
        }
        else
        {
            // Execute any pre-auto-update scripts
            checkSchemaPatchScripts(cfg, connection, preUpdateScriptPatches, true);
            
            // Build and execute changes generated by Hibernate
            File tempFile = null;
            Writer writer = null;
            try
            {
                DatabaseMetadata metadata = new DatabaseMetadata(connection, dialect);
                String[] sqls = cfg.generateSchemaUpdateScript(dialect, metadata);
                if (sqls.length > 0)
                {
                    tempFile = TempFileProvider.createTempFile("AlfrescoSchema-" + dialectStr + "-Update-", ".sql");
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
                executeScriptFile(cfg, connection, tempFile, null);
            }
            
            // Execute any post-auto-update scripts
            checkSchemaPatchScripts(cfg, connection, postUpdateScriptPatches, true);
        }

        // Initialise Activiti DB, using an unclosable connection
        boolean activitiTablesExist = checkActivitiTablesExist(connection);
        if(!activitiTablesExist)
        {
            // Activiti DB updates are performed as patches in alfresco, only give
            // control to activiti when creating new one.
            initialiseActivitiDBSchema(new UnclosableConnection(connection));
            
            // ALF-18996: Upgrade from 3.4.12 to 4.2.0 fails: Activiti tables have not been bootstrapped
            // The Activiti bootstrap is effectively doing the work of all the other patches,
            // which should be considered complete.
            int installedSchemaNumber = getInstalledSchemaNumber(connection);
            for (Patch activitiScriptPatch : updateActivitiScriptPatches)
            {
                AppliedPatch appliedPatch = new AppliedPatch();
                appliedPatch.setId(activitiScriptPatch.getId());
                appliedPatch.setDescription(activitiScriptPatch.getDescription());
                appliedPatch.setFixesFromSchema(activitiScriptPatch.getFixesFromSchema());
                appliedPatch.setFixesToSchema(activitiScriptPatch.getFixesToSchema());
                appliedPatch.setTargetSchema(activitiScriptPatch.getTargetSchema());
                appliedPatch.setAppliedToSchema(installedSchemaNumber);
                appliedPatch.setAppliedToServer("UNKNOWN");
                appliedPatch.setAppliedOnDate(new Date());                   // the date applied
                appliedPatch.setSucceeded(true);
                appliedPatch.setWasExecuted(false);
                appliedPatch.setReport("Placeholder for Activiti bootstrap at schema " + installedSchemaNumber);
                appliedPatchDAO.createAppliedPatch(appliedPatch);
            }
        }
        else
        {
            // Execute any auto-update scripts for Activiti tables
            checkSchemaPatchScripts(cfg, connection, updateActivitiScriptPatches, true);

            // verify that all Activiti patches have been applied correctly
            checkSchemaPatchScripts(cfg, connection, updateActivitiScriptPatches, false);
        }
        
        return create;
    }
    
    /**
     * Initialises the Activiti DB schema, if not present it's created.
     * 
     * @param connection Connection to use the initialise DB schema
     */
    private void initialiseActivitiDBSchema(Connection connection)
    {
        // create instance of activiti engine to initialise schema
        ProcessEngine engine = null;
        ProcessEngineConfiguration engineConfig = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        try
        {
            // build the engine
            engine = engineConfig.setDataSource(dataSource).
                setDatabaseSchemaUpdate("none").
                setProcessEngineName("activitiBootstrapEngine").
                setHistory("full").
                setJobExecutorActivate(false).
                buildProcessEngine();
        
            // create or upgrade the DB schema
            engine.getManagementService().databaseSchemaUpgrade(connection, null, DatabaseMetaDataHelper.getSchema(connection));
        }
        finally
        {
            if (engine != null)
            {
                // close the process engine
                engine.close();
            }
        }
    }
    
    /**
     * Check that the necessary scripts have been executed against the database
     */
    private void checkSchemaPatchScripts(
            Configuration cfg,
            Connection connection,
            List<SchemaUpgradeScriptPatch> scriptPatches,
            boolean apply) throws Exception
    {
        // first check if there have been any applied patches
        int appliedPatchCount = countAppliedPatches(cfg, connection);
        if (appliedPatchCount == 0)
        {
            // This is a new schema, so upgrade scripts are irrelevant
            // and patches will not have been applied yet
            return;
        }
        // Retrieve the first installed schema number
        int installedSchema = getInstalledSchemaNumber(connection);

        nextPatch:
        for (SchemaUpgradeScriptPatch patch : scriptPatches)
        {
            final String patchId = patch.getId();
            final String scriptUrl = patch.getScriptUrl();
            
            // Check if any of the alternative patches were executed
            List<Patch> alternatives = patch.getAlternatives();
            for (Patch alternativePatch : alternatives)
            {
                String alternativePatchId = alternativePatch.getId();
                boolean alternativeSucceeded = didPatchSucceed(connection, alternativePatchId, true);
                if (alternativeSucceeded)
                {
                    continue nextPatch;
                }
            }

            // check if the script was successfully executed
            boolean wasSuccessfullyApplied = didPatchSucceed(connection, patchId, false);
            if (wasSuccessfullyApplied)
            {
                // Either the patch was executed before or the system was bootstrapped
                // with the patch bean present.
                continue;
            }
            else if (!patch.applies(installedSchema))
            {
                // Patch does not apply to the installed schema number
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
        String dialectStr = dialect.getClass().getSimpleName();
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
            tempFile = TempFileProvider.createTempFile("AlfrescoSchema-" + dialectStr + "-Update-", ".sql");
            ContentWriter writer = new FileContentWriter(tempFile);
            writer.putContent(scriptInputStream);
        }
        finally
        {
            try { scriptInputStream.close(); } catch (Throwable e) {}  // usually a duplicate close
        }
        // now execute it
        String dialectScriptUrl = scriptUrl.replaceAll(PLACEHOLDER_DIALECT, dialect.getClass().getName());
        // Replace the script placeholders
        executeScriptFile(cfg, connection, tempFile, dialectScriptUrl);
    }
    
    /**
     * Replaces the dialect placeholder in the resource URL and attempts to find a file for
     * it.  If not found, the dialect hierarchy will be walked until a compatible resource is
     * found.  This makes it possible to have resources that are generic to all dialects.
     * 
     * @return The Resource, otherwise null
     */
    private Resource getDialectResource(Class<?> dialectClass, String resourceUrl)
    {
        // replace the dialect placeholder
        String dialectResourceUrl = resolveDialectUrl(dialectClass, resourceUrl);
        // get a handle on the resource
        Resource resource = rpr.getResource(dialectResourceUrl);
        if (!resource.exists())
        {
            // it wasn't found.  Get the superclass of the dialect and try again
            Class<?> superClass = dialectClass.getSuperclass();
            if (Dialect.class.isAssignableFrom(superClass))
            {
                // we still have a Dialect - try again
                return getDialectResource(superClass, resourceUrl);
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
            return resource;
        }
    }

    /**
     * Takes resource URL containing the {@link SchemaBootstrap#PLACEHOLDER_DIALECT dialect placeholder text}
     * and substitutes the placeholder with the name of the given dialect's class.
     * <p/>
     * For example:
     * <pre>
     *   resolveDialectUrl(MySQLInnoDBDialect.class, "classpath:alfresco/db/${db.script.dialect}/myfile.xml")
     * </pre>
     * would give the following String:
     * <pre>
     *   classpath:alfresco/db/org.hibernate.dialect.MySQLInnoDBDialect/myfile.xml
     * </pre>
     */
    private String resolveDialectUrl(Class<?> dialectClass, String resourceUrl)
    {
        return resourceUrl.replaceAll(PLACEHOLDER_DIALECT, dialectClass.getName());
    }
    
    /**
     * Replaces the dialect placeholder in the script URL and attempts to find a file for
     * it.  If not found, the dialect hierarchy will be walked until a compatible script is
     * found.  This makes it possible to have scripts that are generic to all dialects.
     * 
     * @return Returns an input stream onto the script, otherwise null
     */
    private InputStream getScriptInputStream(Class<?> dialectClazz, String scriptUrl) throws Exception
    {
        Resource resource = getDialectResource(dialectClazz, scriptUrl);
        if (resource == null)
        {
            throw new AlfrescoRuntimeException("Script [ " + scriptUrl + " ] can't be found for " + dialectClazz);
        }
        return resource.getInputStream();
    }
    
    /**
     * @param cfg           the Hibernate configuration
     * @param connection    the DB connection to use
     * @param scriptFile    the file containing the statements
     * @param scriptUrl     the URL of the script to report.  If this is null, the script
     *                      is assumed to have been auto-generated.
     */
    private void executeScriptFile(
            Configuration cfg,
            Connection connection,
            File scriptFile,
            String scriptUrl) throws Exception
    {
        final Dialect dialect = Dialect.getDialect(cfg.getProperties());
        
        StringBuilder executedStatements = executedStatementsThreadLocal.get();
        if (executedStatements == null)
        {
            // Validate the schema, pre-upgrade
            validateSchema("Alfresco-{0}-Validation-Pre-Upgrade-{1}-", null);
            
            dumpSchema("pre-upgrade");

            // There is no lock at this stage.  This process can fall out if the lock can't be applied.
            setBootstrapStarted(connection);
            executedStatements = new StringBuilder(8094);
            executedStatementsThreadLocal.set(executedStatements);
        }
        
        if (scriptUrl == null)
        {
            LogUtil.info(logger, MSG_EXECUTING_GENERATED_SCRIPT, scriptFile);
        }
        else
        {
            LogUtil.info(logger, MSG_EXECUTING_COPIED_SCRIPT, scriptFile, scriptUrl);
        }
        
        InputStream scriptInputStream = new FileInputStream(scriptFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(scriptInputStream, "UTF-8"));
        try
        {
            int line = 0;
            // loop through all statements
            StringBuilder sb = new StringBuilder(1024);
            String fetchVarName = null;
            String fetchColumnName = null;
            boolean doBatch = false;
            int batchUpperLimit = 0;
            int batchSize = 1;
            Map<String, Object> varAssignments = new HashMap<String, Object>(13);
            // Special variable assignments:
            if (dialect instanceof PostgreSQLDialect)
            {
                // Needs 1/0 for true/false
                varAssignments.put("true", "true");
                varAssignments.put("false", "false");
                varAssignments.put("TRUE", "TRUE");
                varAssignments.put("FALSE", "FALSE");
            }
            else
            {
                // Needs true/false as strings
                varAssignments.put("true", "1");
                varAssignments.put("false", "0");
                varAssignments.put("TRUE", "1");
                varAssignments.put("FALSE", "0");
            }
            long now = System.currentTimeMillis();
            varAssignments.put("now", new Long(now).toString());
            varAssignments.put("NOW", new Long(now).toString());
            
            while(true)
            {
                String sqlOriginal = reader.readLine();
                line++;
                
                if (sqlOriginal == null)
                {
                    // nothing left in the file
                    break;
                }
                
                // trim it
                String sql = sqlOriginal.trim();
                // Check of includes
                if (sql.startsWith("--INCLUDE:"))
                {
                    if (sb.length() > 0)
                    {
                        // This can only be set before a new SQL statement
                        throw AlfrescoRuntimeException.create(ERR_STATEMENT_INCLUDE_BEFORE_SQL, (line - 1), scriptUrl);
                    }
                    String includedScriptUrl = sql.substring(10, sql.length());
                    // Execute the script in line
                    executeScriptUrl(cfg, connection, includedScriptUrl);
                }
                // Check for variable assignment
                else if (sql.startsWith("--ASSIGN:"))
                {
                    if (sb.length() > 0)
                    {
                        // This can only be set before a new SQL statement
                        throw AlfrescoRuntimeException.create(ERR_STATEMENT_VAR_ASSIGNMENT_BEFORE_SQL, (line - 1), scriptUrl);
                    }
                    String assignStr = sql.substring(9, sql.length());
                    String[] assigns = assignStr.split("=");
                    if (assigns.length != 2 || assigns[0].length() == 0 || assigns[1].length() == 0)
                    {
                        throw AlfrescoRuntimeException.create(ERR_STATEMENT_VAR_ASSIGNMENT_FORMAT, (line - 1), scriptUrl);
                    }
                    fetchVarName = assigns[0];
                    fetchColumnName = assigns[1];
                    continue;
                }
				// Handle looping control
                else if (sql.startsWith("--FOREACH"))
                {
                    // --FOREACH table.column batch.size.property
                    String[] args = sql.split("[ \\t]+");
                    int sepIndex;
                    if (args.length == 3 && (sepIndex = args[1].indexOf('.')) != -1)
                    {
                        doBatch = true;
						// Select the upper bound of the table column
                        String stmt = "SELECT MAX(" + args[1].substring(sepIndex+1) + ") AS upper_limit FROM " + args[1].substring(0, sepIndex);
                        Object fetchedVal = executeStatement(connection, stmt, "upper_limit", false, line, scriptFile);                        
                        if (fetchedVal instanceof Number)
                        {
                            batchUpperLimit = ((Number)fetchedVal).intValue();
							// Read the batch size from the named property
                            String batchSizeString = globalProperties.getProperty(args[2]);
                            // Fall back to the default property
                            if (batchSizeString == null)
                            {
                                batchSizeString = globalProperties.getProperty(PROPERTY_DEFAULT_BATCH_SIZE);
                            }
                            batchSize = batchSizeString == null ? 10000 : Integer.parseInt(batchSizeString);
                        }
                    }
                    continue;
                }
                // Allow transaction delineation
                else if (sql.startsWith("--BEGIN TXN"))
                {
                   connection.setAutoCommit(false);
                   continue;
                }
                else if (sql.startsWith("--END TXN"))
                {
                   connection.commit();
                   connection.setAutoCommit(true);
                   continue;
                }

                // Check for comments
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
                else if (sql.endsWith("(optional)") || sql.endsWith("(OPTIONAL)"))
                {
                    // Get the end of statement
                    int endIndex = sql.lastIndexOf(';');
                    if (endIndex > -1)
                    {
                        sql = sql.substring(0, endIndex);
                        execute = true;
                        optional = true;
                    }
                    else
                    {
                        // Ends with "(optional)" but there is no semi-colon.
                        // Just take it at face value and probably fail.
                    }
                }
                // Add newline
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                // Add leading whitespace for formatting
                int whitespaceCount = sqlOriginal.indexOf(sql);
                for (int i = 0; i < whitespaceCount; i++)
                {
                    sb.append(" ");
                }
                // append to the statement being built up
                sb.append(sql);
                // execute, if required
                if (execute)
                {
                    // Now substitute and execute the statement the appropriate number of times
                    String unsubstituted = sb.toString();
                    for(int lowerBound = 0; lowerBound <= batchUpperLimit; lowerBound += batchSize)
                    {
                        sql = unsubstituted;
                        
                        // Substitute in the next pair of range parameters
                        if (doBatch)
                        {
                            varAssignments.put("LOWERBOUND", String.valueOf(lowerBound));
                            varAssignments.put("UPPERBOUND", String.valueOf(lowerBound + batchSize - 1));
                        }
                        
                        // Perform variable replacement using the ${var} format
                        for (Map.Entry<String, Object> entry : varAssignments.entrySet())
                        {
                            String var = entry.getKey();
                            Object val = entry.getValue();
                            sql = sql.replaceAll("\\$\\{" + var + "\\}", val.toString());
                        }
                        
                        // Handle the 0/1 values that PostgreSQL doesn't translate to TRUE
                        if (this.dialect != null && this.dialect instanceof PostgreSQLDialect)
                        {
                            sql = sql.replaceAll("\\$\\{TRUE\\}", "TRUE");
                        }
                        else
                        {
                            sql = sql.replaceAll("\\$\\{TRUE\\}", "1");
                        }
                        
                        if (this.dialect != null && this.dialect instanceof MySQLInnoDBDialect)
                        {
                        	// note: enable bootstrap on MySQL 5.5 (eg. for auto-generated SQL, such as JBPM)
                            sql = sql.replaceAll("(?i)TYPE=InnoDB", "ENGINE=InnoDB");
                        }
                        
                        Object fetchedVal = executeStatement(connection, sql, fetchColumnName, optional, line, scriptFile);
                        if (fetchVarName != null && fetchColumnName != null)
                        {
                            varAssignments.put(fetchVarName, fetchedVal);
                        }                        
                    }                        
                    sb.setLength(0);
                    fetchVarName = null;
                    fetchColumnName = null;
                    doBatch = false;
                    batchUpperLimit = 0;
                    batchSize = 1;                    
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
     * 
     * @param fetchColumnName       the name of the column value to return
     */
    private Object executeStatement(
            Connection connection,
            String sql,
            String fetchColumnName,
            boolean optional,
            int line,
            File file) throws Exception
    {
        StringBuilder executedStatements = executedStatementsThreadLocal.get();
        if (executedStatements == null)
        {
            throw new IllegalArgumentException("The executedStatementsThreadLocal must be populated");
        }

        Statement stmt = connection.createStatement();
        Object ret = null;
        try
        {
            if (logger.isDebugEnabled())
            {
                LogUtil.debug(logger, MSG_EXECUTING_STATEMENT, sql);
            }
            boolean haveResults = stmt.execute(sql);
            // Record the statement
            executedStatements.append(sql).append(";\n\n");
            if (haveResults && fetchColumnName != null)
            {
                ResultSet rs = stmt.getResultSet();
                if (rs.next())
                {
                    // Get the result value
                    ret = rs.getObject(fetchColumnName);
                }
            }
        }
        catch (SQLException e)
        {
            if (optional)
            {
                // it was marked as optional, so we just ignore it
                LogUtil.debug(logger, MSG_OPTIONAL_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
            }
            else
            {
                LogUtil.error(logger, ERR_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
                throw e;
            }
        }
        finally
        {
            try { stmt.close(); } catch (Throwable e) {}
        }
        return ret;
    }
    
    /**
     * Performs dialect-specific checking.  This includes checking for InnoDB, dumping the dialect being used
     * as well as setting any runtime, dialect-specific properties.
     */
    private void checkDialect(Dialect dialect)
    {
        Class<?> dialectClazz = dialect.getClass();
        LogUtil.info(logger, MSG_DIALECT_USED, dialectClazz.getName());
        if (dialectClazz.equals(MySQLDialect.class) || dialectClazz.equals(MySQL5Dialect.class))
        {
            LogUtil.error(logger, ERR_DIALECT_SHOULD_USE, dialectClazz.getName(), MySQLInnoDBDialect.class.getName());
            throw AlfrescoRuntimeException.create(WARN_DIALECT_UNSUPPORTED, dialectClazz.getName());
        }
        else if (dialectClazz.equals(HSQLDialect.class))
        {
            LogUtil.info(logger, WARN_DIALECT_HSQL);
        }
        else if (dialectClazz.equals(DerbyDialect.class))
        {
            LogUtil.info(logger, WARN_DIALECT_DERBY);
        }
        else if (dialectClazz.equals(Oracle9iDialect.class) || dialectClazz.equals(Oracle10gDialect.class))
        {
            LogUtil.error(logger, ERR_DIALECT_SHOULD_USE, dialectClazz.getName(), AlfrescoOracle9Dialect.class.getName());
            throw AlfrescoRuntimeException.create(WARN_DIALECT_UNSUPPORTED, dialectClazz.getName());
        }
        else if (dialectClazz.equals(OracleDialect.class) || dialectClazz.equals(Oracle9Dialect.class))
        {
            LogUtil.error(logger, ERR_DIALECT_SHOULD_USE, dialectClazz.getName(), AlfrescoOracle9Dialect.class.getName());
            throw AlfrescoRuntimeException.create(WARN_DIALECT_UNSUPPORTED, dialectClazz.getName());
        }
        
        int maxStringLength = SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH;
        int serializableType = SerializableTypeHandler.getSerializableType();
        // Adjust the maximum allowable String length according to the dialect
        if (dialect instanceof AlfrescoSQLServerDialect)
        {
            // string_value nvarchar(1024) null,
            // serializable_value image null,
            maxStringLength = SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH;
        }
        else if (dialect instanceof AlfrescoSybaseAnywhereDialect)
        {
            // string_value text null,
            // serializable_value varbinary(8192) null,
            maxStringLength = Integer.MAX_VALUE;
        }
        else if (dialect instanceof DB2Dialect)
        {
            // string_value varchar(1024),
            // serializable_value varchar(8192) for bit data,
            maxStringLength = SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH;
            serializableType = Types.BLOB;
        }
        else if (dialect instanceof HSQLDialect)
        {
            // string_value varchar(1024),
            // serializable_value varbinary(8192),
            maxStringLength = SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH;
        }
        else if (dialect instanceof MySQLInnoDBDialect)
        {
            // string_value text,
            // serializable_value blob,
            maxStringLength = Integer.MAX_VALUE;
        }
        else if (dialect instanceof AlfrescoOracle9Dialect)
        {
            // string_value varchar2(1024 char),
            // serializable_value blob,
            maxStringLength = SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH;
        }
        else if (dialect instanceof PostgreSQLDialect)
        {
            // string_value varchar(1024),
            // serializable_value bytea,
            maxStringLength = SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH;
        }
        SchemaBootstrap.setMaxStringLength(maxStringLength);
        SerializableTypeHandler.setSerializableType(serializableType);
        
        // Now override the maximum string length if it was set directly
        if (maximumStringLength > 0)
        {
            SchemaBootstrap.setMaxStringLength(maximumStringLength);
        }
    }
    
    @Override
    public synchronized void onBootstrap(ApplicationEvent event)
    {
        if (event != null)
        {
            // Use the application context to load resources
            rpr = (ApplicationContext)event.getSource();
        }
        
        // do everything in a transaction
        Session session = getSessionFactory().openSession();
        Connection connection = null;
        try
        {
            // make sure that we AUTO-COMMIT
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            LogUtil.info(logger, MSG_DATABASE_USED, connection);
            
            Configuration cfg = localSessionFactory.getConfiguration();
            
            // Check and dump the dialect being used
            checkDialect(this.dialect);
            
            // Ensure that our static connection provider is used
            String defaultConnectionProviderFactoryClass = cfg.getProperty(Environment.CONNECTION_PROVIDER);
            cfg.setProperty(Environment.CONNECTION_PROVIDER, SchemaBootstrapConnectionProvider.class.getName());
            SchemaBootstrapConnectionProvider.setBootstrapConnection(connection);
            
            // Update the schema, if required.
            if (updateSchema)
            {
                // Retries are required here as the DB lock will be applied lazily upon first statement execution.
                // So if the schema is up to date (no statements executed) then the LockFailException cannot be
                // thrown.  If it is thrown, the the update needs to be rerun as it will probably generate no SQL
                // statements the second time around.
                boolean updatedSchema = false;
                boolean createdSchema = false;
                
                for (int i = 0; i < schemaUpdateLockRetryCount; i++)
                {
                    try
                    {
                        createdSchema = updateSchema(cfg, session, connection);
                        updatedSchema = true;
                        break;
                    }
                    catch (LockFailedException e)
                    {
                        try { this.wait(schemaUpdateLockRetryWaitSeconds * 1000L); } catch (InterruptedException ee) {}
                    }
                }
                
                if (!updatedSchema)
                {
                    // The retries were exceeded
                    throw new AlfrescoRuntimeException(ERR_PREVIOUS_FAILED_BOOTSTRAP);
                }
                
                // Copy the executed statements to the output file
                File schemaOutputFile = null;
                if (schemaOuputFilename != null)
                {
                    schemaOutputFile = new File(schemaOuputFilename);
                }
                else
                {
                    schemaOutputFile = TempFileProvider.createTempFile(
                            "AlfrescoSchema-" + this.dialect.getClass().getSimpleName() + "-All_Statements-",
                            ".sql");
                }
                
                StringBuilder executedStatements = executedStatementsThreadLocal.get();
                if (executedStatements == null)
                {
                    LogUtil.info(logger, MSG_NO_CHANGES);
                }
                else
                {
                    FileContentWriter writer = new FileContentWriter(schemaOutputFile);
                    writer.setEncoding("UTF-8");
                    String executedStatementsStr = executedStatements.toString();
                    writer.putContent(executedStatementsStr);
                    LogUtil.info(logger, MSG_ALL_STATEMENTS, schemaOutputFile.getPath());
                }
                
                if (! createdSchema)
                {
                    // verify that all patches have been applied correctly 
                    checkSchemaPatchScripts(cfg, connection, preUpdateScriptPatches, false);       // check scripts
                    checkSchemaPatchScripts(cfg, connection, postUpdateScriptPatches, false);      // check scripts
                }
                
                if (executedStatements != null)
                {
                    // Remove the flag indicating a running bootstrap
                    setBootstrapCompleted(connection);
                }
                
                
                // Report normalized dumps
                if (executedStatements != null)
                {
                    // Validate the schema, post-upgrade
                    validateSchema("Alfresco-{0}-Validation-Post-Upgrade-{1}-", null);
                    
                    // 4.0+ schema dump
                    dumpSchema("post-upgrade");
                }
            }
            else
            {
                LogUtil.info(logger, MSG_BYPASSING_SCHEMA_UPDATE);
            }
            
            if (stopAfterSchemaBootstrap)
            {
                // 4.0+ schema dump
                dumpSchema("forced-exit");
                LogUtil.error(logger, ERR_FORCED_STOP);
                throw new BootstrapStopException();
            }

            // Reset the configuration
            cfg.setProperty(Environment.CONNECTION_PROVIDER, defaultConnectionProviderFactoryClass);

            if (event != null)
            {
                // all done successfully
                ((ApplicationContext) event.getSource()).publishEvent(new SchemaAvailableEvent(this));
            }
        }
        catch (BootstrapStopException e)
        {
            // We just let this out
            throw e;
        }
        catch (Throwable e)
        {
            LogUtil.error(logger, e, ERR_UPDATE_FAILED);
            if (updateSchema)
            {
                throw new AlfrescoRuntimeException(ERR_UPDATE_FAILED, e);
            }
            else
            {
                throw new AlfrescoRuntimeException(ERR_VALIDATION_FAILED, e);
            }
        }
        finally
        {
            try
            {
                if (connection != null)
                {
                    connection.close();
                }
            }
            catch (Throwable e)
            {
                logger.warn("Error closing DB connection: " + e.getMessage());
            }
            // Remove the connection reference from the threadlocal boostrap
            SchemaBootstrapConnectionProvider.setBootstrapConnection(null);
            
        }
    }
    
    /**
     * Collate differences and validation problems with the schema with respect to an appropriate
     * reference schema.
     * 
     * @param outputFileNameTemplate
     * @return the number of potential problems found.
     */
    public synchronized int validateSchema(String outputFileNameTemplate, PrintWriter out)
    {
        int totalProblems = 0;
        
        // Discover available reference files (e.g. for prefixes alf_, avm_ etc.)
        // and process each in turn.
        for (String schemaReferenceUrl : schemaReferenceUrls)
        {
            Resource referenceResource = getDialectResource(dialect.getClass(), schemaReferenceUrl);
            
            if (referenceResource == null || !referenceResource.exists())
            {
                String resourceUrl = resolveDialectUrl(dialect.getClass(), schemaReferenceUrl);
                LogUtil.debug(logger, DEBUG_SCHEMA_COMP_NO_REF_FILE, resourceUrl);
            }
            else
            {
                // Validate schema against each reference file
                int problems = validateSchema(referenceResource, outputFileNameTemplate, out);
                totalProblems += problems;
            }
        }
        
        // Return number of problems found across all reference files.
        return totalProblems;
    }
    
    private int validateSchema(Resource referenceResource, String outputFileNameTemplate, PrintWriter out)
    {
        try
        {
            return attemptValidateSchema(referenceResource, outputFileNameTemplate, out);
        }
        catch (Throwable e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Unable to validate database schema.", e);
            }
            return 0;
        }
    }

    /**
     * Validates and compares current DB schema with schema reference definition, specified in <code>referenceResource</code> parameter.<br />
     * <br />
     * The method supports two mechanisms to report validation results:
     * <ol>
     * <li>using an external output stream, specified as <code>out</code>;</li>
     * <li>using specially created {@link FileOutputStream}, which represents temporary file with name, formatted in accordance with <code>outputFileNameTemplate</code> template.</li>
     * </ol>
     * It is necessary to take care about freeing resources of output stream in case of the 1st approach.<br />
     * <b>N.B.:</b> The method only writes messages of the report. And it <b>doesn't flush and doesn't close</b> the specified output stream!<br />
     * <br />
     * 
     * @param referenceResource - {@link Resource} instance, which determines file of reference schema
     * @param outputFileNameTemplate - {@link String} value, which determines template of temporary filename for validation report. <b>It can't be <code>null</code> if
     *        <code>out</code> is <code>null</code></b>!
     * @param out - {@link PrintWriter} instance, which represents an external output stream for writing a validation report. This stream is never closed or flushed. <b>It can't be
     *        <code>null</code> if <code>outputFileNameTemplate</code> is <code>null</code></b>!
     * @return {@link Integer} value, which determines amount of errors or warnings that were detected during validation
     */
    private int attemptValidateSchema(Resource referenceResource, String outputFileNameTemplate, PrintWriter out)
    {
        Date startTime = new Date(); 
        
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(referenceResource.getInputStream());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to open schema reference file: " + referenceResource);
        }
        
        XMLToSchema xmlToSchema = new XMLToSchema(is);
        xmlToSchema.parse();
        Schema reference = xmlToSchema.getSchema();
        
        ExportDb exporter = new ExportDb(dataSource, dialect, descriptorService);
        // Ensure that the database objects we're validating are filtered
        // by the same prefix as the reference file.  
        exporter.setNamePrefix(reference.getDbPrefix());
        exporter.execute();
        Schema target = exporter.getSchema();
        
        SchemaComparator schemaComparator = new SchemaComparator(reference, target, dialect);
        
        schemaComparator.validateAndCompare();
        
        Results results = schemaComparator.getComparisonResults();
        
        Object[] outputFileNameParams = new Object[]
        {
                    dialect.getClass().getSimpleName(),
                    reference.getDbPrefix()
        };
        PrintWriter pw;
        File outputFile = null;
        if (out == null)
        {
            String outputFileName = MessageFormat.format(outputFileNameTemplate, outputFileNameParams);

            outputFile = TempFileProvider.createTempFile(outputFileName, ".txt");

            try
            {
                pw = new PrintWriter(outputFile, SchemaComparator.CHAR_SET);
            }
            catch (FileNotFoundException error)
            {
                throw new RuntimeException("Unable to open file for writing: " + outputFile);
            }
            catch (UnsupportedEncodingException error)
            {
                throw new RuntimeException("Unsupported char set: " + SchemaComparator.CHAR_SET, error);
            }
        }
        else
        {
            pw = out;
        }
        
        // Populate the file with details of the comparison's results.
        for (Result result : results)
        {
            pw.print(result.describe());
            pw.print(SchemaComparator.LINE_SEPARATOR);
        }

        // We care only about output streams for reporting, which are created specially for current reference resource...
        if (null == out)
        {
            pw.close();
        }

        if (results.size() == 0)
        {
            LogUtil.info(logger, INFO_SCHEMA_COMP_ALL_OK, referenceResource);
        }
        else
        {
            int numProblems = results.size();
            if (outputFile == null)
            {
                LogUtil.warn(logger, WARN_SCHEMA_COMP_PROBLEMS_FOUND_NO_FILE, numProblems);
            }
            else
            {
                LogUtil.warn(logger, WARN_SCHEMA_COMP_PROBLEMS_FOUND, numProblems, outputFile);                
            }
        }
        Date endTime = new Date();
        long durationMillis = endTime.getTime() - startTime.getTime();
        LogUtil.debug(logger, DEBUG_SCHEMA_COMP_TIME_TAKEN, durationMillis);
        
        return results.size();
    }

    /**
     * Produces schema dump in XML format: this is performed pre- and post-upgrade (i.e. if
     * changes are made to the schema) and can made upon demand via JMX.
     * 
     * @return List of output files.
     */
    public List<File> dumpSchema()
    {
        return dumpSchema("", null);
    }
    
    /**
     * Produces schema dump in XML format: this is performed pre- and post-upgrade (i.e. if
     * changes are made to the schema) and can made upon demand via JMX.
     * 
     * @param dbPrefixes Array of database object prefixes to produce the dump for, e.g. "alf_".
     * @return List of output files.
     */
    public List<File> dumpSchema(String[] dbPrefixes)
    {
        return dumpSchema("", dbPrefixes);
    }
    
    /**
     * Dumps the DB schema to temporary file(s), named similarly to:
     * <pre>
     *   Alfresco-schema-DialectName-whenDumped-dbPrefix-23498732.xml
     * </pre>
     * Where the digits serve to create a unique temp file name. If whenDumped is empty or null,
     * then the output is similar to:
     * <pre>
     *   Alfresco-schema-DialectName-dbPrefix-23498732.xml
     * </pre>
     * If dbPrefixes is null, then the default list is used (see {@link MultiFileDumper#DEFAULT_PREFIXES})
     * The dump files' paths are logged at info level.
     * 
     * @param whenDumped
     * @param dbPrefixes Array of database object prefixes to filter by, e.g. "alf_"
     * @return List of output files.
     */
    private List<File> dumpSchema(String whenDumped, String[] dbPrefixes)
    {
        // Build a string to use as the file name template,
        // e.g. "Alfresco-schema-MySQLDialect-pre-upgrade-{0}-"
        StringBuilder sb = new StringBuilder(64);
        sb.append("Alfresco-schema-").
            append(dialect.getClass().getSimpleName());
        if (whenDumped != null && whenDumped.length() > 0)
        {
            sb.append("-");
            sb.append(whenDumped);
        }
        sb.append("-{0}-");
        
        File outputDir = TempFileProvider.getTempDir();
        String fileNameTemplate = sb.toString();
        return dumpSchema(outputDir, fileNameTemplate, dbPrefixes);
    }
    
    /**
     * Same as for {@link #dumpSchema(String, String[])} - except that the default list
     * of database object prefixes is used for filtering.
     * 
     * @see #dumpSchema(String, String[])
     * @param whenDumped
     * @return
     */
    private List<File> dumpSchema(String whenDumped)
    {
        return dumpSchema(whenDumped, null);
    }
    
    private List<File> dumpSchema(File outputDir, String fileNameTemplate, String[] dbPrefixes)
    {
        try
        {
            return attemptDumpSchema(outputDir, fileNameTemplate, dbPrefixes);
        }
        catch (Throwable e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Unable to dump schema to directory " + outputDir, e);
            }
            return null;
        }
    }
    
    private List<File> attemptDumpSchema(File outputDir, String fileNameTemplate, String[] dbPrefixes)
    {
        DbToXMLFactory dbToXMLFactory = new MultiFileDumper.DbToXMLFactoryImpl(getApplicationContext());
        
        MultiFileDumper dumper;
        
        if (dbPrefixes == null)
        {
            dumper = new MultiFileDumper(outputDir, fileNameTemplate, dbToXMLFactory);
        }
        else
        {
            dumper = new MultiFileDumper(dbPrefixes, outputDir, fileNameTemplate, dbToXMLFactory);            
        }
        List<File> files = dumper.dumpFiles();
        
        for (File file : files)
        {            
            if (logger.isInfoEnabled())
            {
                LogUtil.info(logger, MSG_NORMALIZED_SCHEMA, file.getAbsolutePath());
            }
        }
        
        return files;
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
		// Shut down DB, if required
		Class<?> dialectClazz = this.dialect.getClass();
		if (dialectClazz.equals(DerbyDialect.class))
		{
			try
			{
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			}
			// Derby shutdown always triggers an exception, even when clean
			catch (Throwable e) 
			{
			}
		}
	}
    
    /**
     * This is a workaround for the odd Spring-Hibernate interaction during configuration.
     * The Hibernate code assumes that schema scripts will be generated during context
     * initialization.  We want to do it afterwards and have a little more control.  Hence this class.
     * <p>
     * The connection that is used will not be closed or manipulated in any way.  This class
     * merely serves to give the connection to Hibernate.
     * 
     * @author Derek Hulley
     */
    public static class SchemaBootstrapConnectionProvider extends UserSuppliedConnectionProvider
    {
        private static ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<Connection>();
        
        public SchemaBootstrapConnectionProvider()
        {
        }
        
        /**
         * Set the connection for Hibernate to use for schema generation.
         */
        public static void setBootstrapConnection(Connection connection)
        {
            threadLocalConnection.set(connection);
        }

        /**
         * Unsets the connection.
         */
        @Override
        public void close()
        {
            // Leave the connection well alone, just remove it
            threadLocalConnection.set(null);
        }

        /**
         * Does nothing.  The connection was given by a 3rd party and they can close it.
         */
        @Override
        public void closeConnection(Connection conn)
        {
        }

        /**
         * Does nothing.
         */
        @Override
        public void configure(Properties props) throws HibernateException
        {
        }

        /**
         * @see #setBootstrapConnection(Connection)
         */
        @Override
        public Connection getConnection()
        {
            return threadLocalConnection.get();
        }

        @Override
        public boolean supportsAggressiveRelease()
        {
            return false;
        }
    }
    
    /**
     * A {@link Connection} wrapper that delegates all calls to the wrapped class
     * except for the close methods, which are ignored.
     *
     * @author Frederik Heremans
     */
    public class UnclosableConnection implements Connection 
    {
        private Connection wrapped;
        
        public UnclosableConnection(Connection wrappedConnection)
        {
            this.wrapped = wrappedConnection;
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException
        {
            return wrapped.isWrapperFor(iface);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException
        {
            return wrapped.unwrap(iface);
        }

        @Override
        public void clearWarnings() throws SQLException
        {
            wrapped.clearWarnings();
        }

        @Override
        public void close() throws SQLException
        {
            // Ignore this call
        }

        @Override
        public void commit() throws SQLException
        {
            wrapped.commit();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException
        {
            return wrapped.createArrayOf(typeName, elements);
        }

        @Override
        public Blob createBlob() throws SQLException
        {
            return wrapped.createBlob();
        }

        @Override
        public Clob createClob() throws SQLException
        {
            return wrapped.createClob();
        }

        @Override
        public NClob createNClob() throws SQLException
        {
            return wrapped.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException
        {
            return wrapped.createSQLXML();
        }

        @Override
        public Statement createStatement() throws SQLException
        {
            return wrapped.createStatement();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency)
                    throws SQLException
        {
            return wrapped.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency,
                    int resultSetHoldability) throws SQLException
        {
            return wrapped.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException
        {
            return wrapped.createStruct(typeName, attributes);
        }

        @Override
        public boolean getAutoCommit() throws SQLException
        {
            return wrapped.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException
        {
            return wrapped.getCatalog();
        }

        @Override
        public Properties getClientInfo() throws SQLException
        {
            return wrapped.getClientInfo();
        }

        @Override
        public String getClientInfo(String name) throws SQLException
        {
            return wrapped.getClientInfo(name);
        }

        @Override
        public int getHoldability() throws SQLException
        {
            return wrapped.getHoldability();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException
        {
            return wrapped.getMetaData();
        }

        @Override
        public int getTransactionIsolation() throws SQLException
        {
            return wrapped.getTransactionIsolation();
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException
        {
            return wrapped.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException
        {
            return wrapped.getWarnings();
        }

        @Override
        public boolean isClosed() throws SQLException
        {
            return wrapped.isClosed();
        }

        @Override
        public boolean isReadOnly() throws SQLException
        {
            return wrapped.isReadOnly();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException
        {
            return wrapped.isValid(timeout);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException
        {
            return wrapped.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException
        {
            return wrapped.prepareCall(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                    throws SQLException
        {
            return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType,
                    int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException
        {
            return wrapped.prepareStatement(sql);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
                    throws SQLException
        {
            return wrapped.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
                    throws SQLException
        {
            return wrapped.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames)
                    throws SQLException
        {
            return wrapped.prepareStatement(sql, columnNames);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                    int resultSetConcurrency) throws SQLException
        {
            return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                    int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException
        {
            wrapped.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback() throws SQLException
        {
            wrapped.rollback();
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException
        {
            wrapped.rollback(savepoint);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException
        {
            wrapped.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException
        {
            wrapped.setCatalog(catalog);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException
        {
            wrapped.setClientInfo(properties);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException
        {
            wrapped.setClientInfo(name, value);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException
        {
            wrapped.setHoldability(holdability);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException
        {
            wrapped.setReadOnly(readOnly);
        }

        @Override
        public Savepoint setSavepoint() throws SQLException
        {
            return wrapped.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException
        {
            return wrapped.setSavepoint(name);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException
        {
            wrapped.setTransactionIsolation(level);
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException
        {
            wrapped.setTypeMap(map);
        }

        @Override
        public void setSchema(String schema) throws SQLException
        {
            wrapped.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException
        {
            return wrapped.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException
        {
            wrapped.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
        {
            wrapped.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException
        {
            return wrapped.getNetworkTimeout();
        }
    }
}
