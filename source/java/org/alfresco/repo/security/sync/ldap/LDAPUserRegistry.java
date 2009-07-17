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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.sync.ldap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.ldap.LDAPInitialDirContextFactory;
import org.alfresco.repo.security.sync.NodeDescription;
import org.alfresco.repo.security.sync.UserRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A {@link UserRegistry} implementation with the ability to query Alfresco-like descriptions of users and groups from
 * an LDAP directory, optionally restricted to those modified since a certain time.
 * 
 * @author dward
 */
public class LDAPUserRegistry implements UserRegistry, InitializingBean, ActivateableBean
{
    /** The logger. */
    private static Log logger = LogFactory.getLog(LDAPUserRegistry.class);

    /** Is this bean active? I.e. should this part of the subsystem be used? */
    private boolean active = true;

    /** The group query. */
    private String groupQuery = "(objectclass=groupOfNames)";

    /** The group differential query. */
    private String groupDifferentialQuery = "(&(objectclass=groupOfNames)(!(modifyTimestamp<={0})))";

    /** The person query. */
    private String personQuery = "(objectclass=inetOrgPerson)";

    /** The person differential query. */
    private String personDifferentialQuery = "(&(objectclass=inetOrgPerson)(!(modifyTimestamp<={0})))";

    /** The group search base. */
    private String groupSearchBase;

    /** The user search base. */
    private String userSearchBase;

    /** The group id attribute name. */
    private String groupIdAttributeName = "cn";

    /** The user id attribute name. */
    private String userIdAttributeName = "uid";

    /** The member attribute name. */
    private String memberAttributeName = "member";

    /** The modification timestamp attribute name. */
    private String modifyTimestampAttributeName = "modifyTimestamp";

    /** The group type. */
    private String groupType = "groupOfNames";

    /** The person type. */
    private String personType = "inetOrgPerson";

    /** The ldap initial context factory. */
    private LDAPInitialDirContextFactory ldapInitialContextFactory;

    /** The attribute mapping. */
    private Map<String, String> attributeMapping;

    /** The namespace service. */
    private NamespaceService namespaceService;

    /** The attribute defaults. */
    private Map<String, String> attributeDefaults;

    /**
     * The query batch size. If positive, indicates that RFC 2696 paged results should be used to split query results
     * into batches of the specified size. Overcomes any size limits imposed by the LDAP server.
     */
    private int queryBatchSize;

    /** Should we error on missing group members? */
    private boolean errorOnMissingMembers;

    /** Should we error on duplicate group IDs? */
    private boolean errorOnDuplicateGID;

    /** Should we error on missing group IDs? */
    private boolean errorOnMissingGID = false;

    /** Should we error on missing user IDs? */
    private boolean errorOnMissingUID = false;

    /** An array of all LDAP attributes to be queried from users */
    private String[] userAttributeNames;

    /** An array of all LDAP attributes to be queried from groups */
    private String[] groupAttributeNames;

    /** The LDAP generalized time format. */
    private DateFormat timestampFormat;

    public LDAPUserRegistry()
    {
        // Default to official LDAP generalized time format (unfortunately not used by Active Directory)
        setTimestampFormat("yyyyMMddHHmmss'Z'");
    }

    /**
     * Indicates whether this bean is active. I.e. should this part of the subsystem be used?
     * 
     * @param active
     *            <code>true</code> if this bean is active
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * Sets the group id attribute name.
     * 
     * @param groupIdAttributeName
     *            the group id attribute name
     */
    public void setGroupIdAttributeName(String groupIdAttributeName)
    {
        this.groupIdAttributeName = groupIdAttributeName;
    }

    /**
     * Sets the group query.
     * 
     * @param groupQuery
     *            the group query
     */
    public void setGroupQuery(String groupQuery)
    {
        this.groupQuery = groupQuery;
    }

    /**
     * Sets the group differential query.
     * 
     * @param groupDifferentialQuery
     *            the group differential query
     */
    public void setGroupDifferentialQuery(String groupDifferentialQuery)
    {
        this.groupDifferentialQuery = groupDifferentialQuery;
    }

