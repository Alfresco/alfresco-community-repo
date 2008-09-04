/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws.example;

import java.util.List;

import org.alfresco.repo.cmis.ws.FolderTreeType;
import org.alfresco.repo.cmis.ws.PropertiesType;
import org.alfresco.repo.cmis.ws.PropertyStringType;

/**
 * This class executes simple processing for prompting server address and user name and password for authentication this user on specified server. After successful connection this
 * class print contents of Company Home.<br />
 * <br />
 * This class expects next command-line parameters:<nobr />
 * <ul>
 * <li><b>Server Address</b> - with form: (IP_ADDRESS|DOMAIN_NAME):PORT;</li>
 * <li><b>Username</b> - login name of the existent user;</li>
 * <li><b>Password</b> - appropriate password for specified user.</li>
 * </ul>
 * <b>Example: <font color=green>192.168.0.1:8080 admin admin</font></b> - authenticate an user as admin on <font color=gray><b>http://192.168.0.1:8080/alfresco/</b></font>
 * server
 *
 * @author Dmitry Velichkevich
 */
public class SimpleExecutableCmisServicesUtilizer
{
    private static final String FOLDER_IDENTIFICATOR_SAMPLE_STRING = "folder";

    /**
     * Executable entry point - represents main life cycle
     *
     * @param args - not used
     * @see SimpleCmisWsTest description
     */
    public static void main(String[] args)
    {
        String username = null;
        String password = null;
        String serverUrl = null;

        if (args.length != 3)
        {
            System.out.println("Usage: cmis-test.bat server_url username password");
            System.out.println("Example : cmis-test.bat http://localhost:8080/ admin admin");
            return;
        }
        else
        {
            serverUrl = args[0];
            username = args[1];
            password = args[2];
        }

        ExecutableServicesHelper servicesHelper;

        try
        {
            servicesHelper = new ExecutableServicesHelper(username, password, serverUrl);
        }
        catch (Exception e)
        {
            System.out.println("Can't connect to specified server. Message: " + e.getMessage());
            return;
        }

        List<FolderTreeType> response;

        try
        {
            response = servicesHelper.receiveSpaceContent(servicesHelper.receiveAuthorizedNavigationServicePort());
        }
        catch (Exception e)
        {
            System.out.println("Can't receive content of Company Home caused: " + e.getMessage());
            return;
        }

        System.out.println("Outing Company Home contents:");
        for (FolderTreeType item : response)
        {
            if (getPropertyStringValue(item.getProperties(), "baseType").contains(FOLDER_IDENTIFICATOR_SAMPLE_STRING))
            {
                System.out.println("[" + getPropertyStringValue(item.getProperties(), "name") + "]");
            }
            else
            {
                System.out.println(getPropertyStringValue(item.getProperties(), "name"));
            }
        }
    }

    private static String getPropertyStringValue(PropertiesType properties, String propertyName)
    {
        String result = null;

        for (PropertyStringType property : properties.getPropertyString())
        {
            if (propertyName.equals(property.getName()))
            {
                result = property.getValue();
                break;
            }
        }

        return result;
    }

}
