<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    exclude-result-prefixes="chiba xforms xlink xsl">
    <!-- Copyright 2005 Chibacon -->

    <xsl:variable name="data-prefix" select="'d_'"/>
    <xsl:variable name="trigger-prefix" select="'t_'"/>
    <xsl:variable name="remove-upload-prefix" select="'ru_'"/>
    <xsl:param name="scripted" select="'false'"/>

    <!-- change this to your ShowAttachmentServlet -->
    <xsl:variable name="show-attachment-action" select="'http://localhost:8080/chiba-1.0.0/ShowAttachmentServlet'"/>
    
    <!-- This stylesheet contains a collection of templates which map XForms controls to HTML controls. -->
    <xsl:output method="html" version="4.01" indent="yes"/>


    <!-- ######################################################################################################## -->
    <!-- This stylesheet serves as a 'library' for HTML form controls. It contains only named templates and may   -->
    <!-- be re-used in different layout-stylesheets to create the naked controls.                                 -->
    <!-- ######################################################################################################## -->

    <!-- build input control -->
    <xsl:template name="input">
        <td>
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id" />
        <xsl:variable name="pos" select="position()" />
        <xsl:variable name="id" select="@id" />
        <xsl:variable name="incremental" select="@xforms:incremental"/>
        <xsl:variable name="hint" select="normalize-space(xforms:hint)"/>
        <xsl:variable name="help" select="normalize-space(xforms:help)"/>

        <xsl:choose>
            <!-- input bound to 'date' or 'dateTime' type -->
            <xsl:when test="chiba:data[@chiba:type='date' or @chiba:type='dateTime']">
                <input id="{concat($id,'-value')}" type="hidden" value="{chiba:data/text()}" name="{concat($data-prefix,$id)}"/>
                <xsl:choose>
                    <xsl:when test="chiba:data/@chiba:readonly='true'">
                        <input id="{concat($id,'-', chiba:data/@chiba:type, '-display')}" type="text" value="" title="{normalize-space(xforms:hint)}" disabled="">
                            <xsl:call-template name="assembleRepeatClasses">
                                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                                <xsl:with-param name="pos" select="$pos"/>
                                <xsl:with-param name="classes" select="'value'"/>
                            </xsl:call-template>
                        </input>
                        <img alt="calendar" id="{concat($id,'-', chiba:data/@chiba:type, '-button')}" src="forms/scripts/jscalendar/img.gif" style="cursor:pointer;" class="disabled"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <input id="{concat($id,'-', chiba:data/@chiba:type, '-display')}" type="text" value="" title="{normalize-space(xforms:hint)}" readonly="">
                            <xsl:call-template name="assembleRepeatClasses">
                                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                                <xsl:with-param name="pos" select="$pos"/>
                                <xsl:with-param name="classes" select="'value'"/>
                            </xsl:call-template>
                        </input>
                        <img alt="calendar" id="{concat($id,'-', chiba:data/@chiba:type, '-button')}" src="forms/scripts/jscalendar/img.gif" style="cursor:pointer;" class="enabled"/>
                    </xsl:otherwise>
                </xsl:choose>
                <!--<script id="{concat($id,'-script')}" defer="true">-->
                <!--<script id="{concat($id,'-script')}" type="text/javascript">-->
                <script type="text/javascript">
                    calendarSetup('<xsl:value-of select="$id"/>', '<xsl:value-of select="chiba:data"/>', '<xsl:value-of select="chiba:data/@chiba:type"/>');
                </script>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="input">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="concat($data-prefix,$id)"/>
                    </xsl:attribute>
                    <xsl:attribute name="type">text</xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:value-of select="chiba:data/text()"/>
                    </xsl:attribute>

                    <xsl:if test="string-length($hint) != 0">
                        <xsl:choose>
                            <xsl:when test="$scripted='true'">
                                <xsl:attribute name="onmouseover">return overlib('<xsl:value-of select="$hint"/>',
                                 BUBBLE, BUBBLETYPE, 'quotation', ADJBUBBLE, STATUS, 'square popup with ADJBUBBLE',
                                 TEXTSIZE,'83%');</xsl:attribute>
                                <xsl:attribute name="onmouseout">nd();</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="normalize-space(xforms:hint)"/>
                                </xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="$scripted='true'">
                        <!--<xsl:attribute name="onchange">javascript:setXFormsValue(updateUI,'<xsl:value-of select="$id"/>');</xsl:attribute>-->
                        <xsl:choose>
                            <xsl:when test="$incremental='true'">
                                <xsl:attribute name="onkeyup">javascript:setXFormsValue(this);</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="onchange">javascript:setXFormsValue(this);</xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:attribute name="onkeydown">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>
                    </xsl:if>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
        </td>
    </xsl:template>

    <!-- build output -->
    <xsl:template name="output">

        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="css" select="@class"/>
        <xsl:choose>
