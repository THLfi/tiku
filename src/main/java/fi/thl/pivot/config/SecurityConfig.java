package fi.thl.pivot.config;

import fi.thl.pivot.web.tools.NonceGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final String NONCE_HEADER = NonceGenerator.generateNonceHeader();

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll();

        http.headers()
                .contentSecurityPolicy(
                        "default-src 'self'; " +
                                "script-src '" + NONCE_HEADER + "' 'report-sample' 'self' https://www.google-analytics.com/analytics.js https://sotkanet.fi/sotkanet/fi/api/geojson/ https://sotkanet.fi/sotkanet/en/api/geojson/ https://sotkanet.fi/sotkanet/sv/api/geojson/; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "object-src 'none'; " +
                                "base-uri 'self'; " +
                                "connect-src 'self' https://www.google-analytics.com; " +
                                "font-src 'self'; " +
                                "frame-src 'self'; " +
                                "img-src 'self' blob:; " +
                                "manifest-src 'self'; " +
                                "media-src 'self'; " +
                                "worker-src 'none'; "
                ).and().xssProtection().disable();
    }
}
