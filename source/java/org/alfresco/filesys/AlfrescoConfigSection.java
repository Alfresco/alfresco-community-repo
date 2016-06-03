package org.alfresco.filesys;

import org.alfresco.jlan.server.config.ConfigSection;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Alfresco Configuration Section Class
 * 
 *  @author gkspencer
 */
public class AlfrescoConfigSection extends ConfigSection {

  // Alfresco configuration section name
  
  public static final String SectionName = "Alfresco";
  
  // Disk interface to use for shared filesystems
  
  private DiskInterface m_repoDiskInterface;
  
  // Main authentication service, public API
  
  private AuthenticationService m_authenticationService;

  // Authentication component, for internal functions
  
  private AuthenticationComponent m_authenticationComponent;
  
  // Various services
  
  private NodeService m_nodeService;
  private PersonService m_personService;
  private TransactionService m_transactionService;
  private TenantService m_tenantService;
  private SearchService m_searchService;
  private NamespaceService m_namespaceService;
  private AuthorityService m_authorityService;
  
  /**
   * Class constructor
   * 
   * @param config ServerConfigurationBean
   */
  public AlfrescoConfigSection(AbstractServerConfigurationBean config) {
    super( SectionName, config);
    
    // Copy values from the server configuration bean

    m_repoDiskInterface = config.getRepoDiskInterface();
    
    m_authenticationService = config.getAuthenticationService();
    m_authenticationComponent = config.getAuthenticationComponent();
    
    m_nodeService        = config.getNodeService();
    m_personService      = config.getPersonService();
    m_transactionService = config.getTransactionService();
    m_tenantService      = config.getTenantService();
    m_searchService      = config.getSearchService();
    m_namespaceService   = config.getNamespaceService();
    m_authorityService   = config.getAuthorityService();
  }
  
  /**
   * Return the repository disk interface to be used to create shares
   * 
   * @return DiskInterface
   */
  public final DiskInterface getRepoDiskInterface()
  {
      return m_repoDiskInterface;
  }
  
  /**
   * Return the authentication service
   * 
   * @return AuthenticationService
   */
  public final AuthenticationService getAuthenticationService()
  {
      return m_authenticationService;
  }
  
  /**
   * Return the authentication component
   * 
   * @return AuthenticationComponent
   */
  public final AuthenticationComponent getAuthenticationComponent()
  {
      return m_authenticationComponent;
  }
  
  /**
   * Return the node service
   * 
   * @return NodeService
   */
  public final NodeService getNodeService()
  {
      return m_nodeService;
  }
  
  /**
   * Return the person service
   * 
   * @return PersonService
   */
  public final PersonService getPersonService()
  {
      return m_personService;
  }
  
  /**
   * Return the transaction service
   * 
   * @return TransactionService
   */
  public final TransactionService getTransactionService()
  {
      return m_transactionService;
  }
  
  /**
   * Return the tenant service
   * 
   * @return TenantService
   */
  public final TenantService getTenantService()
  {
  	return m_tenantService;
  }
  
  /**
   * Return the search service
   * 
   * @return SearchService
   */
  public final SearchService getSearchService()
  {
  	return m_searchService;
  }
  
  /**
   * Return the namespace service
   * 
   * @return NamespaceService
   */
  public final NamespaceService getNamespaceService()
  {
  	return m_namespaceService;
  }
  
  /**
   * Return the authority service
   * 
   * @return AuthorityService
   */
  public final AuthorityService getAuthorityService()
  {
	return m_authorityService;
  }
}
