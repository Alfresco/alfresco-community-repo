/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * ALF-4203
 * 
 * This patch searches for AVM Node duplicates resulting from case insensitivity issues on 
 * earlier versions of the software (see ALF-1940) and eliminates them by changing the name
 * of duplicates to make them unique. This is achieved by appending a unique suffix to
 * duplicate node names.
 * 
 * @author Dmitry Velichkevich
 */
public class EliminateDuplicatesPatch extends AbstractPatch
{
    private Log LOGGER = LogFactory.getLog(EliminateDuplicatesPatch.class);

    private static final String RENAMED_MARK_KEY = "renamed.duplicate.mark";

    private static final char EXTENSION_DELIMITER = '.';

    private static final char TOKENS_DELIMITER = '-';

    /**
     * Internationalized mark to indicate renamed duplicate
     */
    private String renamedMark;

    /**
     * Temporary extension for renamed duplicates. May be 'not set'
     */
    private String temporaryExtension;

    private AvmDuplicatesIBatisDao helper;

    public EliminateDuplicatesPatch()
    {
        helper = new AvmDuplicatesIBatisDao();
    }

    public String getRenamedMark()
    {
        if (null == renamedMark)
        {
            String mark = I18NUtil.getMessage(RENAMED_MARK_KEY);
            StringBuilder markBuilder = new StringBuilder().append(TOKENS_DELIMITER);
            renamedMark = markBuilder.append(mark).append(TOKENS_DELIMITER).toString();
        }
        return renamedMark;
    }

    public void setTemporaryExtension(String temporaryExtension)
    {
        this.temporaryExtension = temporaryExtension;
    }

    public void setTemplate(SqlMapClientTemplate template)
    {
        helper.setTemplate(template);
    }

    @Override
    protected String applyInternal() throws Exception
    {
        int totalRenamed = 0;
        int duplicateGroupsAmount = 0;

        // Receiving duplicates
        List<DuplicateEntry> duplicates = helper.getDuplicates();
        Iterator<DuplicateEntry> duplicatesIterator = duplicates.iterator();
        DuplicateEntry duplicate = null;
        
        // duplicates contains all the duplicates found ordered by parent_id, lower(name), child_id.
        // Within this ordered list there are groups of duplicates (in which the parent_id and lower(name)
        // are the same but the child_id varies). A duplicate group, by definition, must have two or more entries.
        while (duplicatesIterator.hasNext() && (null != (duplicate = duplicatesIterator.next())))
        {
            // Increasing duplicate groups amount
            duplicateGroupsAmount++;
            int renamed = 0;

            // First element of the duplicates group will be left with its current name. Marking first element of current group as marked
            int processed = 1;

            // amount is the number of duplicates in the current duplicate group
            long amount = duplicate.getAmount();

            // While we have duplicates and we are still within a duplicates group
            // amount = the number of duplicates with the same parent and lowercase name
            // processed = the number of duplicates in the current duplicates group that have been processed
            while (duplicatesIterator.hasNext() && (amount > processed))
            {
                // Skipping first element from each group to leave at least one Node with initial name
                duplicate = duplicatesIterator.next();
                processed++;

                String newName = generatePatchedName(duplicate);
                boolean renamingResult = helper.renameNode(new DuplicateEntry(duplicate.getId(), duplicate.getParentId(), newName, 1L));
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Trying to rename Node with id = '" + duplicate.getId() + "' and parent id = '"
                            + duplicate.getParentId() + "' from '" + duplicate.getName() + "' to '" + newName + "'. Result: "
                            + renamingResult);
                }
                if (renamingResult)
                {
                    renamed++;
                    totalRenamed++;
                }
            }

            // Entries finished or another group reached
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Duplicates for '" + duplicate.getName().toLowerCase() + "' name and parentId='" + duplicate.getParentId() + "': " + duplicate.getAmount()
                        + ". Renamed: " + renamed);
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(duplicateGroupsAmount).append(" duplicate group(s) were found\n");
        result.append(duplicates.size()).append(" affected node entries\n");
        result.append("----------------\nTotal renamed: ").append(totalRenamed).append('\n');
        return result.toString();
    }

    /**
     * With help of {@link GUID} utility class this method generates unique and internationalized name for some Node. Also returned name will be finished with
     * {@link EliminateDuplicatesPatch#temporaryExtension} extension if it is not empty or <b><i>null</i></b>
     * 
     * @param node - {@link DuplicateEntry} instance which contains required old name and Node id
     * @return {@link String} value which represents unique and internationalized name generated from the duplicated one
     */
    private String generatePatchedName(DuplicateEntry node)
    {
        StringBuilder result = new StringBuilder();
        StringBuilder extension = new StringBuilder();
        AVMNode avmNode = AVMDAOs.Instance().fAVMNodeDAO.getByID(node.getId());
        String oldName = node.getName();
        if (((AVMNodeType.LAYERED_FILE == avmNode.getType()) || (AVMNodeType.PLAIN_FILE == avmNode.getType())))
        {
            int dotPosition = oldName.indexOf(EXTENSION_DELIMITER);
            if (-1 != dotPosition)
            {
                extension.append(oldName.substring(dotPosition));
                oldName = oldName.substring(0, dotPosition);
            }
            if ((null != temporaryExtension) && (temporaryExtension.length() > 0))
            {
                extension.append(EXTENSION_DELIMITER).append(temporaryExtension);
            }
        }
        result.append(oldName).append(getRenamedMark()).append(GUID.generate()).append(extension.toString());
        return result.toString();
    }

    /**
     * iBatis helper which introduces functionality for duplicates of some node receiving and renaming in <code>avm_child_entries table</code>
     * 
     * @author Dmitry Velichkevich
     */
    private class AvmDuplicatesIBatisDao
    {
        private static final String QUERY_SELECT_DUPLICATES_ID = "alfresco.patch.select_AvmNodeDuplicates";

        private static final String QUERY_RENAME_NODE_ID = "alfresco.patch.update_AvmNodeNameById";

        private SqlMapClientTemplate template;

        public void setTemplate(SqlMapClientTemplate template)
        {
            this.template = template;
        }

        /**
         * Receives all duplicates found in some repository
         * 
         * @return {@link List}&lt;{@link DuplicateEntry}&gt; list which contains all received duplicates wrapped into the {@link DuplicateEntry}
         */
        @SuppressWarnings("unchecked")
        public List<DuplicateEntry> getDuplicates()
        {
            return template.queryForList(QUERY_SELECT_DUPLICATES_ID);
        }

        /**
         * Renames some child which is duplicate for some other Node(s) in some Space
         * 
         * @param renamedEntry - {@link DuplicateEntry} instance which specifies Id and new name for Node
         * @return {@link Boolean} value which equals to <code>true</code> if renaming update has been finished successfully
         */
        public boolean renameNode(DuplicateEntry renamedEntry)
        {
            return template.update(QUERY_RENAME_NODE_ID, renamedEntry) > 0;
        }
    }
}
