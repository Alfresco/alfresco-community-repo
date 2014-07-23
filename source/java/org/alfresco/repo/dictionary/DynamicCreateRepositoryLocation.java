package org.alfresco.repo.dictionary;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author sglover
 *
 */
public class DynamicCreateRepositoryLocation extends RepositoryLocation
{
    private static final Log logger = LogFactory.getLog(DynamicCreateRepositoryLocation.class);

    private ImporterService importerService;
	private String contentViewLocation;
    private ResourceBundle bundle;
    private NamespaceService namespaceService;
    private SearchService searchService;

	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}

	public void setContentViewLocation(String contentViewLocation)
	{
		this.contentViewLocation = contentViewLocation;
	}

	public void setImporterService(ImporterService importerService)
    {
		this.importerService = importerService;
	}

	public void setBundleName(String bundleName)
	{
        Locale bindingLocale = I18NUtil.getLocale();
        this.bundle = ResourceBundle.getBundle(bundleName, bindingLocale);
	}

    public void checkAndCreate(NodeRef rootNodeRef)
    {
		List<NodeRef> nodes = searchService.selectNodes(rootNodeRef, getPath(), null, namespaceService, false);
		if(nodes.size() == 0)
		{
    		logger.info("Repository location " + getPath() + " does not exist for tenant "
    				+ TenantUtil.getCurrentDomain() + ", creating");
    		create();
		}
    }

    protected String getParentPath()
    {
    	String parentPath = null;

    	String path = getPath();
    	int idx = path.lastIndexOf("/");
    	if(idx != -1)
    	{
    		parentPath = path.substring(0, idx);
    	}
    	else
    	{
    		parentPath = "/";
    	}

    	return parentPath;
    }

    protected void create()
    {
        final File viewFile = ImporterBootstrap.getFile(contentViewLocation);
        ImportPackageHandler acpHandler = new ACPImportPackageHandler(viewFile, null);
        Location location = new Location(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        location.setPath(getParentPath());

        final ImporterBinding binding = new ImporterBinding()
        {
            @Override
            public String getValue(String key)
            {
                return bundle.getString(key);
            }

            @Override
            public UUID_BINDING getUUIDBinding()
            {
                return UUID_BINDING.CREATE_NEW;
            }

            @Override
            public QName[] getExcludedClasses()
            {
                return null;
            }

            @Override
            public boolean allowReferenceWithinTransaction()
            {
                return false;
            }
            
            @Override
            public ImporterContentCache getImportConentCache()
            {
                return null;
            }
        };

        importerService.importView(acpHandler, location, binding, (ImporterProgress) null);
    }
}
