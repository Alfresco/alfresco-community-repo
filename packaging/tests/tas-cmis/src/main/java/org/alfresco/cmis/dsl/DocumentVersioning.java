package org.alfresco.cmis.dsl;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.cmis.CmisWrapper;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.testng.Assert;

/**
 * DSL utility for verifying a document version {@link Document}
 */
public class DocumentVersioning
{
    private CmisWrapper cmisWrapper;
    private CmisObject cmisObject;
    private boolean majorVersion;
    private Object versionLabel;
    private List<Object> versions;

    public DocumentVersioning(CmisWrapper cmisWrapper, CmisObject cmisObject)
    {
        this.cmisWrapper = cmisWrapper;
        this.cmisObject = cmisObject;
    }

    private DocumentVersioning withLatestMajorVersion()
    {
        this.majorVersion = true;
        return this;
    }

    private DocumentVersioning withLatestMinorVersion()
    {
        this.majorVersion = false;
        return this;
    }

    private Document getVersionOfDocument()
    {
        Document document = (Document) cmisObject;
        if (versionLabel != null)
        {
            List<Document> documents = document.getAllVersions();
            for (Document documentVersion : documents)
                if (documentVersion.getVersionLabel().equals(versionLabel.toString()))
                    return documentVersion;
        }
        else
        {
            return document.getObjectOfLatestVersion(majorVersion);
        }
        return document;
    }

    private List<Object> getDocumentVersions(List<Document> documentList)
    {
        List<Object> versions = new ArrayList<Object>();
        for (Document document : documentList)
        {
            versions.add(document.getVersionLabel());
        }
        return versions;
    }

    public CmisWrapper assertVersionIs(Double expectedVersion)
    {
        STEP(String.format("%s Verify if document '%s' has version '%s'", cmisWrapper.getProtocolName(), cmisObject.getName(), expectedVersion));
        Assert.assertEquals(getVersionOfDocument().getVersionLabel(), expectedVersion.toString(), "File has version");
        return cmisWrapper;
    }

    public CmisWrapper assertLatestMajorVersionIs(Double expectedVersion)
    {
        STEP(String.format("%s Verify if latest major version of document '%s' is '%s'", cmisWrapper.getProtocolName(), cmisObject.getName(), expectedVersion));
        Assert.assertEquals(withLatestMajorVersion().getVersionOfDocument().getVersionLabel(), expectedVersion.toString(), "File has version");
        return cmisWrapper;
    }

    public CmisWrapper assertLatestMinorVersionIs(Double expectedVersion)
    {
        STEP(String.format("%s Verify if latest minor version of document '%s' is '%s'", cmisWrapper.getProtocolName(), cmisObject.getName(), expectedVersion));
        Assert.assertEquals(withLatestMinorVersion().getVersionOfDocument().getVersionLabel(), expectedVersion.toString(), "File has version");
        return cmisWrapper;
    }

    public DocumentVersioning getAllDocumentVersions()
    {
        setVersions(getDocumentVersions(cmisWrapper.withCMISUtil().getAllDocumentVersions()));
        return this;
    }

    public CmisWrapper assertHasVersions(Object... versions)
    {
        setVersions(getDocumentVersions(cmisWrapper.withCMISUtil().getAllDocumentVersions()));
        List<Object> documentVersions = getVersions();
        for (Object version : versions)
        {
            STEP(String.format("%s Verify if document '%s' has version '%s'", cmisWrapper.getProtocolName(), cmisObject.getName(), version));
            Assert.assertTrue(documentVersions.contains(version.toString()),
                    String.format("Document %s does not have version %s", cmisObject.getName(), version));
        }
        return cmisWrapper;
    }

    public DocumentVersioning getAllDocumentVersionsBy(OperationContext context)
    {
        setVersions(getDocumentVersions(cmisWrapper.withCMISUtil().getAllDocumentVersionsBy(context)));
        return this;
    }

    public CmisWrapper assertHasVersionsInOrder(Object... versions)
    {
        List<Object> documentVersions = getVersions();
        List<Object> expectedVersions = Arrays.asList(versions);
        STEP(String.format("%s Verify if document '%s' has versions in this order '%s'", cmisWrapper.getProtocolName(), cmisObject.getName(),
                Arrays.toString(expectedVersions.toArray())));
        Assert.assertTrue(documentVersions.toString().equals(expectedVersions.toString()),
                String.format("Document %s does not have versions in this order %s", cmisObject.getName(), Arrays.toString(expectedVersions.toArray())));
        return cmisWrapper;
    }

    public List<Object> getVersions()
    {
        return versions;
    }

    public void setVersions(List<Object> versions)
    {
        this.versions = versions;
    }
}
