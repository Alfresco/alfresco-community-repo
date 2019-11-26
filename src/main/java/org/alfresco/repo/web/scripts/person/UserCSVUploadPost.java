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
package org.alfresco.repo.web.scripts.person;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Webscript implementation for the POST method for uploading a
 *  CSV of users to have them created.
 * 
 * @author Nick Burch
 * @since 3.5
 */
public class UserCSVUploadPost extends DeclarativeWebScript
{
    protected static final QName[] COLUMNS = new QName[] {
        ContentModel.PROP_USERNAME, ContentModel.PROP_FIRSTNAME, 
        ContentModel.PROP_LASTNAME, ContentModel.PROP_EMAIL, 
        null,
        ContentModel.PROP_PASSWORD, ContentModel.PROP_ORGANIZATION, 
        ContentModel.PROP_JOBTITLE, ContentModel.PROP_LOCATION,
        ContentModel.PROP_TELEPHONE, ContentModel.PROP_MOBILE,
        ContentModel.PROP_SKYPE, ContentModel.PROP_INSTANTMSG,
        ContentModel.PROP_GOOGLEUSERNAME,
        ContentModel.PROP_COMPANYADDRESS1, ContentModel.PROP_COMPANYADDRESS2,
        ContentModel.PROP_COMPANYADDRESS3, ContentModel.PROP_COMPANYPOSTCODE,
        ContentModel.PROP_COMPANYTELEPHONE, ContentModel.PROP_COMPANYFAX,
        ContentModel.PROP_COMPANYEMAIL
    };
    
    private static final String ERROR_BAD_FORM = "person.err.userCSV.invalidForm";
    private static final String ERROR_NO_FILE = "person.err.userCSV.noFile";
    private static final String ERROR_CORRUPT_FILE = "person.err.userCSV.corruptFile";
    private static final String ERROR_GENERAL = "person.err.userCSV.general";
    private static final String ERROR_BLANK_COLUMN = "person.err.userCSV.blankColumn";
    private static final String MSG_CREATED = "person.msg.userCSV.created";
    private static final String MSG_EXISTING = "person.msg.userCSV.existing";
    
    private static Log logger = LogFactory.getLog(UserCSVUploadPost.class);
    
    private MutableAuthenticationService authenticationService;
    private AuthorityService authorityService;
    private PersonService personService;
    private TenantService tenantService;
    private DictionaryService dictionaryService;
    private RetryingTransactionHelper retryingTransactionHelper;
    
