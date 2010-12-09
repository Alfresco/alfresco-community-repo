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
package org.alfresco.util.schemadump;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.sql.DataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.util.PropertyCheck;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.TypeNames;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Simple command line utility to help with database schema comparisons and upgrades. Dumps a database schema via JDBC
 * to a normalised XML form.
 * 
 * @author dward
 */
public class Main
{
    /** Reusable empty SAX attribute list. */
    private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    /** Reverse map from database types to JDBC types (loaded from a Hibernate dialect). */
    private final Map<String, Integer> reverseTypeMap = new TreeMap<String, Integer>();

    /** Should we scale down string field widths (assuming 4 bytes to one character?). */
    private boolean scaleCharacters;

    /** The JDBC DataSource. */
    private DataSource dataSource;

    /**
     * The main method.
     * 
     * @param args
     *            the args: &ltcontext.xml&gt &ltoutput.xml&gt
     */
    public static void main(final String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println("Usage:");
            System.out.println("java " + Main.class.getName() + " <context.xml> <output.xml>");
            System.exit(1);
        }
        
        final File outputFile = new File(args[1]);
        new Main(args[0]).execute(outputFile);
    }
    
    /**
     * Creates a new instance of this tool by starting up a full context.
     */
    public Main(final String contextPath) throws Exception
    {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]
        {
            "file:" + contextPath
        });
        this.dataSource = (DataSource) context.getBean("dataSource");

        // Use Java reflection to bypass accessibility rules and get hold of hibernate's type mapping!
        final Properties hibProps = (Properties) context.getBean("hibernateConfigProperties");
        final Dialect dialect = (Dialect) Class.forName(hibProps.getProperty("hibernate.dialect")).newInstance();
        
        // Initialize
        init(dialect);
    }
    
    /**
     * Create a new instance of the tool within the context of an existing database connection
     * 
     * @param connection            the database connection to use for metadata queries
     * @param dialect               the Hibernate dialect
     */
    public Main(final DataSource dataSource, final Dialect dialect) throws Exception
    {
        this.dataSource = dataSource;
        
        // Initialize
        init(dialect);
    }
    
    /**
     * Initializes the fields ready to perform the database metadata reading 
     * @param dialect               the Hibernate dialect
     */
    @SuppressWarnings("unchecked")
    private void init(final Dialect dialect) throws Exception
    {
        this.scaleCharacters = dialect instanceof Oracle8iDialect;
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

    /**
     * Execute, writing the result to the given file.
     * 
     * @param outputFile                the file to write to
     */
    public void execute(File outputFile) throws Exception
    {
        PropertyCheck.mandatory(this, "dataSource", dataSource);
        // Get a Connection
        Connection connection = dataSource.getConnection();
        NamedElementCollection result = null;
        try
        {
            connection.setAutoCommit(false);
            result = execute(connection);
        }
        finally
        {
            try { connection.close(); } catch (Throwable e) {}
        }

        // Set up a SAX TransformerHandler for outputting XML
        final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
        final TransformerHandler xmlOut = stf.newTransformerHandler();
        final Transformer t = xmlOut.getTransformer();
        try
        {
            t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        }
        catch (final IllegalArgumentException e)
        {
            // It was worth a try
        }
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        xmlOut.setResult(new StreamResult(outputFile));

        xmlOut.startDocument();
        result.output(xmlOut);
        xmlOut.endDocument();
    }

    /**
     * Execute.
     * 
     * @return                          Returns the named XML elements
     */
    private NamedElementCollection execute(Connection con) throws Exception
    {
        final NamedElementCollection schemaCol = new NamedElementCollection("schema", "table");

        final DatabaseMetaData dbmd = con.getMetaData();

        // Assume that if there are schemas, we want the one named after the connection user or the one called "dbo" (MS
        // SQL hack)
        String schema = null;
        final ResultSet schemas = dbmd.getSchemas();
        while (schemas.next())
        {
            final String thisSchema = schemas.getString("TABLE_SCHEM");
            if (thisSchema.equals(dbmd.getUserName()) || thisSchema.equalsIgnoreCase("dbo"))
            {
                schema = thisSchema;
                break;
            }
        }
        schemas.close();

        final ResultSet tables = dbmd.getTables(null, schema, "%", new String[]
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

            final NamedElement tableEl = schemaCol.addNamedElement(tableName);
            final NamedElementCollection columnsCol = tableEl.addCollection("columns", "column");
            final ResultSet columns = dbmd.getColumns(null, tables.getString("TABLE_SCHEM"), tableName, "%");
            while (columns.next())
            {
                final NamedElement columnEl = columnsCol.addNamedElement(columns.getString("COLUMN_NAME"));
                columnEl.addAttribute("seq", String.valueOf(columns.getInt("ORDINAL_POSITION")));
                columnEl.addAttribute("type", convertToTypeName(columns.getString("TYPE_NAME"), columns
                        .getInt("COLUMN_SIZE"), columns.getInt("DECIMAL_DIGITS"), columns.getInt("DATA_TYPE")));
                columnEl.addAttribute("nullable", columns.getString("IS_NULLABLE"));
            }
            columns.close();

            final ResultSet primarykeycols = dbmd.getPrimaryKeys(null, tables.getString("TABLE_SCHEM"), tableName);
            String primaryKeyName = null;
            NamedElementCollection primaryKey = null;
            while (primarykeycols.next())
            {
                if (primaryKey == null)
                {
                    primaryKeyName = primarykeycols.getString("PK_NAME");
                    primaryKey = tableEl.addCollection("primarykey", "column");
                }
                final NamedElement pkCol = primaryKey.addNamedElement(primarykeycols.getString("COLUMN_NAME"));
                pkCol.addAttribute("seq", primarykeycols.getString("KEY_SEQ"));
            }
            primarykeycols.close();

            final NamedElementCollection indexCol = tableEl.addCollection("indexes", "index");
            final ResultSet indexes = dbmd.getIndexInfo(null, tables.getString("TABLE_SCHEM"), tableName, false, true);
            String lastIndexName = "";
            NamedElementCollection indexCols = null;
            while (indexes.next())
            {
                final String indexName = indexes.getString("INDEX_NAME");
                if (indexName == null)
                {
                    // Oracle seems to have some dummy index entries
                    continue;
                }
                // Skip the index corresponding to the PK if it is mentioned
                else if (indexName.equals(primaryKeyName))
                {
                    continue;
                }
                if (!indexName.equals(lastIndexName))
                {
                    final NamedElement index = indexCol.addNamedElement(indexName);
                    index.addAttribute("unique", String.valueOf(!indexes.getBoolean("NON_UNIQUE")));
                    indexCols = index.addCollection("columns", "column");
                    lastIndexName = indexName;
                }
                indexCols.addNamedElement(indexes.getString("COLUMN_NAME"));
            }
            indexes.close();

            final NamedElementCollection foreignKeyCol = tableEl.addCollection("foreignkeys", "key");
            final ResultSet foreignkeys = dbmd.getImportedKeys(null, tables.getString("TABLE_SCHEM"), tableName);
            String lastKeyName = "";
            NamedElementCollection foreignKeyCols = null;
            while (foreignkeys.next())
            {
                final String keyName = foreignkeys.getString("FK_NAME");
                if (!keyName.equals(lastKeyName))
                {
                    final NamedElement key = foreignKeyCol.addNamedElement(keyName);
                    foreignKeyCols = key.addCollection("columns", "column");
                    lastKeyName = keyName;
                }
                final NamedElement fkCol = foreignKeyCols.addNamedElement(foreignkeys.getString("FKCOLUMN_NAME"));
                fkCol.addAttribute("table", foreignkeys.getString("PKTABLE_NAME").toUpperCase());
                fkCol.addAttribute("column", foreignkeys.getString("PKCOLUMN_NAME").toUpperCase());
            }
            foreignkeys.close();
        }
        tables.close();
        return schemaCol;
    }

    /**
     * Chooses a JDBC type name, given database and JDBC type information.
     * 
     * @param dbType
     *            the db type
     * @param size
     *            the size
     * @param digits
     *            the number of digits
     * @param sqlType
     *            the sql type
     * @return the string
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    private String convertToTypeName(final String dbType, int size, final int digits, int sqlType)
            throws IllegalArgumentException, IllegalAccessException
    {

        // First see if the Hibernate dialect has a mapping to the database type
        String dbName = dbType.toLowerCase() + "(" + size + "," + digits + ")";
        if (this.reverseTypeMap.containsKey(dbName))
        {
            sqlType = this.reverseTypeMap.get(dbName);
        }
        else
        {
            dbName = dbType.toLowerCase();
            if (this.reverseTypeMap.containsKey(dbName))
            {
                sqlType = this.reverseTypeMap.get(dbName);
            }
        }
        final Field[] fields = Types.class.getFields();
        final int modifiers = Modifier.PUBLIC | Modifier.STATIC;
        for (final Field field : fields)
        {
            if (field.getType().equals(int.class) && (field.getModifiers() & modifiers) == modifiers
                    && field.getInt(null) == sqlType)
            {
                if (size == 0 || this.reverseTypeMap.containsKey(dbName) || sqlType == Types.TIMESTAMP
                        || sqlType == Types.INTEGER)
                {
                    return field.getName();
                }
                else
                {
                    // Hack to work around Oracle's byte semantics
                    if (this.scaleCharacters
                            && (sqlType == Types.CHAR || sqlType == Types.VARCHAR || sqlType == Types.CLOB))
                    {
                        size /= 4;
                    }
                    return field.getName() + "(" + size + ")";
                }
            }
        }
        return String.valueOf(sqlType);
    }

    /**
     * Represents a sorted collection of named elements in the XML model.
     */
    private class NamedElementCollection
    {

        /** The collection name. */
        private final String collectionName;

        /** The repeated element name. */
        private final String elementName;

        /** The attributes of the collection. */
        private final AttributesImpl attributes = new AttributesImpl();

        /** The items in the collection. */
        private final List<NamedElement> items = new ArrayList<NamedElement>(100);

        /**
         * The Constructor.
         * 
         * @param collectionName
         *            the collection name
         * @param elementName
         *            the repeated element name
         */
        public NamedElementCollection(final String collectionName, final String elementName)
        {
            this.collectionName = collectionName;
            this.elementName = elementName;
        }

        /**
         * Adds a named element.
         * 
         * @param name
         *            the name
         * @return the named element
         */
        public NamedElement addNamedElement(final String name)
        {
            final NamedElement retVal = new NamedElement(name);
            this.items.add(retVal);
            return retVal;
        }

        /**
         * Outputs to XML.
         * 
         * @param xmlOut
         *            the SAX content handler
         * @throws SAXException
         *             the SAX exception
         */
        public void output(final ContentHandler xmlOut) throws SAXException
        {
            xmlOut.startElement("", "", this.collectionName, this.attributes);
            Collections.sort(this.items);
            for (final NamedElement item : this.items)
            {
                item.output(xmlOut, this.elementName);
            }
            xmlOut.endElement("", "", this.collectionName);
        }
    }

    /**
     * Represents a named element in the XML model.
     */
    private class NamedElement implements Comparable<NamedElement>
    {

        /** The name. */
        private final String name;

        /** The attributes. */
        private final List<String[]> attributes = new LinkedList<String[]>();

        /** The child collections. */
        private final List<NamedElementCollection> collections = new LinkedList<NamedElementCollection>();;

        /**
         * Instantiates a new named element.
         * 
         * @param name
         *            the name
         */
        public NamedElement(final String name)
        {
            this.name = name.toUpperCase();
        }

        /**
         * Adds an attribute.
         * 
         * @param name
         *            the name
         * @param value
         *            the value
         */
        public void addAttribute(final String name, final String value)
        {
            this.attributes.add(new String[]
            {
                name, value
            });
        }

        /**
         * Adds a child collection.
         * 
         * @param collectionName
         *            the collection name
         * @param elementName
         *            the repeated element name
         * @return the named element collection
         */
        public NamedElementCollection addCollection(final String collectionName, final String elementName)
        {
            final NamedElementCollection retVal = new NamedElementCollection(collectionName, elementName);
            this.collections.add(retVal);
            return retVal;

        }

        /**
         * Outputs to XML.
         * 
         * @param xmlOut
         *            the SAX content handler
         * @param elementName
         *            the element name
         * @throws SAXException
         *             the SAX exception
         */
        public void output(final ContentHandler xmlOut, final String elementName) throws SAXException
        {
            final AttributesImpl attribs = new AttributesImpl();
            attribs.addAttribute("", "", "name", "CDATA", this.name);
            xmlOut.startElement("", "", elementName, attribs);
            for (final String[] attrib : this.attributes)
            {
                xmlOut.startElement("", "", attrib[0], Main.EMPTY_ATTRIBUTES);
                final char[] chars = attrib[1].toCharArray();
                xmlOut.characters(chars, 0, chars.length);
                xmlOut.endElement("", "", attrib[0]);
            }
            for (final NamedElementCollection coll : this.collections)
            {
                coll.output(xmlOut);
            }
            xmlOut.endElement("", "", elementName);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(final NamedElement o)
        {
            return this.name.compareTo(o.name);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof NamedElement))
            {
                return false;
            }
            return this.name.equals(((NamedElement) obj).name);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return this.name.hashCode();
        }

    }
}
