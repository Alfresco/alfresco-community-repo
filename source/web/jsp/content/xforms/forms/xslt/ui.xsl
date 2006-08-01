<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    exclude-result-prefixes="xhtml xforms chiba xlink">

    <!-- ####################################################################################################### -->
    <!-- This stylesheet handles the XForms UI constructs [XForms 1.0, Chapter 9]'group', 'repeat' and           -->
    <!-- 'switch' and offers some standard interpretations for the appearance attribute.                         -->
    <!-- author: joern turner                                                                                    -->
    <!-- ####################################################################################################### -->

    <xsl:param name="chiba-pseudo-item" select="'chiba-pseudo-item'"/>
    <!-- ############################################ PARAMS ################################################### -->
    <!-- ##### should be declared in html4.xsl ###### -->
    <!-- ############################################ VARIABLES ################################################ -->


    <xsl:output method="html" indent="yes"/>

    <xsl:preserve-space elements="*"/>

    <!-- ####################################################################################################### -->
    <!-- #################################### GROUPS ########################################################### -->
    <!-- ####################################################################################################### -->

    <!--
    processing of groups and repeats is handled with a computational pattern (as mentioned in Michael Kay's XSLT
    Programmers Reference) in this stylesheet, that means that when a group or repeat is found its children will
    be processed with for-each. this top-down approach seems to be more adequate for transforming XForms markup
    than to follow a rule-based pattern. Also note that whenever nodesets of XForms controls are processed the
    call template 'buildControl' is used to handle the control. In contrast to apply-templates a call-template
    preserves the position() of the control inside its parent nodeset and this can be valuable information for
    annotating controls with CSS classes that refer to their parent.
    -->
    <!-- ###################################### GROUP ################################################## -->
    <xsl:template match="xforms:group" name="group">
        <xsl:variable name="group-id" select="@id"/>
        <xsl:variable name="group-classes">
            <xsl:call-template name="assemble-compound-classes">
                <xsl:with-param name="appearance" select="@xforms:appearance"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:call-template name="group-body">
            <xsl:with-param name="group-id" select="$group-id"/>
            <xsl:with-param name="group-classes" select="$group-classes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="group-body">
        <xsl:param name="group-id"/>
        <xsl:param name="group-classes"/>

        <fieldset id="{$group-id}" class="{$group-classes}">
            <legend id="{$group-id}-label">
                <xsl:choose>
                    <xsl:when test="xforms:label">
                        <xsl:attribute name="class">
                            <xsl:call-template name="assemble-label-classes"/>
                        </xsl:attribute>
                        <xsl:apply-templates select="xforms:label"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="style">display:none;</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </legend>

            <xsl:for-each select="*">

                <xsl:choose>

                    <!-- **** handle group label ***** -->
                    <xsl:when test="self::xforms:label">
                    <!--
                        <xsl:if test="not(ancestor::xforms:repeat[@xforms:appearance='compact']) and not(ancestor::xforms:group[@xforms:appearance='compact'])">
                            <legend id="{$id}-label" class="minimal-group-label">
                                <xsl:apply-templates select="."/>
                            </legend>
                        </xsl:if>
                    -->
                    </xsl:when>

                    <!-- **** handle group alert ***** -->
                    <xsl:when test="self::xforms:alert">
                        <xsl:apply-templates select="xforms:alert"/>
                    </xsl:when>

                    <!-- **** handle sub group ***** -->
                    <xsl:when test="self::xforms:group">
                        <xsl:apply-templates select="."/>
                    </xsl:when>

                    <!-- **** handle repeat ***** -->
                    <xsl:when test="self::xforms:repeat">
                        <xsl:apply-templates select="."/>
                    </xsl:when>

                    <!-- **** handle switch ***** -->
                    <xsl:when test="self::xforms:switch">
                        <xsl:apply-templates select="."/>
                    </xsl:when>

                    <!-- **** handle chiba:data element ***** -->
                    <xsl:when test="self::chiba:data">
                    </xsl:when>

                    <!-- **** handle trigger + submit ***** -->
                    <xsl:when test="self::xforms:trigger or self::xforms:submit">
                        <xsl:variable name="control-id" select="@id"/>
                        <xsl:variable name="control-classes">
                            <xsl:call-template name="assemble-control-classes"/>
                        </xsl:variable>
                        <span id="{$control-id}" class="{$control-classes}">
                            <xsl:call-template name="buildControl"/>
                        </span>
                    </xsl:when>

                    <!-- **** handle xforms control ***** -->
                    <xsl:when test="self::xforms:*">
                        <xsl:variable name="control-id" select="@id"/>
                        <xsl:variable name="control-classes">
                            <xsl:call-template name="assemble-control-classes"/>
                        </xsl:variable>
                        <xsl:variable name="label-classes">
                            <xsl:call-template name="assemble-label-classes"/>
                        </xsl:variable>
                        <div id="{$control-id}" class="{$control-classes}">
                            <label for="{$control-id}-value" id="{$control-id}-label" class="{$label-classes}"><xsl:apply-templates select="xforms:label"/></label>
                            <xsl:call-template name="buildControl"/>
                        </div>
                    </xsl:when>

                    <!-- **** handle all other ***** -->
                    <xsl:otherwise>
                        <xsl:call-template name="handle-foreign-elements"/>
                    </xsl:otherwise>

                </xsl:choose>
            </xsl:for-each>
        </fieldset>
    </xsl:template>

    <!-- ######################################################################################################## -->
    <!-- ####################################### REPEAT ######################################################### -->
    <!-- ######################################################################################################## -->

    <!-- handle minimal repeat -->
    <xsl:template match="xforms:repeat[@xforms:appearance='minimal']" name="minimal-repeat">
        <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id"/>
        <xsl:variable name="repeat-id" select="@id"/>
        <xsl:variable name="repeat-index" select="chiba:data/@chiba:index"/>
        <xsl:variable name="repeat-classes">
            <xsl:call-template name="assemble-compound-classes">
                <xsl:with-param name="appearance" select="'minimal'"/>
            </xsl:call-template>
        </xsl:variable>

        <table id="{$repeat-id}" class="{$repeat-classes}">
            <!-- register index event handler and generate prototype for scripted environment -->
            <xsl:if test="$scripted='true'">
                <script>
                    <xsl:text>document.getElementById('</xsl:text><xsl:value-of select="$repeat-id"/><xsl:text>').onclick = setRepeatIndex;</xsl:text>
                </script>
                <xsl:if test="not(ancestor::xforms:repeat)">
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']">
                        <xsl:call-template name="processMinimalPrototype">
                            <xsl:with-param name="id" select="$repeat-id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']//xforms:repeat">
                        <xsl:call-template name="processRepeatPrototype"/>
                    </xsl:for-each>
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']//xforms:itemset">
                        <xsl:call-template name="processItemsetPrototype"/>
                    </xsl:for-each>
                </xsl:if>
            </xsl:if>

            <!-- loop repeat entries -->
            <xsl:for-each select="xforms:group[@xforms:appearance='repeated']">
                <xsl:variable name="repeat-item-classes">
                    <xsl:call-template name="assemble-repeat-item-classes">
                        <xsl:with-param name="selected" select="$repeat-index=position()"/>
                    </xsl:call-template>
                </xsl:variable>

                <tr id="{@id}" class="{$repeat-item-classes}">
                    <xsl:if test="not($scripted='true')">
                        <td class="repeat-selector">
                            <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{position()}">
                                <xsl:if test="string($outermost-id)=string($repeat-id) and string($repeat-index)=string(position())">
                                    <xsl:attribute name="checked">checked</xsl:attribute>
                                </xsl:if>
                            </input>
                        </td>
                    </xsl:if>
                    <xsl:call-template name="processMinimalChildren"/>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- prototype for minimal repeat -->
    <xsl:template name="processMinimalPrototype">
        <xsl:param name="id"/>

        <tr id="{$id}-prototype" class="repeat-prototype enabled readonly required valid">
            <xsl:call-template name="processMinimalChildren"/>
        </tr>
    </xsl:template>

    <!-- children for minimal repeat -->
    <!-- todo: unify with minimal group -->
    <xsl:template name="processMinimalChildren">
        <xsl:for-each select="*[not(self::chiba:data)]">
            <xsl:variable name="control-id" select="@id"/>
            <xsl:variable name="control-classes">
                <xsl:call-template name="assemble-control-classes"/>
            </xsl:variable>
            <xsl:variable name="label-classes">
                <xsl:call-template name="assemble-label-classes"/>
            </xsl:variable>
            <td id="{$control-id}" class="{$control-classes}" valign="top">
                <label for="{$control-id}-value" id="{$control-id}-label" class="{$label-classes}"><xsl:apply-templates select="xforms:label"/></label>
                <xsl:call-template name="buildControl"/>
            </td>
        </xsl:for-each>
    </xsl:template>

    <!-- handle compact repeat -->
    <xsl:template match="xforms:repeat[@xforms:appearance='compact']" name="compact-repeat">
        <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id"/>
        <xsl:variable name="repeat-id" select="@id"/>
        <xsl:variable name="repeat-index" select="chiba:data/@chiba:index"/>
        <xsl:variable name="repeat-classes">
            <xsl:call-template name="assemble-compound-classes">
                <xsl:with-param name="appearance" select="'compact'"/>
            </xsl:call-template>
        </xsl:variable>

        <table id="{$repeat-id}" class="{$repeat-classes}">
            <!-- register index event handler and generate prototype for scripted environment -->
            <xsl:if test="$scripted='true'">
                <script>
                    <xsl:text>document.getElementById('</xsl:text><xsl:value-of select="$repeat-id"/><xsl:text>').onclick = setRepeatIndex;</xsl:text>
                </script>
                <xsl:if test="not(ancestor::xforms:repeat)">
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']">
                        <xsl:call-template name="processCompactPrototype">
                            <xsl:with-param name="id" select="$repeat-id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']//xforms:repeat">
                        <xsl:call-template name="processRepeatPrototype"/>
                    </xsl:for-each>
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']//xforms:itemset">
                        <xsl:call-template name="processItemsetPrototype"/>
                    </xsl:for-each>
                </xsl:if>
            </xsl:if>

            <!-- build table header -->
            <xsl:for-each select="xforms:group[@xforms:appearance='repeated'][1]">
                <tr class="repeat-header">
                    <xsl:call-template name="processCompactHeader"/>
                </tr>
            </xsl:for-each>

            <!-- loop repeat entries -->
            <xsl:for-each select="xforms:group[@xforms:appearance='repeated']">
                <xsl:variable name="repeat-item-classes">
                    <xsl:call-template name="assemble-repeat-item-classes">
                        <xsl:with-param name="selected" select="$repeat-index=position()"/>
                    </xsl:call-template>
                </xsl:variable>

                <tr id="{@id}" class="{$repeat-item-classes}">
                    <xsl:if test="not($scripted='true')">
                        <td class="repeat-selector">
                            <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{position()}">
                                <xsl:if test="string($outermost-id)=string($repeat-id) and string($repeat-index)=string(position())">
                                    <xsl:attribute name="checked">checked</xsl:attribute>
                                </xsl:if>
                            </input>
                        </td>
                    </xsl:if>
                    <xsl:call-template name="processCompactChildren"/>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- header for compact repeat -->
    <!-- todo: unify with compact group -->
    <xsl:template name="processCompactHeader">
        <xsl:if test="not($scripted ='true')">
            <!-- empty header for selector cell -->
            <td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
        </xsl:if>
        <xsl:for-each select="*/xforms:label">
            <xsl:variable name="author-classes">
                <xsl:call-template name="get-author-classes"/>
            </xsl:variable>
            <xsl:variable name="name-classes">
                <xsl:call-template name="get-name-classes"/>
            </xsl:variable>
            <xsl:variable name="enabled">
                <xsl:choose>
                    <xsl:when test="../chiba:data/@chiba:enabled='false'">disabled</xsl:when>
                    <xsl:otherwise>enabled</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <td id="{../@id}-label" class="{concat($author-classes, ' ', $name-classes, ' ', $enabled)}">
                <xsl:apply-templates select="self::node()[not(name(..)='xforms:trigger' or name(..)='xforms:submit')]"/>
            </td>
        </xsl:for-each>
    </xsl:template>

    <!-- prototype for compact repeat -->
    <xsl:template name="processCompactPrototype">
        <xsl:param name="id"/>

        <tr id="{$id}-prototype" class="repeat-prototype enabled readonly required valid">
            <xsl:call-template name="processCompactChildren"/>
        </tr>
    </xsl:template>

    <!-- children for compact repeat -->
    <!-- todo: unify with compact group -->
    <xsl:template name="processCompactChildren">
        <xsl:for-each select="*[not(self::chiba:data)]">
            <xsl:variable name="control-id" select="@id"/>
            <xsl:variable name="control-classes">
                <xsl:call-template name="assemble-control-classes"/>
            </xsl:variable>
            <td id="{$control-id}" class="{$control-classes}" valign="top">
                <xsl:call-template name="buildControl"/>
            </td>
        </xsl:for-each>
    </xsl:template>

    <!-- handle full repeat -->
    <xsl:template match="xforms:repeat[@xforms:appearance='full']" name="full-repeat">
        <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id"/>
        <xsl:variable name="repeat-id" select="@id"/>
        <xsl:variable name="repeat-index" select="chiba:data/@chiba:index"/>
        <xsl:variable name="repeat-classes">
            <xsl:call-template name="assemble-compound-classes">
                <xsl:with-param name="appearance" select="'full'"/>
            </xsl:call-template>
        </xsl:variable>

        <div id="{$repeat-id}" class="{$repeat-classes}">
            <!-- register index event handler and generate prototype for scripted environment -->
            <xsl:if test="$scripted='true'">
                <script type="text/javascript">
                    <xsl:text>document.getElementById('</xsl:text><xsl:value-of select="$repeat-id"/><xsl:text>').onclick = setRepeatIndex;</xsl:text>
                </script>
                <xsl:if test="not(ancestor::xforms:repeat)">
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']">
                        <xsl:call-template name="processFullPrototype">
                            <xsl:with-param name="id" select="$repeat-id"/>
                        </xsl:call-template>
                        </xsl:for-each>
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']//xforms:repeat">
                        <xsl:call-template name="processRepeatPrototype"/>
                    </xsl:for-each>
                    <xsl:for-each select="chiba:data/xforms:group[@xforms:appearance='repeated']//xforms:itemset">
                        <xsl:call-template name="processItemsetPrototype"/>
                    </xsl:for-each>
                </xsl:if>
            </xsl:if>

            <!-- loop repeat entries -->
            <xsl:for-each select="xforms:group[@xforms:appearance='repeated']">
                <xsl:variable name="repeat-item-id" select="@id"/>
                <xsl:variable name="repeat-item-classes">
                    <xsl:call-template name="assemble-repeat-item-classes">
                        <xsl:with-param name="selected" select="$repeat-index=position()"/>
                    </xsl:call-template>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="not($scripted='true')">
                        <div>
                            <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{position()}" class="repeat-selector">
                                <xsl:if test="string($outermost-id)=string($repeat-id) and string($repeat-index)=string(position())">
                                    <xsl:attribute name="checked">checked</xsl:attribute>
                                </xsl:if>
                            </input>
                            <xsl:call-template name="group-body">
                                <xsl:with-param name="group-id" select="$repeat-item-id"/>
                                <xsl:with-param name="group-classes" select="$repeat-item-classes"/>
                            </xsl:call-template>
                        </div>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="group-body">
                            <xsl:with-param name="group-id" select="$repeat-item-id"/>
                            <xsl:with-param name="group-classes" select="$repeat-item-classes"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </div>
    </xsl:template>

    <!-- prototype for full repeat -->
    <xsl:template name="processFullPrototype">
        <xsl:param name="id"/>

        <xsl:variable name="repeat-prototype-id" select="concat($id, '-prototype')"/>
        <xsl:variable name="repeat-prototype-classes" select="'repeat-prototype valid readonly required enabled'"/>

        <xsl:call-template name="group-body">
            <xsl:with-param name="group-id" select="$repeat-prototype-id"/>
            <xsl:with-param name="group-classes" select="$repeat-prototype-classes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- handle repeat without appearance -->
    <xsl:template match="xforms:repeat">
        <!-- todo: isn't compact the better default ? -->
        <xsl:call-template name="minimal-repeat"/>
    </xsl:template>

    <!-- handle repeat attributes on foreign elements -->
    <xsl:template match="*[@xforms:repeat-bind]|*[@xforms:repeat-nodeset]">
        <!-- todo: handle xforms:group[@xforms:appearance='repeated'] -->
        <xsl:apply-templates/>
    </xsl:template>

    <!-- repeat prototype helper -->
    <xsl:template name="processRepeatPrototype">
        <xsl:variable name="id" select="@id"/>

        <xsl:choose>
            <xsl:when test="@xforms:appearance='full'">
                <xsl:call-template name="processFullPrototype">
                    <xsl:with-param name="id" select="$id"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="@xforms:appearance='compact'">
                <xsl:call-template name="processCompactPrototype">
                    <xsl:with-param name="id" select="$id"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="processMinimalPrototype">
                    <xsl:with-param name="id" select="$id"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- itemset prototype helper -->
    <xsl:template name="processItemsetPrototype">
        <xsl:variable name="item-id" select="$chiba-pseudo-item"/>
        <xsl:variable name="itemset-id" select="@id"/>
        <xsl:variable name="name" select="concat($data-prefix,../@id)"/>
        <xsl:variable name="parent" select=".."/>

        <xsl:choose>
            <xsl:when test="local-name($parent)='select1' and $parent/@xforms:appearance='full'">
                <xsl:call-template name="build-radiobutton-prototype">
                    <xsl:with-param name="item-id" select="$item-id"/>
                    <xsl:with-param name="itemset-id" select="$itemset-id"/>
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="local-name($parent)='select' and $parent/@xforms:appearance='full'">
                <xsl:call-template name="build-checkbox-prototype">
                    <xsl:with-param name="item-id" select="$item-id"/>
                    <xsl:with-param name="itemset-id" select="$itemset-id"/>
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="build-item-prototype">
                    <xsl:with-param name="item-id" select="$item-id"/>
                    <xsl:with-param name="itemset-id" select="$itemset-id"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- ####################################### SWITCH ######################################################### -->
    <!-- ######################################################################################################## -->

    <!-- ### FULL SWITCH ### -->
    <!--
        Renders a tabsheet. This template requires that the author sticks to an
        authoring convention: The triggers for toggling the different cases MUST
        all appear in a case with id 'switch-toggles'. This convention makes it
        easier to maintain the switch cause all relevant markup is kept under the
        same root element.
    -->
    <xsl:template match="xforms:switch[@xforms:appearance='full']" name="full-switch">
        <xsl:variable name="switch-id" select="@id"/>
        <xsl:variable name="switch-classes">
            <xsl:call-template name="assemble-compound-classes">
                <xsl:with-param name="appearance" select="'full'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="selected-id" select="xforms:case[chiba:data/@chiba:selected='true']/@id"/>

        <table id="{$switch-id}" class="{$switch-classes}">
            <tr>
                <xsl:for-each select="xforms:case[@id='switch-toggles']/xforms:trigger">
                    <xsl:choose>
                        <xsl:when test=".//xforms:toggle/@xforms:case=$selected-id">
                            <td id="{concat(.//xforms:toggle/@xforms:case, '-tab')}" class="active-tab">
                                <xsl:call-template name="trigger"/>
                            </td>
                        </xsl:when>
                        <xsl:otherwise>
                            <td id="{concat(.//xforms:toggle/@xforms:case, '-tab')}" class="inactive-tab">
                                <xsl:call-template name="trigger"/>
                            </td>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <td class="filler-tab">
                    <xsl:value-of select="'&amp;nbsp;'" disable-output-escaping="yes"/>
                </td>
            </tr>
            <tr>
                <td colspan="{count(xforms:case[@id='switch-toggles']/xforms:trigger) + 1}" class="full-switch-body">
                    <xsl:apply-templates select="xforms:case[not(@id='switch-toggles')]"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- ### DEFAULT SWITCH ### -->
    <xsl:template match="xforms:switch">
        <xsl:variable name="switch-id" select="@id"/>
        <xsl:variable name="switch-classes">
            <xsl:call-template name="assemble-compound-classes">
                <xsl:with-param name="appearance" select="@xforms:appaerance"/>
            </xsl:call-template>
        </xsl:variable>

        <div id="{$switch-id}" class="{$switch-classes}">
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- ### SELECTED CASE ### -->
    <xsl:template match="xforms:case[chiba:data/@chiba:selected='true']" name="selected-case">
        <xsl:variable name="case-id" select="@id"/>
        <xsl:variable name="case-classes" select="'case selected-case'"/>

        <div id="{$case-id}" class="{$case-classes}">
            <xsl:apply-templates select="*[not(name()='xforms:label')]" />
        </div>
    </xsl:template>

    <!-- ### DESELECTED CASE ### -->
    <xsl:template match="xforms:case[chiba:data/@chiba:selected='false']" name="deselected-case">
        <!-- render only in scripted environment -->
        <xsl:if test="$scripted='true'">
            <xsl:variable name="case-id" select="@id"/>
            <xsl:variable name="case-classes" select="'case deselected-case'"/>

            <div id="{$case-id}" class="{$case-classes}">
                <xsl:apply-templates select="*[not(name()='xforms:label')]" />
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ### CSS CLASS ASSEMBLY ### -->

    <!-- assembles form control classes -->
    <xsl:template name="assemble-control-classes">
        <xsl:param name="appearance"/>

        <xsl:variable name="name-classes">
            <xsl:call-template name="get-name-classes">
                <xsl:with-param name="appearance" select="$appearance"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="mip-classes">
            <xsl:call-template name="get-mip-classes"/>
        </xsl:variable>
        <xsl:variable name="author-classes">
            <xsl:call-template name="get-author-classes"/>
        </xsl:variable>

        <xsl:value-of select="normalize-space(concat($name-classes, ' ', $mip-classes, ' ', $author-classes))"/>
    </xsl:template>

    <!-- assembles label classes -->
    <xsl:template name="assemble-label-classes">
        <xsl:for-each select="xforms:label[1]">
            <xsl:variable name="name-classes">
                <xsl:call-template name="get-name-classes"/>
            </xsl:variable>
            <xsl:variable name="mip-classes">
                <xsl:call-template name="get-mip-classes">
                    <xsl:with-param name="limited" select="true()"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="author-classes">
                <xsl:call-template name="get-author-classes"/>
            </xsl:variable>

            <xsl:value-of select="normalize-space(concat($name-classes, ' ', $mip-classes, ' ', $author-classes))"/>
        </xsl:for-each>
    </xsl:template>

    <!-- assembles group/switch/repeat classes -->
    <xsl:template name="assemble-compound-classes">
        <xsl:param name="appearance"/>

        <xsl:variable name="name-classes">
            <xsl:call-template name="get-name-classes">
                <xsl:with-param name="appearance" select="$appearance"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="mip-classes">
            <xsl:call-template name="get-mip-classes">
                <xsl:with-param name="limited" select="true()"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="author-classes">
            <xsl:call-template name="get-author-classes"/>
        </xsl:variable>

        <xsl:value-of select="normalize-space(concat($name-classes, ' ', $mip-classes, ' ', $author-classes))"/>
    </xsl:template>

    <!-- assembles repeat item classes -->
    <xsl:template name="assemble-repeat-item-classes">
        <xsl:param name="selected"/>

        <xsl:variable name="name-classes">
            <xsl:choose>
                <xsl:when test="boolean($selected)">
                    <xsl:text>repeat-item repeat-index</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>repeat-item</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="mip-classes">
            <xsl:call-template name="get-mip-classes"/>
        </xsl:variable>

        <xsl:value-of select="normalize-space(concat($name-classes, ' ', $mip-classes))"/>
    </xsl:template>

    <!-- ### CSS CLASS ASSEMBLY HELPERS ### -->
    <xsl:template name="get-author-classes">
        <xsl:choose>
            <xsl:when test="@class">
                <xsl:value-of select="@class"/>
            </xsl:when>
            <xsl:when test="@xhtml:class">
                <xsl:value-of select="@xhtml:class"/>
            </xsl:when>
            <xsl:when test="@xforms:class">
                <xsl:value-of select="@xforms:class"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-name-classes">
        <xsl:param name="name" select="local-name()"/>
        <xsl:param name="appearance"/>

        <xsl:choose>
            <xsl:when test="$appearance">
                <xsl:value-of select="concat($name, ' ', $appearance, '-', $name)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-mip-classes">
        <xsl:param name="limited" select="false()"/>

        <xsl:if test="chiba:data">
            <xsl:choose>
                <xsl:when test="boolean($limited)">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:enabled='false'">
                            <xsl:text>disabled</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>enabled</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="enabled">
                        <xsl:choose>
                            <xsl:when test="chiba:data/@chiba:enabled='false'">
                                <xsl:text>disabled</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>enabled</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="readonly">
                        <xsl:choose>
                            <xsl:when test="chiba:data/@chiba:readonly='true'">
                                <xsl:text>readonly</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>readwrite</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="required">
                        <xsl:choose>
                            <xsl:when test="chiba:data/@chiba:required='true'">
                                <xsl:text>required</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>optional</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="valid">
                        <xsl:choose>
                            <xsl:when test="chiba:data/@chiba:valid='false'">
                                <xsl:text>invalid</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>valid</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:value-of select="concat($enabled,' ',$readonly,' ',$required, ' ', $valid)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
