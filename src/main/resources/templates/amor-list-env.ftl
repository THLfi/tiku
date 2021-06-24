[#ftl]
[#include "common.ftl"]
[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]
[#assign breadcrumbs]
    <li class="breadcrumb-item"><a href="${rc.contextPath}/">Environments</a></li>
    <li class="breadcrumb-item active">${env!}</a></li>
[/#assign]

<script src="${resourceUrl}/js/jquery.js"></script>
<script src="${rc.contextPath}/js/amor-list.min.js"></script>

[@amor_page]

<link rel="stylesheet" href="${rc.contextPath}/css/amor-list.css" />

[#list reports as report]
  [#if report_index > 0 && report.hydra != last_hydra]
    <hr />
  [/#if]
  [#assign last_hydra = report.hydra]

  <div class="report">
    <h2 class="title">
        <i class="fas fa-database"></i>
        <a href="${rc.contextPath}/${env}/fi/${report.subject}">${report.name!report.fact}</a></h2>
    <div class="info">
      <span class="updated">${message("cube.updated")}: ${report.added?string("dd.MM.yyyy HH:mm")}</span>
    </div>
  </div>

[/#list]

[/@amor_page]
