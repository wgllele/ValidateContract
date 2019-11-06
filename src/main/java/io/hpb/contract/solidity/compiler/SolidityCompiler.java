package io.hpb.contract.solidity.compiler;

import static java.util.stream.Collectors.toList;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.hpb.contract.common.ContractConstant;
import io.hpb.contract.util.AppObjectUtil;
import io.hpb.web3.protocol.Web3;
import io.hpb.web3.spring.autoconfigure.Web3Properties;
/**
 * @author ThinkPad
 *SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
  CompilationResult cres = CompilationResult.parse(res.output);
  
 */
@Configuration
@ConditionalOnClass(Web3.class)
@EnableConfigurationProperties(Web3Properties.class)
public class SolidityCompiler {
    private String dockerSolcCmd;
    private String solcVersion;
    public static Log log = LogFactory.getLog(SolidityCompiler.class);
    public SolidityCompiler() {
		this.dockerSolcCmd=ContractConstant.SOLC_CMD_DEFAULT;
		this.solcVersion=ContractConstant.SOLC_STABLE;
	}
    
    public String getDockerSolcCmd() {
		return dockerSolcCmd;
	}

	public void setDockerSolcCmd(String dockerSolcCmd) {
		this.dockerSolcCmd = dockerSolcCmd;
	}

	public String getSolcVersion() {
		return solcVersion;
	}

	public void setSolcVersion(String solcVersion) {
		this.solcVersion = solcVersion;
	}

	public Result compile(File sourceDirectory, boolean combinedJson, Option... options) throws IOException {
        return compileSrc(sourceDirectory, false, combinedJson, options);
    }
    public Result compile(byte[] source, boolean combinedJson, Option... options) throws IOException {
        return compileSrc(source, false, combinedJson, options);
    }

