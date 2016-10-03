[#ftl]
[#include "common.ftl"]
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

<table class="table table-striped">
    <thead>
        <tr>
            <th>Subject</th>
            <th>Hydra</th>
            <th>Report</th>
            <th>Type</th>
            [#if showRestrictedView??]
            [#else]
            <th>Run Id</th>
            [/#if]
            <th>Added</th>
        </tr>
    </thead>
    <tbody>
        [#assign lastSubject = "" /]
        [#assign lastHydra = "" /]
        [#assign lastReport = "" /]
        [#list reports as report]
        [#if showRestrictedView?? && lastSubject == report.subject && lastHydra == report.hydra && lastReport == report.fact]
          [#-- don't display older versions of reports --]
        [#else]
        <tr class="subject-${report.subject} hydra-${report.hydra} report-${report.fact} [#if lastHydra != report.hydra ]fh[/#if]">
            <td><span subject-ref="${report.subject}" class="subject-button">[#if lastSubject != report.subject]${report.subject} <span class="caret"></span></span>[/#if]</td>
            <td>[#if lastSubject != report.subject || lastHydra != report.hydra || lastReport != report.fact]<span class="hydra-button">${report.hydra} <span class="caret"></span></span>[/#if]</td>
            <td><a href="${rc.contextPath}/${env}/fi/${report.subject}/${report.hydra}/[#if "SUMMARY"=report.type]summary_[/#if]${report.fact}[#if lastSubject == report.subject && lastHydra == report.hydra && lastReport == report.fact]?run_id=${report.runId}[/#if]">${report.fact}</a></td>
            <td style="text-transform: lowercase">${report.type}</td>
            [#if showRestrictedView??]
            [#else]
              <td>${report.runId}</td>
            [/#if]
            <td>${report.added?string("dd.MM.yyyy HH:mm")}</td>
        </tr>
        [/#if]
        [#assign lastSubject = report.subject /]
        [#assign lastHydra = report.hydra /]
        [#assign lastReport = report.fact]
        [/#list]
    </tbody>
</table>
[/@amor_page]
