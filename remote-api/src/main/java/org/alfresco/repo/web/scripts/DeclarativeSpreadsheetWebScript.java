/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * Parent of Declarative Webscripts that generate Excel files,
 *  usually based on some sort of dictionary model.
 * 
 * @author Nick Burch
 */
public abstract class DeclarativeSpreadsheetWebScript extends DeclarativeWebScript
{
    public static final String MODEL_CSV = "csv";
    public static final String MODEL_EXCEL = "excel";
    public static final String PARAM_REQ_DELIMITER = "delimiter";
    
    private CSVFormat csvFormat;
    
    protected DictionaryService dictionaryService;
    protected String filenameBase;
    
    /**
     * @param dictionaryService          the DictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Identifies the resource for the webscript.
     */
    protected abstract Object identifyResource(String format, WebScriptRequest req);
    
    /**
     * If the format is requested as HTML, should an exception be raised,
     *  or should an HTML version be called?
     */
    protected abstract boolean allowHtmlFallback(); 
    
    /**
     * Returns the QNames of the model properties to be output in
     *  the header, and if they're required or not
     */
    protected abstract List<Pair<QName, Boolean>> buildPropertiesForHeader(Object resource, String format, WebScriptRequest req);
    
    /**
     * Populates the body of the Excel Workbook, once the header has been
     *  output.
     * This is called if the format is .xls or .xlsx
     */
    protected abstract void populateBody(Object resource, Workbook workbook, Sheet sheet, List<QName> properties)
    		throws IOException;
    
    /**
     * Populates the body of the CSV file, once the header has been
     *  output.
     * This is called if the format is .csv
     */
    protected abstract void populateBody(Object resource, CSVPrinter csv, List<QName> properties)
    		throws IOException;
    
    /**
     * Set the CSVFormat
     * 
     * @param csvFormat CSVFormat
     */
    public void setCsvFormat(CSVFormat csvFormat)
    {
        this.csvFormat = csvFormat;
    }
    
    /**
     * Get the CSVFormat. Returns {@link CSVFormat#EXCEL} if none was set.
     * 
     * @return CSVFormat
     */
    public CSVFormat getCsvFormat()
    {
        if (csvFormat == null)
        {
            return CSVFormat.EXCEL
                    .withQuote('"')
                    .withRecordSeparator("\n")
                    .withFirstRecordAsHeader();
        }
        else
        {
            return csvFormat;
        }
    }
    
    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("success", Boolean.TRUE);
        
