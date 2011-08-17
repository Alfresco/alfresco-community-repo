/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.manifest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.transfer.PathHelper;
import org.alfresco.repo.transfer.TransferVersionImpl;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferVersion;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX XML Content Handler to read a transfer manifest XML Stream and
 * delegate processing of the manifest to the specified TransferManifestProcessor
 *
 * @author Mark Rogers
 */
public class XMLTransferManifestReader extends DefaultHandler implements ContentHandler, NamespacePrefixResolver
{
    private  TransferManifestProcessor processor;

    private static final String MSG_NO_ENCODING = "transfer_service.no_encoding";
    private static final String MSG_UNABLE_DESERIALIZE = "transfer_service.unable_to_deserialise";

    /**
     * These are the namespaces used within the document - there may be a different mapping to
     * the namespaces of the Data Dictionary.
     */
    LinkedList<HashMap<String, String>> namespaces = new LinkedList<HashMap<String, String>>();
    final String TRANSFER_URI = ManifestModel.TRANSFER_MODEL_1_0_URI;
    final String XMLNS_URI = "http://www.w3.org/XML/1998/namespace";

    public XMLTransferManifestReader(TransferManifestProcessor snapshotProcessor)
    {
        this.processor = snapshotProcessor;

        // prefix to uri map
        HashMap<String, String> namespace = new HashMap<String, String>();
        namespace.put("xmlns", XMLNS_URI);
        namespaces.add(namespace);
    }

    public void startDocument() throws SAXException
    {
        processor.startTransferManifest();
    }

    public void endDocument() throws SAXException
    {
        processor.endTransferManifest();
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if(buffer != null)
        {
            buffer.append(ch, start, length);
        }
    }

    /*
     *  Current State of the parser
     */
    private StringBuffer buffer;
    private Map<String, Object>props = new HashMap<String, Object>();

