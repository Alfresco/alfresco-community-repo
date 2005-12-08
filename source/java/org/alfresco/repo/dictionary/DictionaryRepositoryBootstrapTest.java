package org.alfresco.repo.dictionary;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryRepositoryBootstrap.RepositoryLocation;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;

public class DictionaryRepositoryBootstrapTest extends BaseAlfrescoSpringTest
{
    public static final String TEMPLATE_MODEL_XML = 
        "<model name={0} xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>{1}</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "      {2} " +
        "   </imports>" +
    
        "   <namespaces>" +
        "      <namespace uri={3} prefix={4}/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name={5}>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <properties>" +
        "           <property name={6}>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;

    /** The bootstrap service */
    private DictionaryRepositoryBootstrap bootstrap;

    /** The search service */
    private SearchService searchService;

    /** The dictionary DAO */
    private DictionaryDAO dictionaryDAO;

    /** The transaction service */
    private TransactionService transactionService;

    /** The authentication service */
    private AuthenticationComponent authenticationComponent;

    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the behaviour filter and turn the behaviour off for the model type
        this.behaviourFilter = (BehaviourFilter)this.applicationContext.getBean("policyBehaviourFilter");
        this.behaviourFilter.disableBehaviour(ContentModel.TYPE_DICTIONARY_MODEL);
        
        this.searchService = (SearchService)this.applicationContext.getBean("searchService");
        this.dictionaryDAO = (DictionaryDAO)this.applicationContext.getBean("dictionaryDAO");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        
        this.bootstrap = new DictionaryRepositoryBootstrap();
        this.bootstrap.setContentService(this.contentService);
        this.bootstrap.setSearchService(this.searchService);
        this.bootstrap.setDictionaryDAO(this.dictionaryDAO);
        this.bootstrap.setAuthenticationComponent(this.authenticationComponent);
        this.bootstrap.setTransactionService(this.transactionService);        
        
        RepositoryLocation location = this.bootstrap.new RepositoryLocation();
        location.setStoreProtocol(this.storeRef.getProtocol());
        location.setStoreId(this.storeRef.getIdentifier());
        // NOTE: we are not setting the path for now .. in doing so we are searching the whole dictionary
        
        List<RepositoryLocation> locations = new ArrayList<RepositoryLocation>();
        locations.add(location);
        this.bootstrap.setRepositoryLocations(locations);      
    }
    
    /**
     * Test bootstrap
     */
    public void testBootstrap()
    {
        createModelNode(
                "http://www.alfresco.org/model/test2DictionaryBootstrapFromRepo/1.0",
                "test2",
                "testModel2",
                " <import uri=\"http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0\" prefix=\"test1\"/> ",
                "Test model two",
                "base2",
                "prop2");
        createModelNode(
                "http://www.alfresco.org/model/test3DictionaryBootstrapFromRepo/1.0",
                "test3",
                "testModel3",
                " <import uri=\"http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0\" prefix=\"test1\"/> ",
                "Test model three",
                "base3",
                "prop3");
        createModelNode(
                "http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0",
                "test1",
                "testModel1",
                "",
                "Test model one",
                "base1",
                "prop1");
               
        // Check that the model is not in the dictionary yet
        try
        {
            this.dictionaryDAO.getModel(
                    QName.createQName("http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0", "testModel1"));
            fail("The model should not be there.");
        }
        catch (DictionaryException exception)
        {
            // Ignore since we where expecting this
        }        
        
        // Now do the bootstrap
        this.bootstrap.bootstrap();
        
        // Check that the model is now there
        ModelDefinition modelDefinition1 = this.dictionaryDAO.getModel(
                QName.createQName("http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0", "testModel1"));
        assertNotNull(modelDefinition1);
        ModelDefinition modelDefinition2 = this.dictionaryDAO.getModel(
                QName.createQName("http://www.alfresco.org/model/test2DictionaryBootstrapFromRepo/1.0", "testModel2"));
        assertNotNull(modelDefinition2);
        ModelDefinition modelDefinition3 = this.dictionaryDAO.getModel(
                QName.createQName("http://www.alfresco.org/model/test3DictionaryBootstrapFromRepo/1.0", "testModel3"));
        assertNotNull(modelDefinition3);
    }

    /**
     * Create model node 
     * 
     * @param uri
     * @param prefix
     * @param modelLocalName
     * @param importStatement
     * @param description
     * @param typeName
     * @param propertyName
     * @return
     */
    private NodeRef createModelNode(
            String uri, 
            String prefix, 
            String modelLocalName, 
            String importStatement, 
            String description, 
            String typeName,
            String propertyName)
    {
        // Create a model node
        NodeRef model = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}models"),
                ContentModel.TYPE_DICTIONARY_MODEL).getChildRef();
        ContentWriter contentWriter1 = this.contentService.getWriter(model, ContentModel.PROP_CONTENT, true);
        contentWriter1.setEncoding("UTF-8");
        contentWriter1.setMimetype(MimetypeMap.MIMETYPE_XML);        
        String modelOne = getModelString(
                    uri,
                    prefix,
                    modelLocalName,
                    importStatement,
                    description,
                    typeName,
                    propertyName);        
        contentWriter1.putContent(modelOne);
        
        return model;
    }
    
    /**
     * 
     * Gets the model string 
     * 
     * @param uri
     * @param prefix
     * @param modelLocalName
     * @param importStatement
     * @param description
     * @param typeName
     * @param propertyName
     * @return
     */
    private String getModelString(
            String uri, 
            String prefix, 
            String modelLocalName, 
            String importStatement, 
            String description, 
            String typeName,
            String propertyName)
    {
        return MessageFormat.format( 
                TEMPLATE_MODEL_XML, 
                new Object[]{
                        "'" + prefix +":" + modelLocalName + "'",
                        description,
                        importStatement,
                        "'" + uri + "'",
                        "'" + prefix + "'",
                        "'" + prefix + ":" + typeName + "'",
                        "'" + prefix + ":" + propertyName + "'"});
    }
}
