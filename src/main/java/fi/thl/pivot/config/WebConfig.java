package fi.thl.pivot.config;


import java.util.Locale;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import com.google.common.base.Preconditions;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${pivot.defaultLanguage}")
    private String defaultLanguage;

    @Value("${pivot.defaultTimeZone}")
    private String defaultTimeZone;
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("language");
        resolver.setDefaultLocale(new Locale(defaultLanguage));
        resolver.setDefaultTimeZone(TimeZone.getTimeZone(defaultTimeZone));
        return resolver;
    }

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }
  
    @Bean("datasource")
    public DataSource getDataSource() {
		@SuppressWarnings("unchecked")
		DataSourceBuilder <DataSource>  dataSourceBuilder = (DataSourceBuilder<DataSource>) DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(Preconditions.checkNotNull(driverClass));
        dataSourceBuilder.url(Preconditions.checkNotNull(datasourceUrl));
        dataSourceBuilder.username(Preconditions.checkNotNull(username));
        dataSourceBuilder.password(Preconditions.checkNotNull(password));
        return dataSourceBuilder.build();
    }    
}