    /**
     * Start Element
     */
    public void startElement(String uri, String localName, String prefixName, Attributes atts)
    throws SAXException
    {
        QName elementQName = QName.resolveToQName(this, prefixName);

        HashMap<String, String> namespace = new HashMap<String, String>();
        namespaces.addFirst(namespace);

        /**
         * Look for any namespace attributes
         */
        for(int i = 0; i < atts.getLength(); i++)
        {
            QName attributeQName = QName.resolveToQName(this, atts.getQName(i));
            if(attributeQName.getNamespaceURI().equals(XMLNS_URI))
            {
                namespace.put(attributeQName.getLocalName(), atts.getValue(i));
            }
        }

        if(elementQName == null)
        {
            return;
        }

        if(elementQName.getNamespaceURI().equals(TRANSFER_URI));
        {
            // This is one of the transfer manifest elements
            String elementName = elementQName.getLocalName();

            // Simple and stupid parser for now
            if(elementName.equals(ManifestModel.LOCALNAME_TRANSFER_MAINIFEST))
            {
                // Good we got this
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_TRANSFER_HEADER))
            {
                TransferManifestHeader header = new TransferManifestHeader();
                props.put("header", header);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_DELETED_NODE))
            {
                TransferManifestDeletedNode node = new TransferManifestDeletedNode();
                NodeRef nodeRef = new NodeRef(atts.getValue("", "nodeRef"));
                node.setNodeRef(nodeRef);
                props.put("node", node);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_NODE))
            {
                TransferManifestNormalNode node = new TransferManifestNormalNode();
                NodeRef nodeRef = new NodeRef(atts.getValue("", "nodeRef"));
                QName type = QName.createQName(atts.getValue("", "nodeType"));
                node.setNodeRef(nodeRef);
                node.setType(type);
                QName ancestorType = QName.createQName(atts.getValue("", "ancestorType"));
                node.setAncestorType(ancestorType);
                props.put("node", node);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ASPECTS))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                node.setAspects(new HashSet<QName>());
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ASPECT))
            {
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PROPERTIES))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                HashMap<QName, Serializable>properties = new HashMap<QName, Serializable>();
                node.setProperties(properties);
                props.put("properties", properties);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PROPERTY))
            {
                QName name = QName.createQName(atts.getValue("", "name"));
                props.put("name", name);
                props.remove("values");
                props.remove("mlvalues");
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_CREATED_DATE))
            {
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_NODE_COUNT))
            {
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_REPOSITORY_ID))
            {
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOCS))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                ArrayList<ChildAssociationRef> parentAssocs = new ArrayList<ChildAssociationRef>();
                node.setParentAssocs(parentAssocs);
                // To receive the primary parent assoc.
                props.put("parentAssocs", parentAssocs);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_CHILD_ASSOCS))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                node.setChildAssocs(new ArrayList<ChildAssociationRef>());
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_CHILD_ASSOC))
            {
                buffer = new StringBuffer();
                NodeRef to = new NodeRef(atts.getValue("", "to"));
                QName type = QName.createQName(atts.getValue("", "type"));
                Boolean isPrimary = Boolean.parseBoolean(atts.getValue("", "isPrimary"));

                props.put("to", to);
                props.put("type", type);
                props.put("isPrimary", isPrimary);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOC))
            {
                buffer = new StringBuffer();
                NodeRef from = new NodeRef(atts.getValue("", "from"));
                QName type = QName.createQName(atts.getValue("", "type"));
                Boolean isPrimary = Boolean.parseBoolean(atts.getValue("", "isPrimary"));
                props.put("from", from);
                props.put("type", type);
                props.put("isPrimary", isPrimary);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_TARGET_ASSOCS))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                List<AssociationRef> assocs = new ArrayList<AssociationRef>();
                node.setTargetAssocs(assocs);
                props.put("assocs", assocs);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_SOURCE_ASSOCS))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                List<AssociationRef> assocs = new ArrayList<AssociationRef>();
                node.setSourceAssocs(assocs);
                props.put("assocs", assocs);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ASSOC))
            {
                NodeRef source = new NodeRef(atts.getValue("", "source"));
                NodeRef target = new NodeRef(atts.getValue("", "target"));
                QName type = QName.createQName(atts.getValue("", "type"));
                props.put("source", source);
                props.put("target", target);
                props.put("type", type);

            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PRIMARY_PARENT))
            {
                buffer = new StringBuffer();

                ArrayList<ChildAssociationRef> parentAssocs = new ArrayList<ChildAssociationRef>();
                // Synthetic element - To receive the primary parent assoc.
                props.put("parentAssocs", parentAssocs);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUES))
            {
                Collection<Serializable> values = new ArrayList<Serializable>();
                props.put("values", values);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUE_STRING))
            {
                props.put("className", atts.getValue("", "className"));
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUE_NULL))
            {
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUE_SERIALIZED))
            {
                props.put("encoding", atts.getValue("", "encoding"));
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_MLVALUE))
            {
                MLText mltext = (MLText)props.get("mlvalues");
                if(mltext == null)
                {
                    mltext = new MLText();
                    props.put("mlvalues", mltext);
                }
                String strLocale = (String)atts.getValue("", "locale");
                Locale locale = I18NUtil.parseLocale(strLocale);
                props.put("locale", locale);
                buffer = new StringBuffer();
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_CONTENT_HEADER))
            {
                String contentURL = (String)atts.getValue("", "contentURL");
                String mimetype = (String)atts.getValue("", "mimetype");
                String strLocale = (String)atts.getValue("", "locale");
                Locale locale = I18NUtil.parseLocale(strLocale);
                String encoding = (String)atts.getValue("", "encoding");
                String sizeStr = (String)atts.getValue("", "size");
                Long size = Long.valueOf(sizeStr);
                ContentData contentHeader = new ContentData(contentURL, mimetype, size.longValue(), encoding, locale);
                props.put("contentHeader", contentHeader);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ACL))
            {
                String isInherited = (String)atts.getValue("", "isInherited");
                ManifestAccessControl acl = new ManifestAccessControl();

                if("TRUE".equalsIgnoreCase(isInherited))
                {
                    acl.setInherited(true);
                }
                props.put("acl", acl);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_VERSION))
            {
                String versionMajor = (String)atts.getValue("", "versionMajor");
                String versionMinor = (String)atts.getValue("", "versionMinor");
                String versionRevision = (String)atts.getValue("", "versionRevision");
                String edition = (String)atts.getValue("", "edition");

                props.put("headerVersion", new TransferVersionImpl(versionMajor, versionMinor, versionRevision, edition));
            }

            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ACL_PERMISSION))
            {

                String authority = (String)atts.getValue("", "authority");
                String permission = (String)atts.getValue("", "permission");
                String status = (String)atts.getValue("", "status");
                ManifestPermission perm = new ManifestPermission();
                perm.setAuthority(authority);
                perm.setPermission(permission);
                perm.setStatus(status);
                props.put("permission", perm);
            }
        } // if transfer URI
    } // startElement

    /**
     * End Element
     */
    @SuppressWarnings("unchecked")
    public void endElement(String uri, String localName, String prefixName) throws SAXException
    {
        namespaces.removeFirst();

        QName elementQName = QName.resolveToQName(this, prefixName);

        if(elementQName == null)
        {
            return;
        }

        if(elementQName.getNamespaceURI().equals(TRANSFER_URI));
        {
            // This is one of the transfer manifest elements
            String elementName = elementQName.getLocalName();

            if(elementName.equals(ManifestModel.LOCALNAME_TRANSFER_MAINIFEST))
            {
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_TRANSFER_HEADER))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                // User to process the header
                processor.processTransferManifiestHeader(header);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_NODE))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                processor.processTransferManifestNode(node);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_DELETED_NODE))
            {
                TransferManifestDeletedNode node = (TransferManifestDeletedNode)props.get("node");
                processor.processTransferManifestNode(node);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ASPECTS))
            {

            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ASPECT))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                node.getAspects().add(QName.createQName(buffer.toString()));
                buffer = null;
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PROPERTIES))
            {
                // nothing to do
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PROPERTY))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                QName name = (QName)props.get("name");
                Serializable value = (Serializable)props.get("value");
                node.getProperties().put(name, value);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_CREATED_DATE))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                header.setCreatedDate(ISO8601DateFormat.parse(buffer.toString()));
                buffer = null;
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_NODE_COUNT))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                header.setNodeCount(Integer.parseInt(buffer.toString()));
                buffer = null;
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_SYNC))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                header.setSync(true);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_RONLY))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                header.setReadOnly(true);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_REPOSITORY_ID))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                header.setRepositoryId(buffer.toString());
                buffer = null;
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOCS))
            {
                // No-op
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_CHILD_ASSOCS))
            {
                // No-op
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_CHILD_ASSOC))
            {
                String value = buffer.toString();
                QName name = QName.createQName(value);
                NodeRef to = (NodeRef)props.get("to");
                QName type = (QName) props.get("type");
                Boolean isPrimary = (Boolean)props.get("isPrimary");
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");

                ChildAssociationRef childAssociationRef = new ChildAssociationRef(type, node.getNodeRef(), name, to, isPrimary, -1);
                node.getChildAssocs().add(childAssociationRef);

            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PARENT_ASSOC))
            {
                String value = buffer.toString();
                QName name = QName.createQName(value);
                NodeRef from = (NodeRef)props.get("from");
                QName type = (QName) props.get("type");
                Boolean isPrimary = (Boolean)props.get("isPrimary");
                TransferManifestNode node = (TransferManifestNode)props.get("node");
                List<ChildAssociationRef> parentAssocs =    (List<ChildAssociationRef>)props.get("parentAssocs");
                ChildAssociationRef childAssociationRef = new ChildAssociationRef(type, from, name, node.getNodeRef(), isPrimary, -1);
                parentAssocs.add(childAssociationRef);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_TARGET_ASSOCS))
            {
                //TransferManifestNode node = (TransferManifestNode)props.get("node");
                //node.getTargetAssocs().add((AssociationRef)props.get("assoc"));
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_SOURCE_ASSOCS))
            {
                //TransferManifestNode node = (TransferManifestNode)props.get("node");
                //node.getSourceAssocs().add((AssociationRef)props.get("assoc"));
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ASSOC))
            {
                NodeRef source =  (NodeRef)props.get("source");
                NodeRef target =  (NodeRef)props.get("target");
                QName type =  (QName) props.get("type");
                List<AssociationRef> assocs = (List<AssociationRef>)props.get("assocs");
                AssociationRef assoc = new AssociationRef(null, source, type, target);
                assocs.add(assoc);
                props.put("assoc", new AssociationRef(null, source, type, target));
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PRIMARY_PARENT))
            {
                TransferManifestNode node = (TransferManifestNode)props.get("node");
                List<ChildAssociationRef> parentAssocs =    (List<ChildAssociationRef>)props.get("parentAssocs");
                if(parentAssocs != null)
                {
                    // Size should allways be 1.
                    assert(parentAssocs.size() == 1);
                    node.setPrimaryParentAssoc(parentAssocs.get(0));
                }
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_PRIMARY_PATH))
            {
                TransferManifestNode node = (TransferManifestNode)props.get("node");
                String value = buffer.toString();
                Path path = PathHelper.stringToPath(value);
                node.setParentPath(path);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUES))
            {
                props.put("value",  props.get("values"));
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUE_STRING))
            {
                Collection<Serializable> values =  (Collection<Serializable>)props.get("values");
                String className = (String)props.get("className");

                Serializable value = buffer.toString();

                if(className != null && !className.equals("java.lang.String"))
                {
                    // value is not a string and needs to be converted
                    try
                    {
                        value = (Serializable)DefaultTypeConverter.INSTANCE.convert(Class.forName(className), value);
                    }
                    catch (TypeConversionException tcf)
                    {
                        // leave value as string
                    }
                    catch (ClassNotFoundException cnf)
                    {
                        // leave value as string
                    }
                }

                if(values != null)
                {
                    values.add(value);
                }
                else
                {
                    props.put("value", value);
                }
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUE_NULL))
            {
                Collection<Serializable> values =  (Collection<Serializable>)props.get("values");

                if(values != null)
                {
                    values.add(null);
                }
                else
                {
                    props.put("value", null);
                }
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_VALUE_SERIALIZED))
            {
                Collection<Serializable> values =  (Collection<Serializable>)props.get("values");
                String encoding = (String)props.get("encoding");

                String strValue = buffer.toString();
                Object value = null;

                if(encoding.equalsIgnoreCase("base64/ObjectOutputStream"))
                {
                    try
                    {
                        byte[] data = Base64.decode(strValue.getBytes("UTF-8"));
                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                        value = ois.readObject();
                    }
                    catch (IOException error)
                    {
                        throw new TransferException(MSG_UNABLE_DESERIALIZE, error);
                    }
                    catch (ClassNotFoundException error)
                    {
                        throw new TransferException(MSG_UNABLE_DESERIALIZE, error);
                    }
                }
                else
                {
                    throw new TransferException(MSG_NO_ENCODING, new Object[]{encoding});
                }

                if(values != null)
                {
                    // This is a values array
                    values.add((Serializable)value);
                }
                else
                {
                    // This is a single value
                    props.put("value", value);
                }
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_MLVALUE))
            {
                MLText mltext = (MLText)props.get("mlvalues");
                Locale locale = (Locale)props.get("locale");
                String value = buffer.toString();
                mltext.addValue(locale, value);
                props.put("value", mltext);

            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_CONTENT_HEADER))
            {
                ContentData data = (ContentData)props.get("contentHeader");
                props.put("value", data);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_HEADER_VERSION))
            {
                TransferManifestHeader header =  (TransferManifestHeader)props.get("header");
                TransferVersion version = (TransferVersion)props.get("headerVersion");
                header.setTransferVersion(version);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ACL))
            {
                TransferManifestNormalNode node = (TransferManifestNormalNode)props.get("node");
                ManifestAccessControl acl = (ManifestAccessControl)props.get("acl");
                node.setAccessControl(acl);
            }
            else if(elementName.equals(ManifestModel.LOCALNAME_ELEMENT_ACL_PERMISSION))
            {
                ManifestAccessControl acl = (ManifestAccessControl)props.get("acl");
                ManifestPermission permission = (ManifestPermission)props.get("permission");
                acl.addPermission(permission);
            }


        } // if transfer URI
    } // end element



    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
      //NO-OP
    }

    public void processingInstruction(String target, String data) throws SAXException
    {
      //NO-OP
    }

    public void setDocumentLocator(Locator locator)
    {
      //NO-OP
    }

    public void skippedEntity(String name) throws SAXException
    {
      //NO-OP
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        HashMap<String, String> namespace = namespaces.get(0);
        // prefix is key, URI is value
        namespace.put(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
        HashMap<String, String> namespace = namespaces.get(0);
        // prefix is key, URI is value
        namespace.remove(prefix);
    }

    // Namespace Prefix Resolver implementation below

    /**
    * lookup the prefix for a URI e.g. TRANSFER_URI for xfer
    */
    public String getNamespaceURI(String prefix) throws NamespaceException
    {
        for(HashMap<String, String> namespace : namespaces)
        {
            String uri = namespace.get(prefix);
            if(uri != null)
            {
                return uri;
            }
        }
        return null;
    }

    /**
     * @param uri
     * @return the prefix
     */
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        Collection<String> prefixes = new HashSet<String>();

        for(HashMap<String, String> namespace : namespaces)
        {
            for(Entry<String, String> entry : namespace.entrySet())
            {
                if (namespaceURI.equals(entry.getValue()))
                {
                    prefixes.add(entry.getKey());
                }
            }
        }

        return prefixes;
    }

    public Collection<String> getPrefixes()
    {
        Collection<String> prefixes = new HashSet<String>();

        for(HashMap<String, String> namespace : namespaces)
        {
            prefixes.addAll(namespace.keySet());
        }

        return prefixes;
    }

    public Collection<String> getURIs()
    {
        Collection<String> uris = new HashSet<String>();

        for(HashMap<String, String> namespace : namespaces)
        {
            uris.addAll(namespace.values());
        }

        return uris;
    }
}