<!--
            <xsl:when test="@xforms:appearance='minimal'">
                <span id="{concat($id,'-value')}">
                    <xsl:value-of select="chiba:data/text()"/>
                </span>
            </xsl:when>
-->
            <xsl:when test="@xforms:appearance='image'">
                <xsl:element name="img">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:if test="$css">
                        <xsl:attribute name="class">
                            <xsl:value-of select="$css"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="src">
                        <xsl:value-of select="chiba:data/text()"/>
                    </xsl:attribute>
                    <xsl:attribute name="alt"><xsl:value-of select="xforms:label"/></xsl:attribute>
                </xsl:element>
            </xsl:when>
            <xsl:when test="@xforms:appearance='anchor'">
                <xsl:element name="a">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:if test="$css">
                        <xsl:attribute name="class">
                            <xsl:value-of select="$css"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="href">
                        <xsl:value-of select="chiba:data/text()"/>
                    </xsl:attribute>
                    <xsl:value-of select="chiba:data/text()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <span id="{$id}">
                    <span id="{concat($id,'-value')}">
                        <xsl:value-of select="chiba:data/text()"/>
                    </span>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- build range -->
    <!--
         todo: add input/output control at the side of slider in scripted mode
         todo: support different appearances ?
    -->
    <xsl:template name="range">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="name" select="concat($data-prefix,$id)"/>
        <xsl:variable name="start" select="@xforms:start"/>
        <xsl:variable name="end" select="@xforms:end"/>
        <xsl:variable name="step" select="@xforms:step"/>
        <xsl:variable name="showInput">
            <xsl:choose>
                <xsl:when test="@xforms:appearance='full'">true</xsl:when>
                <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="incremental" select="@incremental"/>
        <xsl:variable name="value" select="chiba:data/text()"/>

        <div>
            <xsl:choose>
                <xsl:when test="$scripted='true'">
                    <table border="0" cellpadding="0" cellspacing="1" id="{$id}-value" class="range-widget">
                        <tr class="rangesteps" bgcolor="silver">
                            <xsl:call-template name="drawRangeScripted">
                                <xsl:with-param name="rangeId" select="$id"/>
                                <xsl:with-param name="value" select="$value"/>
                                <xsl:with-param name="current" select="$start"/>
                                <xsl:with-param name="step" select="$step"/>
                                <xsl:with-param name="end" select="$end"/>
                            </xsl:call-template>
                        </tr>
                    </table>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="select">
                        <xsl:attribute name="id">
                            <xsl:value-of select="concat($id,'-value')"/>
                        </xsl:attribute>
<!--
                        <xsl:attribute name="name">
                            <xsl:value-of select="$name"/>
                        </xsl:attribute>
-->
                        <xsl:attribute name="size">1</xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:value-of select="normalize-space(xforms:hint)"/>
                        </xsl:attribute>
                        <xsl:call-template name="assembleRepeatClasses">
                            <xsl:with-param name="repeat-id" select="$repeat-id"/>
                            <xsl:with-param name="pos" select="$pos"/>
                            <xsl:with-param name="classes" select="'value'"/>
                        </xsl:call-template>
                        <xsl:if test="chiba:data/@chiba:readonly='true'">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                        </xsl:if>
                        <xsl:call-template name="drawRangeBasic">
                            <xsl:with-param name="rangeId" select="$id"/>
                            <xsl:with-param name="value" select="$value"/>
                            <xsl:with-param name="current" select="$start"/>
                            <xsl:with-param name="step" select="$step"/>
                            <xsl:with-param name="end" select="$end"/>
                        </xsl:call-template>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>

    <!-- *** graphical representation of range as slider component *** -->
    <xsl:template name="drawRangeScripted">
        <xsl:param name="rangeId"/>
        <xsl:param name="value"/>
        <xsl:param name="current"/>
        <xsl:param name="step"/>
        <xsl:param name="end"/>

        <xsl:if test="$current &lt;= $end">
            <xsl:variable name="classes">
                <xsl:choose>
                    <xsl:when test="$value = $current">step rangevalue</xsl:when>
                    <xsl:otherwise>step</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:element name="td">
                <!-- mark the currently active value with name -->
                <!-- todo: change this - this breaks html conformity! -->
