package fi.thl.pivot.web;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccessTokens {

    private Set<String> tokens = new HashSet<>();

    public void putToken(String token) {
        tokens.add(token);
    }

    public boolean canAccess(String token) {
        return tokens.contains(token);
    }
}
