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

            cmisRepositoryEntryType[] repositories = repositoryService.getRepositories(null);
            string repositoryId = repositories[0].repositoryId;
            Console.WriteLine("Repositories description were received. Repositories amount: '" + repositories.Length + "'. First Repository Id='" + repositoryId + "'.");
            string rootFolder = repositoryService.getRepositoryInfo(repositoryId, null).rootFolderId;
            Console.WriteLine("Root folder Id='" + rootFolder + "'.\n");

            Console.WriteLine("Trying to get RepositoryInfo for the first repository:");
            cmisRepositoryInfoType repositoryInfo = repositoryService.getRepositoryInfo(repositoryId, null);
            Console.WriteLine("Repository info was received, name='" + repositoryInfo.repositoryName + "'.\n");

            try
            {
                Console.WriteLine("Actual Reaction of ObjectService.getProperties() service method with invalid Object Id:");
                objectService.getProperties(repositoryId, "Invalid Object Id", "*", null);
                Console.WriteLine(INVALID_REACTION_MESSAGE);
            }
            catch (FaultException<cmisFaultType> e)
            {
                Console.WriteLine("Expected error was returned. Message: " + e.Message + "\n");
            }

            cmisObjectInFolderListType childrenResponse = null;
            try
            {
                Console.WriteLine("Trying to receive the first 20 Children of Root Folder...");
                childrenResponse = navigationService.getChildren(repositoryId, rootFolder, "*", null, false, enumIncludeRelationships.none, null, false, "20", "0", null);
            }
            catch (FaultException<cmisFaultType> e)
            {
                Console.WriteLine("Can't receive children of Root Folder. Cause error message: " + e.Message);
            }

            if (null != childrenResponse && null != childrenResponse.objects)
            {
                Console.WriteLine("Children of Root Folder were received."); 
                Console.WriteLine("Total amount: '" + childrenResponse.numItems + "'");
                Console.WriteLine("Received: '" + childrenResponse.objects.Length + "'"); 
                Console.WriteLine("Has More Items='" + childrenResponse.hasMoreItems + "'");
                Console.WriteLine("Root folder listing: ");
                foreach (cmisObjectInFolderType cmisObject in childrenResponse.objects)
                {
                    if (null != cmisObject && null != cmisObject.@object)
                    {
                        cmisProperty nameProperty = searchForProperty(cmisObject.@object.properties, "cmis:name");
                        cmisProperty baseTypeProperty = searchForProperty(cmisObject.@object.properties, "cmis:baseTypeId");
                        Console.WriteLine((("[" + getPropertyValue(baseTypeProperty) + "] ")) + getPropertyValue(nameProperty) + "'");
                    }
                }
            }
        }

        private static void initialize()
        {
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
            if (null != property)
            {
                result = (null != property.propertyDefinitionId) ? (property.propertyDefinitionId) : (property.localName);
            }
            return result;
        }

        private static object getPropertyValue(cmisProperty property)
        {
            if (null != property)
            {
                Type propertyType = property.GetType();
                PropertyInfo valueProperty = propertyType.GetProperty("value");
                if ((null != valueProperty) && valueProperty.CanRead)
                {
                    object[] values = (object[])valueProperty.GetValue(property, null);
                    if ((null != values) && (values.Length > 0))
                    {
                        return values[0];
                    }
                }
            }
            return null;
        }

        private static cmisProperty searchForProperty(cmisPropertiesType properties, string propertyName)
        {
            if ((null != properties) && (null != properties.Items) && (properties.Items.Length > 0) && (null != propertyName) && !"".Equals(propertyName))
            {
                foreach (cmisProperty property in properties.Items)
                {
                    string name = getPropertyName(property);
                    if ((null != name) && name.Equals(propertyName))
                    {
                        return property;
                    }
                }
            }
            return null;
        }

    }
}
