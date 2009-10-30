/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.ws.example;

import java.util.List;

import org.alfresco.repo.cmis.ws.CmisObjectInFolderType;
import org.alfresco.repo.cmis.ws.CmisObjectType;
import org.alfresco.repo.cmis.ws.CmisPropertiesType;
import org.alfresco.repo.cmis.ws.CmisProperty;
import org.alfresco.repo.cmis.ws.CmisPropertyId;
import org.alfresco.repo.cmis.ws.CmisPropertyString;

/**
 * This class expects next command-line parameters:<nobr />
 * <ul>
 * <li><b>Server Address</b> - with form: (IP_ADDRESS|DOMAIN_NAME):PORT;</li>
 * <li><b>Username</b> - login name of the existent user;</li>
 * <li><b>Password</b> - appropriate password for specified user.</li>
 * </ul>
 * <b>Example: <font color=green>192.168.0.1:8080 admin admin</font></b> - authenticate an user as admin on <font color=gray><b>http://192.168.0.1:8080/alfresco/</b></font> server
 * 
 * @author Dmitry Velichkevich
 */
public class CmisSampleClient
{
    private static final String NAME_PROPERTY = "cmis:name";
    private static final String BASE_TYPE_ID_PROPERTY = "cmis:baseTypeId";

    /**
     * Executable entry point - represents main life-cycle
     * 
     * @param args - <b>String[]</b> array that contains command line arguments
     */
    public static void main(String[] args)
    {
        String username = null;
        String password = null;
        String serverUrl = null;

        if (args.length != 3)
        {
            System.out.println("Usage: cmis-test.bat server_url username password");
            System.out.println("Example : cmis-test.bat http://localhost:8080 admin admin");
            return;
        }
        else
        {
            serverUrl = args[0];
            username = args[1];
            password = args[2];
        }

        CmisUtils servicesHelper;
        try
        {
            servicesHelper = new CmisUtils(username, password, serverUrl);
        }
        catch (Exception e)
        {
            System.out.println("Can't connect to specified server. Cause error message: " + e.getMessage());
            return;
        }

        List<CmisObjectInFolderType> response;
        try
        {
            response = servicesHelper.receiveFolderEntry(servicesHelper.getRootFolderId());
        }
        catch (Exception e)
        {
            System.out.println("Can't receive content of Company Home caused: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (null == response)
        {
            System.out.println("Children for Company Home were not returned");
        }
        else
        {
            System.out.println("Outing Company Home contents:");
            for (CmisObjectInFolderType item : response)
            {
                String itemName = "NULL";
                boolean thisIsFolder = false;
                if ((null != item) && (null != item.getObject()) && (null != item.getObject().getProperties()))
                {
                    CmisObjectType object = item.getObject();
                    thisIsFolder = ((CmisPropertyId) getCmisProperty(object.getProperties(), BASE_TYPE_ID_PROPERTY)).getValue().get(0).contains("folder");
                    itemName = ((CmisPropertyString) getCmisProperty(object.getProperties(), NAME_PROPERTY)).getValue().get(0);
                }
                System.out.println(((thisIsFolder) ? ("[") : ("")) + itemName + ((thisIsFolder) ? ("]") : ("")));
            }
        }
    }

    private static CmisProperty getCmisProperty(CmisPropertiesType properties, String cmisPropertyName)
    {
        for (CmisProperty cmisProperty : properties.getProperty())
        {
            if (cmisPropertyName.equals(getPropertyName(cmisProperty)))
            {
                return cmisProperty;
            }
        }

        return null;
    }

    private static String getPropertyName(CmisProperty property)
    {
        String result = null;
        if (null != property)
        {
            result = (null != property.getPropertyDefinitionId()) ? (property.getPropertyDefinitionId()) : (property.getLocalName());
            result = (null != result) ? (result) : (property.getDisplayName());
        }
        return result;
    }
}
