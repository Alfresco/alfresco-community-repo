/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests the processing of different types of files (csv, xls, xlsx) used for bulk upload of users, see {@link UserCSVUploadPost}.
 * @author Ancuta Morarasu
 */
public class UserCSVUploadTest extends BaseWebScriptTest
{
    private static final String RESOURCE_PREFIX = "org/alfresco/repo/web/scripts/uploadcsv/";

    private DictionaryService dictionaryService;
    private UserCSVUploadPost uploadWebscript;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        this.dictionaryService = (DictionaryService) ctx.getBean("DictionaryService");

        this.uploadWebscript = new UserCSVUploadPost();
        this.uploadWebscript.setDictionaryService(dictionaryService);
    }

    private InputStream getResourceAsStream(String fileName)
    {
        InputStream csvStream = UserCSVUploadTest.class.getClassLoader().getResourceAsStream(RESOURCE_PREFIX + fileName);
        assertNotNull(csvStream);
        return csvStream;
    }

    public void testProcessCVSUserTemplate() throws Exception
    {
        List<Map<QName,String>> users = new ArrayList<>();
        this.uploadWebscript.processCSVUpload(getResourceAsStream("userCSV.csv"), users);
        assertEquals(2, users.size());
    }

    public void testProcessCVSWithHeaderUserTemplate() throws Exception
    {
        List<Map<QName,String>> users = new ArrayList<>();
        this.uploadWebscript.processCSVUpload(getResourceAsStream("userCSV_header.csv"), users);
        assertEquals(2, users.size());
    }

    public void testProcessXLSUserTemplate() throws Exception
    {
        List<Map<QName,String>> users = new ArrayList<>();
        this.uploadWebscript.processXLSUpload(getResourceAsStream("userCSV.xls"), users);
        assertEquals(2, users.size());
    }

    public void testProcessXLSXUserTemplate() throws Exception
    {
        List<Map<QName,String>> users = new ArrayList<>();
        this.uploadWebscript.processXLSXUpload(getResourceAsStream("userCSV.xlsx"), users);
        assertEquals(2, users.size());
    }

    public void testProcessOtherUserTemplate() throws Exception
    {
        List<Map<QName,String>> users = new ArrayList<>();
        //should be processed as csv
        this.uploadWebscript.processUpload(getResourceAsStream("userCSV.csv1"), "userCSV.csv1", users);
        assertEquals(2, users.size());
    }
}
