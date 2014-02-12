<!--
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
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
-->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:wcm="http://alfresco.com/wcm"
          xmlns:f="http://java.sun.com/jsf/core">
  <script type="text/javascript">
    function _xforms_getSubmitButtons()
    {
      return [ document.getElementById("wizard:next-button"),
               document.getElementById("wizard:finish-button") ];
    }

    function _xforms_getSaveDraftButtons()
    {
      return [ document.getElementById("wizard:back-button") ];
    }
  </script>
  <wcm:formProcessor id="form-data-renderer"
		     formProcessorSession="#{WizardManager.bean.formProcessorSession}" 
		     formInstanceData="#{WizardManager.bean.instanceDataDocument}" 
		     formInstanceDataName="#{WizardManager.bean.fileName}" 
		     form="#{WizardManager.bean.form}"/>
</jsp:root>


