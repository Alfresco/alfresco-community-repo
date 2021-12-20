/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.dictionary;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import static org.alfresco.repo.dictionary.M2ModelDiff.DIFF_CREATED;
import static org.alfresco.repo.dictionary.M2ModelDiff.DIFF_DELETED;
import static org.alfresco.repo.dictionary.M2ModelDiff.DIFF_UNCHANGED;
import static org.alfresco.repo.dictionary.M2ModelDiff.DIFF_UPDATED;
import static org.alfresco.repo.dictionary.M2ModelDiff.DIFF_UPDATED_INC;
import static org.alfresco.repo.dictionary.M2ModelDiff.TYPE_ASPECT;
import static org.alfresco.repo.dictionary.M2ModelDiff.TYPE_ASSOCIATION;
import static org.alfresco.repo.dictionary.M2ModelDiff.TYPE_PROPERTY;
import static org.alfresco.repo.dictionary.M2ModelDiff.TYPE_TYPE;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class DiffModelTest extends AbstractModelTest
{

    public static final String MODEL1_DUPLICATED_XML = 
            "<model name=\"test1:model11\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
            
            "   <description>Another description</description>" +
            "   <author>Alfresco</author>" +
            "   <published>2007-08-01</published>" +
            "   <version>1.0</version>" +
            
            "   <imports>" +
            "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
            "   </imports>" +
            
            "   <namespaces>" +
            "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
            "   </namespaces>" +
            
            "   <aspects>" +
            
            "      <aspect name=\"test1:aspect1\">" +
            "        <title>Base</title>" +
            "        <description>The Base Aspect 1</description>" +
            "        <properties>" +
            "           <property name=\"test1:prop9\">" +
            "              <type>d:text</type>" +
            "           </property>" +
            "           <property name=\"test1:prop10\">" +
            "              <type>d:int</type>" +
            "           </property>" +        
            "        </properties>" +
            "      </aspect>" +
                  
            "   </aspects>" +        
            
            "</model>";
    public void testDeleteModel()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL1_XML.getBytes());

        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, null);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_DELETED), 3,
                new Pair(TYPE_ASPECT, DIFF_DELETED), 3);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    @SuppressWarnings("unused")
    public void testNoExistingModelToDelete()
    {
        try
        {
            List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(null, null);
            assertTrue("Should throw exeception that there is no previous version of the model to delete", true);
        }
        catch (AlfrescoRuntimeException e)
        {
            assertTrue("Wrong error message", e.getMessage().equals("Invalid arguments - no previous version of model to delete"));
        }
        
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL1_XML.getBytes());

        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        
        CompiledModel compiledModel = dictionaryDAO.getCompiledModel(modelName);
        
        try
        {
            List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(null, compiledModel);
            assertTrue("Should throw exeception that there is no previous version of the model to delete", true);
        }
        catch (AlfrescoRuntimeException e)
        {
            assertTrue("Wrong error message", e.getMessage().equals("Invalid arguments - no previous version of model to delete"));
        }
    }
    
    public void testNewModel()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL1_XML.getBytes());

        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(null, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_CREATED), 3,
                new Pair(TYPE_ASPECT, DIFF_CREATED), 3);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testDuplicateModels()
    {
        ByteArrayInputStream byteArrayInputStream1 = new ByteArrayInputStream(AbstractModelTest.MODEL1_XML.getBytes());
        ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(MODEL1_DUPLICATED_XML.getBytes());

        M2Model model1 = M2Model.createModel(byteArrayInputStream1);
        dictionaryDAO.putModel(model1);

        M2Model model2 = M2Model.createModel(byteArrayInputStream2);

        try
        {
            dictionaryDAO.putModel(model2);
            fail("This model with this URI has already been defined");
        }
        catch (NamespaceException exception)
        {
            // Ignore since we where expecting this
        }
    }
    
    public void testNonIncUpdateModel()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL1_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL1_UPDATE1_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_CREATED), 1,
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 2,
                new Pair(TYPE_TYPE, DIFF_DELETED), 1,

                new Pair(TYPE_ASPECT, DIFF_CREATED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 2,
                new Pair(TYPE_ASPECT, DIFF_DELETED), 1,

                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 6,
                new Pair(TYPE_PROPERTY, DIFF_UPDATED), 1,
                new Pair(TYPE_PROPERTY, DIFF_DELETED), 1);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testIncUpdatePropertiesAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL2_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL2_EXTRA_PROPERTIES_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 4,
                new Pair(TYPE_PROPERTY, DIFF_CREATED), 2);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }

    public void testIncUpdateTypesAndAspectsAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL3_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL3_EXTRA_TYPES_AND_ASPECTS_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_TYPE, DIFF_CREATED), 1,
                new Pair(TYPE_ASPECT, DIFF_CREATED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 4);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testIncUpdateAssociationsAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL5_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL5_EXTRA_ASSOCIATIONS_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UPDATED_INC), 1,
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 2,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 6,
                new Pair(TYPE_ASSOCIATION, DIFF_CREATED), 2);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testIncUpdateTitleDescription()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL6_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL6_UPDATE1_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UPDATED_INC), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UPDATED_INC), 2);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testNonIncUpdatePropertiesRemoved()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL2_EXTRA_PROPERTIES_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL2_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 4,
                new Pair(TYPE_PROPERTY, DIFF_DELETED), 2);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testNonIncUpdateTypesAndAspectsRemoved()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL3_EXTRA_TYPES_AND_ASPECTS_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL3_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_TYPE, DIFF_DELETED), 1,
                new Pair(TYPE_ASPECT, DIFF_DELETED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 4);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testNonIncUpdateDefaultAspectAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL4_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL4_EXTRA_DEFAULT_ASPECT_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UPDATED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 2);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testNonIncUpdateAssociationsRemoved()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL5_EXTRA_ASSOCIATIONS_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL5_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_TYPE, DIFF_UPDATED), 1,
                new Pair(TYPE_TYPE, DIFF_UNCHANGED), 1,
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 2,
                new Pair(TYPE_PROPERTY, DIFF_UNCHANGED), 6,
                new Pair(TYPE_ASSOCIATION, DIFF_DELETED), 2);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testIncUpdatePropertiesAddedToMandatoryAspect()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL7_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL7_EXTRA_PROPERTIES_MANDATORY_ASPECTS_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 2,
                new Pair(TYPE_PROPERTY, DIFF_CREATED), 1);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    public void testNonIncUpdatePropertiesRemovedFromMandatoryAspect()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL7_EXTRA_PROPERTIES_MANDATORY_ASPECTS_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL7_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 2,
                new Pair(TYPE_PROPERTY, DIFF_DELETED), 1);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    /**
     * Changing a property from mandatory/enforced/protected to NON mandatory/enforced/protected
     * is an incremental change and it should be allowed.
     */
    public void testIncChangeMandatoryProperties()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL8_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL8_CHANGE_MANDATORY_PROPERTIES_ASPECTS_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UPDATED_INC), 1);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }
    
    /**
     * Changing a property from NOT mandatory/enforced/protected to mandatory/enforced/protected
     * is considered to be a non incremental change.
     */
    public void testNonIncChangeMandatoryProperties()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL8_CHANGE_MANDATORY_PROPERTIES_ASPECTS_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        CompiledModel previousVersion = dictionaryDAO.getCompiledModel(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL8_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModel(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);

        Map<Pair<String, String>, Integer> expected = Map.of(
                new Pair(TYPE_ASPECT, DIFF_UNCHANGED), 1,
                new Pair(TYPE_PROPERTY, DIFF_UPDATED), 1);
        assertEquals("Unexpected set of diffs found.", expected, getAllDiffCounts(modelDiffs));
    }

    /**
     * Count the diffs grouping by element type and diff type.
     *
     * @param m2ModelDiffs The list of diffs returned from the dictionaryDAO.
     * @return A map from (elementType, diffType) to the number of occurrences of matching diffs in the list.
     */
    private Map<Pair<String, String>, Integer> getAllDiffCounts(List<M2ModelDiff> m2ModelDiffs)
    {
        return m2ModelDiffs.stream()
                           .map(modelDiff -> new Pair<>(modelDiff.getElementType(), modelDiff.getDiffType()))
                           .collect(toMap(identity(), pair -> 1, Integer::sum));
    }
}
