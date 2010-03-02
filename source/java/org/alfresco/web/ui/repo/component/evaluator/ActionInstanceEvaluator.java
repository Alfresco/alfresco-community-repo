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
package org.alfresco.web.ui.repo.component.evaluator;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.evaluator.BaseEvaluator;

/**
 * Evaluator for executing an ActionEvaluator instance.
 * 
 * @author Kevin Roast
 */
public class ActionInstanceEvaluator extends BaseEvaluator
{
   private static final String EVALUATOR_CACHE = "_alf_evaluator_cache";
   
   /**
    * Evaluate by executing the specified action instance evaluator.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      try
      {
         final Object obj = this.getValue();
         if (obj instanceof Node)
         {
            result = evaluateCachedResult((Node)obj);
         }
         else
         {
            result = this.getEvaluator().evaluate(obj);
         }
      }
      catch (Exception err)
      {
         // return default value on error and report meaningful error
         StringBuilder builder = new StringBuilder("Error during ActionInstanceEvaluator evaluation of ");
         builder.append(this.getEvaluator()).append(": ");
         String msg = err.getMessage();
         if (msg != null)
         {
            builder.append(msg);
         }
         else
         {
            StringWriter strWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(strWriter);
            err.printStackTrace(writer);
            builder.append(strWriter.toString());
         }
         
         s_logger.warn(builder.toString());
      }
      
      return result;
   }
   
   /**
    * To reduce invocations of a particular evaluator for a particular node
    * save a cache of evaluator result for a node against the current request.
    * Since the same evaluator may get reused several times for multiple actions, but
    * in effect execute against the same node instance, this can significantly reduce
    * the number of invocations required for a particular evaluator. 
    * 
    * @param node Node to evaluate against
    * 
    * @return evaluator result
    */
   private boolean evaluateCachedResult(Node node)
   {
      Boolean result;
      
      ActionEvaluator evaluator = getEvaluator();
      String cacheKey = node.getNodeRef().toString() + '_' + evaluator.getClass().getName();
      Map<String, Boolean> cache = getEvaluatorResultCache();
      result = cache.get(cacheKey);
      if (result == null)
      {
         result = evaluator.evaluate(node);
         cache.put(cacheKey, result);
      }
      
      return result;
   }

   /**
    * @return the evaluator result cache - tied to the current request
    */
   private Map<String, Boolean> getEvaluatorResultCache()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      Map<String, Boolean> cache = (Map<String, Boolean>)fc.getExternalContext().getRequestMap().get(EVALUATOR_CACHE);
      if (cache == null)
      {
         cache = new HashMap<String, Boolean>(64, 1.0f);
         fc.getExternalContext().getRequestMap().put(EVALUATOR_CACHE, cache);
      }
      return cache;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.evaluator = (ActionEvaluator)values[1];
      this.evaluatorClassName = (String)values[2];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      return new Object[] {
         // standard component attributes are saved by the super class
         super.saveState(context),
         this.evaluator,
         this.evaluatorClassName 
      };
   }
   
   /** 
    * @return the ActionEvaluator to execute
    */
   public ActionEvaluator getEvaluator()
   {
      if (this.evaluator == null)
      {
         Object objEvaluator;
         try
         {
            Class clazz = Class.forName(getEvaluatorClassName());
            objEvaluator = clazz.newInstance();
         }
         catch (Throwable err)
         {
            throw new AlfrescoRuntimeException("Unable to construct action evaluator: " + getEvaluatorClassName());
         }
         if (objEvaluator instanceof ActionEvaluator == false)
         {
            throw new AlfrescoRuntimeException("Must implement ActionEvaluator interface: " + getEvaluatorClassName());
         }
         this.evaluator = (ActionEvaluator)objEvaluator;
      }
      return this.evaluator;
   }
   
   /**
    * @param evaluator     The ActionEvaluator to execute
    */
   public void setEvaluator(ActionEvaluator evaluator)
   {
      this.evaluator = evaluator;
   }
   
   /**
    * @return the evaluatorClassName
    */
   public String getEvaluatorClassName()
   {
      ValueBinding vb = getValueBinding("evaluatorClassName");
      if (vb != null)
      {
         this.evaluatorClassName = (String)vb.getValue(getFacesContext());
      }
      return this.evaluatorClassName;
   }

   /**
    * @param evaluatorClassName the evaluatorClassName to set
    */
   public void setEvaluatorClassName(String evaluatorClassName)
   {
      this.evaluatorClassName = evaluatorClassName;
   }
   
   private ActionEvaluator evaluator;
   private String evaluatorClassName;
}
