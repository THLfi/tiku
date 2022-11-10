[#ftl]<!DOCTYPE html>
[#include "common.ftl" /]

[#setting locale="fi"]

[#assign containsMap = false /]
[#assign features = {} /]
[#assign dimensionSeparator = '-' /]
[#assign nodeSeparator = '.' /]

[#function value val]
    [#return val /]
[/#function]

[#function subtitleHeadingLevel presentation]
	[#assign headingLevel = presentation.type?keep_after("subtitle") /]
    [#if headingLevel == "" || !["1", "2", "3"]?seq_contains(headingLevel)]
        [#assign headingLevel = "3" /]
    [#else]
        [#assign headingLevel = headingLevel?number + 2 /]
    [/#if]
	[#return headingLevel]
[/#function]

[#macro show_filter_values presentation]

[/#macro]
[#macro summary_hierarchical_option option stages level=1]
  [#if stages?seq_contains(option.level.id)]
    <option value="${option.surrogateId}" ${selected} data-level="l${level}">[@label option /]</option>
  [/#if]
  [#list option.children as child]
    [@summary_hierarchical_option child stages level + 1/]
  [/#list]
[/#macro]

[#macro recursiveAreaOptions level]
  <option value="${level.id}">${level.label.getValue("fi")}</option>
  [#if level.childLevel??]
    [@recursiveAreaOptions level.childLevel /]
  [/#if]
[/#macro]
[#macro summary_filter_form]

<form id="summary-form" class="form form-horizontal" method="GET" action="#" role="region">

[#list summary.presentations as presentation]
  [#if presentation.type == "map"]
    [#list presentation.dimensions as d]
      [#if d.dimension == "area"]
        [#assign mapLevels = d.levels /]
      [/#if]
    [/#list]
    [#if mapLevels?? && mapLevels?size > 1]
    <div class="form-group">
      <label class="bold" for="geo">
        [#if lang="en"]Select region
        [#elseif lang="sv"]Välj områden
        [#else]Valitse aluejako
        [/#if]
      </label>
      <select class="form-control" name="geo" id="geo">
      [#list mapLevels as i]
        <option value="${i.id}" [#if summary.geometry! == i.id]selected[/#if]>
          ${i.label.getValue(lang)}
        </option>
      [/#list]
      </select>
    </div>
    <br />
    [/#if]
  [/#if]

[/#list]


[#list summary.selections as filter]
    [#assign dim = filter.dimensionEntity /]

    [#if !filter.visible]
      <input type="hidden" name="${filter.id}_0" value="${filter.selected[0].id}" />
    [#else]
      [#list filter.filterStages as filterStage]
        <div class="form-group search">
          <label class="bold" for="s-${filter.id}-${filterStage_index}">${filterStage.label.getValue(lang)}</label>
          [#if filter.searchable && !filter.multiple]
            <div class="dropdown">
                <div class="search-group">
                    <span class="fas fa-search"></span>
                    <input class="form-control search-control" placeholder="[#if filterStage.selected??]${filterStage.selected[0].label.getValue(lang)}[/#if]"/>
                </div>
              <ul class="dropdown-menu">
                <li><small class="float-end dropdown-close">
                  <a href="#">
                  [#if lang="en"]close
                  [#elseif lang="sv"]stäng
                  [#else]sulje
                  [/#if]
                  </a></small>
                </li>
              </ul>
            </div>
          [/#if]
              <select class="form-control" name="${filter.id}_${filterStage_index}" id="s-${filter.id}-${filterStage_index}" [#if filter.multiple]multiple[/#if]>
                [#if filterStage_index > 0]
                  <option value=""></option>
                [/#if]
                [#list filterStage.options as option]
                    [#if option.hidden && option.measure]
                    [#else]
                        [#assign selected]
                            [#if filterStage.selected??]
                              [#list filterStage.selected as s]
                                [#if s.id == option.id]
                                  selected
                                  [#break /]
                                [/#if]
                              [/#list]
                            [/#if]
                        [/#assign]
                        <option value="${option.surrogateId}" ${selected} data-level=[#if filter.isCompleteDimension]"${option.level.index}"[#else]"0"[/#if]>
                          [@label option /]
                        </option>
                    [/#if]
                [/#list]
              </select>
        </div>
      [/#list]
      <br />
    [/#if]
[/#list]
[#list summary.drilledDimensions as dim]
    [#list summary.getDrilledNodes(dim) as dn]
        <input name="drill-${dim}" value="${dn.id}" type="hidden" />
    [/#list]
[/#list]
</form>
[/#macro]

[#macro export presentation type]
<div class="row justify-content-end">
    <div class="col-md-auto">
        <div class="export btn-group dropdown" role="group" aria-label="...">
          <button type="button" class="btn btn-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">${message("cube.export")}</button>
          <ul class="dropdown-menu" role="menu">
            <li class="csv-action" role="presentation">
              <a class="dropdown-item" role="menuitem" href="${factTable}.csv?${presentation.dataUrl}">
                <span class="fas fa-file"></span>
                ${message("cube.export.csv")}
              </a>
            </li>
            <li class="xlsx-action" role="presentation">
              <a class="dropdown-item" role="menuitem" href="${factTable}.xlsx?${presentation.dataUrl}">
                <span class="fas fa-file"></span>
                ${message("cube.export.xlsx")}
              </a>
            </li>
            [#if type == "image"]
            <li class="img-action" role="presentation">
              <a class="dropdown-item" role="menuitem" href="#">
                <span class="fas fa-file"></span>
                ${message("cube.export.image")}
              </a>
            </li>
            [/#if]
          </ul>
        </div>
    </div>
</div>
[/#macro]

[#macro summary_presentations]
[#list summary.sections as row]

  <div class="row presentation-row">
    [#list row.children as block]

    [#assign colWidth][#if block.width??]${block.width}[#else]${12/row.children?size}[/#if][/#assign]
    <div class="col-md-${colWidth}">
     [#list block.presentations as presentation]

      [#if presentation.groupSize > 0]
          [#if presentation.isFirst()]
          [#-- put a group into a single row --]
          <div class="row">
          [/#if]
          [#-- each presentation into their respective column --]
          <div class="col-sm-${12/presentation.groupSize}">
      [/#if]

      [#if !presentation.valid]
            <p>
                ...
            </p>
      [#elseif !presentation.visible]
            <p>
                hidden
            </p>
      [#elseif (presentation.type!"")?starts_with("subtitle")]
          [#assign headingLevel = subtitleHeadingLevel(presentation) /]
          <h${headingLevel}>
              ${presentation.getContent(lang)}
          </h${headingLevel}>
      [#elseif "info" = presentation.type]
          <p>
              ${presentation.getContent(lang)}
          </p>
      [#elseif "bar" = presentation.type  || "barstacked" = presentation.type || "barstacked100" = presentation.type]
          [@show_filter_values presentation /]
          <div
              id="[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation bar"
              [#if presentation.min??]data-min="${presentation.min}"[/#if]
              [#if presentation.max??]data-max="${presentation.max}"[/#if]
              [#if "barstacked" = presentation.type]data-stacked="true" data-stacked="true"[/#if]
              [#if "barstacked100" = presentation.type]data-stacked="true" data-percent="true"[/#if]
              [#if presentation.showConfidenceInterval]data-ci="true"[/#if]
              [#if presentation.showSampleSize]data-n="true"[/#if]             
              [#if presentation.emphasizedNode??]data-em="[#list presentation.emphasizedNode as n][#if n_index>0],[/#if]${n.surrogateId}[/#list]"[/#if]
              data-ref="${factTable}.json?${presentation.dataUrl}"
              data-sort="${presentation.sortMode}">
              [@export presentation "image" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
              </div>
          <br />
      [#elseif "column" = presentation.type || "columnstacked" = presentation.type || "columnstacked100" = presentation.type]
          [@show_filter_values presentation /]
          [#assign emindex = 0 /]
          <div
              id="$[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation column[#if presentation.widthMeasure??]-mekko[/#if]"
              [#if "columnstacked" = presentation.type]data-stacked="true"[/#if]
              [#if "columnstacked100" = presentation.type]data-stacked="true" data-percent="true"[/#if]
              [#if presentation.min??]data-min="${presentation.min}"[/#if]
              [#if presentation.max??]data-max="${presentation.max}"[/#if]
              [#if presentation.showConfidenceInterval]data-ci="true"[/#if]
              [#if presentation.showSampleSize]data-n="true"[/#if]              
              [#if presentation.emphasizedNode??]data-em="[#list presentation.emphasizedNode as n][#if n??][#if emindex>0],[/#if][#assign emindex=1/]${n.surrogateId}[/#if][/#list]"[/#if]
              data-ref="${factTable}.json?${presentation.dataUrl}"
              data-sort="${presentation.sortMode}"
              [#if presentation.legendless??]data-legendless="${presentation.legendless}"[/#if]
              [#if presentation.widthMeasure??]data-width-measure="${presentation.widthMeasure.surrogateId!'N/A'}"[/#if]>
              [@export presentation "image" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
          </div>
         <br />
      [#elseif "line" = presentation.type]
          [@show_filter_values presentation /]
          <div
              id = "[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation line"
              [#if presentation.min??]data-min="${presentation.min}"[/#if]
              [#if presentation.max??]data-max="${presentation.max}"[/#if]
              data-ref="${factTable}.json?${presentation.dataUrl}"
              data-sort="${presentation.sortMode}"
               [#if presentation.showSampleSize]data-n="true"[/#if]
              [#if presentation.showConfidenceInterval]data-ci="true"[/#if]>
              [@export presentation "image" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
              </div>
         <br />
       [#elseif "radar" = presentation.type]
          [@show_filter_values presentation /]
          <div
              id = "[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation radar"
              [#if presentation.min??]data-min="${presentation.min}"[/#if]
              [#if presentation.max??]data-max="${presentation.max}"[/#if]
              data-ref="${factTable}.json?${presentation.dataUrl}"
              data-sort="${presentation.sortMode}"
              [#if presentation.showSampleSize]data-n="true"[/#if]
              [#if presentation.showConfidenceInterval]data-ci="true"[/#if]>
              [@export presentation "image" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
              </div>
         <br />
      [#elseif "pie" = presentation.type]
          [@show_filter_values presentation /]
          <div
              id="[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation pie"
              data-ref="${factTable}.json?${presentation.dataUrl}"
              [#if presentation.showSampleSize]data-n="true"[/#if]>
              [@export presentation "image" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
          </div>
         <br />
      [#elseif "gauge" = presentation.type]
          [@show_filter_values presentation /]
          <div
              id="[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation gauge"
              [#if presentation.showSampleSize]data-n="true"[/#if]
              [#if presentation.min??]data-min="${presentation.min}"[/#if]
              [#if presentation.max??]data-max="${presentation.max}"[/#if]
              [#if presentation.palette??]data-palette="${presentation.palette}"[/#if]
              data-ref="${factTable}.json?${presentation.dataUrl}"
                   [#if presentation.measure?? && presentation.measure.limits??]
                        [#assign limits = presentation.measure.limits /]
                        data-limit-order="[#if limits.ascendingOrder]asc[#else]desc[/#if]"
                        data-limits="${limits}"
                        data-limit-include="[#if limits.lessThanOrEqualTo]lte[#else]gte[/#if]"
                        [#list limits.areas as limitArea]
                          data-limitarea-${limitArea_index}="${limitArea}"
                        [/#list]
                        [#list limits.labels as limitLabel]
                          data-limit-${limitLabel_index}="${limitLabel.getValue(lang)?html}"
                        [/#list]
                      [#else]
                        data-limit-order="asc"
                        data-limits="0,25,75,100"
                        data-limit-include="lte"
                      [/#if]
              >
              [@export presentation "image" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
          </div>
         <br />
      [#elseif "table" = presentation.type]
          [@show_filter_values presentation /]
          <div
              id="$[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
              class="presentation table"
              data-column-count="${presentation.columns?size}"
              data-columns="[#list presentation.columns as column]${column.id!}[/#list]"
              data-align="[#list presentation.columns as column]${column.valueAlign!} ${column.headerAlign!}[/#list]"
              data-row-count="${presentation.rows?size}"
              data-rows="[#list presentation.rows as row]${row.id!}[/#list]"
              data-ref="${factTable}.json?${presentation.dataUrl}"
              data-suppress="${presentation.suppress}"
              data-highlight="${presentation.highlight}"
              data-row-dim-highlights="[#list presentation.rows as row][#if "HIGHLIGHT" == row.totalMode!]1 [#else]0 [/#if][/#list]"
              data-column-dim-highlights="[#list presentation.columns as column][#if "HIGHLIGHT" == column.totalMode!]1 [#else]0 [/#if][/#list]"
              data-row-highlight-nodes="[#list presentation.rows as row][#if row.totalHighlightNodes??][#list row.totalHighlightNodes as node]${node.surrogateId} [/#list][/#if][/#list]"
              data-column-highlight-nodes="[#list presentation.columns as column][#if column.totalHighlightNodes??][#list column.totalHighlightNodes as node]${node.surrogateId} [/#list][/#if][/#list]">
              [@export presentation "table" /]
              <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
          </div>
         <br />
        [#elseif "list" = presentation.type]
            [@show_filter_values presentation /]
            <div
                id="[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
                class="presentation list"
                data-ref="${factTable}.json?${presentation.dataUrl}"
                [@export presentation "table" /]
                <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
            </div>
            <br />
      [#elseif "map" = presentation.type]
      <div 
        id="[#if presentation.id??]${presentation.id!}[#else]p${presentation_index}[/#if]"
        class="presentation map"
        data-ref="${factTable}.json?${presentation.dataUrl}" 
        data-geometry="${presentation.geometry!}" 
        data-stage="${presentation.area}"
        data-palette="${presentation.palette!}"
        [#if presentation.measure??]
        data-label="${presentation.measure.label.getValue(lang)?html}"
        [/#if]
        [#if presentation.measure?? && presentation.measure.limits??]
          [#assign limits = presentation.measure.limits /]
          [#assign measure = presentation.measure /]
          data-limit-order="[#if limits.ascendingOrder]asc[#else]desc[/#if]"
          data-limits="${limits}"
          data-limit-include="[#if limits.lessThanOrEqualTo]lte[#else]gte[/#if]"
          data-decimals="${measure.decimals}"
          [#list limits.labels as limitLabel]
            data-limit-${limitLabel_index}="${limitLabel.getValue(lang)?html}"
          [/#list]
        [#else]
        data-no-limits[/#if]
      >
      [@export presentation "image" /]
      <img src="${rc.contextPath}/images/loading.gif" alt="loading"/>
      </div>
      [#assign containsMap = true]
      [#else]
      <h3>${presentation.type}</h3>
      <p>${presentation.content!""}</p>
      [/#if]


      [#if presentation.groupSize > 0]
          [#-- close presentation groups --]
          </div>
          [#if presentation.isLast()]
          [#-- close row --]
          </div>
          [/#if]
      [/#if]

       [/#list]

      </div>
    [/#list]
  </div>

[/#list]
[/#macro]
[#macro breadcrumbs drillNode isLeaf = true]
    [#if drillNode.parent??]
        [#if drillNode.level.id != summary.getMaximumLevelId(drillNode.dimension.id)]
            [@breadcrumbs drillNode.parent false /]
        [/#if]
    [/#if]
    <li class="breadcrumb-item active">
        [#if isLeaf]
            [@label drillNode /]
        [#else]
            <a href="#" data-node="${drillNode.surrogateId}" data-dim="${drillNode.dimension.id}">
                [@label drillNode /]
            </a>
        [/#if]
    </li>
[/#macro]
[#macro label e][#if e?? && e.label??]${e.label.getValue(lang)} [#else]???[/#if][/#macro]

[#macro printContactInfo type]
  [#if contactInformation.getContactInformationByTypeName(type)??]
    [#assign contactLabel = contactInformation.getContactInformationByTypeName(type) /]
    [#if type == "link"]
    <span class="contact-info">
      <a target="_blank" href="${contactLabel.getValue(lang)!}">${contactLabel.getValue(lang)!}</a>
    </span>
    [#else]
    <span class="contact-info">${contactLabel.getValue(lang)!}</span>
    [/#if]
  [/#if]
[/#macro]

<html lang="${lang!"fi"}">
    <head>
        <title>${summary.title.getValue(lang)} - ${factName.getValue(lang)} -  ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/fontawesome.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/solid.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/fonts/source-sans-pro/2.0.10/source-sans-pro.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css?v=${buildTimestamp}" />
        <link rel="stylesheet" href="${rc.contextPath}/css/summary.css?v=${buildTimestamp}" />
        <link href="${rc.contextPath}/images/favicon.ico" rel="shortcut icon" type="image/x-icon" />
       

        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/js/respond.min.js"></script>
        <![endif]-->

    </head>
    <body class="common">

    <header role="banner" class="summary-header container-fluid">
        <div class="logo">
            <img 
            src="${rc.contextPath}/images/THL_tunnus_pitka_${uiLanguage!"fi"}_RGB.svg">
        </div>
        <div class="col-xs-12 col-md-9">
            <h1>
              ${summary.subject.getValue(lang)}
              [#if env != "prod"]
              <span class="environment">${env}</span>
              [/#if]
            </h1>
        </div>

        <div class="clearfix"></div>
    </header>


    <div class="stripe">

      <div id="languages" role="navigation" aria-labelledby="languages">
        [#if supportedLanguages?? && supportedLanguages?size > 1]
          <ul>         
          <li><div class="hide-xl btn-group vl"></div></li>
          [#list supportedLanguages as x]
            [#if x != lang]
              <li><a href="${rc.contextPath}/${cubeRequest.getSummaryUrl(x)}">${x}</a></li>
            [#else]
              <li class="active"><a href="${rc.contextPath}/${cubeRequest.summaryUrl}">${x}</a></li>
            [/#if]
          [/#list]
          </ul>
        [/#if]
      </div>

      <div class="stripe-left" role="navigation">
        <div class="btn-group float-start filter-toggle">
          <button class="btn btn-default">
             <i class="fas fa-sliders-h"></i>
             <span class="sr-only">${message("cube.options")}</span>
          </button>
        </div>

        <div class="float-end rightest hide-xs"></div>

        [#if requireLogin]
        <div class="btn-group float-end" role="group" aria-label="${message("site.logout")}">
            <form class="form" method="POST" action="${rc.contextPath}/${summaryRequest.summaryUrl}/logout">
                <button type="submit" class="btn btn-default">
                	<i class="fas fa-sign-out-alt"></i>
                	<span class="hide-xs">${message("site.logout")}</span>
                </button>
            </form>
        </div>
        [/#if]
        <div class="btn-group float-end help" role="group">
          [#if summary.link.getValue(lang)?? && !summary.link.getValue(lang)?ends_with('n/a')]
          <a href="${summary.link.getValue(lang)}" target="_blank" type="submit" class="btn btn-default">
            <span class="fas fa-info-circle"></span>
            <span class="hide-xs">${message("summary.more")}</span>
          </a>
          [/#if]
          <a href="${message("site.help.url")}" target="_blank" type="submit" class="btn btn-default">
            <span class="fas fa-question-circle"></span>
            <span class="hide-xs">${message("site.help")}</span>
          </a>
        </div>
  
        [#if reports?size>0]
        <div class="btn-group help small-button" role="group">
          <a href="${rc.contextPath}/${summaryRequest.env}/${lang}/${reports?first.subject}">
            <i class="fa fa-th"></i>
            <span class="hide-xs">${message("site.changeMaterial")}</span>	
     	    </a>
        </div>
        [/#if]
  
        [#if env == "deve" || env == "test"]
        <div class="btn-group help small-button" role="group">
          <a href="${rc.contextPath}/${summaryRequest.summaryUrl}/source">
            <i class="fa fa-code"></i> 
            <span class="hide-xs">${message("summary.view-source")}</span>
          </a>
        </div>
        [/#if]
        
      </div>
    </div> <!-- end stripe -->

    <div class="summary-body container-fluid">
        <div class="summary-content row">

            <div class="col-sm-3">
                [@summary_filter_form /]
            </div>

            <div class="col-sm-9">

                [#list drillNodes as drillNode]
                <ul class="breadcrumb">
                    [@breadcrumbs drillNode /]
                </ul>
                [/#list]

                <div class="content" role="main">
                  <h2>${summary.title.getValue(lang)}</h2>
                  [#if summary.note.getValue(lang)! == "n/a"]
                  [#else]
                  <p>${summary.note.getValue(lang)!}</p>
                  [/#if]

                  [@summary_presentations /]
                </div>

                <div class="data-info col-xs-12 text-center">
                  <span class="bold">${message("site.contact")}: </span>
                  [#if contactInformation??]
                  <div class="inline-block">
                    [@printContactInfo "name"/]
                    [@printContactInfo "mail"/]
                    [@printContactInfo "phone"/]
                    [@printContactInfo "link"/]
                  </div>
                  [#else]
                  ${message("site.contact.information")}
                  [/#if]
                  <div>
                    &copy; ${message("site.company")} ${.now?string("yyyy")}[#if isOpenData], ${message("site.license.cc")}[/#if]. ${message("summary.data-updated")} ${runDate?string("dd.MM.yyyy")}
                  </div>
                </div>
            </div>

            <div class="clearfix"></div>
        </div>
        <div class="clearfix"></div>

        [#include "cube/cube-modal.ftl" /]

    </div>

    [@footer/]

    <script src="${rc.contextPath}/js/jquery.js"></script>
    <script src="${rc.contextPath}/js/jquery-ui.js"></script>
    <script src="${rc.contextPath}/js/jquery.ui.touch-punch.min.js"></script>
    <script src="${rc.contextPath}/js/bootstrap.bundle.min.js"></script>

    <script nonce="${cspNonce}">
        [#include "summary-script.ftl" /]
    </script>

    <script src="${rc.contextPath}/js/d3.min.js"></script>
    <script src="${rc.contextPath}/js/json-stat.js"></script>
    <script src="${rc.contextPath}/js/map-palette.min.js?v=${buildTimestamp}"></script>
    <script src="${rc.contextPath}/js/summary.js?v=${buildTimestamp}"></script>

    <script nonce="${cspNonce}">
    var labels = {},
        dimensionData = {},
        isDrillEnabled = [#if summary.isDrillEnabled()]true[#else]false[/#if];
    [#list summary.nodes as n]
    [#if n??]
    labels[${n.surrogateId!"-1"}] = '[#if n?? && n.label??]${n.label.getValue(lang)?js_string}[#else]???[/#if]';
    dimensionData[${n.surrogateId}] = {
        hasChild: [#if n.children?size>0]true[#else]false[/#if],
        dim: [#if n.dimension??]'${n.dimension.id}'[#else]'n/a'[/#if],
        code: "${n.code!?js_string}"
    };
    [/#if]
    [/#list]
    </script>

    [#if containsMap] 
      [#assign mapLang][#if lang="sv"]sv[#elseif lang="en"]en[#else]fi[/#if][/#assign]
      <link href="${rc.contextPath}/css/leaflet.css" rel="stylesheet">
      <script src="${rc.contextPath}/js/leaflet.js"></script>
      <script src="${rc.contextPath}/js/proj4.min.js"></script>
      <script src="${rc.contextPath}/js/proj4leaflet.js"></script>
      <script src="https://sotkanet.fi/sotkanet/${mapLang}/api/geojson/MAA"></script>

      [#list summary.presentations as presentation]
      [#if presentation.type == "map"]
      <script src="https://sotkanet.fi/sotkanet/${mapLang}/api/geojson/${presentation.area}"></script>
      [/#if]
      [/#list]
    [/#if]

    <script src="${rc.contextPath}/js/google-analytics.min.js"></script>
    </body>
</html>
