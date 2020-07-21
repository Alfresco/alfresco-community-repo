/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.schema.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.domain.dialect.MySQLClusterNDBDialect;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.repo.domain.dialect.PostgreSQLDialect;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.DialectUtil;
import org.alfresco.util.LogUtil;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;


public class ScriptExecutorImpl implements ScriptExecutor
{
    /** The global property containing the default batch size used by --FOREACH */
    private static final String PROPERTY_DEFAULT_BATCH_SIZE = "system.upgrade.default.batchsize";
    private static final String MSG_EXECUTING_GENERATED_SCRIPT = "schema.update.msg.executing_generated_script";
    private static final String MSG_EXECUTING_COPIED_SCRIPT = "schema.update.msg.executing_copied_script";
    private static final String MSG_EXECUTING_STATEMENT = "schema.update.msg.executing_statement";
    private static final String MSG_OPTIONAL_STATEMENT_FAILED = "schema.update.msg.optional_statement_failed";
    private static final String ERR_STATEMENT_FAILED = "schema.update.err.statement_failed";
    private static final String ERR_SCRIPT_NOT_FOUND = "schema.update.err.script_not_found";
    private static final String ERR_STATEMENT_INCLUDE_BEFORE_SQL = "schema.update.err.statement_include_before_sql";
    private static final String ERR_STATEMENT_VAR_ASSIGNMENT_BEFORE_SQL = "schema.update.err.statement_var_assignment_before_sql";
    private static final String ERR_STATEMENT_VAR_ASSIGNMENT_FORMAT = "schema.update.err.statement_var_assignment_format";
    private static final String ERR_STATEMENT_VAR_ASSIGNMENT_NULL = "schema.update.err.statement_var_assignment_null";
    private static final String ERR_STATEMENT_TERMINATOR = "schema.update.err.statement_terminator";    
    private static final String ERR_DELIMITER_SET_BEFORE_SQL = "schema.update.err.delimiter_set_before_sql";
    private static final String ERR_DELIMITER_INVALID = "schema.update.err.delimiter_invalid";
    private static final int DEFAULT_MAX_STRING_LENGTH = 1024;
    private static final int DEFAULT_MAX_STRING_LENGTH_NDB = 400;
    private static volatile int maxStringLength = DEFAULT_MAX_STRING_LENGTH;
    private Dialect dialect;
    private ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
    private static Log logger = LogFactory.getLog(ScriptExecutorImpl.class);
    private Properties globalProperties;
    private ThreadLocal<StringBuilder> executedStatementsThreadLocal = new ThreadLocal<StringBuilder>();
    private DataSource dataSource;


    /**
     * @return      Returns the maximum number of characters that a string field can be
     */
    public static final int getMaxStringLength()
    {
        return ScriptExecutorImpl.maxStringLength;
    }
    
    /**
     * Truncates or returns a string that will fit into the string columns in the schema.  Text fields can
     * either cope with arbitrarily long text fields or have the default limit, {@link #DEFAULT_MAX_STRING_LENGTH}.
     * 
     * @param value             the string to check
     * @return                  Returns a string that is short enough for {@link ScriptExecutorImpl#getMaxStringLength()}
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
     * Sets the previously auto-detected Hibernate dialect.
     * 
     * @param dialect
     *            the dialect
     */
    public void setDialect(Dialect dialect)
    {
        this.dialect = dialect;
    }

    public ScriptExecutorImpl()
    {
        globalProperties = new Properties();
    }
    
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
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

    @Override
    public void executeScriptUrl(String scriptUrl) throws Exception
    {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        try
        {
            executeScriptUrl(connection, scriptUrl);
        }
        finally
        {
            connection.close();
        }
    }

    private void executeScriptUrl(Connection connection, String scriptUrl) throws Exception
    {
        Dialect dialect = this.dialect;
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
        String dialectScriptUrl = scriptUrl.replaceAll(DialectUtil.PLACEHOLDER_DIALECT, dialect.getClass().getName());
        // Replace the script placeholders
        executeScriptFile(connection, tempFile, dialectScriptUrl);
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
        Resource resource = DialectUtil.getDialectResource(rpr, dialectClazz, scriptUrl);
        if (resource == null)
        {
            return null;
        }
        return resource.getInputStream();
    }
    
