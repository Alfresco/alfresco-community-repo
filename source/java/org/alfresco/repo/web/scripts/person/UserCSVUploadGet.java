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
package org.alfresco.repo.web.scripts.person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.web.scripts.DeclarativeSpreadsheetWebScript;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Webscript implementation for giving information on uploading
 *  users via a CSV.
 * 
 * @author Nick Burch
 */
public class UserCSVUploadGet extends DeclarativeSpreadsheetWebScript
{
   public UserCSVUploadGet()
   {
      this.filenameBase = "ExampleUserUpload";
   }

	/**
	 * We have a HTML version
	 */
    @Override
	protected boolean allowHtmlFallback() {
		return true;
	}

    /**
     * We don't have a resource
     */
	@Override
	protected Object identifyResource(String format, WebScriptRequest req) {
		return null;
	}

	@Override
	protected List<Pair<QName, Boolean>> buildPropertiesForHeader(
			Object resource, String format, WebScriptRequest req) {
		List<Pair<QName,Boolean>> properties = 
			new ArrayList<Pair<QName,Boolean>>(UserCSVUploadPost.COLUMNS.length);
		boolean required = true;
		for(QName qname : UserCSVUploadPost.COLUMNS) 
		{
			Pair<QName,Boolean> p = null;
			if(qname != null)
			{
				p = new Pair<QName, Boolean>(qname, required);
			}
			else
			{
				required = false;
			}
			properties.add(p);
		}
		return properties;
	}

   @Override
   protected void populateBody(Object resource, CSVPrinter csv, List<QName> properties) throws IOException {
       // Just a blank line is needed
       csv.println();
   }

   @Override
   protected void populateBody(Object resource, Workbook workbook, Sheet sheet,
		       List<QName> properties) throws IOException {
       // Set the sheet name
       workbook.setSheetName(0, "UsersToAdd");

       // Add a blank line
       sheet.createRow(1);
	}
}
