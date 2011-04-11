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
package org.alfresco.repo.web.scripts.person;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
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
 * Webscript implementation for giving information on uploading
 *  users via a CSV.
 * 
 * @author Nick Burch
 * @since 3.5
 */
public class UserCSVUploadGet extends DeclarativeWebScript
{
    public static final String MODEL_CSV = "csv";
    public static final String MODEL_EXCEL = "excel";
    
    private DictionaryService dictionaryService;
    
    /**
     * @param dictionaryService          the DictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("success", Boolean.TRUE);
        
        // What format are they after?
        String format = req.getFormat();
        if ("csv".equals(format) || "xls".equals(format) ||
           "xlsx".equals(format) || "excel".equals(format))
        {
            try
            {
                generateTemplateFile(format, req, status, model);
                return model;
            }
            catch (IOException e)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, 
                        "Unable to generate template file", e);
            }
        }
        
        // Give them the help and the upload form
        return model;
    }
    
    /**
     * Generates a template file to help the user figure out what to
     *  put into their CSV
     */
    private void generateTemplateFile(String format, WebScriptRequest req, Status status, Map<String, Object> model)
        throws IOException
    {
        Pattern p = Pattern.compile("([A-Z][a-z]+)([A-Z].*)");
        
        String[] headings = new String[UserCSVUploadPost.COLUMNS.length];
        String[] descriptions = new String[UserCSVUploadPost.COLUMNS.length];
        for (int i=0; i<headings.length; i++)
        {
            QName column = UserCSVUploadPost.COLUMNS[i];
            if (column == null)
            {
                headings[i] = "";
            }
            else
            {
                // Ask the dictionary service nicely
                PropertyDefinition pd = dictionaryService.getProperty(column);
                if (pd != null && pd.getTitle() != null)
                {
                    // Use the friendly titles, which may even be localised!
                    headings[i] = pd.getTitle();
                    descriptions[i] = pd.getDescription();
                }
                else
                {
                    // Nothing friendly found, try to munge the raw qname into
                    //  something we can show to a user...
                    String raw = column.getLocalName();
                    raw = raw.substring(0, 1).toUpperCase() + raw.substring(1);
    
                    Matcher m = p.matcher(raw);
                    if (m.matches())
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
        
        if ("csv".equals(format))
        {
            StringWriter sw = new StringWriter();
            CSVPrinter csv = new CSVPrinter(sw, CSVStrategy.EXCEL_STRATEGY);
            csv.println(headings);
            csv.println();
            
            model.put(MODEL_CSV, sw.toString());
        }
        else
        {
            Workbook wb;
            if ("xlsx".equals(format))
            {
                wb = new XSSFWorkbook();
            }
            else
            {
                wb = new HSSFWorkbook();
            }
            
            // Add our header row
            Sheet sheet = wb.createSheet("UsersToAdd");
            Row hr = sheet.createRow(0);
            sheet.createFreezePane(0, 1);
            
            Font fb = wb.createFont();
            fb.setBoldweight(Font.BOLDWEIGHT_BOLD);
            Font fi = wb.createFont();
            fi.setBoldweight(Font.BOLDWEIGHT_BOLD);
            fi.setItalic(true);
            
            CellStyle csReq = wb.createCellStyle();
            csReq.setFont(fb);
            CellStyle csOpt = wb.createCellStyle();
            csOpt.setFont(fi);
            
            // Populate the header
            CellStyle cs = csReq;
            Drawing draw = null;
            for (int i=0; i<headings.length; i++)
            {
                Cell c = hr.createCell(i);
                c.setCellStyle(cs);
                c.setCellValue(headings[i]);
                
                if (headings[i].length() == 0)
                {
                    sheet.setColumnWidth(i, 3*250);
                    cs = csOpt;
                }
                else
                {
                    sheet.setColumnWidth(i, 18*250);
                }
                
                if (descriptions[i] != null && descriptions[i].length() > 0)
                {
                    // Add a description for it too
                    if (draw == null)
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
            
            // Add an empty data row
            sheet.createRow(1);
            
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
        model.put("writeExcel", new WriteExcel(res, model, req.getFormat()));
        return model;
    }
    
    public static class WriteExcel 
    {
        private String format;
        private WebScriptResponse res;
        private Map<String, Object> model;
        
        private WriteExcel(WebScriptResponse res, Map<String, Object> model, String format)
        {
            this.res = res;
            this.model = model;
            this.format = format;
        }
        
        public void write() throws IOException
        {
            String filename = "ExampleUserUpload." + format;
            
            // If it isn't a CSV, reset so we can send binary
            if (!"csv".equals(format))
            {
                res.reset();
            }
            
            // Tell the browser it's a file download
            res.addHeader("Content-Disposition", "attachment; filename=" + filename);
            
            // Now send that data
            if ("csv".equals(format))
            {
                res.getWriter().append((String)model.get(MODEL_CSV));
            }
            else
            {
                // Set the mimetype, as we've reset
                if ("xlsx".equals(format))
                {
                    res.setContentType(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET);
                } 
                else 
                {
                    res.setContentType(MimetypeMap.MIMETYPE_EXCEL);
                }
                
                // Send the raw excel bytes
                byte[] excel = (byte[])model.get(MODEL_EXCEL);
                res.getOutputStream().write(excel);
            }
        }
    }
}