/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.model.filefolder;

import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.ml.tools.AbstractMultilingualTestCases;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Multilingual container type test cases
 * 
 * @see org.alfresco.service.cmr.ml.MLContainerType
 * 
 * @author yanipig
 */
public class FolderTypeTest extends AbstractMultilingualTestCases {

    
    @SuppressWarnings("unused")
    public void testRemoveSpace() throws Exception
    {
        NodeRef pivot  = createContent();
        NodeRef trans2 = createContent();
        NodeRef trans3 = createContent();
        NodeRef empty;
        
        NodeRef mlContainer = multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        multilingualContentService.addTranslation(trans2, pivot, Locale.GERMAN);
        multilingualContentService.addTranslation(trans3, pivot, Locale.ITALIAN);;
        empty = multilingualContentService.addEmptyTranslation(pivot, "Empty_" + System.currentTimeMillis(), Locale.ENGLISH);
        
        
        NodeRef space = fileFolderService.create(
                   nodeService.getPrimaryParent(pivot).getParentRef(),
                   "folder_" + System.currentTimeMillis(),
                   ContentModel.TYPE_FOLDER).getNodeRef();
        
        nodeService.moveNode(pivot,  space, ContentModel.ASSOC_CONTAINS, this.nodeService.getPrimaryParent(pivot).getQName());
        nodeService.moveNode(trans2, space, ContentModel.ASSOC_CONTAINS, this.nodeService.getPrimaryParent(trans2).getQName());
        nodeService.moveNode(trans3, space, ContentModel.ASSOC_CONTAINS, this.nodeService.getPrimaryParent(trans3).getQName());
        nodeService.moveNode(empty,  space, ContentModel.ASSOC_CONTAINS, this.nodeService.getPrimaryParent(empty).getQName());
        
        // Ensure that the nodes are correctly moved
        assertEquals("Move nodes failed", 4, nodeService.getChildAssocs(space).size());
        
        // 1. Delete space
        
        nodeService.deleteNode(space);
        
        // Ensute that the space is deleted 
        assertFalse("The deletion of the space failed", nodeService.exists(space));

        // Ensure that the nodes are archived
        assertTrue("The space " + space  + " must be archived", nodeService.exists(nodeArchiveService.getArchivedNode(trans2)));
        assertTrue("The node "  + pivot  + " must be archived", nodeService.exists(nodeArchiveService.getArchivedNode(pivot)));
        assertTrue("The node "  + trans2 + " must be archived", nodeService.exists(nodeArchiveService.getArchivedNode(trans2)));
        assertTrue("The node "  + trans3 + " must be archived", nodeService.exists(nodeArchiveService.getArchivedNode(trans3)));

        // Ensure that the mlContainer is deleted and not archived
        assertFalse("The mlContainer " + mlContainer + " must be deleted", nodeService.exists(mlContainer));
        assertFalse("The mlContainer " + mlContainer + " can't be archived", nodeService.exists(nodeArchiveService.getArchivedNode(mlContainer)));
        
        // 2. Restore space
        nodeArchiveService.restoreArchivedNode(nodeArchiveService.getArchivedNode(space));
        
        // Ensure that the nodes are restaured
        assertFalse("The space " + space  + "must be restored", nodeService.exists(nodeArchiveService.getArchivedNode(space)));
        assertFalse("The node "  + pivot  + "must be restored", nodeService.exists(nodeArchiveService.getArchivedNode(pivot)));
        assertFalse("The node "  + trans2 + "must be restored", nodeService.exists(nodeArchiveService.getArchivedNode(trans2)));
        assertFalse("The node "  + trans3 + "must be restored", nodeService.exists(nodeArchiveService.getArchivedNode(trans3)));
        
        // Ensure that the mlContainer is not restored
        assertFalse("The mlContainer " + mlContainer + " must be deleted and can't be restored", nodeService.exists(mlContainer));
        
        // 3. Delete space and remove it from the arhives
        
        nodeService.deleteNode(space);
        nodeService.deleteNode(nodeArchiveService.getArchivedNode(space));        
        
        assertFalse("The space " + space  + "can't be archived", nodeService.exists(nodeArchiveService.getArchivedNode(space)));
        assertFalse("The node "  + pivot  + "can't be archived", nodeService.exists(nodeArchiveService.getArchivedNode(pivot)));
        assertFalse("The node "  + trans2 + "can't be archived", nodeService.exists(nodeArchiveService.getArchivedNode(trans2)));
        assertFalse("The node "  + trans3 + "can't be archived", nodeService.exists(nodeArchiveService.getArchivedNode(trans3)));

        assertFalse("The space " + space  + "must be deleted", nodeService.exists(space));
        assertFalse("The node "  + pivot  + "must be deleted", nodeService.exists(pivot));
        assertFalse("The node "  + trans2 + "must be deleted", nodeService.exists(trans2));
        assertFalse("The node "  + trans3 + "must be deleted", nodeService.exists(trans3));            
    }
}
