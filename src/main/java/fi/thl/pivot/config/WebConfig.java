package fi.thl.pivot.config;


import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.google.common.base.Preconditions;

import fi.thl.pivot.web.LocaleInterceptor;

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
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("fi", "FI"));
        return slr;
    }

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}
	/*
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
	    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
	    lci.setParamName("lang");
	    return lci;
	}
	 */
	 @Override
	 public void addInterceptors(InterceptorRegistry registry) {
	     registry.addInterceptor(new LocaleInterceptor());
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
