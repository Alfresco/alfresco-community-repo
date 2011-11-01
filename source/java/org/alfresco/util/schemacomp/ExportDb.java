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
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.alfresco.util.PropertyCheck;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
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

    /** The JDBC DataSource. */
    private DataSource dataSource;

    /** The object graph we're building */
    private Schema schema;

    private Dialect dialect;
    
    
    
    public ExportDb(ApplicationContext context) throws Exception
    {
        this((DataSource) context.getBean("dataSource"),
             (Dialect) context.getBean("dialect"));
    }
    
    
    /**
     * Create a new instance of the tool within the context of an existing database connection
     * 
     * @param connection            the database connection to use for metadata queries
     * @param dialect               the Hibernate dialect
     */
    public ExportDb(final DataSource dataSource, final Dialect dialect) throws Exception
    {
        this.dataSource = dataSource;
        this.dialect = dialect;
        init();
    }
    
    
    /**
     * Initializes the fields ready to perform the database metadata reading 
     * @param dialect               the Hibernate dialect
     */
    @SuppressWarnings("unchecked")
    private void init() throws Exception
    {
        final Field typeNamesField = Dialect.class.getDeclaredField("typeNames");
        typeNamesField.setAccessible(true);
        final TypeNames typeNames = (TypeNames) typeNamesField.get(dialect);
        final Field defaultsField = TypeNames.class.getDeclaredField("defaults");
        defaultsField.setAccessible(true);
        final Map<Integer, String> forwardMap2 = (Map<Integer, String>) defaultsField.get(typeNames);
        for (final Map.Entry<Integer, String> e : forwardMap2.entrySet())
        {
            this.reverseTypeMap.put(e.getValue(), e.getKey());
        }

        final Field weightedField = TypeNames.class.getDeclaredField("weighted");
        weightedField.setAccessible(true);
        final Map<Integer, Map<Integer, String>> forwardMap1 = (Map<Integer, Map<Integer, String>>) weightedField
                .get(typeNames);
        for (final Map.Entry<Integer, Map<Integer, String>> e : forwardMap1.entrySet())
        {
            for (final String type : e.getValue().values())
            {
                this.reverseTypeMap.put(type, e.getKey());
            }
        }
    }

    
    
    public void execute() throws Exception
    {
        PropertyCheck.mandatory(this, "dataSource", dataSource);
        // Get a Connection
        Connection connection = dataSource.getConnection();
        
        try
        {
            connection.setAutoCommit(false);
            execute(connection);
        }
        finally
        {
            try { connection.close(); } catch (Throwable e) {}
        }
    }

    
    
    private void execute(Connection con) throws Exception
    {
        final DatabaseMetaData dbmd = con.getMetaData();

        // Assume that if there are schemas, we want the one named after the connection user or the one called "dbo" (MS
        // SQL hack)
        String schemaName = null;
        final ResultSet schemas = dbmd.getSchemas();
        while (schemas.next())
        {
            final String thisSchema = schemas.getString("TABLE_SCHEM");
            if (thisSchema.equals(dbmd.getUserName()) || thisSchema.equalsIgnoreCase("dbo"))
            {
                schemaName = thisSchema;
                break;
            }
        }
        schemas.close();

        schema = new Schema(schemaName);
        
        final ResultSet tables = dbmd.getTables(null, schemaName, "%", new String[]
        {
            "TABLE", "VIEW"
        });
        
        
        while (tables.next())
        {
            final String tableName = tables.getString("TABLE_NAME");

            // Oracle hack: ignore tables in the recycle bin
            if (tableName.startsWith("BIN$"))
            {
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
            }
            primarykeycols.close();
            
            // If this table has a primary key, add it. 
            if (pk != null)
            {
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

    protected String generateType(final String dbType, int size, final int digits, int sqlType)
        throws IllegalArgumentException, IllegalAccessException
    {
        String dbName = dbType.toLowerCase() + "(" + size + ")";
        if (this.reverseTypeMap.containsKey(dbName))
        {
            // the map may contain an exact match, e.g. "char(1)"
            return dbName;
        }

        dbName = dbType.toLowerCase() + "(" + size + ", " + digits + ")";
        if (this.reverseTypeMap.containsKey(dbName))
        {
            // the map may contain an exact match, e.g. "numeric(3, 1)"
            return dbName;
        }
        else
        {
            String precisionScaleType = dbType + "(" + size + ", " + digits + ")";
            String sizeType = dbType + "(" + size + ")";
            
            for (String key : reverseTypeMap.keySet())
            {
                // Populate the placeholders, examples:
                // varchar($l) => varchar(20)
                // numeric($p, $s) => numeric(5, 2)
                String popKey = key.replaceAll("\\$p", String.valueOf(size));
                popKey = popKey.replaceAll("\\$s", String.valueOf(digits));
                popKey = popKey.replaceAll("\\$l", String.valueOf(size));
                
                // If the populated key matches a precision/scale type or a size type
                // then the populated key gives us the string we're after.
                if (popKey.equals(precisionScaleType) || popKey.equals(sizeType))
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
    
    
    
    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException
    {
        ExportDb exportDb = null;
        try
        {
            exportDb = new ExportDb(null, new PostgreSQLDialect());
        }
        catch (Exception error)
        {
            throw new RuntimeException(error);
        }
        
        if (exportDb != null)
        {
            String varchar = exportDb.generateType("varchar", 20, 0, 12);
            System.out.println("varchar: " + varchar);
            
            String int4 = exportDb.generateType("int4", 20, 0, 4);
            System.out.println("int4: " + int4);
        }
    }
}
