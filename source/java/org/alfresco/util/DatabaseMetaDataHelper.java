/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to collect all of our DatabaseMetaData interpretations in one place.
 * 
 * @author sfrensley
 *
 */
public class DatabaseMetaDataHelper {
	
	private static Log logger = LogFactory.getLog(DatabaseMetaDataHelper.class);

	/**
	 * Trys to determine the schema name from the DatabaseMetaData obtained from the Connection.
	 * @param connection A database connection
	 * @return
	 */
	public static String getSchema(Connection connection) 
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
}