    public Result compileSrc(File source, boolean optimize, boolean combinedJson, Option... options) throws IOException {
        List<String> commandParts = prepareCommandOptions(optimize, combinedJson, options);

        commandParts.add(source.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(commandParts);

        Process process = processBuilder.start();

        ParallelReader error = new ParallelReader(process.getErrorStream());
        ParallelReader output = new ParallelReader(process.getInputStream());
        error.start();
        output.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        boolean success = process.exitValue() == 0;

        return new Result(error.getContent(), output.getContent(), success);
    }

    private List<String> prepareCommandOptions(boolean optimize, boolean combinedJson, Option... options) throws IOException {
        List<String> commandParts = new ArrayList<>();
        addSolcCmd(commandParts);
        if (optimize) {
            commandParts.add("--" + Options.OPTIMIZE.getName());
        }
        if (combinedJson) {
            Option combinedJsonOption = new Options.CombinedJson(getElementsOf(OutputOption.class, options));
            commandParts.add("--" + combinedJsonOption.getName());
            commandParts.add(combinedJsonOption.getValue());
        } else {
            for (Option option : getElementsOf(OutputOption.class, options)) {
                commandParts.add("--" + option.getName());
            }
        }
        for (Option option : getElementsOf(ListOption.class, options)) {
            commandParts.add("--" + option.getName());
            commandParts.add(option.getValue());
        }

        for (Option option : getElementsOf(CustomOption.class, options)) {
            commandParts.add("--" + option.getName());
            if (option.getValue() != null) {
                commandParts.add(option.getValue());
            }
        }

        return commandParts;
    }

	private void addSolcCmd(List<String> commandParts) {
		String[] solcCmds = getDockerSolcCmd().split(" ");
		int length = solcCmds.length;
		for(int i=0;i<length;i++) {
			if(i<length-1) {
				commandParts.add(solcCmds[i]);
			}else {
				commandParts.add(solcCmds[i]+":"+getSolcVersion());
			}
		}
	}
    public Result compileSrc(byte[] source, boolean optimize, boolean combinedJson, Option... options) throws IOException {
        List<String> commandParts = prepareCommandOptions(optimize, combinedJson, options);

        //new in solidity 0.5.0: using stdin requires an explicit "-". The following output
        //of 'solc' if no file is provided, e.g.,: solc --combined-json abi,bin,interface,metadata
        //
        // No input files given. If you wish to use the standard input please specify "-" explicitly.
        //
        // For older solc version "-" is not an issue as it is accepet as well
        commandParts.add("-");

        ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
        log.info(AppObjectUtil.toJson(processBuilder.command()));
        Process process = processBuilder.start();

        try (BufferedOutputStream stream = new BufferedOutputStream(process.getOutputStream())) {
            stream.write(source);
            log.info(new String(source,StandardCharsets.UTF_8));
        }

        ParallelReader error = new ParallelReader(process.getErrorStream());
        ParallelReader output = new ParallelReader(process.getInputStream());
        error.start();
        output.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        boolean success = process.exitValue() == 0;

        return new Result(error.getContent(), output.getContent(), success);
    }
    /**
     * This class is mainly here for backwards compatibility; however we are now reusing it making it the solely public
     * interface listing all the supported options.
     */
    public static final class Options {
        public static final OutputOption AST = OutputOption.AST;
        public static final OutputOption BIN = OutputOption.BIN;
        public static final OutputOption INTERFACE = OutputOption.INTERFACE;
        public static final OutputOption ABI = OutputOption.ABI;
        public static final OutputOption METADATA = OutputOption.METADATA;
        public static final OutputOption ASTJSON = OutputOption.ASTJSON;

        private static final NameOnlyOption OPTIMIZE = NameOnlyOption.OPTIMIZE;
        private static final NameOnlyOption VERSION = NameOnlyOption.VERSION;

        private static class CombinedJson extends ListOption {
			private static final long serialVersionUID = 6289506689012346255L;

			private CombinedJson(List<OutputOption> values) {
                super("combined-json", values);
            }
        }
        public static class AllowPaths extends ListOption {
			private static final long serialVersionUID = -7022622913320262972L;

			public AllowPaths(@SuppressWarnings("rawtypes") List values) {
                super("allow-paths", values);
            }
        }
    }

    public interface Option extends Serializable {
        String getValue();
        String getName();
    }

    @SuppressWarnings("rawtypes")
    private static class ListOption implements Option {
		private static final long serialVersionUID = 9087589472335109635L;
		private String name;
		private List values;

       private ListOption(String name, List values) {
            this.name = name;
            this.values = values;
        }

        @Override public String getValue() {
            StringBuilder result = new StringBuilder();
            for (Object value : values) {
                if (OutputOption.class.isAssignableFrom(value.getClass())) {
                    result.append((result.length() == 0) ? ((OutputOption) value).getName() : ',' + ((OutputOption) value).getName());
                } else if (Path.class.isAssignableFrom(value.getClass())) {
                    result.append((result.length() == 0) ? ((Path) value).toAbsolutePath().toString() : ',' + ((Path) value).toAbsolutePath().toString());
                } else if (File.class.isAssignableFrom(value.getClass())) {
                    result.append((result.length() == 0) ? ((File) value).getAbsolutePath() : ',' + ((File) value).getAbsolutePath());
                } else if (String.class.isAssignableFrom(value.getClass())) {
                    result.append((result.length() == 0) ? value : "," + value);
                } else {
                    throw new UnsupportedOperationException("Unexpected type, value '" + value + "' cannot be retrieved.");
                }
            }
            return result.toString();
        }
        @Override public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    private enum NameOnlyOption implements Option {
        OPTIMIZE("optimize"),
        VERSION("version");

        private String name;

        NameOnlyOption(String name) {
            this.name = name;
        }

        @Override public String getValue() { return ""; }
        @Override public String getName() { return name; }
        @Override public String toString() {
            return name;
        }
    }

    private enum OutputOption implements Option {
        AST("ast"),
        BIN("bin"),
        INTERFACE("interface"),
        ABI("abi"),
        METADATA("metadata"),
        ASTJSON("ast-json");

        private String name;

        OutputOption(String name) {
            this.name = name;
        }

        @Override public String getValue() { return ""; }
        @Override public String getName() { return name; }
        @Override public String toString() {
            return name;
        }
    }

    public static class CustomOption implements Option {
		private static final long serialVersionUID = -1724776375762559165L;
		private String name;
        private String value;

        public CustomOption(String name) {
            if (name.startsWith("--")) {
                this.name = name.substring(2);
            } else {
                this.name = name;
            }
        }

        public CustomOption(String name, String value) {
            this(name);
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static class Result {
        public String errors;
        public String output;
        private boolean success;

        public Result(String errors, String output, boolean success) {
            this.errors = errors;
            this.output = output;
            this.success = success;
        }

        public boolean isFailed() {
            return !success;
        }
    }

    private static class ParallelReader extends Thread {

        private InputStream stream;
        private StringBuilder content = new StringBuilder();

        ParallelReader(InputStream stream) {
            this.stream = stream;
        }

        public String getContent() {
            return getContent(true);
        }

        public synchronized String getContent(boolean waitForComplete) {
            if (waitForComplete) {
                while(stream != null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return content.toString();
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                synchronized (this) {
                    stream = null;
                    notifyAll();
                }
            }
        }
    }

    private static <T> List<T> getElementsOf(Class<T> clazz, Option... options) {
        return Arrays.stream(options).filter(clazz::isInstance).map(clazz::cast).collect(toList());
    }
    public String runGetVersionOutput() throws IOException {
        List<String> commandParts = new ArrayList<>();
        addSolcCmd(commandParts);
        commandParts.add("--" + Options.VERSION.getName());
        ProcessBuilder processBuilder = new ProcessBuilder(commandParts);

        Process process = processBuilder.start();
        ParallelReader error = new ParallelReader(process.getErrorStream());
        ParallelReader output = new ParallelReader(process.getInputStream());
        error.start();
        output.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (process.exitValue() == 0) {
            return output.getContent();
        }

        throw new RuntimeException("Problem getting solc version: " + error.getContent());
    }
}