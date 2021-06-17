[#ftl]
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
