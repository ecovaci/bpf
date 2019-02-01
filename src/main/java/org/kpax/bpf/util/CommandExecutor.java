package org.kpax.bpf.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kpax.bpf.exception.CommandExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for executing command line instructions.
 */
public class CommandExecutor {

	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	/**
	 * Executes a command with parameters.
	 * @param command The command's parameters.
	 * @return The command's output.
	 * @throws CommandExecutionException
	 */
	public static List<String> execute(String... command) throws CommandExecutionException {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(command);
			Process process = builder.start();
			return IOUtils.readLines(new InputStreamReader(process.getInputStream()));
		} catch (IOException e) {
			throw new CommandExecutionException(e);
		}
	}

	/**
	 * It issues a <code>nslookup</code> command to get the list of KDC servers for a specific domain.
	 * @param domain The network domain.
	 * @return The list of KDC servers.
	 * @throws CommandExecutionException
	 */
	public static Set<String> nslookupKdc(String domain) throws CommandExecutionException {
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		String[] command;
		if (isWindows) {
			command = new String[] { "cmd.exe", "/c", "nslookup -type=srv _kerberos._tcp." + domain.toUpperCase() };
		} else {
			command = new String[] { "sh", "-c", "nslookup -type=srv _kerberos._tcp." + domain.toUpperCase() };
		}
		logger.info("Execute command: {}", Arrays.stream(command).collect(Collectors.joining(" ")));
		List<String> output = CommandExecutor.execute(command);
		logger.info("nslookup output: \n{}", output.stream().collect(Collectors.joining("\n")));
		String tag = isWindows ? "svr hostname" : "service";
		return output.stream().filter((item) -> item.trim().startsWith(tag))
				.map((item) -> {
					return Arrays.stream(item.replace(tag, "").replace("=", "")
							.split("\\s"))
							.filter((it) -> StringUtils.isNotEmpty(it) && !it.matches("[\\d]+"))
							.findFirst()
							.get().trim();
				})
				.collect(Collectors.toSet());
	}

}
