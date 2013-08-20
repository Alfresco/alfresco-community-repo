[#ftl]
<!DOCTYPE HTML>
<html>
  <head>
    <title>Bulk Filesystem Import Tool</title>
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

    <!-- YUI 3.x -->
    <link rel="stylesheet" type="text/css" href="${url.context}/css/yui-3.3.0-dependencies.css">
    <style type="text/css">
      .yui3-aclist-content {
        background-color   : white;
        border             : 1px solid darkgrey;
        box-shadow         : 3px 3px 4px lightgrey;
        -webkit-box-shadow : 3px 3px 4px lightgrey; /* Safari and Chrome */

       }
    </style>

    <script type="text/javascript" src="${url.context}/scripts/yui-3.3.0-dependencies.js"></script>

    <!-- Validation functions -->
    <script type="text/javascript">
      function validateRequired(field, errorMessageElement, errorMessage)
      {
        var result = true;

        if (field.value == null || field.value == "")
        {
          errorMessageElement.textContent = errorMessage;
          result = false;
        }
        else
        {
          errorMessageElement.textContent = "";
        }

        return result;
      }


      function validateForm(form)
      {
        var result = true;

        result = validateRequired(form.sourceDirectory, document.getElementById("sourceDirectoryMessage"), "Source directory is mandatory.");

        if (result)
        {
          result = ( (validateRequired(form.targetPath, document.getElementById("targetSpaceMessage"), "Target space or noderef is mandatory.")) ||
                     (validateRequired(form.targetNodeRef, document.getElementById("targetSpaceMessage"), "Target space or noderef is mandatory."))
                   ) ;
        }

        return result;
      }
    </script>
  </head>
  <body class="yui-skin-sam">
    <table>
      <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr>Bulk Filesystem Import Tool</nobr></td>
      </tr>
      <tr><td><td>Alfresco ${server.edition} v${server.version}
    </table>
    <form action="${url.service}/initiate" method="post" enctype="multipart/form-data" charset="utf-8" onsubmit="return validateForm(this);">
      <table>
        <tr>
          <td>Import directory:</td><td><input type="text" name="sourceDirectory" size="128" /></td><td id="sourceDirectoryMessage" style="color:red"></td>
        </tr>

        <tr>
          <td><br/><label for="targetPath">Target space :</label></td>
          <td id="targetSpaceMessage" style="color:red"></td>
        </tr>

        <tr>
        <!-- TODO i18n for this string -->
          <td><br/><label for="targetPath">Path:</label></td>
          <td>
            <div id="targetNodeRefAutoComplete">
              <input id="targetPath" type="text" name="targetPath" size="128" />
              <div id="targetPathAutoSuggestContainer"></div>
            </div>
          </td>
        </tr>

        <tr>
        <!-- TODO i18n for this string -->
          <td><br/><label for="targetNodeRef">or NodeRef:</label></td>
          <td>
              <input id="targetNodeRef" type="text" name="targetNodeRef" size="128" />
          </td>
        </tr>

        <tr>
          <td colspan="3">&nbsp;</td>
        </tr>
        <tr>
          <td><label for="disableRules">Disable rules:</label></td><td><input type="checkbox" id="disableRules" name="disableRules" value="disableRules" unchecked/> (unchecked means rules are enabled during the import)</td><td></td>
        </tr>
        <tr>
          <td><label for="replaceExisting">Replace existing files:</label></td><td><input type="checkbox" id="replaceExisting" name="replaceExisting" value="replaceExisting" unchecked/> (unchecked means skip files that already exist in the repository)</td><td></td>
        </tr>
        <tr>
          <td>Batch Size:</td>
          <td colspan="2"><input type="text" name="batchSize" size="5"></td>
        </tr>
        <tr>
          <td>Number of Threads:</td>
          <td colspan="2"><input type="text" name="numThreads" size="5"></td>
        </tr>
        <tr>
          <td colspan="3">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3"><input type="submit" name="submit" value="Initiate Bulk Import"></td>
        </tr>
      </table>
      <br/>
    </form>
    <script type="text/javascript">
    YUI().use("autocomplete", "autocomplete-highlighters", "datasource-get", function(Y)
    {
      Y.one('#targetPath').plug(Y.Plugin.AutoComplete,
      {
        source            : '${url.service}/ajax/suggest/spaces.json?query={query}',
        maxResults        : 25,
        resultHighlighter : 'phraseMatch',
        resultListLocator : 'data',
        resultTextLocator : 'path'
      });
    });
    </script>    
  </body>
</html>
