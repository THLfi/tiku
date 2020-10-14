package fi.thl.pivot.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import fi.thl.pivot.web.LoggingFreeMarkerView;
import freemarker.template.TemplateException;
import freemarker.template.utility.HtmlEscape;
import freemarker.template.utility.XmlEscape;

@Configuration
public class FreemarkerConfig {
	
	@Value("${resourceUrl}")
	private String resourceUrl;

	@Bean
	public ViewResolver fmViewResolver() {
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setViewClass(LoggingFreeMarkerView.class);
		resolver.setCache(true);
		resolver.setPrefix("");
		resolver.setSuffix(".ftl");
		resolver.setRequestContextAttribute("rc");
		resolver.setExposeSpringMacroHelpers(true);
		resolver.setContentType("text/html;charset=UTF-8");
		return resolver;
	}

	@Bean
	public FreeMarkerConfigurationFactoryBean fmConfiguration() {
		FreeMarkerConfigurationFactoryBean config = new FreeMarkerConfigurationFactoryBean();

		config.setTemplateLoaderPath("classpath:/templates/");
		Properties settings = new Properties();
		settings.setProperty("datetime_format", "dd.mm.yyyy");
		settings.setProperty("number_format", "#");
		settings.setProperty("whitespace_stripping", "true");
		settings.setProperty("template_exception_handler", "rethrow");
		settings.setProperty("cache_storage", "freemarker.cache.StrongCacheStorage");
		config.setFreemarkerSettings(settings);
		Map<String, Object> vars = new HashMap<>();
		vars.put("xml_escape", new XmlEscape());
		vars.put("html_escape", new HtmlEscape());
		vars.put("resourceUrl", resourceUrl);
		config.setFreemarkerVariables(vars);
		return config;
	}

	@Bean
	public FreeMarkerConfigurer fmConfigurer() throws IOException, TemplateException {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setConfiguration(fmConfiguration().createConfiguration());
		return configurer;
	}

}