<!--
                <xsl:if test="$value = $current">
                    <xsl:attribute name="name"><xsl:value-of select="concat($rangeId,'-value')"/></xsl:attribute>
                </xsl:if>
-->
                <xsl:attribute name="id"><xsl:value-of select="concat($rangeId,$current)"/></xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="$classes"/></xsl:attribute>
                <!-- todo: change to use 'this' instead of 'rangeId' -->
                <a href="javascript:setRange('{$rangeId}',{$current});"><img alt="" src="images/trans.gif" height="25" width="6" title="{$current}"/></a>
            </xsl:element>

        </xsl:if>

        <xsl:variable name="newStep" select="$current + $step"/>
        <xsl:if test="$newStep &lt;= $end">
            <xsl:call-template name="drawRangeScripted">
                <xsl:with-param name="rangeId" select="$rangeId"/>
                <xsl:with-param name="value" select="$value"/>
                <xsl:with-param name="current" select="$newStep"/>
                <xsl:with-param name="step" select="$step"/>
                <xsl:with-param name="end" select="$end"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- *** fallback template for representing range as a combobox in non-scripted mode *** -->
    <xsl:template name="drawRangeBasic">
        <xsl:param name="rangeId"/>
        <xsl:param name="value"/>
        <xsl:param name="current"/>
        <xsl:param name="step"/>
        <xsl:param name="end"/>

        <xsl:if test="$current &lt;= $end">
            <xsl:variable name="classes">
                <xsl:choose>
                    <xsl:when test="$value = $current">step rangevalue</xsl:when>
                    <xsl:otherwise>step</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <option id="{$rangeId}-value" value="{$current}" title="{xforms:hint}" class="selector-item">
                <xsl:if test="$value = $current">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="$current"/>
            </option>
        </xsl:if>

        <xsl:variable name="newStep" select="$current + $step"/>
        <xsl:if test="$newStep &lt;= $end">
            <xsl:call-template name="drawRangeBasic">
                <xsl:with-param name="rangeId" select="$rangeId"/>
                <xsl:with-param name="value" select="$value"/>
                <xsl:with-param name="current" select="$newStep"/>
                <xsl:with-param name="step" select="$step"/>
                <xsl:with-param name="end" select="$end"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- build secret control -->
    <xsl:template name="secret">
        <xsl:param name="maxlength"/>

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="incremental" select="@incremental"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($data-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="type">password</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="chiba:data/text()"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="$maxlength">
                <xsl:attribute name="maxlength">
                    <xsl:value-of select="$maxlength"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onchange">javascript:setXFormsValue(this);</xsl:attribute>
                <xsl:attribute name="onkeydown">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>


    <xsl:template name="select1">

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="name" select="concat($data-prefix,$id)"/>
        <xsl:variable name="parent" select="."/>
        <xsl:variable name="incremental" select="@incremental"/>
        <xsl:variable name="handler">
            <xsl:choose>
                <xsl:when test="$incremental='false'">onblur</xsl:when>
                <xsl:otherwise>onchange</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="@xforms:appearance='compact'">
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">5</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="{$handler}">javascript:setXFormsValue(this);</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- handle itemset prototype -->
                <xsl:if test="$scripted='true' and not(ancestor::xforms:repeat)">
                    <xsl:for-each select="xforms:itemset/chiba:data/xforms:item">
                        <xsl:call-template name="build-item-prototype">
                            <xsl:with-param name="item-id" select="@id"/>
                            <xsl:with-param name="itemset-id" select="../../@id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{$name}" value=""/>
            </xsl:when>
            <xsl:when test="@xforms:appearance='full'">
                <xsl:call-template name="build-radiobuttons">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
                <!-- handle itemset prototype -->
                <xsl:if test="$scripted='true' and not(ancestor::xforms:repeat)">
                    <xsl:for-each select="xforms:itemset/chiba:data/xforms:item">
                        <xsl:call-template name="build-radiobutton-prototype">
                            <xsl:with-param name="item-id" select="@id"/>
                            <xsl:with-param name="itemset-id" select="../../@id"/>
                            <xsl:with-param name="name" select="$name"/>
                            <xsl:with-param name="parent" select="$parent"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
                <!-- create hidden parameter for identification and deselection -->
                <input type="hidden" id="{$id}-value" name="{$name}" value=""/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">1</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="{$handler}">javascript:setXFormsValue(this);</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- handle itemset prototype -->
                <xsl:if test="$scripted='true' and not(ancestor::xforms:repeat)">
                    <xsl:for-each select="xforms:itemset/chiba:data/xforms:item">
                        <xsl:call-template name="build-item-prototype">
                            <xsl:with-param name="item-id" select="@id"/>
                            <xsl:with-param name="itemset-id" select="../../@id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{$name}" value=""/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="select">

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="name" select="concat($data-prefix,$id)"/>
        <xsl:variable name="parent" select="."/>
        <xsl:variable name="handler">
        <xsl:variable name="incremental" select="@incremental"/>

        <xsl:choose>
                <xsl:when test="$incremental='false'">onblur</xsl:when>
                <xsl:otherwise>onchange</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="@xforms:appearance='compact'">
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:attribute name="multiple">multiple</xsl:attribute>
                    <xsl:attribute name="size">5</xsl:attribute>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="class">value</xsl:attribute>
                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="{$handler}">javascript:setXFormsValue(this);</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="value" select="chiba:data/text()"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- handle itemset prototype -->
                <xsl:if test="$scripted='true' and not(ancestor::xforms:repeat)">
                    <xsl:for-each select="xforms:itemset/chiba:data/xforms:item">
                        <xsl:call-template name="build-item-prototype">
                            <xsl:with-param name="item-id" select="@id"/>
                            <xsl:with-param name="itemset-id" select="../../@id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{$name}" value=""/>
            </xsl:when>
            <xsl:when test="@xforms:appearance='full'">
                <xsl:call-template name="build-checkboxes">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
                <!-- handle itemset prototype -->
                <xsl:if test="$scripted='true' and not(ancestor::xforms:repeat)">
                    <xsl:for-each select="xforms:itemset/chiba:data/xforms:item">
                        <xsl:call-template name="build-checkbox-prototype">
                            <xsl:with-param name="item-id" select="@id"/>
                            <xsl:with-param name="itemset-id" select="../../@id"/>
                            <xsl:with-param name="name" select="$name"/>
                            <xsl:with-param name="parent" select="$parent"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
                <!-- create hidden parameter for identification and deselection -->
                <input type="hidden" id="{$id}-value" name="{$name}" value=""/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:attribute name="multiple">multiple</xsl:attribute>
                    <xsl:attribute name="size">3</xsl:attribute>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>

                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="{$handler}">javascript:setXFormsValue(this);</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="value" select="chiba:data/text()"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- handle itemset prototype -->
                <xsl:if test="$scripted='true' and not(ancestor::xforms:repeat)">
                    <xsl:for-each select="xforms:itemset/chiba:data/xforms:item">
                        <xsl:call-template name="build-item-prototype">
                            <xsl:with-param name="item-id" select="@id"/>
                            <xsl:with-param name="itemset-id" select="../../@id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{$name}" value=""/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- build textarea control -->
    <xsl:template name="textarea">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="incremental" select="@incremental"/>

        <xsl:element name="textarea">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($data-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:attribute name="rows">5</xsl:attribute>
            <xsl:attribute name="cols">30</xsl:attribute>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onchange">javascript:setXFormsValue(this);</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="chiba:data/text()"/>
        </xsl:element>
    </xsl:template>

    <!-- build submit -->
    <xsl:template name="submit">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$scripted='true'">
                    <xsl:attribute name="type">button</xsl:attribute>
                    <xsl:attribute name="onclick">javascript:activate(this);</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="type">submit</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($trigger-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="xforms:label"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <!--            <xsl:if test="chiba:data/@chiba:enabled='false'">-->
            <!--                <xsl:attribute name="disabled">true</xsl:attribute>-->
            <!--            </xsl:if>-->
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>

        </xsl:element>
    </xsl:template>

    <!-- build trigger -->
    <xsl:template name="trigger">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($trigger-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="type">button</xsl:attribute>
            <xsl:attribute name="src">
                <xsl:value-of select="concat($contextroot, xforms:label/@xlink:href)"/>
            </xsl:attribute>
            <xsl:attribute name="onclick">javascript:activate(this);</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="xforms:label"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <!--            <xsl:if test="chiba:data/@chiba:enabled='false'">-->
            <!--                <xsl:attribute name="disabled">true</xsl:attribute>-->
            <!--            </xsl:if>-->
            <xsl:if test="@xforms:accesskey">
                <xsl:attribute name="accesskey">
                    <xsl:value-of select="@xforms:accesskey"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="normalize-space(xforms:hint)"/> - KEY: [ALT]+
                    <xsl:value-of select="@xforms:accesskey"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@xforms:src,'.gif') or contains(@xforms:src,'.jpg') or contains(@xforms:src,'.png')">
                <img alt="{xforms:label}" src="{@xforms:src}" id="{@id}-label"/>
            </xsl:if>
        </xsl:element>

    </xsl:template>

    <!-- build upload control -->
    <xsl:template name="upload">
        <!-- the stylesheet using this template has to take care, that form enctype is set to 'multipart/form-data' -->
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($data-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="type">file</xsl:attribute>
            <xsl:attribute name="value"></xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>

            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>

            <!-- Content types accepted, from mediatype xforms:upload attribute
            to accept input attribute -->
            <xsl:attribute name="accept">
                <xsl:value-of select="translate(normalize-space(@xforms:mediatype),' ',',')"/>
            </xsl:attribute>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onchange">submitFile(this);</xsl:attribute>
                <!--<xsl:attribute name="onchange">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>                        -->
            </xsl:if>
        </xsl:element>

        <xsl:if test="$scripted='true'">
            <div class="progressbar" id="{$id}-progress"><div class="border"><div id="{$id}-progress-bg" class="background"></div></div></div>
        </xsl:if>
        <xsl:if test="xforms:filename">
            <input type="hidden" id="{xforms:filename/@id}" value="{xforms:filename/chiba:data}"/>
        </xsl:if>
        <xsl:if test="@chiba:destination">
            <!-- create hidden parameter for destination -->
            <input type="hidden" id="{$id}-destination" value="{@chiba:destination}"/>
        </xsl:if>
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- ########################################## HELPER TEMPLATES FOR SELECT, SELECT1 ######################## -->
    <!-- ######################################################################################################## -->

    <xsl:template name="build-items">
        <xsl:param name="parent"/>

        <!-- todo: refactor to handle xforms:choice / xforms:itemset by matching -->
        <optgroup id="{xforms:itemset/@id}" label="">
            <!-- add an empty item, cause otherwise deselection is not possible -->
            <option value="">
                <xsl:if test="string-length($parent/chiba:data/text()) = 0">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
            </option>

            <!-- handle items, items in choices, and items in itemsets, but neither of these in chiba:data  -->
            <xsl:for-each select="$parent/xforms:item|$parent/xforms:choices/xforms:item|$parent/xforms:itemset/xforms:item">
                <option id="{@id}-value" value="{xforms:value}" title="{xforms:hint}" class="selector-item">
                    <xsl:if test="@xforms:selected='true'">
                        <xsl:attribute name="selected">selected</xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="xforms:label"/>
                </option>
            </xsl:for-each>
        </optgroup>
    </xsl:template>

    <xsl:template name="build-item-prototype">
        <xsl:param name="item-id"/>
        <xsl:param name="itemset-id"/>

        <select id="{$itemset-id}-prototype" class="selector-prototype">
            <option id="{$item-id}-value" value="{xforms:value}" title="{xforms:hint}" class="selector-prototype">
                <xsl:if test="@xforms:selected='true'">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="xforms:label"/>
            </option>
        </select>
    </xsl:template>

    <xsl:template name="build-checkboxes">
        <xsl:param name="id"/>
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <!-- todo: refactor to handle xforms:choice / xforms:itemset by matching -->
        <span id="{xforms:itemset/@id}">
            <!-- handle items, items in choices, and items in itemsets, but neither of these in chiba:data  -->
            <xsl:for-each select="$parent/xforms:item|$parent/xforms:choices/xforms:item|$parent/xforms:itemset/xforms:item">
                <xsl:variable name="title">
                    <xsl:choose>
                        <xsl:when test="xforms:hint">
                            <xsl:value-of select="xforms:hint"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$parent/xforms:hint"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <span id="{@id}" class="selector-item">
                    <input id="{@id}-value" class="value" type="checkbox" name="{$name}" value="{xforms:value}" title="{$title}">
                        <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@xforms:selected='true'">
                            <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="$scripted='true'">
                            <xsl:attribute name="onclick">javascript:setXFormsValue(this);</xsl:attribute>
                            <xsl:attribute name="onkeydown">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>
                        </xsl:if>
                    </input>
                    <span id="{@id}-label" class="label">
                        <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates select="xforms:label"/>
                    </span>
                </span>
            </xsl:for-each>
        </span>
    </xsl:template>

    <xsl:template name="build-checkbox-prototype">
        <xsl:param name="item-id"/>
        <xsl:param name="itemset-id"/>
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="xforms:hint">
                    <xsl:value-of select="xforms:hint"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$parent/xforms:hint"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <span id="{$itemset-id}-prototype" class="selector-prototype">
            <input id="{$item-id}-value" class="value" type="checkbox" name="{$name}" value="{xforms:value}" title="{$title}">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:if test="@xforms:selected='true'">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
                <xsl:if test="$scripted='true'">
                    <xsl:attribute name="onclick">javascript:setXFormsValue(this);</xsl:attribute>
                    <xsl:attribute name="onkeydown">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>
                </xsl:if>
            </input>
            <span id="{@id}-label" class="label">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select="xforms:label"/>
            </span>
        </span>
    </xsl:template>

    <!-- overwrite/change this template, if you don't like the way labels are rendered for checkboxes -->
    <xsl:template name="build-radiobuttons">
        <xsl:param name="id"/>
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <!-- todo: refactor to handle xforms:choice / xforms:itemset by matching -->
        <span id="{xforms:itemset/@id}">
            <!-- handle items, items in choices, and items in itemsets, but neither of these in chiba:data  -->
            <xsl:for-each select="$parent/xforms:item|$parent/xforms:choices/xforms:item|$parent/xforms:itemset/xforms:item">
                <xsl:variable name="title">
                    <xsl:choose>
                        <xsl:when test="xforms:hint">
                            <xsl:value-of select="xforms:hint"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$parent/xforms:hint"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <span id="{@id}" class="selector-item">
                    <input id="{@id}-value" class="value" type="radio" name="{$name}" value="{xforms:value}" title="{$title}">
                        <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@xforms:selected='true'">
                            <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="$scripted='true'">
                            <xsl:attribute name="onclick">javascript:setXFormsValue(this);</xsl:attribute>
                            <xsl:attribute name="onkeydown">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>
                        </xsl:if>
                    </input>
                    <span id="{@id}-label" class="label">
                        <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                            <xsl:attribute name="disabled">disabled</xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates select="xforms:label"/>
                    </span>
                </span>
            </xsl:for-each>
        </span>
    </xsl:template>

    <xsl:template name="build-radiobutton-prototype">
        <xsl:param name="item-id"/>
        <xsl:param name="itemset-id"/>
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="xforms:hint">
                    <xsl:value-of select="xforms:hint"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$parent/xforms:hint"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <span id="{$itemset-id}-prototype" class="selector-prototype">
            <input id="{$item-id}-value" class="value" type="radio" name="{$name}" value="{xforms:value}" title="{$title}">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:if test="@xforms:selected='true'">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
                <xsl:if test="$scripted='true'">
                    <xsl:attribute name="onclick">javascript:setXFormsValue(this);</xsl:attribute>
                    <xsl:attribute name="onkeydown">DWRUtil.onReturn(event,submitFunction)</xsl:attribute>
                </xsl:if>
            </input>
            <span id="{@id}-label" class="label">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select="xforms:label"/>
            </span>
        </span>
    </xsl:template>


    <!-- ########## builds indexed classname for styling repeats rendered as tables ########## -->
    <xsl:template name="assembleRepeatClasses">
        <xsl:param name="repeat-id"/>
        <xsl:param name="pos"/>
        <xsl:param name="classes"/>
        <xsl:choose>
            <xsl:when test="boolean(string-length($repeat-id) > 0)">
                <xsl:attribute name="class">
                    <xsl:value-of select="concat($repeat-id,'-',$pos,' ',$classes)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="boolean(string-length(@class) > 0)">
                <xsl:attribute name="class">
                    <xsl:value-of select="concat(@class, ' ',$classes)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="class">
                    <xsl:value-of select="$classes"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
