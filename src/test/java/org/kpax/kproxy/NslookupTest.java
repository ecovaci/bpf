package org.kpax.kproxy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kpax.bpf.util.CommandExecutor;

public class NslookupTest {
    public static void main(String[] args) {
        try {
            System.out.println(nslookupKdc(false));
            //System.out.println(nslookupKdc(false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<String> nslookupKdc(boolean isWindows) throws Exception {
        String[] command = null;
        if (isWindows) {
            command = new String[]{"cmd.exe", "/c", "C:/Users/Quasimodo/Workspace/IdeaProjects/bpf/src/test/resources/nslookup_mock.bat"};
        } else {
            command = new String[]{"cmd.exe", "/c", "C:/Users/Quasimodo/Workspace/IdeaProjects/bpf/src/test/resources/nslookup_mock_sh.bat"};
        }
        System.out.println("Execute command "+ Arrays.stream(command).collect(Collectors.joining(" ")));
        List<String> output = CommandExecutor.execute(command);
        System.out.println("nslookup output: " + output.stream().collect(Collectors.joining("\n")));
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