    /**
     * @param connection    the DB connection to use
     * @param scriptFile    the file containing the statements
     * @param scriptUrl     the URL of the script to report.  If this is null, the script
     *                      is assumed to have been auto-generated.
     */
    private void executeScriptFile(
            Connection connection,
            File scriptFile,
            String scriptUrl) throws Exception
    {
        final Dialect dialect = this.dialect;
        
        StringBuilder executedStatements = executedStatementsThreadLocal.get();
        if (executedStatements == null)
        {
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
            Object defaultFetchValue = null;
            String batchTableName = null;
            boolean doBatch = false;
            int batchUpperLimit = 0;
            int batchSize = 1;
            Map<String, Object> varAssignments = new HashMap<String, Object>(13);
            String delimiter = ";";
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
                    executeScriptUrl(connection, includedScriptUrl);
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
                    String[] fetchMapping = assignStr.split("!");
                    String[] assigns = fetchMapping[0].split("=");
                    if (assigns.length != 2 || assigns[0].length() == 0 || assigns[1].length() == 0)
                    {
                        throw AlfrescoRuntimeException.create(ERR_STATEMENT_VAR_ASSIGNMENT_FORMAT, (line - 1), scriptUrl);
                    }
                    fetchVarName = assigns[0];
                    fetchColumnName = assigns[1];
                    if (fetchMapping.length > 1 && fetchMapping[1].length() > 0)
                    {
                        defaultFetchValue = fetchMapping[1];
                    }
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
                        batchTableName = args[1].substring(0, sepIndex);
                        String stmt = "SELECT MAX(" + args[1].substring(sepIndex+1) + ") AS upper_limit FROM " + batchTableName;
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
                else if (sql.startsWith("--DELETE_NOT_EXISTS"))
                {
                    DeleteNotExistsExecutor deleteNotExists = new DeleteNotExistsExecutor(connection, sql, line, scriptFile, globalProperties);
                    deleteNotExists.execute();

                    // Reset
                    sb.setLength(0);
                    fetchVarName = null;
                    fetchColumnName = null;
                    defaultFetchValue = null;
                    batchTableName = null;
                    doBatch = false;
                    batchUpperLimit = 0;
                    batchSize = 1;

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
                else if (sql.startsWith("--SET-DELIMITER:"))
                {
                    if (sb.length() > 0)
                    {
                        // This can only be set before a new SQL statement
                        throw AlfrescoRuntimeException.create(ERR_DELIMITER_SET_BEFORE_SQL, (line - 1), scriptUrl);
                    }

                    // We're good...so set the new delimiter
                    String newDelim = sql.substring(16).trim();
                    if (newDelim.length() == 0)
                    {
                        throw AlfrescoRuntimeException.create(ERR_DELIMITER_INVALID, (line - 1), scriptUrl);
                    }
                    delimiter = newDelim;
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
                        throw AlfrescoRuntimeException.create(ERR_STATEMENT_TERMINATOR, delimiter, (line - 1), scriptUrl);
                    }
                    // there has not been anything to execute - it's just a comment line
                    continue;
                }
                // have we reached the end of a statement?
                boolean execute = false;
                boolean optional = false;
                if (sql.endsWith(delimiter))
                {
                    sql = sql.substring(0, sql.length() - 1);
                    execute = true;
                    optional = false;
                }
                else if (sql.endsWith("(optional)") || sql.endsWith("(OPTIONAL)"))
                {
                    // Get the end of statement
                    int endIndex = sql.lastIndexOf(delimiter);
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
                            logger.info("Processing from " + lowerBound + " to " + (lowerBound + batchSize) + " rows of " + batchUpperLimit + " rows from table " + batchTableName + ".");
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
                            // note: enable bootstrap on MySQL 5.5 (eg. for auto-generated SQL)
                            sql = sql.replaceAll("(?i)TYPE=InnoDB", "ENGINE=InnoDB");
                        }
                        
                        if (this.dialect != null && this.dialect instanceof MySQLClusterNDBDialect)
                        {
                            // note: enable bootstrap on MySQL Cluster NDB
                            /*
                        	 * WARNING: Experimental/unsupported - see MySQLClusterNDBDialect !
                    		 */
                        	sql = sql.replaceAll("(?i)TYPE=InnoDB", "ENGINE=NDB"); // belts-and-braces
                            sql = sql.replaceAll("(?i)ENGINE=InnoDB", "ENGINE=NDB");
                            
                            sql = sql.replaceAll("(?i) BIT ", " BOOLEAN ");
                            sql = sql.replaceAll("(?i) BIT,", " BOOLEAN,");
                            
                            sql = sql.replaceAll("(?i) string_value text", " string_value VARCHAR("+DEFAULT_MAX_STRING_LENGTH_NDB+")");
                            
                            sql = sql.replaceAll("(?i) VARCHAR(4000)", "TEXT(4000)");
                        }
                        
                        Object fetchedVal = executeStatement(connection, sql, fetchColumnName, optional, line, scriptFile);
                        if (fetchVarName != null && fetchColumnName != null)
                        {
                            if (fetchedVal == null)
                            {
                                fetchedVal = defaultFetchValue;
                            }
                            // We must have some value
                            if (fetchedVal == null)
                            {
                                // The variable is null (not even empty)
                                throw AlfrescoRuntimeException.create(ERR_STATEMENT_VAR_ASSIGNMENT_NULL, fetchVarName, fetchVarName, (line - 1), scriptUrl);
                            }
                            varAssignments.put(fetchVarName, fetchedVal);
                        }
                    }
                    sb.setLength(0);
                    fetchVarName = null;
                    fetchColumnName = null;
                    defaultFetchValue = null;
                    batchTableName = null;
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
}
