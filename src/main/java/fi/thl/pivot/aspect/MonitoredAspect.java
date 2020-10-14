package fi.thl.pivot.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import fi.thl.pivot.annotation.Monitored;

/**
 * Capturs all method calls in this project annotated with {@link Monitored} annotation 
 * and logs their execution time using log4j
 * 
 * @author aleksiyrttiaho
 *
 */
@Component
@Aspect
public class MonitoredAspect {

    private final Logger logger = LoggerFactory.getLogger(MonitoredAspect.class);

    @Around("execution(* fi.thl.pivot..*(..)) && @annotation(fi.thl.pivot.annotation.Monitored)")
    public Object invoke(ProceedingJoinPoint invocation) throws Throwable {
        StopWatch localWatch = new StopWatch();
        localWatch.start(invocation.toString());
        try {
            return invocation.proceed();
        } finally {
            localWatch.stop();
            logger.debug(invocation.toShortString() + ": " + localWatch.prettyPrint());
        }
    }
}
