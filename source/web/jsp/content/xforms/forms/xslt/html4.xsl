<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    exclude-result-prefixes="xhtml xforms chiba xlink">
    <!-- Copyright 2005 Chibacon -->

    <xsl:include href="ui.xsl"/>
    <xsl:include href="html-form-controls.xsl"/>


    <!-- ####################################################################################################### -->
    <!-- This stylesheet transcodes a XTHML2/XForms input document to HTML 4.0.                                  -->
    <!-- It serves as a reference for customized stylesheets which may import it to overwrite specific templates -->
    <!-- or completely replace it.                                                                               -->
    <!-- This is the most basic transformator for HTML browser clients and assumes support for HTML 4 tagset     -->
    <!-- but does NOT rely on javascript.                                                                        -->
    <!-- author: joern turner                                                                                    -->
    <!-- ####################################################################################################### -->

    <!-- ############################################ PARAMS ################################################### -->
    <xsl:param name="contextroot" select="''"/>
    <!-- ### this url will be used to build the form action attribute ### -->
    <xsl:param name="action-url" select="'http://localhost:8080/chiba-1.0.0/XFormsServlet'"/>


    <xsl:param name="form-id" select="'chibaform'"/>
    <xsl:param name="form-name" select="//xhtml:title"/>
    <xsl:param name="debug-enabled" select="'true'"/>

    <!-- ### specifies the parameter prefix for repeat selectors ### -->
    <xsl:param name="selector-prefix" select="'s_'"/>

    <!-- ### contains the full user-agent string as received from the servlet ### -->
    <xsl:param name="user-agent" select="'default'"/>

    <!-- ### this parameter is used when the Adapter wants to specify the CSS to use ### -->
    <xsl:param name="css-file" select="''"/>


    <xsl:param name="scripted" select="'false'"/>

    <xsl:param name="CSS-managed-alerts" select="'true'"/>

    <!-- ############################################ VARIABLES ################################################ -->
    <!-- ### checks, whether this form uses uploads. Used to set form enctype attribute ### -->
    <xsl:variable name="uses-upload" select="boolean(//*/xforms:upload)"/>

    <!-- ### checks, whether this form makes use of date types and needs datecontrol support ### -->
    <xsl:variable name="uses-dates" select="boolean(//xforms:bind[@xforms:type='date'])"/>

    <!-- ### the CSS stylesheet to use ### -->
    <xsl:variable name="default-css" select="concat($contextroot,'/jsp/content/xforms/forms/styles/xforms.css')"/>


    <xsl:output method="html" version="4.01" encoding="UTF-8" indent="yes"
                doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>
    <!-- ### transcodes the XHMTL namespaced elements to HTML ### -->
    <!--<xsl:namespace-alias stylesheet-prefix="xhtml" result-prefix="#default"/>-->

    <xsl:preserve-space elements="*"/>
    <xsl:strip-space elements="xforms:action"/>

    <!-- ####################################################################################################### -->
    <!-- ##################################### TEMPLATES ####################################################### -->
    <!-- ####################################################################################################### -->

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="xhtml:head">
        <head>
            <title>
                <xsl:value-of select="$form-name"/>
            </title>
            <link rel="shortcut icon" href="forms/images/favicon.ico.gif"/>
            <xsl:call-template name="getCSS"/>

            <xsl:if test="$scripted='true'">
                <!-- PLEASE DON'T CHANGE THE FORMATTING OF THE XSL:TEXT ELEMENTS - THEY PROVIDE CLEAN LINE BREAKS IN THE OUTPUT -->
                <!-- for DateControl - only included if dates are used in the form -->
                <xsl:if test="$uses-dates">
                    <!--<link type= "text/css" rel="stylesheet" href="forms/scripts/jscalendar/calendar-system.css"></link>-->
                    <link type="text/css" rel="stylesheet" href="{concat($contextroot,'/jsp/content/xforms/forms/scripts/jscalendar/calendar-green.css')}"></link>
                    <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/jscalendar/calendar.js')}">&#160;</script>
                    <xsl:text>
