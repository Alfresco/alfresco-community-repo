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

import static org.alfresco.util.ParameterCheck.mandatoryString;

/**
 * POJO representing the relationship display name
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RelationshipDisplayName
{
    /** The source text of the relationship */
    private String sourceText;

    /** The target text of the relationship */
    private String targetText;

    /**
     * Constructor for creating the relationship display name.
     * In case of a bidirectional relationship the source
     * text and target text will be the same.
     *
     * @param sourceText The source text of the relationship
     * @param targetText The target text of the relationship
     */
    public RelationshipDisplayName(String sourceText, String targetText)
    {
        mandatoryString("sourceText", sourceText);
        mandatoryString("targetText", targetText);

        setSourceText(sourceText);
        setTargetText(targetText);
    }

    /**
     * Gets the source text of the relationship
     *
     * @return The source text of the relationship
     */
    public String getSourceText()
    {
        return this.sourceText;
    }

    /**
     * Sets the source text of the relationship
     *
     * @param sourceText The source text of the relationship
     */
    private void setSourceText(String sourceText)
    {
        this.sourceText = sourceText;
    }

    /**
     * Gets the target text of the relationship
     *
     * @return The target text of the relationship
     */
    public String getTargetText()
    {
        return this.targetText;
    }

    /**
     * Sets the target text of the relationship
     *
     * @param targetText The target text of the relationship
     */
    private void setTargetText(String targetText)
    {
        this.targetText = targetText;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
          .append("source=").append(sourceText)
          .append(", target=").append(targetText)
          .append(")");
        return sb.toString();
    }
}