    /**
     * @param authenticationService    the AuthenticationService to set
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * @param authorityService          the AuthorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param personService          the PersonService to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * @param tenantService          the TenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * @param dictionaryService          the DictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param retryingTransactionHelper  the helper for running transactions to set
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    
    /**
     * @see DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        final List<Map<QName,String>> users = new ArrayList<Map<QName,String>>();
        final ResourceBundle rb = getResources();
        

        // Try to load the user details from the upload
        FormData form = (FormData)req.parseContent();
        if (form == null || !form.getIsMultiPart())
        {
            throw new ResourceBundleWebScriptException(Status.STATUS_BAD_REQUEST, rb, ERROR_BAD_FORM);
        }
        
        boolean processed = false;
        for (FormData.FormField field : form.getFields())
        {
            if (field.getIsFile())
            {
                processUpload(
                        field.getInputStream(),
                        field.getFilename(),
                        users);
                processed = true;
                break;
            }
        }
        
        if (!processed)
        {
            throw new ResourceBundleWebScriptException(Status.STATUS_BAD_REQUEST, rb, ERROR_NO_FILE);
        }
        
        // Should we send emails?
        boolean sendEmails = true;
        if (req.getParameter("email") != null)
        {
            sendEmails = Boolean.parseBoolean(req.getParameter("email"));
        }
        
        if (form.hasField("email"))
        {
            sendEmails = Boolean.parseBoolean(form.getParameters().get("email")[0]);
        }
        
        // Now process the users
        final MutableInt totalUsers = new MutableInt(0);
        final MutableInt addedUsers = new MutableInt(0);
        final Map<String, String> results = new HashMap<String, String>();
        final boolean doSendEmails = sendEmails;

        // Do the work in a new transaction, so that if we hit a problem
        //  during the commit stage (eg too many users) then we get to
        //  hear about it, and handle it ourselves.
        // Otherwise, commit exceptions occur deep inside RepositoryContainer
        //  and we can't control the status code
        RetryingTransactionCallback<Void> work = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                try
                {
                    doAddUsers(totalUsers, addedUsers, results, users, rb, doSendEmails);
                    return null;
                }
                catch (Throwable t)
                {
                    // Make sure we rollback from this
                    UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
                    if (userTrx != null && userTrx.getStatus() != javax.transaction.Status.STATUS_MARKED_ROLLBACK)
                    {
                        try
                        {
                            userTrx.setRollbackOnly();
                        }
                        catch (Throwable t2) {}
                    }
                    // Report the problem further down
                    throw t;
                }
            }
        };
        
        try
        {
            retryingTransactionHelper.doInTransaction(work);
        }
        catch (Throwable t)
        {
            // Tell the client of the problem
            if (t instanceof WebScriptException)
            {
                // We've already wrapped it properly, all good
                throw (WebScriptException)t;
            }
            else
            {
                // Something unexpected has ripped up
                // Return the details with a 200, so that Share does the right thing
                throw new ResourceBundleWebScriptException(
                        Status.STATUS_OK, rb, ERROR_GENERAL, t);
            }
        }

        
        // If we get here, then adding the users didn't throw any exceptions,
        //  so tell the client which users went in and which didn't
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("totalUsers", totalUsers);
        model.put("addedUsers", addedUsers);
        model.put("users", results);
        
        return model;
    }
                
    private void doAddUsers(final MutableInt totalUsers, final MutableInt addedUsers,
            final Map<String, String> results, final List<Map<QName,String>> users,
            final ResourceBundle rb, final boolean sendEmails)
    {
        for (Map<QName,String> user : users)
        {
            totalUsers.setValue( totalUsers.intValue()+1 );
            
            // Grab the username, and do any MT magic on it
            String username = user.get(ContentModel.PROP_USERNAME);
            try
            {
                username = PersonServiceImpl.updateUsernameForTenancy(username, tenantService);
            }
            catch (TenantDomainMismatchException e)
            {
                throw new ResourceBundleWebScriptException(
                        Status.STATUS_OK, rb,
                        ERROR_GENERAL, e);
            }
            
            // Do they already exist?
            if (personService.personExists(username))
            {
                results.put(username, rb.getString(MSG_EXISTING));
                if (logger.isDebugEnabled())
                {
                    logger.debug("Not creating user as already exists: " + username + " for " + user);
                }
            }
            else
            {
                String password = user.get(ContentModel.PROP_PASSWORD);
                user.remove(ContentModel.PROP_PASSWORD); // Not for the person service
                
                try
                {
                    // Add the person
                    personService.createPerson( (Map<QName,Serializable>)(Map)user );
                    
                    // Add the underlying user
                    authenticationService.createAuthentication(username, password.toCharArray());
                    
                    // If required, notify the user
                    if (sendEmails)
                    {
                        personService.notifyPerson(username, password);
                    }
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Creating user from upload: " + username + " for " + user);
                    }
                    
                    // All done
                    String msg = MessageFormat.format(
                            rb.getString(MSG_CREATED),
                            new Object[] { user.get(ContentModel.PROP_EMAIL) });
                    results.put(username, msg);
                    addedUsers.setValue( addedUsers.intValue()+1 );
                }
                catch (Throwable t)
                {
                    throw new ResourceBundleWebScriptException(
                            Status.STATUS_OK, rb, ERROR_GENERAL, t);
                }
            }
        }
    }
    
    protected void processUpload(InputStream input, String filename, List<Map<QName,String>> users)
    {
        try
        {
            if (filename != null && filename.length() > 0)
            {
                if (filename.endsWith(".csv"))
                {
                    processCSVUpload(input, users);
                    return;
                }
                if (filename.endsWith(".xls"))
                {
                    processXLSUpload(input, users);
                    return;
                }
                if (filename.endsWith(".xlsx"))
                {
                    processXLSXUpload(input, users);
                    return;
                }
            }
            
            // If in doubt, assume it's probably a .csv
            processCSVUpload(input, users);
        } 
        catch (IOException e)
        {
            // Return the error as a 200 so the user gets a friendly
            //  display of the error message in share
            throw new ResourceBundleWebScriptException(
                    Status.STATUS_OK, getResources(),
                    ERROR_CORRUPT_FILE, e);
        }
    }
    
    protected void processCSVUpload(InputStream input, List<Map<QName,String>> users)
        throws IOException
    {
        InputStreamReader reader = new InputStreamReader(input, Charset.forName("UTF-8"));
        CSVFormat format = CSVFormat.EXCEL.withHeader();
        CSVParser csv = format.parse(reader);
        String delimiter = String.valueOf(format.getDelimiter());

        String[][] data = (String[][]) csv.getRecords().stream()
                .map(record -> record.toString().split(delimiter))
                .toArray();

        if(data != null && data.length > 0)
        {
            processSpreadsheetUpload(data, users);
        }
    }
    
    protected void processXLSUpload(InputStream input, List<Map<QName,String>> users)
        throws IOException
    {
        Workbook wb = new HSSFWorkbook(input);
        processSpreadsheetUpload(wb, users);
    }
    
    protected void processXLSXUpload(InputStream input, List<Map<QName,String>> users)
        throws IOException
    {
        Workbook wb = new XSSFWorkbook(input);
        processSpreadsheetUpload(wb, users);
    }
    
    private void processSpreadsheetUpload(Workbook wb, List<Map<QName,String>> users)
        throws IOException
    {
        if (wb.getNumberOfSheets() > 1)
        {
            logger.info("Uploaded Excel file has " + wb.getNumberOfSheets() + 
                    " sheets, ignoring  all except the first one"); 
        }
        
        int firstRow = 0;
        Sheet s = wb.getSheetAt(0);
        DataFormatter df = new DataFormatter();
        
        String[][] data = new String[s.getLastRowNum()+1][];
                                     
        // If there is a heading freezepane row, skip it
        PaneInformation pane = s.getPaneInformation();
        if (pane != null && pane.isFreezePane() && pane.getHorizontalSplitTopRow() > 0)
        {
            firstRow = pane.getHorizontalSplitTopRow();
            logger.debug("Skipping excel freeze header of " + firstRow + " rows");
        }
        
        // Process each row in turn, getting columns up to our limit
        for (int row=firstRow; row <= s.getLastRowNum(); row++)
        {
            Row r = s.getRow(row);
            if (r != null)
            {
                String[] d = new String[COLUMNS.length];
                for (int cn=0; cn<COLUMNS.length; cn++)
                {
                    Cell cell = r.getCell(cn);
                    if (cell != null && cell.getCellType() != CellType.BLANK)
                    {
                        d[cn] = df.formatCellValue(cell);
                    }
                }
                data[row] = d;
            }
        }
        
        // Handle the contents
        processSpreadsheetUpload(data, users);
    }
    
    /**
     * Builds user objects based on the supplied data. If a row is empty, then
     *  the child String array should be empty too. (Needs to be present so that
     *  the line number reporting works)
     */
    private void processSpreadsheetUpload(String[][] data, List<Map<QName,String>> users)
    {
        // What we consider to be the literal string "user name" when detecting
        //  if a row is an example/header one or not
        // Note - all of these want to be lower case!
        List<String> usernameIsUsername = new ArrayList<String>();
        // The English literals
        usernameIsUsername.add("username");
        usernameIsUsername.add("user name");
        // And the localised form too if found
        PropertyDefinition unPD = dictionaryService.getProperty(ContentModel.PROP_USERNAME);
        if (unPD != null)
        {
            if(unPD.getTitle(dictionaryService) != null)
                usernameIsUsername.add(unPD.getTitle(dictionaryService).toLowerCase());
            if(unPD.getDescription(dictionaryService) != null)
                usernameIsUsername.add(unPD.getDescription(dictionaryService).toLowerCase());
        }
        
        
        // Process the contents of the spreadsheet
        for (int lineNumber=0; lineNumber<data.length; lineNumber++)
        {
            Map<QName,String> user = new HashMap<QName, String>();
            String[] userData = data[lineNumber];
            
            if (userData == null || userData.length == 0 || 
               (userData.length == 1 && userData[0].trim().length() == 0))
            {
                // Empty line, skip
                continue;
            }
            
            boolean required = true;
            for (int i=0; i<COLUMNS.length; i++)
            {
                if (COLUMNS[i] == null)
                {
                    required = false;
                    continue;
                }
                
                String value = null;
                if (userData.length > i)
                {
                    value = userData[i];
                }
                if (value == null || value.length() == 0)
                {
                    if (required)
                    {
                        throw new ResourceBundleWebScriptException(
                                Status.STATUS_OK, getResources(),
                                ERROR_BLANK_COLUMN, new Object[] {
                                        COLUMNS[i].getLocalName(), (i+1), (lineNumber+1)});
                    }
                }
                else
                {
                    user.put(COLUMNS[i], value);
                }
            }

            // If no password was given, use their surname
            if (!user.containsKey(ContentModel.PROP_PASSWORD))
            {
                user.put(ContentModel.PROP_PASSWORD, "");
            }
            
            // Skip any user who looks like an example file heading
            // i.e. a username of "username" or "user name"
            String username = user.get(ContentModel.PROP_USERNAME).toLowerCase();
            if (usernameIsUsername.contains(username))
            {
                // Skip
            }
            else
            {
                // Looks like a real line, keep it
                users.add(user);
            }
        }
    }
    
    protected static class ResourceBundleWebScriptException extends WebScriptException 
    {
        private String message;
        
        public ResourceBundleWebScriptException(int status, ResourceBundle rb, String msgId, Object... args)
        {
            super(status, msgId, args);
            buildMessageIfPossible(rb, msgId, args);
        }
        
        public ResourceBundleWebScriptException(int status, ResourceBundle rb, String msgId)
        {
            super(status, msgId);
            buildMessageIfPossible(rb, msgId, new Object[0]);
        }
        
        public ResourceBundleWebScriptException(int status, ResourceBundle rb, String msgId, Throwable cause)
        {
            super(status, msgId, cause);
            buildMessageIfPossible(rb, msgId, new Object[0]);
        }
        
        public String getMessage()
        {
            return message;
        }
        
        private void buildMessageIfPossible(ResourceBundle rb, String msgId, Object... args)
        {
            String msg = rb.getString(msgId);
            if (msg != null)
            {
                if (args == null || args.length == 0)
                {
                    message = msg;
                }
                else
                {
                    message = MessageFormat.format(msg, args);
                }
            }
            if (getCause() != null)
            {
                message = message + "\n" + getCause().getMessage();
            }
        }
    }
}