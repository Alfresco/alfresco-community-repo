/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
/**
 * This package contains the types that deliver the Metadata Referral feature.
 * Metadata referral allows node metadata to be shared between multiple Alfresco nodes.
 * <p/>
 * In this way nodes can 'inherit' some of their metadata from another node which may
 * have benefits when more than one node is required to share some of the same metadata.
 * <p/>
 * Only aspect metadata can be shared and it is only shared as read-only data to the other nodes.
 * The node which contains the metadata values is the 'referent' node and any nodes which have been
 * linked to the referent and share the metadata are known as referrers.
 * <p/>
 * Multiple nodes may share the same referent node and one node may be linked to multiple referrers.
 * <p/>
 * The linking of nodes to their metadata referents is done with Alfresco peer associations.
 * Association types must be declared in an Alfresco content model in the normal way.
 * Spring configuration is used to assign each association type a set of aspects which will
 * be available from the referent via the association.
 * <p/>
 * See {@link org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferralAdminService}
 * for details on how to create and destroy metadata links between nodes.
 * <p/>
 * See {@link org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataService}
 * for details on how the data access is performed.
 * <p/>
 * See {@link org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferralRegistry}
 * for details on what {@link org.alfresco.module.org_alfresco_module_rm.referredmetadata.MetadataReferral}s
 * are defined in the system.
 */
package org.alfresco.module.org_alfresco_module_rm.referredmetadata;