</xsl:text>
                    <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/jscalendar/calendar-setup.js')}">&#160;</script>
                    <xsl:text>
</xsl:text>
                    <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/jscalendar/lang/calendar-en.js')}">&#160;</script>
                    <xsl:text>
</xsl:text>
                </xsl:if>

                <!-- prototype lib -->
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/prototype.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- for DWR AJAX -->
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/FluxInterface.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- for DWR AJAX -->
                <script type="text/javascript" src="{concat($contextroot,'/Flux/engine.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- for DWR AJAX -->
                <script type="text/javascript" src="{concat($contextroot,'/Flux/interface/Flux.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- for DWR AJAX -->
                <script type="text/javascript" src="{concat($contextroot,'/Flux/util.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- XForms Client -->
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/PresentationContext.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- general xforms utils -->
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/xforms-util.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <!-- import slider JavaScript in case range elements are used -->
<!--
                <xsl:if test="//xforms:range">
                    <script type="text/javascript" src="forms/scripts/slider.js">&#160;</script>
                    <xsl:text>
</xsl:text>
                </xsl:if>
-->
                <!-- import rico for visual effects -->
<!--
                <script type="text/javascript" src="forms/scripts/rico.js">&#160;</script>
                <xsl:text>
</xsl:text>
-->
                <!-- import overlibmws for popups -->
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/overlibmws/overlibmws.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/overlibmws/overlibmws_bubble.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <script type="text/javascript" src="{concat($contextroot,'/jsp/content/xforms/forms/scripts/overlibmws/iframecontentmws.js')}">&#160;</script>
                <xsl:text>
</xsl:text>
                <xsl:for-each select="xhtml:script">
                    <script>
                        <xsl:attribute name="type">
                            <xsl:value-of select="@type"/>
                        </xsl:attribute>
                        <xsl:attribute name="src">
                            <xsl:value-of select="@src"/>
                        </xsl:attribute>
                    </script>
                    <xsl:text>
</xsl:text>
                </xsl:for-each>

            </xsl:if>

        </head>
    </xsl:template>

    <!-- copy unmatched mixed markup, comments, whitespace, and text -->
    <!-- ### copy elements from the xhtml2 namespace to html (without any namespace) by re-creating the     ### -->
    <!-- ### elements. Other Elements are just copied with their original namespaces.                       ### -->
    <xsl:template match="*|@*|text()" name="handle-foreign-elements">
        <xsl:choose>
            <xsl:when test="namespace-uri(.)='http://www.w3.org/1999/xhtml'">
                <xsl:element name="{local-name(.)}" namespace="">
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="xhtml:html">
        <html>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="xhtml:link">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="xhtml:body">
        <!--<xsl:copy-of select="@*"/>-->
        <body>
            <xsl:copy-of select="@*"/>
            <!--
            <div>
                <a href="jsp/forms.jsp">
                    <img src="forms/images/chiba50t.gif" style="border:none;" alt="Chiba Logo" width="113" height="39" id="chiba-logo"/>
                </a>
            </div>

            <table border="0">
                <tr>
                    <td></td>
                    <td>
-->
            <div id="loading">
                <img src="{concat($contextroot,'/jsp/content/xforms/forms/images/chiba-logo_klein2.gif')}" class="disabled" id="indicator" alt="loading" />
            </div>
                        <xsl:element name="form">
                            <xsl:attribute name="name">
                                <xsl:value-of select="$form-id"/>
                            </xsl:attribute>
                            <xsl:attribute name="action">
                                <xsl:value-of select="$action-url"/>
                            </xsl:attribute>
                            <xsl:attribute name="method">POST</xsl:attribute>
                            <xsl:attribute name="enctype">application/x-www-form-urlencoded</xsl:attribute>
                            <xsl:if test="$uses-upload">
                                <xsl:attribute name="enctype">multipart/form-data</xsl:attribute>
                                <xsl:if test="$scripted = 'true'">
                                    <iframe id="UploadTarget" name="UploadTarget" src="" style="width:0px;height:0px;border:0"></iframe>
                                </xsl:if>
                            </xsl:if>
                            <xsl:if test="$scripted != 'true'">
                                <input type="submit" value="refresh page" class="refresh-button"/>
                            </xsl:if>

                            <xsl:apply-templates/>
                            <xsl:if test="$scripted != 'true'">
                                <input type="submit" value="refresh page" class="refresh-button"/>
                            </xsl:if>
                        </xsl:element>
