[#ftl]
[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]
if (typeof thl === 'undefined') {
  thl = {};
}
if (typeof thl.messages === 'undefined') {
  thl.messages = {}
}
thl.messages["no-data"] = '${message("summary.no-data")?js_string}';
