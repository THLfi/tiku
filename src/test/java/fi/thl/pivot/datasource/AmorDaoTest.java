package fi.thl.pivot.datasource;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.thl.pivot.model.Report;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AmorDaoTest.Configuration.class })

@TestPropertySource(properties = "database.environment.schema=test")
public class AmorDaoTest {

    public static class Configuration {

        @Bean
        @Qualifier("queries")
        public Properties myProperties() {
            PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
            propertiesFactoryBean.setLocation(new ClassPathResource("/sql.xml"));
            Properties properties = null;
            try {
                propertiesFactoryBean.afterPropertiesSet();
                properties = propertiesFactoryBean.getObject();

            } catch (IOException e) {
                System.err.println("Cannot load properties file.");
            }
            return properties;
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().addScript("classpath:create-table-hsql.sql").build();
        }

        @Bean
        @Autowired
        public JdbcTemplate template(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public AmorDao dao() {
            return new AmorDao();
        }

    }

    @Autowired
    private AmorDao dao;

    @Test
    public void shouldListReportsInDifferentStates() {
        assertEquals(3, dao.listReports("test").size());
        assertEquals(1, dao.listReports("prod").size());
    }

    @Test
    public void sholdListReportsInOrder() {
        List<Report> reports = dao.listReports("test");
        assertEquals("1003", reports.get(0).getRunId());
        assertEquals("1001", reports.get(1).getRunId());
        assertEquals("1000", reports.get(2).getRunId());

    }

}