    /**
     * Sets the person query.
     * 
     * @param personQuery
     *            the person query
     */
    public void setPersonQuery(String personQuery)
    {
        this.personQuery = personQuery;
    }

    /**
     * Sets the person differential query.
     * 
     * @param personDifferentialQuery
     *            the person differential query
     */
    public void setPersonDifferentialQuery(String personDifferentialQuery)
    {
        this.personDifferentialQuery = personDifferentialQuery;
    }

    /**
     * Sets the group type.
     * 
     * @param groupType
     *            the group type
     */
    public void setGroupType(String groupType)
    {
        this.groupType = groupType;
    }

    /**
     * Sets the member attribute name.
     * 
     * @param memberAttribute
     *            the member attribute name
     */
    public void setMemberAttribute(String memberAttribute)
    {
        this.memberAttributeName = memberAttribute;
    }

    /**
     * Sets the person type.
     * 
     * @param personType
     *            the person type
     */
    public void setPersonType(String personType)
    {
        this.personType = personType;
    }

    /**
     * Sets the group search base.
     * 
     * @param groupSearchBase
     *            the group search base
     */
    public void setGroupSearchBase(String groupSearchBase)
    {
        this.groupSearchBase = groupSearchBase;
    }

    /**
     * Sets the user search base.
     * 
     * @param userSearchBase
     *            the user search base
     */
    public void setUserSearchBase(String userSearchBase)
    {
        this.userSearchBase = userSearchBase;
    }

    /**
     * Sets the user id attribute name.
     * 
     * @param userIdAttributeName
     *            the user id attribute name
     */
    public void setUserIdAttributeName(String userIdAttributeName)
    {
        this.userIdAttributeName = userIdAttributeName;
    }

    /**
     * Sets the modification timestamp attribute name.
     * 
     * @param modifyTimestampAttributeName
     *            the modification timestamp attribute name
     */
    public void setModifyTimestampAttributeName(String modifyTimestampAttributeName)
    {
        this.modifyTimestampAttributeName = modifyTimestampAttributeName;
    }

