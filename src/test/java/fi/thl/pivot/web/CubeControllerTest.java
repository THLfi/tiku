package fi.thl.pivot.web;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes=CubeControllerTest.class)
public class CubeControllerTest extends AbstractMvcTestCase{
   
    
    @Bean
    public CubeController controller() {
        return new CubeController();
    }
    
    @Test
    public void shouldRun() {
        fail();
    }
    
   

}
