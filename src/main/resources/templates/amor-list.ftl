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
    <li class="breadcrumb-item active">${env!}</li>
[/#assign]
[@amor_page message("amor-list.title")]

<link rel="stylesheet" href="${rc.contextPath}/css/amor-list.css">

<script src="${rc.contextPath}/js/jquery.js"></script>
<script src="${rc.contextPath}/js/amor-list.min.js"></script>

[#list reports as report]
  [#if report_index > 0 && report.hydra != last_hydra]
    <hr />
  [/#if]
  [#assign last_hydra = report.hydra]

  <div class="report">
    <h2 class="title">
      [#if "SUMMARY"=report.type]
        <i class="fas fa-chart-line"></i>
      [#else]
        <i class="fas fa-table"></i>
      [/#if]
      <a href="${rc.contextPath}/${env}/${lang}/${report.subject}/${report.hydra}/[#if "SUMMARY"=report.type]summary_[/#if]${report.fact}">${report.name!report.fact}</a>
      [#if report.isProtected() ]<i class="fas fa-lock"></i>[/#if]
    </h2>
    <div class="info">
      <span class="updated">[#if "SUMMARY"=report.type]${message("summary.data-updated")}[#else]${message("cube.updated")}[/#if]: ${report.added?string("dd.MM.yyyy HH:mm")}</span>
      [#if "SUMMARY"=report.type && (env = "test" || env = "deve")]
      <a href="${rc.contextPath}/${env}/${lang}/${report.subject}/${report.hydra}/summary_${report.fact}/source">${message("summary.view-source")}</a>
      [/#if]
    </div>
  </div>

[/#list]

[/@amor_page]
