package fi.thl.pivot.config;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
public class AppConfiguration {

	@Bean("queries")
	public Properties queries() throws IOException {
		Properties p = new Properties();
		p.loadFromXML(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql.xml"));
		return p;
	}

	@Bean
	public ResourceBundleMessageSource messageSource() {
		var source = new ResourceBundleMessageSource();
		source.setBasenames("messages/messages");
		source.setUseCodeAsDefaultMessage(true);
		source.setDefaultLocale(new Locale("fi", "FI"));
		return source;
	}

	@Bean
	public FilterRegistrationBean<CharacterEncodingFilter> filterRegistrationBean() {
		FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<CharacterEncodingFilter>();
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setForceEncoding(true);
		characterEncodingFilter.setEncoding("UTF-8");
		registrationBean.setFilter(characterEncodingFilter);
		return registrationBean;
	}
}
