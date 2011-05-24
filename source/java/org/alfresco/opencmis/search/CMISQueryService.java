package org.alfresco.opencmis.search;

import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;

public interface CMISQueryService
{
    CMISResultSet query(CMISQueryOptions options);

    CMISResultSet query(String query, StoreRef storeRef);

    boolean getPwcSearchable();

    boolean getAllVersionsSearchable();

    CapabilityQuery getQuerySupport();

    CapabilityJoin getJoinSupport();
}