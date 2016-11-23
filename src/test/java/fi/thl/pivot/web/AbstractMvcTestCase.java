package fi.thl.pivot.web;

import java.util.Properties;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import fi.thl.pivot.datasource.AmorDao;
import fi.thl.pivot.datasource.LogSource;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractMvcTestCase {

    @Autowired
    private WebApplicationContext context;

    @Bean
    protected AmorDao amorDao() {
        return Mockito.mock(AmorDao.class);
    }

    @Bean
    protected FreeMarkerConfig freemarker() {
        return Mockito.mock(FreeMarkerConfig.class);
    }

    @Bean
    private AccessTokens accessToken() {
        return Mockito.mock(AccessTokens.class);
    }

    @Bean
    protected LogSource logSource() {
        return Mockito.mock(LogSource.class);
    }

    @Bean
    protected JdbcTemplate jdbcTemplate() {
        return Mockito.mock(JdbcTemplate.class);
    }

    @Bean
    protected Properties queries() {
        return Mockito.mock(Properties.class);
    }

    protected MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

}
