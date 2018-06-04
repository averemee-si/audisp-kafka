package eu.solutions.a2.audit.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtils.class);

	/**
	 * Display the stacktrace contained in an exception.
	 * @param exception Exception
	 * @return String with the output from printStackTrace
	 * @see Exception.printStackTrace()
	 **/
	public static String getExceptionStackTrace(Exception exception) {
		String result = exception.getMessage();
		try (
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw)) {
			exception.printStackTrace(pw);
			result += sw.toString();
		} catch (Exception e) {
			LOGGER.error(
					"Exception while converting exception's stack trace to string!\n" +
					e.getMessage());
		}
		return result;
	}

}
