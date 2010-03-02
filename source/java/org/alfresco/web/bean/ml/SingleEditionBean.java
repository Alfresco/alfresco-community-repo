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
package org.alfresco.web.bean.ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.web.bean.repository.MapNode;

/**
 * Util class which represents a light weight representation of an edition history for the
 * client side needs.
 *
 * @author pignoya
 */
public class SingleEditionBean implements Serializable
{
    private static final long serialVersionUID = 9145202732094403340L;

    /** The edition in a list */
    private List<MapNode> edition = null;

    /** The translation list of the edition */
    private List<MapNode> translations = null;

    /**
     * @return
     */
    public String getEditionLabel()
    {
        return (String) this.getEdition().get(0).get("editionLabel");
    }

    /**
     * @return the edition
     */
    public List<MapNode> getEdition()
    {
        return edition;
    }

    /**
     * @param edition the edition to set
     */
    public void setEdition(MapNode edition)
    {
        this.edition = new ArrayList<MapNode>(1);
        translations = null;

        this.edition.add(edition);
    }

    /**
     * @return the translations
     */
    public List<MapNode> getTranslations()
    {
        return translations;
    }

    /**
     * @param translation the translations to add to the list
     */
    public void addTranslations(MapNode translation)
    {
        if (this.translations == null)
        {
            this.translations = new ArrayList<MapNode>();
        }

        this.translations.add(translation);
    }
}