        // What format are they after?
        String format = req.getFormat();
        if("csv".equals(format) || "xls".equals(format) ||
           "xlsx".equals(format) || "excel".equals(format))
        {
            // Identify the thing to process
            Object resource = identifyResource(format, req);
        	
            // Generate the spreadsheet
            try
            {
                generateSpreadsheet(resource, format, req, status, model);
                return model;
            }
            catch(IOException e)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, 
                        "Unable to generate template file", e);
            }
        }
        
        // If we get here, then it isn't a spreadsheet version
        if(allowHtmlFallback())
        {
	        // There's some sort of help / upload form
	        return model;
        }
        else
        {
           throw new WebScriptException("Web Script format '" + format + "' is not supported");
        }
    }
    
    /**
     * Generates the spreadsheet, based on the properties in the header
     *  and a callback for the body.
     */
    public void generateSpreadsheet(Object resource, String format, WebScriptRequest req, 
    		Status status, Map<String, Object> model) throws IOException
    {
        Pattern qnameMunger = Pattern.compile("([A-Z][a-z]+)([A-Z].*)");
        String delimiterParam = req.getParameter(PARAM_REQ_DELIMITER);
        CSVFormat reqCSVFormat = null;

        if (delimiterParam != null && !delimiterParam.isEmpty())
        {
            reqCSVFormat = CSVFormat.EXCEL
                            .withDelimiter(delimiterParam.charAt(0))
                            .withQuote('"')
                            .withRecordSeparator("\n")
                            .withFirstRecordAsHeader();
        }

        // Build up the details of the header
        List<Pair<QName, Boolean>> propertyDetails = buildPropertiesForHeader(resource, format, req);
        String[] headings = new String[propertyDetails.size()];
        String[] descriptions = new String[propertyDetails.size()];
        boolean[] required = new boolean[propertyDetails.size()];
        for(int i=0; i<headings.length; i++)
        {
        	Pair<QName, Boolean> property = propertyDetails.get(i);
            if(property == null || property.getFirst() == null)
            {
                headings[i] = "";
                required[i] = false;
            }
            else
            {
                QName column = property.getFirst();
                required[i] = property.getSecond();
                
                // Ask the dictionary service nicely for the details
                PropertyDefinition pd = dictionaryService.getProperty(column);
                if(pd != null && pd.getTitle(dictionaryService) != null)
                {
                    // Use the friendly titles, which may even be localised!
                    headings[i] = pd.getTitle(dictionaryService);
                    descriptions[i] = pd.getDescription(dictionaryService);
                }
                else
                {
                    // Nothing friendly found, try to munge the raw qname into
                    //  something we can show to a user...
                    String raw = column.getLocalName();
                    raw = raw.substring(0, 1).toUpperCase() + raw.substring(1);
    
                    Matcher m = qnameMunger.matcher(raw);
                    if(m.matches())
                    {
                        headings[i] = m.group(1) + " " + m.group(2);
                    }
                    else
                    {
                        headings[i] = raw;
                    }
                }
            }
        }
        
        // Build a list of just the properties
        List<QName> properties = new ArrayList<QName>(propertyDetails.size());
        for(Pair<QName,Boolean> p : propertyDetails)
        {
           QName qn = null;
           if(p != null)
           {
              qn = p.getFirst();
           }
           properties.add(qn);
        }
        
        // Output
        if("csv".equals(format))
        {
            StringWriter sw = new StringWriter();
            CSVPrinter csv = new CSVPrinter(sw, reqCSVFormat != null ? reqCSVFormat : getCsvFormat());
            csv.printRecord(headings);
            
            populateBody(resource, csv, properties);
            
            model.put(MODEL_CSV, sw.toString());
        }
        else
        {
            Workbook wb;
            if("xlsx".equals(format))
            {
                wb = new XSSFWorkbook();
                // TODO Properties
            }
            else
            {
                wb = new HSSFWorkbook();
                // TODO Properties
            }
            
            // Add our header row
            Sheet sheet = wb.createSheet("Export");
            Row hr = sheet.createRow(0);
            sheet.createFreezePane(0, 1);
            
            Font fb = wb.createFont();
            fb.setBold(true);
            Font fi = wb.createFont();
            fi.setBold(true);
            fi.setItalic(true);
            
            CellStyle csReq = wb.createCellStyle();
            csReq.setFont(fb);
            CellStyle csOpt = wb.createCellStyle();
            csOpt.setFont(fi);
            
            // Populate the header
            Drawing draw = null;
            for(int i=0; i<headings.length; i++)
            {
                Cell c = hr.createCell(i);
                c.setCellValue(headings[i]);
                
                if(required[i])
                {
                	c.setCellStyle(csReq);
                }
                else
                {
                	c.setCellStyle(csOpt);
                }
                
                if(headings[i].length() == 0)
                {
                    sheet.setColumnWidth(i, 3*250);
                }
                else
                {
                    sheet.setColumnWidth(i, 18*250);
                }
                
                if(descriptions[i] != null && descriptions[i].length() > 0)
                {
                    // Add a description for it too
                    if(draw == null)
                    {
                        draw = sheet.createDrawingPatriarch();
                    }
                    ClientAnchor ca = wb.getCreationHelper().createClientAnchor();
                    ca.setCol1(c.getColumnIndex());
                    ca.setCol2(c.getColumnIndex()+1);
                    ca.setRow1(hr.getRowNum());
                    ca.setRow2(hr.getRowNum()+2);
                    
                    Comment cmt = draw.createCellComment(ca);
                    cmt.setAuthor("");
                    cmt.setString(wb.getCreationHelper().createRichTextString(descriptions[i]));
                    cmt.setVisible(false);
                    c.setCellComment(cmt);
                }
            }
            
            // Have the contents populated
            populateBody(resource, wb, sheet, properties);
            
            // Save it for the template
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            model.put(MODEL_EXCEL, baos.toByteArray());
        }
    }

    @Override
    protected Map<String, Object> createTemplateParameters(WebScriptRequest req, WebScriptResponse res,
            Map<String, Object> customParams)
    {
        Map<String, Object> model = super.createTemplateParameters(req, res, customParams);
        // We sometimes need to monkey around to do the binary output... 
        model.put("req", req);
        model.put("res", res);
        model.put("writeExcel", new WriteExcel(res, model, req.getFormat(), filenameBase));
        return model;
    }
    
    public static class WriteExcel 
    {
        private String format;
        private String filenameBase;
        private WebScriptResponse res;
        private Map<String, Object> model;
        private WriteExcel(WebScriptResponse res, Map<String, Object> model, String format, String filenameBase)
        {
            this.res = res;
            this.model = model;
            this.format = format;
            this.filenameBase = filenameBase;
        }
        public void write() throws IOException
        {
            String filename = filenameBase + "." + format;
            
            // If it isn't a CSV, reset so we can send binary
            if(! "csv".equals(format))
            {
                res.reset();
            }
            
            // Tell the browser it's a file download
            res.addHeader("Content-Disposition", "attachment; filename=" + filename);
            
            // Now send that data
            if("csv".equals(format))
            {
                res.getWriter().append((String)model.get(MODEL_CSV));
            }
            else
            {
                // Set the mimetype, as we've reset
                if("xlsx".equals(format))
                {
                    res.setContentType(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET);
                } else {
                    res.setContentType(MimetypeMap.MIMETYPE_EXCEL);
                }
                
                // Send the raw excel bytes
                byte[] excel = (byte[])model.get(MODEL_EXCEL);
                res.getOutputStream().write(excel);
            }
        }
    }
}