    /**
     * Sets the timestamp format. Unfortunately, this varies between directory servers.
     * 
     * @param timestampFormat
     *            the timestamp format
     *            <ul>
     *            <li>OpenLDAP: "yyyyMMddHHmmss'Z'"
     *            <li>Active Directory: "yyyyMMddHHmmss'.0Z'"
     *            </ul>
     */
    public void setTimestampFormat(String timestampFormat)
    {
        this.timestampFormat = new SimpleDateFormat(timestampFormat);
        this.timestampFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Decides whether to error on missing group members.
     * 
     * @param errorOnMissingMembers
     *            <code>true</code> if we should error on missing group members
     */
    public void setErrorOnMissingMembers(boolean errorOnMissingMembers)
    {
        this.errorOnMissingMembers = errorOnMissingMembers;
    }

    /**
     * Decides whether to error on missing group IDs.
     * 
     * @param errorOnMissingGID
     *            <code>true</code> if we should error on missing group IDs
     */
    public void setErrorOnMissingGID(boolean errorOnMissingGID)
    {
        this.errorOnMissingGID = errorOnMissingGID;
    }

    /**
     * Decides whether to error on missing user IDs.
     * 
     * @param errorOnMissingUID
     *            <code>true</code> if we should error on missing user IDs
     */
    public void setErrorOnMissingUID(boolean errorOnMissingUID)
    {
        this.errorOnMissingUID = errorOnMissingUID;
    }

    /**
     * Decides whether to error on duplicate group IDs.
     * 
     * @param errorOnDuplicateGID
     *            <code>true</code> if we should error on duplicate group IDs
     */
    public void setErrorOnDuplicateGID(boolean errorOnDuplicateGID)
    {
        this.errorOnDuplicateGID = errorOnDuplicateGID;
    }

    /**
     * Sets the LDAP initial dir context factory.
     * 
     * @param ldapInitialDirContextFactory
     *            the new LDAP initial dir context factory
     */
    public void setLDAPInitialDirContextFactory(LDAPInitialDirContextFactory ldapInitialDirContextFactory)
    {
        this.ldapInitialContextFactory = ldapInitialDirContextFactory;
    }

    /**
     * Sets the attribute defaults.
     * 
     * @param attributeDefaults
     *            the attribute defaults
     */
    public void setAttributeDefaults(Map<String, String> attributeDefaults)
    {
        this.attributeDefaults = attributeDefaults;
    }

    /**
     * Sets the namespace service.
     * 
     * @param namespaceService
     *            the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the attribute mapping.
     * 
     * @param attributeMapping
     *            the attribute mapping
     */
    public void setAttributeMapping(Map<String, String> attributeMapping)
    {
        this.attributeMapping = attributeMapping;
    }

    /**
     * Sets the query batch size.
     * 
     * @param queryBatchSize
     *            If positive, indicates that RFC 2696 paged results should be used to split query results into batches
     *            of the specified size. Overcomes any size limits imposed by the LDAP server.
     */
    public void setQueryBatchSize(int queryBatchSize)
    {
        this.queryBatchSize = queryBatchSize;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive()
    {
        return this.active;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        Set<String> userAttributeSet = new TreeSet<String>();
        userAttributeSet.add(this.userIdAttributeName);
        userAttributeSet.add(this.modifyTimestampAttributeName);
        for (String attribute : this.attributeMapping.values())
        {
            if (attribute != null)
            {
                userAttributeSet.add(attribute);
            }
        }
        this.userAttributeNames = new String[userAttributeSet.size()];
        userAttributeSet.toArray(this.userAttributeNames);
        this.groupAttributeNames = new String[]
        {
            this.groupIdAttributeName, this.modifyTimestampAttributeName, this.memberAttributeName
        };
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistry#getPersons(java.util.Date)
     */
    public Iterator<NodeDescription> getPersons(Date modifiedSince)
    {
        return new PersonIterator(modifiedSince);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.sync.UserRegistry#getGroups(java.util.Date)
     */
    public Iterator<NodeDescription> getGroups(Date modifiedSince)
    {
        Map<String, NodeDescription> lookup = new TreeMap<String, NodeDescription>();
        SearchControls userSearchCtls = new SearchControls();
        userSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        userSearchCtls.setReturningAttributes(this.groupAttributeNames);

        InitialDirContext ctx = null;
        try
        {
            ctx = this.ldapInitialContextFactory.getDefaultIntialDirContext(this.queryBatchSize);

            LdapName groupDistinguishedNamePrefix = new LdapName(this.groupSearchBase);
            LdapName userDistinguishedNamePrefix = new LdapName(this.userSearchBase);

            // Work out whether the user and group trees are disjoint. This may allow us to optimize reverse DN
            // resolution.
            boolean disjoint = !groupDistinguishedNamePrefix.startsWith(userDistinguishedNamePrefix)
                    && !userDistinguishedNamePrefix.startsWith(groupDistinguishedNamePrefix);

            do
            {
                NamingEnumeration<SearchResult> searchResults;

                if (modifiedSince == null)
                {
                    searchResults = ctx.search(this.groupSearchBase, this.groupQuery, userSearchCtls);
                }
                else
                {
                    searchResults = ctx.search(this.groupSearchBase, this.groupDifferentialQuery, new Object[]
                    {
                        this.timestampFormat.format(modifiedSince)
                    }, userSearchCtls);
                }

                while (searchResults.hasMoreElements())
                {
                    SearchResult result = searchResults.next();
                    Attributes attributes = result.getAttributes();
                    Attribute gidAttribute = attributes.get(this.groupIdAttributeName);
                    if (gidAttribute == null)
                    {
                        if (this.errorOnMissingGID)
                        {
                            throw new AlfrescoRuntimeException(
                                    "NodeDescription returned by group search does not have mandatory group id attribute "
                                            + attributes);
                        }
                        else
                        {
                            LDAPUserRegistry.logger.warn("Missing GID on " + attributes);
                            continue;
                        }
                    }
                    String gid = "GROUP_" + gidAttribute.get(0);

                    NodeDescription group = lookup.get(gid);
                    if (group == null)
                    {
                        group = new NodeDescription();
                        group.getProperties().put(ContentModel.PROP_AUTHORITY_NAME, gid);
                        lookup.put(gid, group);
                    }
                    else if (this.errorOnDuplicateGID)
                    {
                        throw new AlfrescoRuntimeException("Duplicate group id found for " + gid);
                    }
                    else
                    {
                        LDAPUserRegistry.logger.warn("Duplicate gid found for " + gid + " -> merging definitions");
                    }

                    Attribute modifyTimestamp = attributes.get(this.modifyTimestampAttributeName);
                    if (modifyTimestamp != null)
                    {
                        group.setLastModified(this.timestampFormat.parse(modifyTimestamp.get().toString()));
                    }
                    Set<String> childAssocs = group.getChildAssociations();

                    Attribute memAttribute = attributes.get(this.memberAttributeName);
                    // check for null
                    if (memAttribute != null)
                    {
                        for (int i = 0; i < memAttribute.size(); i++)
                        {
                            String attribute = (String) memAttribute.get(i);
                            if (attribute != null && attribute.length() > 0)
                            {
                                try
                                {
                                    // Attempt to parse the member attribute as a DN. If this fails we have a fallback
                                    // in the catch block
                                    LdapName distinguishedName = new LdapName(attribute);
                                    Attribute nameAttribute;

                                    // If the user and group search bases are different we may be able to recognize user
                                    // and group DNs without a secondary lookup
                                    if (disjoint)
                                    {
                                        Attributes nameAttributes = distinguishedName.getRdn(
                                                distinguishedName.size() - 1).toAttributes();

                                        // Recognize user DNs
                                        if (distinguishedName.startsWith(userDistinguishedNamePrefix)
                                                && (nameAttribute = nameAttributes.get(this.userIdAttributeName)) != null)
                                        {
                                            childAssocs.add((String) nameAttribute.get());
                                            continue;
                                        }

                                        // Recognize group DNs
                                        if (distinguishedName.startsWith(groupDistinguishedNamePrefix)
                                                && (nameAttribute = nameAttributes.get(this.groupIdAttributeName)) != null)
                                        {
                                            childAssocs.add("GROUP_" + nameAttribute.get());
                                            continue;
                                        }
                                    }

                                    // If we can't determine the name and type from the DN alone, try a directory lookup
                                    if (distinguishedName.startsWith(userDistinguishedNamePrefix)
                                            || distinguishedName.startsWith(groupDistinguishedNamePrefix))
                                    {
                                        try
                                        {
                                            Attributes childAttributes = ctx.getAttributes(attribute, new String[]
                                            {
                                                "objectclass", this.groupIdAttributeName, this.userIdAttributeName
                                            });
                                            Attribute objectClass = childAttributes.get("objectclass");
                                            if (hasAttributeValue(objectClass, this.personType))
                                            {
                                                nameAttribute = childAttributes.get(this.userIdAttributeName);
                                                if (nameAttribute == null)
                                                {
                                                    if (this.errorOnMissingUID)
                                                    {
                                                        throw new AlfrescoRuntimeException(
                                                                "User missing user id attribute DN =" + attribute
                                                                        + "  att = " + this.userIdAttributeName);
                                                    }
                                                    else
                                                    {
                                                        LDAPUserRegistry.logger
                                                                .warn("User missing user id attribute DN =" + attribute
                                                                        + "  att = " + this.userIdAttributeName);
                                                        continue;
                                                    }
                                                }

                                                childAssocs.add((String) nameAttribute.get());
                                                continue;
                                            }
                                            else if (hasAttributeValue(objectClass, this.groupType))
                                            {
                                                nameAttribute = childAttributes.get(this.groupIdAttributeName);
                                                if (nameAttribute == null)
                                                {
                                                    if (this.errorOnMissingGID)
                                                    {
                                                        throw new AlfrescoRuntimeException(
                                                                "Group returned by group search does not have mandatory group id attribute "
                                                                        + attributes);
                                                    }
                                                    else
                                                    {
                                                        LDAPUserRegistry.logger.warn("Missing GID on "
                                                                + childAttributes);
                                                        continue;
                                                    }
                                                }
                                                childAssocs.add("GROUP_" + nameAttribute.get());
                                                continue;
                                            }
                                        }
                                        catch (NamingException e)
                                        {
                                            // Unresolvable name
                                        }
                                    }
                                    if (this.errorOnMissingMembers)
                                    {
                                        throw new AlfrescoRuntimeException("Failed to resolve distinguished name: "
                                                + attribute);
                                    }
                                    LDAPUserRegistry.logger.warn("Failed to resolve distinguished name: " + attribute);
                                }
                                catch (InvalidNameException e)
                                {
                                    // The member attribute didn't parse as a DN. So assume we have a group class like posixGroup (FDS) that directly lists user names
                                    childAssocs.add(attribute);
                                }
                            }
                        }
                    }
                }
            }
            while (this.ldapInitialContextFactory.hasNextPage(ctx, this.queryBatchSize));

            if (LDAPUserRegistry.logger.isDebugEnabled())
            {
                LDAPUserRegistry.logger.debug("Found " + lookup.size());
            }

            return lookup.values().iterator();
        }
        catch (NamingException e)
        {
            throw new AlfrescoRuntimeException("User and group import failed", e);
        }
        catch (ParseException e)
        {
            throw new AlfrescoRuntimeException("User and group import failed", e);
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
                }
            }
        }
    }

    /**
     * Does a case-insensitive search for the given value in an attribute
     * 
     * @param attribute
     *            the attribute
     * @param value
     *            the value to search for
     * @return <code>true</code>, if the value was found
     * @throws NamingException
     *             if there is a problem accessing the attribute values
     */
    private boolean hasAttributeValue(Attribute attribute, String value) throws NamingException
    {
        NamingEnumeration<?> values = attribute.getAll();
        while (values.hasMore())
        {
            try
            {
                if (value.equalsIgnoreCase((String) values.next()))
                {
                    return true;
                }
            }
            catch (ClassCastException e)
            {
                // Not a string value. ignore and continue
            }
        }
        return false;
    }

    /**
     * Wraps the LDAP user query as an {@link Iterator}.
     */
    public class PersonIterator implements Iterator<NodeDescription>
    {

        /** The directory context. */
        private InitialDirContext ctx;

        private SearchControls userSearchCtls;

        private Date modifiedSince;

        /** The search results. */
        private NamingEnumeration<SearchResult> searchResults;

        /** The uids. */
        private HashSet<String> uids = new HashSet<String>();

        /** The next node description to return. */
        private NodeDescription next;

        /**
         * Instantiates a new person iterator.
         * 
         * @param modifiedSince
         *            if non-null, then only descriptions of users modified since this date should be returned; if
         *            <code>null</code> then descriptions of all users should be returned.
         */
        public PersonIterator(Date modifiedSince)
        {
            try
            {
                this.ctx = LDAPUserRegistry.this.ldapInitialContextFactory
                        .getDefaultIntialDirContext(LDAPUserRegistry.this.queryBatchSize);

                // Authentication has been successful.
                // Set the current user, they are now authenticated.

                this.userSearchCtls = new SearchControls();
                this.userSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                this.userSearchCtls.setReturningAttributes(LDAPUserRegistry.this.userAttributeNames);

                this.modifiedSince = modifiedSince;

                this.next = fetchNext();
            }
            catch (NamingException e)
            {
                throw new AlfrescoRuntimeException("Failed to import people.", e);
            }
            finally
            {
                if (this.searchResults == null)
                {
                    try
                    {
                        this.ctx.close();
                    }
                    catch (Exception e)
                    {
                    }
                    this.ctx = null;
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return this.next != null;
        }

        /*
         * (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public NodeDescription next()
        {
            if (this.next == null)
            {
                throw new IllegalStateException();
            }
            NodeDescription current = this.next;
            try
            {
                this.next = fetchNext();
            }
            catch (NamingException e)
            {
                throw new AlfrescoRuntimeException("Failed to import people.", e);
            }
            return current;
        }

        /**
         * Pre-fetches the next node description to be returned.
         * 
         * @return the node description
         * @throws NamingException
         *             on a naming exception
         */
        private NodeDescription fetchNext() throws NamingException
        {
            boolean readyForNextPage;
            do
            {
                readyForNextPage = this.searchResults == null;
                while (!readyForNextPage && this.searchResults.hasMoreElements())
                {
                    SearchResult result = this.searchResults.next();
                    Attributes attributes = result.getAttributes();
                    Attribute uidAttribute = attributes.get(LDAPUserRegistry.this.userIdAttributeName);
                    if (uidAttribute == null)
                    {
                        if (LDAPUserRegistry.this.errorOnMissingUID)
                        {
                            throw new AlfrescoRuntimeException(
                                    "User returned by user search does not have mandatory user id attribute "
                                            + attributes);
                        }
                        else
                        {
                            LDAPUserRegistry.logger
                                    .warn("User returned by user search does not have mandatory user id attribute "
                                            + attributes);
                            continue;
                        }
                    }
                    String uid = (String) uidAttribute.get(0);

                    if (this.uids.contains(uid))
                    {
                        LDAPUserRegistry.logger
                                .warn("Duplicate uid found - there will be more than one person object for this user - "
                                        + uid);
                    }

                    this.uids.add(uid);

                    if (LDAPUserRegistry.logger.isDebugEnabled())
                    {
                        LDAPUserRegistry.logger.debug("Adding user for " + uid);
                    }

                    NodeDescription person = new NodeDescription();

                    Attribute modifyTimestamp = attributes.get(LDAPUserRegistry.this.modifyTimestampAttributeName);
                    if (modifyTimestamp != null)
                    {
                        try
                        {
                            person.setLastModified(LDAPUserRegistry.this.timestampFormat.parse(modifyTimestamp.get()
                                    .toString()));
                        }
                        catch (ParseException e)
                        {
                            throw new AlfrescoRuntimeException("Failed to import people.", e);
                        }
                    }

                    PropertyMap properties = person.getProperties();
                    for (String key : LDAPUserRegistry.this.attributeMapping.keySet())
                    {
                        QName keyQName = QName.createQName(key, LDAPUserRegistry.this.namespaceService);

                        // cater for null
                        String attributeName = LDAPUserRegistry.this.attributeMapping.get(key);
                        if (attributeName != null)
                        {
                            Attribute attribute = attributes.get(attributeName);
                            if (attribute != null)
                            {
                                String value = (String) attribute.get(0);
                                if (value != null)
                                {
                                    properties.put(keyQName, value);
                                }
                            }
                            else
                            {
                                String defaultValue = LDAPUserRegistry.this.attributeDefaults.get(key);
                                if (defaultValue != null)
                                {
                                    properties.put(keyQName, defaultValue);
                                }
                            }
                        }
                        else
                        {
                            String defaultValue = LDAPUserRegistry.this.attributeDefaults.get(key);
                            if (defaultValue != null)
                            {
                                properties.put(keyQName, defaultValue);
                            }
                        }
                    }
                    return person;
                }

                // Examine the paged results control response for an indication that another page is available
                if (!readyForNextPage)
                {
                    readyForNextPage = LDAPUserRegistry.this.ldapInitialContextFactory.hasNextPage(this.ctx,
                            LDAPUserRegistry.this.queryBatchSize);
                }

                // Fetch the next page if there is one
                if (readyForNextPage)
                {
                    if (this.modifiedSince == null)
                    {
                        this.searchResults = this.ctx.search(LDAPUserRegistry.this.userSearchBase,
                                LDAPUserRegistry.this.personQuery, this.userSearchCtls);
                    }
                    else
                    {
                        this.searchResults = this.ctx.search(LDAPUserRegistry.this.userSearchBase,
                                LDAPUserRegistry.this.personDifferentialQuery, new Object[]
                                {
                                    LDAPUserRegistry.this.timestampFormat.format(this.modifiedSince)
                                }, this.userSearchCtls);
                    }
                }
            }
            while (readyForNextPage);
            this.searchResults.close();
            this.searchResults = null;
            this.ctx.close();
            this.ctx = null;
            return null;
        }

        /*
         * (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
