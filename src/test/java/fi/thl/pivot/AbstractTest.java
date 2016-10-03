package fi.thl.pivot;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.BeforeClass;

public class AbstractTest {

	@BeforeClass
	public static void setUpLogger() {
		Logger log = Logger.getLogger("fi.thl");
		log.addAppender(new ConsoleAppender(new SimpleLayout(),
				ConsoleAppender.SYSTEM_OUT));
		log.setLevel(Level.INFO);
	}
}
