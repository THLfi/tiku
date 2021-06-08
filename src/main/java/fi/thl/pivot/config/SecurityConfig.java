package fi.thl.pivot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/**").permitAll();

        http.headers()
                .contentSecurityPolicy(
                        "default-src 'self'; " +
                                "script-src 'report-sample' 'self' 'unsafe-inline' 'unsafe-eval' https://repo.thl.fi/sites/pivot/js/d3.min.js https://repo.thl.fi/sites/pivot/js/json-stat.js https://repo.thl.fi/sites/pivot/js/jquery.js https://repo.thl.fi/sites/pivot/js/jquery.ui.touch-punch.min.js https://repo.thl.fi/sites/pivot/js/bootstrap.js https://repo.thl.fi/sites/pivot/js/jquery-ui.js https://www.google-analytics.com/analytics.js; " +
                                "style-src 'report-sample' 'self' 'unsafe-inline' https://repo.thl.fi; " +
                                "object-src 'none'; " +
                                "base-uri 'self'; " +
                                "connect-src 'self' https://www.google-analytics.com; " +
                                "font-src 'self' https://repo.thl.fi; " +
                                "frame-src 'self'; " +
                                "img-src 'self' https://repo.thl.fi; " +
                                "manifest-src 'self'; " +
                                "media-src 'self'; " +
                                "worker-src 'none'; "
                ).reportOnly().and()
                .xssProtection().disable();  // set report only, remove when policy needs to be forced
    }
}
