using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Web.Services;
using System.Web.Services.Protocols;
using System.Net;
using System.ServiceModel;
using System.ServiceModel.Security;
using System.ServiceModel.Security.Tokens;
using System.Security.Cryptography.X509Certificates;
using System.Net.Security;
using System.ServiceModel.Channels;
using www.cmis.org.ns._1._01;

namespace CmisTest
{
    class Program
    {

        static void Main(string[] args)
        {
            ServicePointManager.ServerCertificateValidationCallback = delegate(object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
            {
                return true;
            };

            RepositoryServicePortClient repositoryService = new RepositoryServicePortClient();
            repositoryService.ClientCredentials.UserName.UserName = "admin";
            repositoryService.ClientCredentials.UserName.Password = "admin";

            NavigationServicePortClient navigationService = new NavigationServicePortClient();
            navigationService.ClientCredentials.UserName.UserName = "admin";
            navigationService.ClientCredentials.UserName.Password = "admin";

            ObjectServicePortClient objectService = new ObjectServicePortClient();
            objectService.ClientCredentials.UserName.UserName = "admin";
            objectService.ClientCredentials.UserName.Password = "admin";

            folderObjectType rootFolder = repositoryService.getRootFolder("*");
            Console.WriteLine("Root folder OID = {0}\n", rootFolder.objectID);

            try
            {
                repositoryService.getRootFolder("a");
            }
            catch (FaultException<basicFault> e)
            {
                Console.WriteLine(e.Message);
            }


            try
            {
                objectService.getProperties(rootFolder + "1", versionEnum.@this, "*");
            }
            catch (FaultException e)
            {
                Console.WriteLine(e.Message);
            }

            bool hasMoreItems;
            documentOrFolderObjectType[] rootFolderListing = navigationService.getChildren(rootFolder.objectID, typesOfObjectsEnum.FoldersAndDocumets, "*", null, null,
                                          out hasMoreItems);

            string guestFolderOID = null;

            Console.WriteLine("Root folder listing: ");
            foreach (documentOrFolderObjectType docFolder in rootFolderListing)
            {
                if (docFolder.name == "Guest Home")
                {
                    guestFolderOID = docFolder.objectID;
                }

                Console.WriteLine(docFolder.name);
            }

            if (guestFolderOID != null)
            {
                Console.Write("\nGet children for bad OID, error : ");
                try
                {
                    navigationService.getChildren(guestFolderOID + 1, typesOfObjectsEnum.FoldersAndDocumets, "*", null, null,
                                                  out hasMoreItems);
                }
                catch (FaultException e)
                {
                    Console.WriteLine(e.Message);
                }

                documentOrFolderObjectType[] guestFolderListing =
                    navigationService.getChildren(guestFolderOID, typesOfObjectsEnum.FoldersAndDocumets, "*", null, null,
                                                  out hasMoreItems);

                string alfrescoTutOID = null;

                Console.WriteLine("\nGuest folder listing: ");
                foreach (documentOrFolderObjectType docFolder in guestFolderListing)
                {
                    if (docFolder.name == "Alfresco-Tutorial.pdf")
                    {
                        alfrescoTutOID = docFolder.objectID;
                    }

                    Console.WriteLine(docFolder.name);
                }

                if (alfrescoTutOID != null)
                {
                    byte[] bytes = objectService.getContentStream(alfrescoTutOID, null, "100");
                    Console.WriteLine("\nAlfresco-Tutorial.pdf retrieved");
                }
            }

         }
    }
}
