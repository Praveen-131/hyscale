package io.hyscale.ctl.builder.services.handler;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.utils.TailHandler;

public class BuildLogHandler implements TailHandler {

	private static String EOF_MARKER = "(.*)Successfully tagged(.*)";

	@Override
	public void handleLine(String line) {
		System.out.println(line);
	}

	@Override
	public boolean handleEOF(String line) {
		if (line == null) {
			return true;
		}
		boolean eof = line.matches(EOF_MARKER);
		if (eof) {
			WorkflowLogger.footer();
		}
		return eof;
	}

}