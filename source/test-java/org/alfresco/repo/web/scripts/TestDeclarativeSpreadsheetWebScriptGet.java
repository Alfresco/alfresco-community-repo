package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Test class for DeclarativeSpreadsheetWebScriptTest class
 * @author alex.mukha
 * @since 4.2.4
 */
public class TestDeclarativeSpreadsheetWebScriptGet extends DeclarativeSpreadsheetWebScript
{
    @Override
    protected Object identifyResource(String format, WebScriptRequest req)
    {
        return null;
    }

    @Override
    protected boolean allowHtmlFallback()
    {
        return false;
    }

    @Override
    protected List<Pair<QName, Boolean>> buildPropertiesForHeader(Object resource, String format, WebScriptRequest req)
    {
        List<Pair<QName,Boolean>> properties = 
                new ArrayList<Pair<QName,Boolean>>(DeclarativeSpreadsheetWebScriptTest.COLUMNS.length);
            boolean required = true;
            for(QName qname : DeclarativeSpreadsheetWebScriptTest.COLUMNS) 
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
    protected void populateBody(Object resource, Workbook workbook, Sheet sheet, List<QName> properties) throws IOException
    {
        // Set the sheet name
        workbook.setSheetName(0, "test");
    }

    @Override
    protected void populateBody(Object resource, CSVPrinter csv, List<QName> properties) throws IOException
    {
    }
}