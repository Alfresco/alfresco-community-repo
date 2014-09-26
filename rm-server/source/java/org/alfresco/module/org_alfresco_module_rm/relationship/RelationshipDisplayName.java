/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.relationship;

/**
 * POJO representing the relationship display name
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipDisplayName
{
    /** The label text for {@link RelationshipType#BIDIRECTIONAL} */
    private String labelText;

    /** The source text for {@link RelationshipType#PARENTCHILD} */
    private String sourceText;

    /** The target text for {@link RelationshipType#PARENTCHILD} */
    private String targetText;

    /**
     * Constructor for creating the relationship display name
     *
     * @param sourceText The source text of the relationship definition
     * @param targetText The target text of the relationship definition
     * @param labelText The label text of the relationship definition
     */
    public RelationshipDisplayName(String sourceText, String targetText, String labelText)
    {
        // Parameters might be blank. No check required.

        setSourceText(sourceText);
        setTargetText(targetText);
        setLabelText(labelText);
    }

    /**
     * Gets the label text of {@link RelationshipType#BIDIRECTIONAL}
     *
     * @return The label text of {@link RelationshipType#BIDIRECTIONAL}
     */
    public String getLabelText()
    {
        return this.labelText;
    }

    /**
     * Sets the label text of {@link RelationshipType#BIDIRECTIONAL}
     *
     * @param labelText The label text of {@link RelationshipType#BIDIRECTIONAL}
     */
    private void setLabelText(String labelText)
    {
        this.labelText = labelText;
    }

    /**
     * Gets the source text of {@link RelationshipType#PARENTCHILD}
     *
     * @return The source text of {@link RelationshipType#PARENTCHILD}
     */
    public String getSourceText()
    {
        return this.sourceText;
    }

    /**
     * Sets the source text of {@link RelationshipType#PARENTCHILD}
     *
     * @param sourceText The source text of {@link RelationshipType#PARENTCHILD}
     */
    private void setSourceText(String sourceText)
    {
        this.sourceText = sourceText;
    }

    /**
     * Gets the target text of {@link RelationshipType#PARENTCHILD}
     *
     * @return The target text of {@link RelationshipType#PARENTCHILD}
     */
    public String getTargetText()
    {
        return this.targetText;
    }

    /**
     * Sets the target text of {@link RelationshipType#PARENTCHILD}
     *
     * @param targetText The target text of {@link RelationshipType#PARENTCHILD}
     */
    private void setTargetText(String targetText)
    {
        this.targetText = targetText;
    }
}
