/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.authentication.ldap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ExportSource;
import org.alfresco.repo.importer.ExportSourceImporterException;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class LDAPGroupExportSource implements ExportSource, InitializingBean
{
    private static Log s_logger = LogFactory.getLog(LDAPGroupExportSource.class);

    private String groupQuery = "(objectclass=groupOfNames)";

    private String searchBase;

    private String groupIdAttributeName = "cn";

    private String userIdAttributeName = "uid";

    private String groupType = "groupOfNames";

    private String personType = "inetOrgPerson";

    private LDAPInitialDirContextFactory ldapInitialContextFactory;

    private NamespaceService namespaceService;

    private String memberAttribute = "member";

    private boolean errorOnMissingMembers = false;

    private QName viewRef;

    private QName viewId;

    private QName viewAssociations;

    private QName childQName;

    private QName viewValueQName;

    private QName viewIdRef;

    private AuthorityDAO authorityDAO;

    public LDAPGroupExportSource()
    {
        super();
    }

    public void setGroupIdAttributeName(String groupIdAttributeName)
    {
        this.groupIdAttributeName = groupIdAttributeName;
    }

    public void setGroupQuery(String groupQuery)
    {
        this.groupQuery = groupQuery;
    }

    public void setGroupType(String groupType)
    {
        this.groupType = groupType;
    }

    public void setLDAPInitialDirContextFactory(LDAPInitialDirContextFactory ldapInitialDirContextFactory)
    {
        this.ldapInitialContextFactory = ldapInitialDirContextFactory;
    }

    public void setMemberAttribute(String memberAttribute)
    {
        this.memberAttribute = memberAttribute;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setPersonType(String personType)
    {
        this.personType = personType;
    }

    public void setSearchBase(String searchBase)
    {
        this.searchBase = searchBase;
    }

    public void setUserIdAttributeName(String userIdAttributeName)
    {
        this.userIdAttributeName = userIdAttributeName;
    }

    public void setErrorOnMissingMembers(boolean errorOnMissingMembers)
    {
        this.errorOnMissingMembers = errorOnMissingMembers;
    }

    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    public void generateExport(XMLWriter writer)
    {
        HashSet<Group> rootGroups = new HashSet<Group>();
        HashMap<String, Group> lookup = new HashMap<String, Group>();
        HashSet<SecondaryLink> secondaryLinks = new HashSet<SecondaryLink>();

        buildGroupsAndRoots(rootGroups, lookup, secondaryLinks);

        buildXML(rootGroups, lookup, secondaryLinks, writer);

    }

    private void buildXML(HashSet<Group> rootGroups, HashMap<String, Group> lookup,
            HashSet<SecondaryLink> secondaryLinks, XMLWriter writer)
    {

        Collection<String> prefixes = namespaceService.getPrefixes();
        QName childQName = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "childName", namespaceService);

        try
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, childQName.getLocalName(), childQName
                    .toPrefixString(), null, ContentModel.TYPE_PERSON.toPrefixString(namespaceService));

            writer.startDocument();

            for (String prefix : prefixes)
            {
                if (!prefix.equals("xml"))
                {
                    String uri = namespaceService.getNamespaceURI(prefix);
                    writer.startPrefixMapping(prefix, uri);
                }
            }

            writer.startElement(NamespaceService.REPOSITORY_VIEW_PREFIX, "view",
                    NamespaceService.REPOSITORY_VIEW_PREFIX + ":" + "view", new AttributesImpl());

            // Create group structure

            for (Group group : rootGroups)
            {
                addRootGroup(lookup, group, writer);
            }

            // Create secondary links.

            for (SecondaryLink sl : secondaryLinks)
            {
                addSecondarylink(lookup, sl, writer);
            }

            for (String prefix : prefixes)
            {
                if (!prefix.equals("xml"))
                {
                    writer.endPrefixMapping(prefix);
                }
            }

            writer.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, "view", NamespaceService.REPOSITORY_VIEW_PREFIX
                    + ":" + "view");

            writer.endDocument();
        }
        catch (SAXException e)
        {
            throw new ExportSourceImporterException("Failed to create file for import.", e);
        }

    }

    private void addSecondarylink(HashMap<String, Group> lookup, SecondaryLink sl, XMLWriter writer)
            throws SAXException
    {

        String fromId = lookup.get(sl.from).guid;
        String toId = lookup.get(sl.to).guid;

        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(viewIdRef.getNamespaceURI(), viewIdRef.getLocalName(), viewIdRef.toPrefixString(), null,
                fromId);

        writer.startElement(viewRef.getNamespaceURI(), viewRef.getLocalName(),
                viewRef.toPrefixString(namespaceService), attrs);

        writer.startElement(viewAssociations.getNamespaceURI(), viewAssociations.getLocalName(), viewAssociations
                .toPrefixString(namespaceService), new AttributesImpl());

        writer.startElement(ContentModel.ASSOC_MEMBER.getNamespaceURI(), ContentModel.ASSOC_MEMBER.getLocalName(),
                ContentModel.ASSOC_MEMBER.toPrefixString(namespaceService), new AttributesImpl());

        AttributesImpl attrsRef = new AttributesImpl();
        attrsRef.addAttribute(viewIdRef.getNamespaceURI(), viewIdRef.getLocalName(), viewIdRef.toPrefixString(), null,
                toId);
        attrsRef.addAttribute(childQName.getNamespaceURI(), childQName.getLocalName(), childQName.toPrefixString(),
                null, QName.createQName(ContentModel.USER_MODEL_URI, sl.to).toPrefixString(namespaceService));

        writer.startElement(viewRef.getNamespaceURI(), viewRef.getLocalName(),
                viewRef.toPrefixString(namespaceService), attrsRef);

        writer.endElement(viewRef.getNamespaceURI(), viewRef.getLocalName(), viewRef.toPrefixString(namespaceService));

        writer.endElement(ContentModel.ASSOC_MEMBER.getNamespaceURI(), ContentModel.ASSOC_MEMBER.getLocalName(),
                ContentModel.ASSOC_MEMBER.toPrefixString(namespaceService));

        writer.endElement(viewAssociations.getNamespaceURI(), viewAssociations.getLocalName(), viewAssociations
                .toPrefixString(namespaceService));

        writer.endElement(viewRef.getNamespaceURI(), viewRef.getLocalName(), viewRef.toPrefixString(namespaceService));

    }

    private void addRootGroup(HashMap<String, Group> lookup, Group group, XMLWriter writer) throws SAXException
    {
        QName nodeUUID = QName.createQName("sys:node-uuid", namespaceService);

        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, childQName.getLocalName(), childQName
                .toPrefixString(), null, QName.createQName(ContentModel.USER_MODEL_URI, group.gid).toPrefixString(
                namespaceService));
        attrs.addAttribute(viewId.getNamespaceURI(), viewId.getLocalName(), viewId.toPrefixString(), null, group.guid);

        writer.startElement(ContentModel.TYPE_AUTHORITY_CONTAINER.getNamespaceURI(),
                ContentModel.TYPE_AUTHORITY_CONTAINER.getLocalName(), ContentModel.TYPE_AUTHORITY_CONTAINER
                        .toPrefixString(namespaceService), attrs);

        if ((authorityDAO != null ) && authorityDAO.authorityExists(group.gid))
        {
            NodeRef authNodeRef = authorityDAO.getAuthorityNodeRefOrNull(group.gid);
            if (authNodeRef != null)
            {
                String uguid = authorityDAO.getAuthorityNodeRefOrNull(group.gid).getId();

                writer.startElement(nodeUUID.getNamespaceURI(), nodeUUID.getLocalName(), nodeUUID
                        .toPrefixString(namespaceService), new AttributesImpl());

                writer.characters(uguid.toCharArray(), 0, uguid.length());

                writer.endElement(nodeUUID.getNamespaceURI(), nodeUUID.getLocalName(), nodeUUID
                        .toPrefixString(namespaceService));
            }
        }
        
        writer.startElement(ContentModel.PROP_AUTHORITY_NAME.getNamespaceURI(), ContentModel.PROP_AUTHORITY_NAME
                .getLocalName(), ContentModel.PROP_AUTHORITY_NAME.toPrefixString(namespaceService),
                new AttributesImpl());

        writer.characters(group.gid.toCharArray(), 0, group.gid.length());

        writer.endElement(ContentModel.PROP_AUTHORITY_NAME.getNamespaceURI(), ContentModel.PROP_AUTHORITY_NAME
                .getLocalName(), ContentModel.PROP_AUTHORITY_NAME.toPrefixString(namespaceService));

        if (group.members.size() > 0)
        {
            writer.startElement(ContentModel.PROP_MEMBERS.getNamespaceURI(), ContentModel.PROP_MEMBERS.getLocalName(),
                    ContentModel.PROP_MEMBERS.toPrefixString(namespaceService), new AttributesImpl());

            for (String member : group.members)
            {
                writer.startElement(viewValueQName.getNamespaceURI(), viewValueQName.getLocalName(), viewValueQName
                        .toPrefixString(namespaceService), new AttributesImpl());

                writer.characters(member.toCharArray(), 0, member.length());

                writer.endElement(viewValueQName.getNamespaceURI(), viewValueQName.getLocalName(), viewValueQName
                        .toPrefixString(namespaceService));
            }

            writer.endElement(ContentModel.PROP_MEMBERS.getNamespaceURI(), ContentModel.PROP_MEMBERS.getLocalName(),
                    ContentModel.PROP_MEMBERS.toPrefixString(namespaceService));
        }

        for (Group child : group.children)
        {
            addgroup(lookup, child, writer);
        }

        writer.endElement(ContentModel.TYPE_AUTHORITY_CONTAINER.getNamespaceURI(),
                ContentModel.TYPE_AUTHORITY_CONTAINER.getLocalName(), ContentModel.TYPE_AUTHORITY_CONTAINER
                        .toPrefixString(namespaceService));

    }

    private void addgroup(HashMap<String, Group> lookup, Group group, XMLWriter writer) throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();

        writer.startElement(ContentModel.ASSOC_MEMBER.getNamespaceURI(), ContentModel.ASSOC_MEMBER.getLocalName(),
                ContentModel.ASSOC_MEMBER.toPrefixString(namespaceService), attrs);

        addRootGroup(lookup, group, writer);

        writer.endElement(ContentModel.ASSOC_MEMBER.getNamespaceURI(), ContentModel.ASSOC_MEMBER.getLocalName(),
                ContentModel.ASSOC_MEMBER.toPrefixString(namespaceService));
    }

    private void buildGroupsAndRoots(HashSet<Group> rootGroups, HashMap<String, Group> lookup,
            HashSet<SecondaryLink> secondaryLinks)
    {
        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getDefaultIntialDirContext();

            SearchControls userSearchCtls = new SearchControls();
            userSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration searchResults = ctx.search(searchBase, groupQuery, userSearchCtls);
            while (searchResults.hasMoreElements())
            {
                SearchResult result = (SearchResult) searchResults.next();
                Attributes attributes = result.getAttributes();
                Attribute gidAttribute = attributes.get(groupIdAttributeName);
                if (gidAttribute == null)
                {
                    throw new ExportSourceImporterException(
                            "Group returned by group search does not have mandatory group id attribute " + attributes);
                }
                String gid = (String) gidAttribute.get(0);

                Group group = lookup.get(gid);
                if (group == null)
                {
                    group = new Group(gid);
                    lookup.put(group.gid, group);
                    rootGroups.add(group);
                }
                Attribute memAttribute = attributes.get(memberAttribute);
                // check for null
                if (memAttribute != null)
                {
                    for (int i = 0; i < memAttribute.size(); i++)
                    {
                        String attribute = (String) memAttribute.get(i);
                        if (attribute != null)
                        {
                            group.distinguishedNames.add(attribute);
                        }
                    }
                }
            }

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Found " + lookup.size());
            }

            for (Group group : lookup.values())
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Linking " + group.gid);
                }
                for (String dn : group.distinguishedNames)
                {
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("... " + dn);
                    }
                    String id;
                    Boolean isGroup = null;

                    SearchControls memberSearchCtls = new SearchControls();
                    memberSearchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);
                    NamingEnumeration memberSearchResults;
                    try
                    {
                        memberSearchResults = ctx.search(dn, "(objectClass=*)", memberSearchCtls);
                    }
                    catch (NamingException e)
                    {
                        if (errorOnMissingMembers)
                        {
                            throw e;
                        }
                        s_logger.warn("Failed to resolve distinguished name: " + dn);
                        continue;
                    }
                    while (memberSearchResults.hasMoreElements())
                    {
                        id = null;

                        SearchResult result;
                        try
                        {
                            result = (SearchResult) memberSearchResults.next();
                        }
                        catch (NamingException e)
                        {
                            if (errorOnMissingMembers)
                            {
                                throw e;
                            }
                            s_logger.warn("Failed to resolve distinguished name: " + dn);
                            continue;
                        }
                        Attributes attributes = result.getAttributes();
                        Attribute objectclass = attributes.get("objectclass");
                        if (objectclass == null)
                        {
                            throw new ExportSourceImporterException("Failed to find attribute objectclass for DN " + dn);
                        }
                        for (int i = 0; i < objectclass.size(); i++)
                        {
                            String testType;
                            try
                            {
                                testType = (String) objectclass.get(i);
                            }
                            catch (NamingException e)
                            {
                                if (errorOnMissingMembers)
                                {
                                    throw e;
                                }
                                s_logger.warn("Failed to resolve object class attribute for distinguished name: " + dn);
                                continue;
                            }
                            if (testType.equals(groupType))
                            {
                                isGroup = true;
                                try
                                {
                                    Attribute groupIdAttribute = attributes.get(groupIdAttributeName);
                                    if (groupIdAttribute == null)
                                    {
                                        throw new ExportSourceImporterException("Group missing group id attribute DN ="
                                                + dn + "  att = " + groupIdAttributeName);
                                    }
                                    id = (String) groupIdAttribute.get(0);
                                }
                                catch (NamingException e)
                                {
                                    if (errorOnMissingMembers)
                                    {
                                        throw e;
                                    }
                                    s_logger.warn("Failed to resolve group identifier "
                                            + groupIdAttributeName + " for distinguished name: " + dn);
                                    id = "Unknown sub group";
                                }
                                break;
                            }
                            else if (testType.equals(personType))
                            {
                                isGroup = false;
                                try
                                {
                                    Attribute userIdAttribute = attributes.get(userIdAttributeName);
                                    if (userIdAttribute == null)
                                    {
                                        throw new ExportSourceImporterException("User missing user id attribute DN ="
                                                + dn + "  att = " + userIdAttributeName);
                                    }
                                    id = (String) userIdAttribute.get(0);
                                }
                                catch (NamingException e)
                                {
                                    if (errorOnMissingMembers)
                                    {
                                        throw e;
                                    }
                                    s_logger.warn("Failed to resolve group identifier "
                                            + userIdAttributeName + " for distinguished name: " + dn);
                                    id = "Unknown member";
                                }
                                break;
                            }
                        }

                        if (id != null)
                        {
                            if (isGroup == null)
                            {
                                throw new ExportSourceImporterException("Type not recognised for DN" + dn);
                            }
                            else if (isGroup)
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... is sub group");
                                }
                                Group child = lookup.get("GROUP_" + id);
                                if (child == null)
                                {
                                    throw new ExportSourceImporterException("Failed to find child group " + id);
                                }
                                if (rootGroups.contains(child))
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("...       Primary created from "
                                                + group.gid + " to " + child.gid);
                                    }
                                    group.children.add(child);
                                    rootGroups.remove(child);
                                }
                                else
                                {
                                    if (s_logger.isDebugEnabled())
                                    {
                                        s_logger.debug("...      Secondary created from "
                                                + group.gid + " to " + child.gid);
                                    }
                                    secondaryLinks.add(new SecondaryLink(group.gid, child.gid));
                                }
                            }
                            else
                            {
                                if (s_logger.isDebugEnabled())
                                {
                                    s_logger.debug("... is member");
                                }
                                group.members.add(id);
                            }
                        }
                    }
                }
            }
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Top " + rootGroups.size());
                s_logger.debug("Secondary " + secondaryLinks.size());
            }
        }
        catch (NamingException e)
        {
            throw new ExportSourceImporterException("Failed to import people.", e);
        }
        finally
        {
            if (ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                    throw new ExportSourceImporterException("Failed to import people.", e);
                }
            }
        }
    }

    private static class Group
    {
        String gid;

        String guid = GUID.generate();

        HashSet<Group> children = new HashSet<Group>();

        HashSet<String> members = new HashSet<String>();

        HashSet<String> distinguishedNames = new HashSet<String>();

        private Group(String gid)
        {
            this.gid = "GROUP_" + gid;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Group))
            {
                return false;
            }
            Group g = (Group) o;
            return this.gid.equals(g.gid);
        }

        @Override
        public int hashCode()
        {
            return gid.hashCode();
        }
    }

    private static class SecondaryLink
    {
        String from;

        String to;

        private SecondaryLink(String from, String to)
        {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Group))
            {
                return false;
            }
            SecondaryLink l = (SecondaryLink) o;
            return EqualsHelper.nullSafeEquals(this.from, l.from) && EqualsHelper.nullSafeEquals(this.to, l.to);
        }

        @Override
        public int hashCode()
        {
            int hashCode = 0;
            if (from != null)
            {
                hashCode = hashCode * 37 + from.hashCode();
            }
            if (to != null)
            {
                hashCode = hashCode * 37 + to.hashCode();
            }
            return hashCode;
        }
    }

    public static void main(String[] args) throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        ExportSource source = (ExportSource) ctx.getBean("ldapGroupExportSource");

        TransactionService txs = (TransactionService) ctx.getBean("transactionComponent");
        UserTransaction tx = txs.getUserTransaction();
        tx.begin();
        
        File file = new File(args[0]);
        Writer writer = new BufferedWriter(new FileWriter(file));
        XMLWriter xmlWriter = createXMLExporter(writer);
        source.generateExport(xmlWriter);
        xmlWriter.close();

        tx.commit();
    }

    private static XMLWriter createXMLExporter(Writer writer)
    {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding("UTF-8");

        // Construct an XML Exporter

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        return xmlWriter;
    }

    public void afterPropertiesSet() throws Exception
    {
        viewRef = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "reference", namespaceService);
        viewId = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "id", namespaceService);
        viewIdRef = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "idref", namespaceService);
        viewAssociations = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "associations", namespaceService);
        childQName = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "childName", namespaceService);
        viewValueQName = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, "value", namespaceService);

    }
}
