/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.DatabaseMetaDataHelper;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.TypeNames;
import org.springframework.context.ApplicationContext;


/**
 * Exports a database schema to an in-memory {@link Schema} object.
 * 
 * @author Matt Ward
 */
public class ExportDb
{
    /** Reverse map from database types to JDBC types (loaded from a Hibernate dialect). */
    private final Map<String, Integer> reverseTypeMap = new TreeMap<String, Integer>();

    /** The database metadata helper */
    private DatabaseMetaDataHelper databaseMetaDataHelper;

    /** The JDBC DataSource. */
    private DataSource dataSource;

    /** The object graph we're building */
    private Schema schema;

    /** What type of DBMS are we running? */
    private Dialect dialect;

    /** Used to gain the repository's schema version */
    private DescriptorService descriptorService;
    
    /** Only top-level tables starting with namePrefix will be exported, set to empty string for all objects */
    private String namePrefix = "alf_";

    /** Default schema name to use */
    private String dbSchemaName;

    private final static Log log = LogFactory.getLog(ExportDb.class);
    
    
    public ExportDb(ApplicationContext context)
    {
        this((DataSource) context.getBean("dataSource"),
             (Dialect) context.getBean("dialect"),
             (DescriptorService) context.getBean("descriptorComponent"),
             (DatabaseMetaDataHelper) context.getBean("databaseMetaDataHelper"));
    }
    
    
    /**
     * Create a new instance of the tool within the context of an existing database connection
     * 
     * @param connection            the database connection to use for metadata queries
     * @param dialect               the Hibernate dialect
     */
    public ExportDb(final DataSource dataSource, final Dialect dialect, DescriptorService descriptorService, DatabaseMetaDataHelper databaseMetaDataHelper)
    {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.descriptorService = descriptorService;
        this.databaseMetaDataHelper = databaseMetaDataHelper;
        init();
    }
    
    
    private void init()
    {
        try
        {
            attemptInit();
        }
        catch (SecurityException error)
        {
            throw new RuntimeException("Unable to generate type map using hibernate.", error);
        }
        catch (IllegalArgumentException error)
        {
            throw new RuntimeException("Unable to generate type map using hibernate.", error);
        }
        catch (NoSuchFieldException error)
        {
            throw new RuntimeException("Unable to generate type map using hibernate.", error);
        }
        catch (IllegalAccessException error)
        {
            throw new RuntimeException("Unable to generate type map using hibernate.", error);
        }
    }
    
    
    /**
     * Initializes the fields ready to perform the database metadata reading 
     * @param dialect               the Hibernate dialect
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @SuppressWarnings("unchecked")
    private void attemptInit() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        final Field typeNamesField = Dialect.class.getDeclaredField("typeNames");
        typeNamesField.setAccessible(true);
        final TypeNames typeNames = (TypeNames) typeNamesField.get(dialect);
        final Field defaultsField = TypeNames.class.getDeclaredField("defaults");
        defaultsField.setAccessible(true);
        final Map<Integer, String> forwardMap2 = (Map<Integer, String>) defaultsField.get(typeNames);
        for (final Map.Entry<Integer, String> e : forwardMap2.entrySet())
        {
            this.reverseTypeMap.put(e.getValue().toLowerCase(), e.getKey());
        }

        final Field weightedField = TypeNames.class.getDeclaredField("weighted");
        weightedField.setAccessible(true);
        final Map<Integer, Map<Integer, String>> forwardMap1 = (Map<Integer, Map<Integer, String>>) weightedField
                .get(typeNames);
        for (final Map.Entry<Integer, Map<Integer, String>> e : forwardMap1.entrySet())
        {
            for (final String type : e.getValue().values())
            {
                this.reverseTypeMap.put(type.toLowerCase(), e.getKey());
            }
        }
    }

    
    
    public void execute()
    {
        PropertyCheck.mandatory(this, "dataSource", dataSource);
        PropertyCheck.mandatory(this, "dialect", dialect);
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
        
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            Descriptor descriptor = descriptorService.getServerDescriptor();
            int schemaVersion = descriptor.getSchema();                
            execute(connection, schemaVersion);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to execute export.", e);
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
                // Little can be done at this stage.
            }
        }
    }

    
    
    private void execute(Connection con, int schemaVersion) throws Exception
    {
        final DatabaseMetaData dbmd = con.getMetaData();

        String schemaName = databaseMetaDataHelper.getSchema(con);

        schema = new Schema(schemaName, namePrefix, schemaVersion, true);
        String[] prefixFilters = namePrefixFilters(dbmd);
        
        for (String filter : prefixFilters)
        {
            extractSchema(dbmd, schemaName, filter);
        }
    }

    
    private void extractSchema(DatabaseMetaData dbmd, String schemaName, String prefixFilter)
                throws SQLException, IllegalArgumentException, IllegalAccessException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Retrieving tables: schemaName=[" + schemaName + "], prefixFilter=[" + prefixFilter + "]");
        }
        
        
        final ResultSet tables = dbmd.getTables(null, schemaName, prefixFilter, new String[]
        {
            "TABLE", "VIEW", "SEQUENCE"
        });
        
        
        while (tables.next())
        {
            final String tableName = tables.getString("TABLE_NAME");

            if (log.isDebugEnabled())
            {
                log.debug("Examining table tableName=[" + tableName + "]");
            }
            
            // Oracle hack: ignore tables in the recycle bin
            // ALF-14129 fix, check whether schema already contains object with provided name
            if (tableName.startsWith("BIN$") || schema.containsByName(tableName))
            {
                continue;
            }

            if (tables.getString("TABLE_TYPE").equals("SEQUENCE"))
            {
                Sequence sequence = new Sequence(tableName);
                schema.add(sequence);
                continue;
            }
            
            Table table = new Table(tableName);
            schema.add(table);
            
            // Table columns
            final ResultSet columns = dbmd.getColumns(null, tables.getString("TABLE_SCHEM"), tableName, "%");
            while (columns.next())
            {
                String columnName = columns.getString("COLUMN_NAME");
                Column column = new Column(columnName);
                
                String dbType = columns.getString("TYPE_NAME");
                int colSize = columns.getInt("COLUMN_SIZE");
                int scale = columns.getInt("DECIMAL_DIGITS");
                int jdbcType = columns.getInt("DATA_TYPE");
                String type = generateType(dbType, colSize, scale, jdbcType);
                column.setType(type);
                
                String nullableString = columns.getString("IS_NULLABLE");
                column.setNullable(parseBoolean(nullableString));
                
                column.setOrder(columns.getInt("ORDINAL_POSITION"));
                
                try
                {
                    String autoIncString = columns.getString("IS_AUTOINCREMENT");
                    column.setAutoIncrement(parseBoolean(autoIncString));
                }
                catch(SQLException jtdsDoesNOtHAveIsUatoincrement)
                {
                    column.setAutoIncrement((dbType.endsWith("identity")));
                }
                
                column.setParent(table);
                table.getColumns().add(column);
            }
            columns.close();
            
            
            // Primary key
            final ResultSet primarykeycols = dbmd.getPrimaryKeys(null, tables.getString("TABLE_SCHEM"), tableName);
            
            PrimaryKey pk = null;
            
            while (primarykeycols.next())
            {
                if (pk == null)
                {
                    String pkName = primarykeycols.getString("PK_NAME");
                    pk = new PrimaryKey(pkName);
                }
                String columnName = primarykeycols.getString("COLUMN_NAME");
                pk.getColumnNames().add(columnName);
                
                int columnOrder = primarykeycols.getInt("KEY_SEQ");
                pk.getColumnOrders().add(columnOrder);
            }
            primarykeycols.close();
            
            // If this table has a primary key, add it. 
            if (pk != null)
            {
                pk.setParent(table);
                table.setPrimaryKey(pk);
            }

            
            // Indexes
            final ResultSet indexes = dbmd.getIndexInfo(null, tables.getString("TABLE_SCHEM"), tableName, false, true);
            String lastIndexName = "";
            
            Index index = null;
            
            while (indexes.next())
            {
                final String indexName = indexes.getString("INDEX_NAME");
                if (indexName == null)
                {
                    // Oracle seems to have some dummy index entries
                    continue;
                }
                // Skip the index corresponding to the PK if it is mentioned
                else if (indexName.equals(table.getPrimaryKey().getName()))
                {
                    continue;
                }
                
                if (!indexName.equals(lastIndexName))
                {
                    index = new Index(indexName);
                    index.setUnique(!indexes.getBoolean("NON_UNIQUE"));
                    index.setParent(table);
                    table.getIndexes().add(index);
                    lastIndexName = indexName;
                }
                if (index != null)
                {
                    String columnName = indexes.getString("COLUMN_NAME");
                    index.getColumnNames().add(columnName);
                }
            }
            indexes.close();

            
            
            final ResultSet foreignkeys = dbmd.getImportedKeys(null, tables.getString("TABLE_SCHEM"), tableName);
            String lastKeyName = "";
            
            ForeignKey fk = null;
            
            while (foreignkeys.next())
            {
                final String keyName = foreignkeys.getString("FK_NAME");
                if (!keyName.equals(lastKeyName))
                {
                    fk = new ForeignKey(keyName);
                    fk.setParent(table);
                    table.getForeignKeys().add(fk);
                    lastKeyName = keyName;
                }
                if (fk != null)
                {
                    fk.setLocalColumn(foreignkeys.getString("FKCOLUMN_NAME"));
                    fk.setTargetTable(foreignkeys.getString("PKTABLE_NAME"));
                    fk.setTargetColumn(foreignkeys.getString("PKCOLUMN_NAME"));
                }
            }
            foreignkeys.close();
        }
        tables.close();
    }


    /**
     * Convert a boolean string as used in the database, to a boolean value.
     * 
     * @param nullableString
     * @return true if "YES", false if "NO"
     */
    private boolean parseBoolean(String nullableString)
    {
        // TODO: what about (from the javadoc):
        // empty string --- if the nullability for the parameter is unknown
        if (nullableString.equals("NO"))
        {
            return false;
        }
        if (nullableString.equals("YES"))
        {
            return true;
        }
        
        throw new IllegalArgumentException("Unsupported term \"" + nullableString +
                    "\", perhaps this database doesn't use YES/NO for booleans?");
        
    }

