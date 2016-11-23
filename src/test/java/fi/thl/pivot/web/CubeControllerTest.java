package fi.thl.pivot.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=CubeControllerTest.class)
@WebAppConfiguration
public class CubeControllerTest extends AbstractMvcTestCase{ 
   
    @Bean
    public CubeController controller() {
        return new CubeController();
    }
    
    @Test
    public void shouldRun() {
    
    }
    
   

}
