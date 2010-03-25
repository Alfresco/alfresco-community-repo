package org.alfresco.repo.cmis.rest.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.chemistry.abdera.ext.CMISConstants;
import org.apache.chemistry.abdera.ext.CMISExtensionFactory;
import org.apache.chemistry.abdera.ext.CMISObject;
import org.apache.chemistry.abdera.ext.CMISProperties;
import org.apache.chemistry.abdera.ext.CMISProperty;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.StringRequestEntity;


public class BulkCreateAndQuerySystemTest
{
    private static Abdera abdera;
    private static final String URL = "http://localhost:8080";
    private static final String rootFolder = "testfolder";

    public static void main(String[] args) throws Exception {
      abdera = new Abdera();
      abdera.getFactory().registerExtension(new CMISExtensionFactory());

      AbderaClient client = new AbderaClient(abdera);

      client.setMaxConnectionsPerHost(100);
      client.addCredentials(URL, null, "basic", new UsernamePasswordCredentials("admin", "admin"));

      String rootId = createFolder(client, URL + "/alfresco/service/cmis/p/children", rootFolder);

      for (int i = 0; i < 10000; i++) {
        createFolder(client, URL + "/alfresco/service/cmis/p/" + rootFolder + "/children", "folder_" + i);
        findFolder(client, rootId, "folder_" + i);
      }
    }

    private static void findFolder(AbderaClient client, String inFolderId, String name) {
      System.out.println("querying for " + name + " in " + inFolderId);
      String query = "<query xmlns=\"http://docs.oasis-open.org/ns/cmis/core/200908/\">\n" +
          "<statement>SELECT cmis:Name, cmis:ObjectId FROM cmis:folder WHERE cmis:Name='" + name + "' AND IN_FOLDER('" + inFolderId + "')</statement>\n" +
          "<pageSize>100</pageSize>\n" +
          "<skipCount>0</skipCount>\n" +
          "</query>";

      StringRequestEntity requestEntity;
      try {
        requestEntity = new StringRequestEntity(query, "application/cmisquery+xml", "ISO-8859-1");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }

      ClientResponse response = client.post(URL + "/alfresco/service/cmis/queries", requestEntity);
      try {
        if (response.getType() == Response.ResponseType.SUCCESS) {

          Document<Feed> feed = response.getDocument();
          List<Entry> entries = feed.getRoot().getEntries();
          for (Entry entry : entries) {
            System.out.println("received query result with name " + entry.getTitle());
          }
        } else {
          throw new RuntimeException(response.getStatusText());
        }
      } finally {
        response.release();
      }
    }

    private static String createFolder(AbderaClient client, String parent, String name) throws Exception {
      Entry entry = abdera.newEntry();
      entry.setTitle(name);
      entry.setSummary("Summary...");

      CMISObject cmisObject = entry.addExtension(CMISConstants.OBJECT);
      CMISProperties properties = cmisObject.addExtension(CMISConstants.PROPERTIES);
      CMISProperty property = properties.addExtension(CMISConstants.ID_PROPERTY);
      property.setAttributeValue(CMISConstants.PROPERTY_ID, CMISConstants.PROP_OBJECT_TYPE_ID);
      Element value = property.addExtension(CMISConstants.PROPERTY_VALUE);
      value.setText("cmis:folder");
      
      ClientResponse resp = client.post(parent, entry);

      try {
        if (resp.getType() != Response.ResponseType.SUCCESS) {
          printError(resp);

          throw new RuntimeException(resp.getStatusText());
        } else {
          System.out.println("Created folder " + name);

          Document<Entry> entryDoc = resp.getDocument();
          Entry root = entryDoc.getRoot();

          CMISObject object = root.getExtension(CMISConstants.OBJECT);
          CMISProperties p = object.getProperties();

          return p.find(CMISConstants.PROP_OBJECT_ID).getStringValue();
        }
      } finally {
        resp.release();
      }
    }

    private static void printError(ClientResponse resp) {
      try {
        InputStream inputStream = resp.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        while (line != null) {
          System.out.println(line);
          line = reader.readLine();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
}