<!--
                    </td>
                </tr>
                <tr>
                    <td/>
                    <td>
                        <table width="100%" border="0">
                            <tr>
                                <td>

-->
                                <span id="legend">
                                    <span style="color:#A42322;">*</span> - required |
                                    <b>?</b> - help
                                </span>
                                <div id="chiba-logo">
                                    <a href="jsp/forms.jsp">
                                        <img src="forms/images/poweredby.gif" style="border:none;" alt="powered by Chiba"/>
                                    </a>
                                </div>
                                <div id="copyright">
                                    <xsl:text disable-output-escaping="yes">&amp;copy; 2001-2005 Chiba Project</xsl:text>
                                </div>
<!--
                                </td>
                                <td align="right">
-->
<!--
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
-->
            <xsl:if test="$debug-enabled='true' and $scripted='true'">
                <form id="debugform" name="debugform" action="" onsubmit="return false;">
                    <textarea id="debugarea" name="debugarea" rows="5" cols="15"></textarea>
                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                    <input id="debugappend" type="checkbox" value="Append"/>
                    <label for="debugappend">Append</label>
                    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                    <input id="debugclear" type="button" value="Clear" onclick="document.debugform.debugarea.value=''; return true;"/>
                </form>
            </xsl:if>

        </body>
    </xsl:template>

    <xsl:template match="xhtml:span">
        <span>
            <xsl:copy-of select="@xhtml:class"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <!-- ### skip chiba:data elements ### -->
    <xsl:template match="chiba:data"/>

    <!-- ### skip model section ### -->
    <xsl:template match="xforms:model"/>

    <!-- ######################################################################################################## -->
    <!-- #####################################  CONTROLS ######################################################## -->
    <!-- ######################################################################################################## -->

    <!-- ### handle xforms:input ### -->
    <xsl:template match="xforms:input">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:output ### -->
    <xsl:template match="xforms:output">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:range ### -->
    <xsl:template match="xforms:range">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:secret ### -->
    <xsl:template match="xforms:secret">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:select ### -->
    <xsl:template match="xforms:select">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:select1 ### -->
    <xsl:template match="xforms:select1">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:submit ### -->
    <xsl:template match="xforms:submit">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:trigger ### -->
    <xsl:template match="xforms:trigger">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:textarea ### -->
    <xsl:template match="xforms:textarea">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:upload ### -->
    <xsl:template match="xforms:upload">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle label ### -->
    <xsl:template match="xforms:label">
        <xsl:variable name="group-id" select="ancestor::xforms:group[1]/@id"/>
        <xsl:variable name="img" select="@xforms:src"/>

        <xsl:choose>
            <!--
                        <xsl:when test="name(..)='xforms:group'">
                            <xsl:apply-templates/>
                        </xsl:when>
            -->
            <xsl:when test="name(..)='xforms:item'">
                <span id="{@id}" class="label">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <!-- suppress trigger labels - they are handle by the control itself -->
            <xsl:when test="parent::xforms:trigger">
            </xsl:when>
            <!-- if there's an output child -->
            <xsl:when test="self::xforms:output">
                <xsl:apply-templates select="xforms:output"/>
            </xsl:when>
            <!-- if there's a src attribute pointing to some image file the image is linked in -->
            <xsl:when test="boolean($img) and ( contains($img,'.gif') or contains($img,'.jpg') or contains($img,'.png') )">
                <img src="{$img}" id="{@id}-label"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="../chiba:data/@chiba:required='true'"><span class="required-symbol">*</span></xsl:if>
    </xsl:template>

    <!-- ### handle hint ### -->
    <xsl:template match="xforms:hint">
        <!--  already handled by individual controls in html-form-controls.xsl -->
    </xsl:template>

    <!-- ### handle help ### -->
    <!-- ### only reacts on help elements with a 'src' attribute and interprets it as html href ### -->
    <xsl:template match="xforms:help">
        <xsl:param name="id"/>

        <xsl:variable name="self" select="."/>
        <xsl:if test="string-length(.) != 0">
            <span class="help-symbol">
                <script type="text/javascript">

                </script>
                <xsl:element name="a">
                    <xsl:choose>
                        <xsl:when test="$scripted='true'">
                            <xsl:attribute name="onclick">
                                return overlib('<xsl:value-of select="normalize-space(.)"/>',
                                TEXTPADDING,0, BORDER,2, STICKY, CLOSECLICK,
                                CAPTIONPADDING,4, CAPTION,'Help', CAPTIONSIZE,'12px', CLOSESIZE,'10px',
                                MIDX,0, RELY,10,TEXTSIZE,'12px',
                                STATUS,'Help');
                            </xsl:attribute>
                            <xsl:attribute name="href">javascript:void(0);</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:attribute name="href">javascript:void(0);</xsl:attribute>
                    <img src="forms/images/help_icon.gif" alt="?" border="0"/>
                </xsl:element>
            </span>

            <span id="{$id}-help" class="help-text" style="display:none;">
                <xsl:value-of select="."/>
            </span>
        </xsl:if>
    
    </xsl:template>

    <!-- ### handle explicitely enabled alert ### -->
    <!--    <xsl:template match="xforms:alert[../chiba:data/@chiba:valid='false']">-->
    <xsl:template match="xforms:alert">
        <xsl:choose>
            <xsl:when test="$CSS-managed-alerts='true'">
                <span id="{../@id}-alert" class="alert">
                    <xsl:value-of select="."/>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="../chiba:data/@chiba:valid='false' or ../chiba:data/chiba:visited='false'">
                    <span id="{../@id}-alert" class="alert">
                        <xsl:value-of select="."/>
                    </span>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ### handle extensions ### -->
    <xsl:template match="xforms:extension">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="chiba:selector">
    </xsl:template>


    <!-- ########################## ACTIONS ####################################################### -->
    <!-- these templates serve no real purpose here but are shown for reference what may be over-   -->
    <!-- written by customized stylesheets importing this one. -->
    <!-- ########################## ACTIONS ####################################################### -->

    <!-- action nodes are simply copied to output without any modification -->
    <xsl:template match="xforms:action">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="xforms:dispatch"/>
    <xsl:template match="xforms:rebuild"/>
    <xsl:template match="xforms:recalculate"/>
    <xsl:template match="xforms:revalidate"/>
    <xsl:template match="xforms:refresh"/>
    <xsl:template match="xforms:setfocus"/>
    <xsl:template match="xforms:load"/>
    <xsl:template match="xforms:setvalue"/>
    <xsl:template match="xforms:send"/>
    <xsl:template match="xforms:reset"/>
    <xsl:template match="xforms:message"/>
    <xsl:template match="xforms:toggle"/>
    <xsl:template match="xforms:insert"/>
    <xsl:template match="xforms:delete"/>
    <xsl:template match="xforms:setindex"/>


    <!-- ####################################################################################################### -->
    <!-- #####################################  HELPER TEMPLATES '############################################## -->
    <!-- ####################################################################################################### -->

    <xsl:template name="buildControl">
        <xsl:choose>
            <xsl:when test="local-name()='input'">
                <xsl:call-template name="input"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='output'">
                <xsl:call-template name="output"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='range'">
                <xsl:call-template name="range"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='secret'">
                <xsl:call-template name="secret"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select'">
                <xsl:call-template name="select"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select1'">
                <xsl:call-template name="select1"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='submit'">
                <xsl:call-template name="submit"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='trigger'">
                <xsl:call-template name="trigger"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='textarea'">
                <xsl:call-template name="textarea"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='upload'">
                <xsl:call-template name="upload"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='repeat'">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:when test="local-name()='group'">
                <xsl:apply-templates select="."/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='switch'">
                <xsl:apply-templates select="."/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- chooses the CSS stylesheet to use -->
    <xsl:template name="getCSS">
        <xsl:choose>
            <!-- if the 'css-file' parameter has been set this takes precedence -->
            <xsl:when test="string-length($css-file) > 0">
                <link rel="stylesheet" type="text/css" href="{$css-file}"/>
            </xsl:when>
            <!-- if there's a stylesheet linked plainly, then take this stylesheet. -->
            <xsl:when test="xhtml:link">
                <!-- Include all user specified CSS files -->
               <xsl:for-each
                   select="xhtml:link[@type='text/css']">
                   <link type="text/css" rel="stylesheet">
                       <xsl:attribute name="href">
                           <xsl:value-of select="@href"/>
                       </xsl:attribute>
                       <xsl:if test="@media">
                           <xsl:attribute name="media">
                               <xsl:value-of select="@media"/>
                           </xsl:attribute>
                       </xsl:if>
                   </link>
               </xsl:for-each>
            </xsl:when>

            <!--  if nothings present standard stylesheets for Mozilla and IE are choosen. -->
            <xsl:otherwise>
                <link rel="stylesheet" type="text/css" href="{$default-css}"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="xhtml:style">
            <style type="text/css">
                <xsl:value-of select="xhtml:style"/>
            </style>
        </xsl:if>
    </xsl:template>

    <!-- ***** builds a string containing the correct css-classes reflecting UI-states like
    readonly/readwrite, enabled/disabled, valid/invalid ***** -->
    <xsl:template name="assembleClasses">

        <xsl:variable name="authorClasses">
            <xsl:call-template name="collectExistingClasses"/>
        </xsl:variable>

        <!-- only execute if there's a data element which is e.g. not the case for unbound groups -->
        <xsl:variable name="pseudoClasses">
            <xsl:if test="chiba:data">
                <xsl:variable name="valid">
                    <xsl:choose>
                        <xsl:when test="string-length(chiba:data) = 0 and chiba:data/@chiba:visited='false'">valid</xsl:when>
                        <xsl:when test="chiba:data/@chiba:valid='true'">valid</xsl:when>
                        <xsl:otherwise>invalid</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="readonly">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:readonly='true'">readonly</xsl:when>
                        <xsl:otherwise>readwrite</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="required">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:required='true'">required</xsl:when>
                        <xsl:otherwise>optional</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="enabled">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:enabled='true'">enabled</xsl:when>
                        <xsl:otherwise>disabled</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:value-of select="concat(' ',$valid,' ',$readonly,' ',$required, ' ', $enabled)"/>
            </xsl:if>
        </xsl:variable>

        <xsl:value-of select="normalize-space(concat(local-name(),' ',$authorClasses,$pseudoClasses))"/>

    </xsl:template>

    <xsl:template name="collectExistingClasses">
        <xsl:variable name="classes">
            <xsl:choose>
                <xsl:when test="@class">
                    <xsl:value-of select="@class"/>
                </xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="$classes"/>
    </xsl:template>

    <xsl:template name="labelClasses">

        <!-- only execute if there's a data element which is e.g. not the case for unbound groups -->
        <xsl:choose>
            <xsl:when test="chiba:data">
                <xsl:variable name="enabled">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:enabled='true'">enabled</xsl:when>
                        <xsl:otherwise>disabled</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="concat('label ',$enabled)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'label'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="selectorName">
        <xsl:variable name="repeat-id" select="ancestor-or-self::xforms:repeat/@id" xmlns:xforms="http://www.w3.org/2002/xforms"/>
        <xsl:value-of select="concat($selector-prefix, $repeat-id)"/>
    </xsl:template>

</xsl:stylesheet>
