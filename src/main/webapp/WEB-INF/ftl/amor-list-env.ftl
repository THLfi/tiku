[#ftl]
[#include "common.ftl"]
[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]
[#assign breadcrumbs]
    <li class="first"><a href="${rc.contextPath}/">Environments</a></li>
    <li class="active">${env!}</a></li>
[/#assign]
[#assign javascript]
    $('.subject-button').click(function() {
        var target =
            $(this)
                .closest('tr')
                .siblings('.subject-' + $(this).attr('subject-ref'));
        if($(this)
            .find('.caret')
            .toggleClass('caret-right')
            .is('.caret-right')) {
            target.hide();
        } else {
            target.show();
        }
    });
    $('.hydra-button').click(function() {
        var p = $(this).closest('tr');
        p.siblings('.' + p.attr('class').replace(/\s+(fh)?$/,'').replace(/\s+/g,'.')).toggle();
        $(this).find('.caret').toggleClass('caret-right');
    }).click();
[/#assign]
[@amor_page]

<style>
  .report {
    margin-bottom: 30px;
  }
  .title {
    font-size: 1.2em;
    margin: 0;
  }
  .info {
    font-size: 12px;
    margin-bottom: 15px;
  }

</style>

[#list reports as report]
  [#if report_index > 0 && report.hydra != last_hydra]
    <hr />
  [/#if]
  [#assign last_hydra = report.hydra]

  <div class="report">
    <h2 class="title">
      <i class="fa fa-database"></i>
      <a href="${rc.contextPath}/${env}/fi/${report.subject}">${report.name!report.fact}</a></h2>
    <div class="info">
      <span class="updated">${message("cube.updated")}: ${report.added?string("dd.MM.yyyy HH:mm")}</span>
    </div>
  </div>

[/#list]

[/@amor_page]
