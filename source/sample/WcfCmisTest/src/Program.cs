/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
using System;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using WcfCmisWSTests.CmisServices;
using System.Reflection;

namespace CmisTest
{
    public class Program
    {
        private const string INVALID_REACTION_MESSAGE = "Invalid Reaction! Exception should be thrown";

        private static RepositoryServicePortClient repositoryService = null;
        private static NavigationServicePortClient navigationService = null;
        private static ObjectServicePortClient objectService = null;

        public static void Main(string[] args)
        {
            initialize();

            cmisRepositoryEntryType[] repositories = repositoryService.getRepositories();
            string repositoryId = repositories[0].id;
            Console.WriteLine("Repositories description were received. Repositories amount: '" + repositories.Length + "'. First Repository Id='" + repositoryId + "'.");
            string rootFolder = repositoryService.getRepositoryInfo(repositoryId).rootFolderId;
            Console.WriteLine("Root folder Id='" + rootFolder + "'.\n");

            try
            {
                Console.WriteLine("Actual Reaction of RepositoryService.getRepositoryInfo() service method with invalid Repository Id:");
                repositoryService.getRepositoryInfo("Invalid Repository Id");
                Console.WriteLine(INVALID_REACTION_MESSAGE);
            }
            catch (FaultException<cmisFaultType> e)
            {
                Console.WriteLine("    " + e.Message);
            }

            try
            {
                Console.WriteLine("Actual Reaction of ObjectService.getProperties() service method with invalid Object Id:");
                objectService.getProperties(repositoryId, ("Invalid Object Id"), "*", false, null, false);
                Console.WriteLine(INVALID_REACTION_MESSAGE);
            }
            catch (FaultException<cmisFaultType> e)
            {
                Console.WriteLine("    " + e.Message + "\n");
            }

            cmisObjectType[] childrenResponse = null; 
            try
            {
                bool hasMoreItems;
                Console.WriteLine("Trying to receive Children Objects of Root Folder...");
                childrenResponse = navigationService.getChildren(repositoryId, rootFolder, "*", false, null, false, false, "0", "0", null, out hasMoreItems);
                Console.WriteLine("Children of Root Folder were received. Elements amount: '" + childrenResponse.Length + "'. Has More Items='" + hasMoreItems + "' (how it WAS " + ((hasMoreItems) ? ("NOT ") : ("")) + "expected).");
            } catch (FaultException<cmisFaultType> e) {
                Console.WriteLine("Can't receive Children of Root Folder. Cause error message: " + e.Message);
            }

            if (null != childrenResponse) {
                Console.WriteLine("Root folder listing: ");
                foreach (cmisObjectType cmisObject in childrenResponse) {
                    if (null != cmisObject) {
                        cmisProperty nameProperty = searchForProperty(cmisObject.properties, "cmis:Name");
                        cmisProperty baseTypeProperty = searchForProperty(cmisObject.properties, "cmis:BaseTypeId");
                        Console.WriteLine((("cmis:folder".Equals(getPropertyValue(baseTypeProperty))) ? ("Folder") : ("Document")) + " Child with Name='" + getPropertyValue(nameProperty) + "'");
                    }
                }
            }
        }

        private static void initialize() {
            ServicePointManager.ServerCertificateValidationCallback = delegate(object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
            {
                return true;
            };

            repositoryService = new RepositoryServicePortClient();
            repositoryService.ClientCredentials.UserName.UserName = "admin";
            repositoryService.ClientCredentials.UserName.Password = "admin";
            navigationService = new NavigationServicePortClient();
            navigationService.ClientCredentials.UserName.UserName = "admin";
            navigationService.ClientCredentials.UserName.Password = "admin";
            objectService = new ObjectServicePortClient();
            objectService.ClientCredentials.UserName.UserName = "admin";
            objectService.ClientCredentials.UserName.Password = "admin";
        }

        private static string getPropertyName(cmisProperty property)
        {
            string result = null;
            if (null != property) {
                result = (null != property.pdid) ? (property.pdid):(property.localname);
                result = (null != result) ? (result):(property.displayname);
            }
            return result;
        }

        private static object getPropertyValue(cmisProperty property) {
            if (null != property) {
                Type propertyType = property.GetType();
                PropertyInfo valueProperty = propertyType.GetProperty("value");
                if ((null != valueProperty) && valueProperty.CanRead) {
                    object[] values = (object[])valueProperty.GetValue(property, null);
                    if ((null != values) && (values.Length > 0)) {
                        return values[0];
                    }
                }
            }
            return null;
        }

        private static cmisProperty searchForProperty(cmisPropertiesType properties, string propertyName) {
            if((null != properties) && (null != properties.Items) && (properties.Items.Length > 0) && (null != propertyName) && !"".Equals(propertyName)) {
                foreach(cmisProperty property in properties.Items) {
                    string name = getPropertyName(property);
                    if((null != name) && name.Equals(propertyName)) {
                        return property;
                    }
                }
            }
            return null;
        }

    }
}
