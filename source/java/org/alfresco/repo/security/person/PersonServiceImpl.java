/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class PersonServiceImpl implements PersonService
{
    public static final String SYSTEM_FOLDER = "/sys:system";

    public static final String PEOPLE_FOLDER = SYSTEM_FOLDER + "/sys:people";

    // IOC

    private StoreRef storeRef;

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    private SearchService searchService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private boolean createMissingPeople;

    private boolean userNamesAreCaseSensitive;

    private String companyHomePath;

    private NodeRef companyHomeNodeRef;

    private static Set<QName> mutableProperties;

    static
    {
        Set<QName> props = new HashSet<QName>();
        props.add(ContentModel.PROP_HOMEFOLDER);
        props.add(ContentModel.PROP_FIRSTNAME);
        // Middle Name
        props.add(ContentModel.PROP_LASTNAME);
        props.add(ContentModel.PROP_EMAIL);
        props.add(ContentModel.PROP_ORGID);
        mutableProperties = Collections.unmodifiableSet(props);
    }

    public PersonServiceImpl()
    {
        super();
    }

    public boolean getUserNamesAreCaseSensitive()
    {
        return userNamesAreCaseSensitive;
    }

    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }

    public NodeRef getPerson(String caseSensitiveUserName)
    {
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName : caseSensitiveUserName.toLowerCase();
        NodeRef personNode = getPersonOrNull(userName);
        if (personNode == null)
        {
            if (createMissingPeople())
            {
                return createMissingPerson(userName);
            }
            else
            {
                throw new PersonException("No person found for user name " + userName);
            }

        }
        else
        {
            return personNode;
        }
    }

    public boolean personExists(String caseSensitiveUserName)
    {
        return getPersonOrNull(caseSensitiveUserName) != null;
    }

    public NodeRef getPersonOrNull(String caseSensitiveUserName)
    {
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName : caseSensitiveUserName.toLowerCase();
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        QueryParameterDefinition[] defs = new QueryParameterDefinition[1];
        DataTypeDefinition text = dictionaryService.getDataType(DataTypeDefinition.TEXT);
        defs[0] = new QueryParameterDefImpl(QName.createQName("cm", "var", namespacePrefixResolver), text, true,
                userName);
        List<NodeRef> results = searchService.selectNodes(rootNode, PEOPLE_FOLDER
                + "/cm:person[@cm:userName = $cm:var ]", defs, namespacePrefixResolver, false);
        if (results.size() != 1)
        {
            return null;
        }
        return results.get(0);
    }

    public boolean createMissingPeople()
    {
        return createMissingPeople;
    }

    public Set<QName> getMutableProperties()
    {
        return mutableProperties;
    }

    public void setPersonProperties(String caseSensitiveUserName, Map<QName, Serializable> properties)
    {
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName : caseSensitiveUserName.toLowerCase();
        NodeRef personNode = getPersonOrNull(userName);
        if (personNode == null)
        {
            if (createMissingPeople())
            {
                personNode = createMissingPerson(userName);
            }
            else
            {
                throw new PersonException("No person found for user name " + userName);
            }

        }

        properties.put(ContentModel.PROP_USERNAME, userName);

        nodeService.setProperties(personNode, properties);
    }

    public boolean isMutable()
    {
        return true;
    }

    private NodeRef createMissingPerson(String userName)
    {
        HashMap<QName, Serializable> properties = getDefaultProperties(userName);
        return createPerson(properties);
    }

    private HashMap<QName, Serializable> getDefaultProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_HOMEFOLDER, getCompanyHome());
        properties.put(ContentModel.PROP_FIRSTNAME, userName);
        properties.put(ContentModel.PROP_LASTNAME, "");
        properties.put(ContentModel.PROP_EMAIL, "");
        properties.put(ContentModel.PROP_ORGID, "");
        return properties;
    }

    public NodeRef createPerson(Map<QName, Serializable> properties)
    {
        String caseSensitiveUserName = DefaultTypeConverter.INSTANCE.convert(String.class, properties
                .get(ContentModel.PROP_USERNAME));
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName : caseSensitiveUserName.toLowerCase();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return nodeService.createNode(getPeopleContainer(), ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_PERSON,
                ContentModel.TYPE_PERSON, properties).getChildRef();
    }

    public NodeRef getPeopleContainer()
    {
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> results = searchService.selectNodes(rootNodeRef, PEOPLE_FOLDER, null, namespacePrefixResolver,
                false);
        NodeRef typesNode = null;
        if (results.size() == 0)
        {

            List<ChildAssociationRef> result = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL,
                    QName.createQName("sys", "system", namespacePrefixResolver));
            NodeRef sysNode = null;
            if (result.size() == 0)
            {
                sysNode = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("sys", "system", namespacePrefixResolver), ContentModel.TYPE_CONTAINER)
                        .getChildRef();
            }
            else
            {
                sysNode = result.get(0).getChildRef();
            }
            result = nodeService.getChildAssocs(sysNode, RegexQNamePattern.MATCH_ALL, QName.createQName("sys",
                    "people", namespacePrefixResolver));

            if (result.size() == 0)
            {
                typesNode = nodeService.createNode(sysNode, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("sys", "people", namespacePrefixResolver), ContentModel.TYPE_CONTAINER)
                        .getChildRef();
                return typesNode;
            }
            else
            {
                return result.get(0).getChildRef();
            }

        }
        else
        {
            return results.get(0);
        }
    }

    public void deletePerson(String userName)
    {
        NodeRef personNodeRef = getPersonOrNull(userName);
        if (personNodeRef != null)
        {
            nodeService.deleteNode(personNodeRef);
        }

    }

    public Set<NodeRef> getAllPeople()
    {
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        List<NodeRef> results = searchService.selectNodes(rootNode, PEOPLE_FOLDER + "/cm:person", null,
                namespacePrefixResolver, false);
        HashSet<NodeRef> all = new HashSet<NodeRef>();
        all.addAll(results);
        return all;
    }

    public void setCreateMissingPeople(boolean createMissingPeople)
    {
        this.createMissingPeople = createMissingPeople;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }

    public synchronized NodeRef getCompanyHome()
    {
        if (companyHomeNodeRef == null)
        {
            List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(storeRef), companyHomePath, null,
                    namespacePrefixResolver, false);
            if (refs.size() != 1)
            {
                throw new IllegalStateException("Invalid company home path: found : " + refs.size());
            }
            companyHomeNodeRef = refs.get(0);
        }
        return companyHomeNodeRef;
    }

    // IOC Setters

}