    protected String generateType(final String dbTypeRaw, int size, final int digits, int sqlType)
        throws IllegalArgumentException, IllegalAccessException
    {
        final String dbType = dbTypeRaw.toLowerCase();
        
        final String sizeType = dbType + "(" + size + ")";
        if (this.reverseTypeMap.containsKey(sizeType))
        {
            // the map may contain an exact match, e.g. "char(1)"
            return sizeType;
        }

        final String precisionScaleType = dbType + "(" + size + ", " + digits + ")";
        if (this.reverseTypeMap.containsKey(precisionScaleType))
        {
            // the map may contain an exact match, e.g. "numeric(3, 1)"
            return precisionScaleType;
        }
        else
        {
            for (String key : reverseTypeMap.keySet())
            {
                // Populate the placeholders, examples:
                // varchar($l) => varchar(20)
                // numeric($p, $s) => numeric(5, 2)
                String popKey = key.replaceAll("\\$p", String.valueOf(size));
                popKey = popKey.replaceAll("\\$s", String.valueOf(digits));
                popKey = popKey.replaceAll("\\$l", String.valueOf(size));
                
                // Variation of the size type with size unit of char, e.g. varchar2(255 char)
                final String charSizeType = dbType + "(" + size + " char)";
                
                // If the populated key matches a precision/scale type or a size type
                // then the populated key gives us the string we're after.
                if (popKey.equals(precisionScaleType) || popKey.equals(sizeType) || popKey.equals(charSizeType))
                {
                    return popKey;
                }
            }
        }
        
        return dbType;
    }

    
    /**
     * @return the schema
     */
    public Schema getSchema()
    {
        return this.schema;
    }

    
    /**
     * @return the namePrefix
     */
    public String getNamePrefix()
    {
        return this.namePrefix;
    }

    private String[] namePrefixFilters(DatabaseMetaData dbmd) throws SQLException
    {
        String filter = namePrefix + "%";
        // We're assuming the prefixes are either PREFIX_ or prefix_
        // but not mixed-case.
        return new String[]
        {
            filter.toLowerCase(),
            filter.toUpperCase()
        };
    }

    
    /**
     * @param namePrefix the namePrefix to set
     */
    public void setNamePrefix(String namePrefix)
    {
        this.namePrefix = namePrefix;
    }

    /**
     * Set the default schema name
     *
     * @param dbSchemaName the default schema name
     */
    public void setDbSchemaName(String dbSchemaName)
    {
        this.dbSchemaName = dbSchemaName;
    }
}
