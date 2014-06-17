/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.cli;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;

import org.geogit.api.Context;
import org.geogit.api.DefaultPlatform;
import org.geogit.api.DefaultProgressListener;
import org.geogit.api.GeoGIT;
import org.geogit.api.GlobalContextBuilder;
import org.geogit.api.Platform;
import org.geogit.api.ProgressListener;
import org.geogit.api.hooks.CannotRunGeogitOperationException;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.api.porcelain.ConfigException;
import org.geogit.api.porcelain.ConfigGet;
import org.geogit.cli.annotation.ObjectDatabaseReadOnly;
import org.geogit.cli.annotation.ReadOnly;
import org.geogit.cli.annotation.RemotesReadOnly;
import org.geogit.cli.annotation.RequiresRepository;
import org.geogit.cli.annotation.StagingDatabaseReadOnly;
import org.geogit.repository.Hints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;

//import org.python.core.exceptions;

/**
 * Command Line Interface for geogit.
 * <p>
 * Looks up and executes {@link CLICommand} implementations provided by any {@link Guice}
 * {@link Module} that implements {@link CLIModule} declared in any classpath's
 * {@code META-INF/services/com.google.inject.Module} file.
 */
public class GeogitCLI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeogitCLI.class);

    static {
        GlobalContextBuilder.builder = new CLIContextBuilder();
    }

    private static final com.google.inject.Injector commandsInjector;
    static {
        Iterable<CLIModule> plugins = ServiceLoader.load(CLIModule.class);
        commandsInjector = Guice.createInjector(plugins);
    }

    private Context geogitInjector;

    private Platform platform;

    private GeoGIT geogit;

    private final GeoGIT providedGeogit;

    private final ConsoleReader consoleReader;

    protected ProgressListener progressListener;

    private boolean exitOnFinish = true;

    private static final Hints READ_WRITE = Hints.readWrite();

    private Hints hints = READ_WRITE;

    private boolean progressListenerDisabled;

    /**
     * Construct a GeogitCLI with the given console reader.
     * 
     * @param consoleReader
     */
    public GeogitCLI(final ConsoleReader consoleReader) {
        this(null, consoleReader);
    }

    /**
     * Constructor to use the provided {@code GeoGIT} instance and never try to close it.
     */
    public GeogitCLI(final GeoGIT geogit, final ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
        this.platform = new DefaultPlatform();
        this.providedGeogit = geogit;
    }

    /**
     * @return the platform being used by the geogit command line interface.
     * @see Platform
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Sets the platform for the command line interface to use.
     * 
     * @param platform the platform to use
     * @see Platform
     */
    public void setPlatform(Platform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    public void disableProgressListener() {
        this.progressListenerDisabled = true;
    }

    /**
     * Provides a GeoGIT facade configured for the current repository if inside a repository,
     * {@code null} otherwise.
     * <p>
     * Note the repository is lazily loaded and cached afterwards to simplify the execution of
     * commands or command options that do not need a live repository.
     * 
     * @return the GeoGIT facade associated with the current repository, or {@code null} if there's
     *         no repository in the current {@link Platform#pwd() working directory}
     * @see ResolveGeogitDir
     */
    @Nullable
    public synchronized GeoGIT getGeogit() {
        if (providedGeogit != null) {
            return providedGeogit;
        }
        if (geogit == null) {
            GeoGIT geogit = loadRepository();
            setGeogit(geogit);
        }
        return geogit;
    }

    @VisibleForTesting
    public synchronized GeoGIT getGeogit(Hints hints) {
        close();
        GeoGIT geogit = loadRepository(hints);
        setGeogit(geogit);
        return geogit;
    }

    /**
     * Gives the command line interface a GeoGIT facade to use.
     * 
     * @param geogit
     */
    public void setGeogit(@Nullable GeoGIT geogit) {
        this.geogit = geogit;
    }

    /**
     * Sets flag controlling whether the cli will call {@link System#exit(int)} when done running
     * the command.
     * <p>
     * Commands should call this method only in cases where the starts a server or creates
     * additional threads.
     * </p>
     * 
     * @param exit <tt>true</tt> will cause the cli to exit.
     */
    public void setExitOnFinish(boolean exit) {
        this.exitOnFinish = exit;
    }

    /**
     * Returns flag controlling whether cli will exit on completion.
     * 
     * @see {@link #setExitOnFinish(boolean)}
     */
    public boolean isExitOnFinish() {
        return exitOnFinish;
    }

    /**
     * Loads the repository _if_ inside a geogit repository and returns a configured {@link GeoGIT}
     * facade.
     * 
     * @return a geogit for the current repository or {@code null} if not inside a geogit repository
     *         directory.
     */
    @Nullable
    private GeoGIT loadRepository() {
        return loadRepository(this.hints);
    }

    @Nullable
    private GeoGIT loadRepository(Hints hints) {
        GeoGIT geogit = newGeoGIT(hints);

        if (geogit.command(ResolveGeogitDir.class).call().isPresent()) {
            geogit.getRepository();
            return geogit;
        }
        geogit.close();

        return null;
    }

    /**
     * Constructs and returns a new read-write geogit facade, which will not be managed by this
     * GeogitCLI instance, so the calling code is responsible for closing/disposing it after usage
     * 
     * @return the constructed GeoGIT.
     */
    public GeoGIT newGeoGIT() {
        return newGeoGIT(Hints.readWrite());
    }

    public GeoGIT newGeoGIT(Hints hints) {
        Context inj = newGeogitInjector(hints);
        GeoGIT geogit = new GeoGIT(inj, platform.pwd());
        try {
            geogit.getRepository();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return geogit;
    }

    /**
     * @return the Guice injector being used by the command line interface. If one hasn't been made,
     *         it will be created.
     */
    public Context getGeogitInjector() {
        return getGeogitInjector(this.hints);
    }

    private Context getGeogitInjector(Hints hints) {
        if (this.geogitInjector == null || !Objects.equal(this.hints, hints)) {
            // System.err.println("Injector hints: " + hints);
            geogitInjector = newGeogitInjector(hints);
        }
        return geogitInjector;
    }

    private Context newGeogitInjector(Hints hints) {
        Context geogitInjector = GlobalContextBuilder.builder.build(hints);
        return geogitInjector;
    }

    /**
     * @return the console reader being used by the command line interface.
     */
    public ConsoleReader getConsole() {
        return consoleReader;
    }

    /**
     * Closes the GeoGIT facade if it exists.
     */
    public synchronized void close() {
        if (providedGeogit != null) {
            return;
        }
        if (geogit != null) {
            geogit.close();
            geogit = null;
        }
        this.hints = READ_WRITE;
        this.geogitInjector = null;
    }

    /**
     * @return true if a command is being ran
     */
    public synchronized boolean isRunning() {
        return geogit != null;
    }

    /**
     * Entry point for the command line interface.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Logging.tryConfigureLogging();
        ConsoleReader consoleReader;
        try {
            consoleReader = new ConsoleReader(System.in, System.out);
            // needed for CTRL+C not to let the console broken
            consoleReader.getTerminal().setEchoEnabled(true);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        final GeogitCLI cli = new GeogitCLI(consoleReader);
        addShutdownHook(cli);
        int exitCode = cli.execute(args);

        try {
            cli.close();
        } finally {
            try {
                consoleReader.getTerminal().restore();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                exitCode = -1;
            }
            consoleReader.shutdown();
        }

        if (exitCode != 0 || cli.isExitOnFinish()) {
            System.exit(exitCode);
        }
    }

   
    /**
     * Finds all commands that are bound do the command injector.
     * 
     * @return a collection of keys, one for each command
     */
    private Collection<Key<?>> findCommands() {
        Map<Key<?>, Binding<?>> commands = commandsInjector.getBindings();
        return commands.keySet();
    }

    public JCommander newCommandParser() {
        JCommander jc = new JCommander(this);
        jc.setProgramName("geogit");
        for (Key<?> cmd : findCommands()) {
            Object obj = commandsInjector.getInstance(cmd);
            if (obj instanceof CLICommand || obj instanceof CLICommandExtension) {
                jc.addCommand(obj);
            }
        }
        return jc;
    }

    @VisibleForTesting
    public Exception exception;

    /**
     * Processes a command, catching any exceptions and printing their messages to the console.
     * 
     * @param args
     * @return 0 for normal exit, -1 if there was an exception.
     */
    public int execute(String... args) {
        exception = null;
        String consoleMessage = null;
        boolean printError = true;
        try {
            executeInternal(args);
            return 0;
        } catch (ParameterException paramParseException) {
            exception = paramParseException;
            consoleMessage = paramParseException.getMessage() + ". See geogit --help";

        } catch (InvalidParameterException paramValidationError) {
            exception = paramValidationError;
            consoleMessage = paramValidationError.getMessage();

        } catch (CannotRunGeogitOperationException cannotRun) {

            consoleMessage = cannotRun.getMessage();

        } catch (CommandFailedException cmdFailed) {
            exception = cmdFailed;
            if (null == cmdFailed.getMessage()) {
                // this is intentional, see the javadoc for CommandFailedException
                printError = false;
            } else {
                LOGGER.error(consoleMessage, cmdFailed.getCause());
                consoleMessage = cmdFailed.getMessage();
            }
        } catch (RuntimeException e) {
            exception = e;
            // e.printStackTrace();
            consoleMessage = String.format(
                    "An unhandled error occurred: %s. See the log for more details.",
                    e.getMessage());
            LOGGER.error(consoleMessage, e);
        } catch (IOException ioe) {
            exception = ioe;
            // can't write to the console, see the javadocs for CLICommand.run().
            LOGGER.error(
                    "An IOException was caught, should only happen if an error occurred while writing to the console",
                    ioe);
        } finally {
            // close after executing a command for the next one to reopen with its own hints and not
            // to keep the db's open for write meanwhile
            close();
        }
        if (printError) {
            try {
                consoleReader.println(Optional.fromNullable(consoleMessage).or("Unknown Error"));
                consoleReader.flush();
            } catch (IOException e) {
                LOGGER.error("Error writing to the console. Original error: {}", consoleMessage, e);
            }
        }
        return -1;
    }

    /**
     * Executes a command.
     * 
     * @param args
     * @throws exceptions thrown by the executed commands.
     */
    private void executeInternal(String... args) throws ParameterException, CommandFailedException,
            IOException, CannotRunGeogitOperationException {

        JCommander mainCommander = newCommandParser();
        if (null == args || args.length == 0) {
            printShortCommandList(mainCommander);
            return;
        }
        {
            args = unalias(args);
            final String commandName = args[0];
            JCommander commandParser = mainCommander.getCommands().get(commandName);

            if (commandParser == null) {
                consoleReader.println(args[0] + " is not a geogit command. See geogit --help.");
                // check for similar commands
                Map<String, JCommander> candidates = spellCheck(mainCommander.getCommands(),
                        commandName);
                if (!candidates.isEmpty()) {
                    String msg = candidates.size() == 1 ? "Did you mean this?"
                            : "Did you mean one of these?";
                    consoleReader.println();
                    consoleReader.println(msg);
                    for (String name : candidates.keySet()) {
                        consoleReader.println("\t" + name);
                    }
                }
                consoleReader.flush();
                throw new CommandFailedException(String.format("'%s' is not a command.",
                        commandName));
            }

            Object object = commandParser.getObjects().get(0);
            if (object instanceof CLICommandExtension) {
                args = Arrays.asList(args).subList(1, args.length)
                        .toArray(new String[args.length - 1]);
                mainCommander = ((CLICommandExtension) object).getCommandParser();
                if (Lists.newArrayList(args).contains("--help")) {
                    printUsage(mainCommander);
                    return;
                }
            }
        }

        mainCommander.parse(args);
        final String parsedCommand = mainCommander.getParsedCommand();
        if (null == parsedCommand) {
            if (mainCommander.getObjects().size() == 0) {
                printUsage(mainCommander);
            } else if (mainCommander.getObjects().get(0) instanceof CLICommandExtension) {
                CLICommandExtension extension = (CLICommandExtension) mainCommander.getObjects()
                        .get(0);
                printUsage(extension.getCommandParser());
            } else {
                printUsage(mainCommander);
                throw new CommandFailedException();
            }
        } else {
            JCommander jCommander = mainCommander.getCommands().get(parsedCommand);
            List<Object> objects = jCommander.getObjects();
            CLICommand cliCommand = (CLICommand) objects.get(0);
            Class<? extends CLICommand> cmdClass = cliCommand.getClass();
            if (cliCommand instanceof AbstractCommand && ((AbstractCommand) cliCommand).help) {
                ((AbstractCommand) cliCommand).printUsage(this);
                getConsole().flush();
                return;
            }
            Hints hints = gatherHints(cmdClass);
            this.hints = hints;

            if (cmdClass.isAnnotationPresent(RequiresRepository.class)
                    && cmdClass.getAnnotation(RequiresRepository.class).value()) {
                String workingDir;
                Platform platform = getPlatform();
                if (platform == null || platform.pwd() == null) {
                    workingDir = "Couln't determine working directory.";
                } else {
                    workingDir = platform.pwd().getAbsolutePath();
                }
                if (getGeogit() == null) {
                    throw new CommandFailedException("Not in a geogit repository: " + workingDir);
                }
            }

            cliCommand.run(this);
            getConsole().flush();
        }
    }

    /**
     * This method should be used instead of {@link JCommander#usage()} so the help string is
     * printed to the cli's {@link #getConsole() console} (and hence to wherever its output is sent)
     * instead of directly to {@code System.out}
     */
    public void printUsage(JCommander mainCommander) {
        StringBuilder out = new StringBuilder();
        mainCommander.usage(out);
        ConsoleReader console = getConsole();
        try {
            console.println(out.toString());
            console.flush();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private Hints gatherHints(Class<? extends CLICommand> cmdClass) {
        Hints hints = new Hints();

        checkAnnotationHint(cmdClass, ReadOnly.class, Hints.OBJECTS_READ_ONLY, hints);
        checkAnnotationHint(cmdClass, ReadOnly.class, Hints.STAGING_READ_ONLY, hints);

        checkAnnotationHint(cmdClass, ObjectDatabaseReadOnly.class, Hints.OBJECTS_READ_ONLY, hints);
        checkAnnotationHint(cmdClass, StagingDatabaseReadOnly.class, Hints.STAGING_READ_ONLY, hints);
        checkAnnotationHint(cmdClass, RemotesReadOnly.class, Hints.REMOTES_READ_ONLY, hints);

        return hints;
    }

    private void checkAnnotationHint(Class<? extends CLICommand> cmdClass,
            Class<? extends Annotation> annotation, String key, Hints hints) {

        if (cmdClass.isAnnotationPresent(annotation)) {
            hints.set(key, Boolean.TRUE);
        }
    }

    /**
     * If the passed arguments contains an alias, it replaces it by the full command corresponding
     * to that alias and returns anew set of arguments
     * 
     * IF not, it returns the passed arguments
     * 
     * @param args
     * @return
     */
    private String[] unalias(String[] args) {
        final String aliasedCommand = args[0];
        String configParam = "alias." + aliasedCommand;
        boolean closeGeogit = false;
        GeoGIT geogit = this.providedGeogit == null ? this.geogit : this.providedGeogit;
        if (geogit == null) { // in case the repo is not initialized yet
            closeGeogit = true;
            geogit = newGeoGIT(Hints.readOnly());
        }
        try {
            Optional<String> unaliased = Optional.absent();
            if (geogit.command(ResolveGeogitDir.class).call().isPresent()) {
                unaliased = geogit.command(ConfigGet.class).setName(configParam).call();
            }
            if (!unaliased.isPresent()) {
                unaliased = geogit.command(ConfigGet.class).setGlobal(true).setName(configParam)
                        .call();
            }
            if (!unaliased.isPresent()) {
                return args;
            }
            Iterable<String> tokens = Splitter.on(" ").split(unaliased.get());
            List<String> allArgs = Lists.newArrayList(tokens);
            allArgs.addAll(Lists.newArrayList(Arrays.copyOfRange(args, 1, args.length)));
            return allArgs.toArray(new String[0]);
        } catch (ConfigException e) {
            return args;
        } finally {
            if (closeGeogit) {
                geogit.close();
            }
        }
    }

    /**
     * Return all commands with a command name at a levenshtein distance of less than 3, as
     * potential candidates for a mistyped command
     * 
     * @param commands the list of all available commands
     * @param commandName the command name
     * @return a map filtered according to distance between command names
     */
    private Map<String, JCommander> spellCheck(Map<String, JCommander> commands,
            final String commandName) {
        Map<String, JCommander> candidates = Maps.filterEntries(commands,
                new Predicate<Map.Entry<String, JCommander>>() {
                    @Override
                    public boolean apply(@Nullable Entry<String, JCommander> entry) {
                        char[] s1 = entry.getKey().toCharArray();
                        char[] s2 = commandName.toCharArray();
                        int[] prev = new int[s2.length + 1];
                        for (int j = 0; j < s2.length + 1; j++) {
                            prev[j] = j;
                        }
                        for (int i = 1; i < s1.length + 1; i++) {
                            int[] curr = new int[s2.length + 1];
                            curr[0] = i;
                            for (int j = 1; j < s2.length + 1; j++) {
                                int d1 = prev[j] + 1;
                                int d2 = curr[j - 1] + 1;
                                int d3 = prev[j - 1];
                                if (s1[i - 1] != s2[j - 1]) {
                                    d3 += 1;
                                }
                                curr[j] = Math.min(Math.min(d1, d2), d3);
                            }
                            prev = curr;
                        }
                        return prev[s2.length] < 3;
                    }
                });
        return candidates;
    }

    /**
     * This prints out only porcelain commands
     * 
     * @param mainCommander
     * 
     * @throws IOException
     */
    public void printShortCommandList(JCommander mainCommander) {
        TreeSet<String> commandNames = Sets.newTreeSet();
        int longestCommandLenght = 0;
        // do this to ignore aliases
        for (String name : mainCommander.getCommands().keySet()) {
            JCommander command = mainCommander.getCommands().get(name);
            Class<? extends Object> clazz = command.getObjects().get(0).getClass();
            String packageName = clazz.getPackage().getName();
            if (!packageName.startsWith("org.geogit.cli.plumbing")) {
                commandNames.add(name);
                longestCommandLenght = Math.max(longestCommandLenght, name.length());
            }
        }
        ConsoleReader console = getConsole();
        try {
            console.println("usage: geogit <command> [<args>]");
            console.println();
            console.println("The most commonly used geogit commands are:");
            for (String cmd : commandNames) {
                console.print(Strings.padEnd(cmd, longestCommandLenght, ' '));
                console.print("\t");
                console.println(mainCommander.getCommandDescription(cmd));
            }
            console.flush();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * This prints out all commands, including plumbing ones, without description
     * 
     * @param mainCommander
     * @throws IOException
     */
    public void printCommandList(JCommander mainCommander) {
        TreeSet<String> commandNames = Sets.newTreeSet();
        int longestCommandLenght = 0;
        // do this to ignore aliases
        for (String name : mainCommander.getCommands().keySet()) {
            commandNames.add(name);
            longestCommandLenght = Math.max(longestCommandLenght, name.length());
        }
        ConsoleReader console = getConsole();
        try {
            console.println("usage: geogit <command> [<args>]");
            console.println();
            int i = 0;
            for (String cmd : commandNames) {
                console.print(Strings.padEnd(cmd, longestCommandLenght, ' '));
                i++;
                if (i % 3 == 0) {
                    console.println();
                } else {
                    console.print("\t");
                }
            }
            console.flush();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * /**
     * 
     * @return the ProgressListener for the command line interface. If it doesn't exist, a new one
     *         will be constructed.
     * @see ProgressListener
     */
    public synchronized ProgressListener getProgressListener() {
        if (this.progressListener == null) {
            if (progressListenerDisabled) {
                this.progressListener = new DefaultProgressListener();
                return this.progressListener;
            }
            this.progressListener = new DefaultProgressListener() {

                private final Platform platform = getPlatform();

                private final ConsoleReader console = getConsole();

                private final NumberFormat percentFormat = NumberFormat.getPercentInstance();

                private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

                private final long delayNanos = TimeUnit.NANOSECONDS.convert(100,
                        TimeUnit.MILLISECONDS);

                // Don't skip the first update
                private volatile long lastRun = 0;

                @Override
                public void started() {
                    super.started();
                    lastRun = -(delayNanos + 1);
                }

                @Override
                public void setDescription(String s) {
                    try {
                        console.println();
                        console.println(s);
                        console.flush();
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }
                }

                @Override
                public synchronized void complete() {
                    // avoid double logging if caller missbehaves
                    if (super.isCompleted()) {
                        return;
                    }
                    super.complete();
                    super.dispose();
                    try {
                        log(getProgress());
                        console.println();
                        console.flush();
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }
                }

                @Override
                public synchronized void setProgress(float percent) {
                    super.setProgress(percent);
                    long nanoTime = platform.nanoTime();
                    if ((nanoTime - lastRun) > delayNanos) {
                        lastRun = nanoTime;
                        log(percent);
                    }
                }

                private void log(float percent) {
                    CursorBuffer cursorBuffer = console.getCursorBuffer();
                    cursorBuffer.clear();
                    String description = getDescription();
                    if (description != null) {
                        cursorBuffer.write(description);
                    }
                    if (percent > 100) {
                        cursorBuffer.write(numberFormat.format(percent));
                    } else {
                        cursorBuffer.write(percentFormat.format(percent / 100f));
                    }
                    try {
                        console.redrawLine();
                        console.flush();
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }
                }
            };

        }
        return this.progressListener;
    }

    static void addShutdownHook(final GeogitCLI cli) {
        // try to grafefully shutdown upon CTRL+C
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (cli.isRunning()) {
                    System.err.println("Forced shut down, wait for geogit to be closed...");
                    System.err.flush();
                    cli.close();
                    System.err.println("geogit closed.");
                    System.err.flush();
                }
            }
        });
    }
}
