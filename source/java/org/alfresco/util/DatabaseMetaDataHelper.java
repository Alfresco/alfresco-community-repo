package org.alfresco.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Helper class to collect all of our DatabaseMetaData interpretations in one place.
 * 
 * @author sfrensley
 *
 */
public class DatabaseMetaDataHelper {
	
	private static Log logger = LogFactory.getLog(DatabaseMetaDataHelper.class);

    private Configuration cfg;

    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory)
    {
        this.cfg = localSessionFactory.getConfiguration();
    }

	/**
	 * Trys to determine the schema name from the DatabaseMetaData obtained from the Connection.
	 * @param connection A database connection
	 * @return String
	 */
	private String getSchemaFromConnection(Connection connection) 
	{
	
		if (connection == null) {
			logger.error("Unable to determine schema due to null connection.");
			return null;
		}
		
		ResultSet schemas = null;
		
		try 
		{
			final DatabaseMetaData dbmd = connection.getMetaData();
	
			// Assume that if there are schemas, we want the one named after the connection user or the one called "dbo" (MS
			// SQL hack)
			String schema = null;
			schemas = dbmd.getSchemas();
			while (schemas.next())
			{
				final String thisSchema = schemas.getString("TABLE_SCHEM");
				if (thisSchema.equals(dbmd.getUserName()) || thisSchema.equalsIgnoreCase("dbo"))
				{
					schema = thisSchema;
					break;
				}
			}
			return schema;
		} 
		catch (Exception e) 
		{
			logger.error("Unable to determine current schema.",e);
		} 
		finally 
		{
			if (schemas != null) 
			{
				try 
				{
					schemas.close();
				} 
				catch (Exception e)
				{
					//noop
				}
			}
		}
		return null;
	}

    public String getSchema(Connection connection)
    {
        String schema = null;

        if (this.cfg != null)
        {
            String tmpSchema = this.cfg.getProperty("hibernate.default_schema");
            if (tmpSchema != null && tmpSchema.trim().length() > 0)
            {
                schema = tmpSchema;
            }
        }

        // if hibernate.default_schema was specified as a system property, then override previous value
        String tmpSchema = System.getProperty("hibernate.default_schema");
        if (tmpSchema != null && tmpSchema.length() > 0)
        {
            schema = tmpSchema;
        }

        if (schema == null)
        {
            schema = getSchemaFromConnection(connection);
        }

        return schema;
    }

}
