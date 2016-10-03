package fi.thl.pivot.web;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.google.common.collect.ImmutableList;

import fi.thl.pivot.util.ThreadRole;

public class LocaleInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        @SuppressWarnings("rawtypes")
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        LocaleResolver resolver = RequestContextUtils.getLocaleResolver(request);
        if (null != pathVariables && pathVariables.containsKey("locale")) {
            if (ImmutableList.of("fi", "en", "sv").contains(pathVariables.get("locale"))) {
                resolver.setLocale(request, response, new Locale((String) pathVariables.get("locale")));
                ThreadRole.setLanguage((String) pathVariables.get("locale"));
            } else {
                resolver.setLocale(request, response, new Locale("fi"));
                ThreadRole.setLanguage("fi");
            }
        }
        return super.preHandle(request, response, handler);
    }

}
