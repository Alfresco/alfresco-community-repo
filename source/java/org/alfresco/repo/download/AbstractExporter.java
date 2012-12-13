package org.alfresco.repo.download;

import java.io.InputStream;
import java.util.Locale;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.namespace.QName;

public class AbstractExporter implements Exporter
{

    @Override
    public void start(ExporterContext context)
    {
    }

    @Override
    public void startNamespace(String prefix, String uri)
    {
    }

    @Override
    public void endNamespace(String prefix)
    {
    }

    @Override
    public void startNode(NodeRef nodeRef)
    {
    }

    @Override
    public void endNode(NodeRef nodeRef)
    {
    }

    @Override
    public void startReference(NodeRef nodeRef, QName childName)
    {
    }

    @Override
    public void endReference(NodeRef nodeRef)
    {
    }

    @Override
    public void startAspects(NodeRef nodeRef)
    {
    }

    @Override
    public void startAspect(NodeRef nodeRef, QName aspect)
    {
    }

    @Override
    public void endAspect(NodeRef nodeRef, QName aspect)
    {
    }

    @Override
    public void endAspects(NodeRef nodeRef)
    {
    }

    @Override
    public void startACL(NodeRef nodeRef)
    {
    }

    @Override
    public void permission(NodeRef nodeRef, AccessPermission permission)
    {
    }

    @Override
    public void endACL(NodeRef nodeRef)
    {
    }

    @Override
    public void startProperties(NodeRef nodeRef)
    {
    }

    @Override
    public void startProperty(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void endProperty(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void endProperties(NodeRef nodeRef)
    {
    }

    @Override
    public void startValueCollection(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull)
    {
    }

    @Override
    public void endValueMLText(NodeRef nodeRef)
    {
    }

    @Override
    public void value(NodeRef nodeRef, QName property, Object value, int index)
    {
    }

    @Override
    public void content(NodeRef nodeRef, QName property, InputStream content,
                ContentData contentData, int index)
    {
    }

    @Override
    public void endValueCollection(NodeRef nodeRef, QName property)
    {
    }

    @Override
    public void startAssocs(NodeRef nodeRef)
    {
    }

    @Override
    public void startAssoc(NodeRef nodeRef, QName assoc)
    {
    }

    @Override
    public void endAssoc(NodeRef nodeRef, QName assoc)
    {
    }

    @Override
    public void endAssocs(NodeRef nodeRef)
    {
    }

    @Override
    public void warning(String warning)
    {
    }

    @Override
    public void end()
    {
    }

}
