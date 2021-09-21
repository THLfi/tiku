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
                .authorizeRequests()
                .antMatchers("/**").permitAll();

        http.headers()
                .contentSecurityPolicy(
                        "default-src 'self'; " +
                                "script-src '" + NONCE_HEADER + "' 'report-sample' 'self' https://repo.thl.fi/sites/pivot/library-update/pivot/js/jquery.stickytable.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/d3.min.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/json-stat.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/jquery.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/jquery.ui.touch-punch.min.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/bootstrap.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/bootstrap.min.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/bootstrap.bundle.min.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/jquery-ui.js https://www.google-analytics.com/analytics.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/leaflet.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/proj4.min.js https://repo.thl.fi/sites/pivot/library-update/pivot/js/proj4leaflet.js https://sotkanet.fi/sotkanet/fi/api/geojson/ https://sotkanet.fi/sotkanet/en/api/geojson/ https://sotkanet.fi/sotkanet/sv/api/geojson/; " +
                                "style-src 'self' 'unsafe-inline' https://repo.thl.fi; " +
                                "object-src 'none'; " +
                                "base-uri 'self'; " +
                                "connect-src 'self' https://www.google-analytics.com; " +
                                "font-src 'self' https://repo.thl.fi; " +
                                "frame-src 'self'; " +
                                "img-src 'self' blob: https://repo.thl.fi; " +
                                "manifest-src 'self'; " +
                                "media-src 'self'; " +
                                "worker-src 'none'; "
                ).and().xssProtection().disable();
    }
}
