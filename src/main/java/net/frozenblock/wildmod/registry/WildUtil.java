package net.frozenblock.wildmod.registry;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.frozenblock.wildmod.world.gen.random.WildAbstractRandom;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.CharPredicate;
import net.minecraft.datafixer.Schemas;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WildUtil extends net.minecraft.util.Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_PARALLELISM = 255;
    private static final String MAX_BG_THREADS_PROPERTY = "max.bg.threads";
    private static final AtomicInteger NEXT_WORKER_ID = new AtomicInteger(1);
    private static final ExecutorService BOOTSTRAP_EXECUTOR = createWorker("Bootstrap");
    private static final ExecutorService MAIN_WORKER_EXECUTOR = createWorker("Main");
    private static final ExecutorService IO_WORKER_EXECUTOR = createIoWorker();
    public static LongSupplier nanoTimeSupplier = System::nanoTime;
    public static final Ticker TICKER = new Ticker() {
        public long read() {
            return WildUtil.nanoTimeSupplier.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider JAR_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders()
            .stream()
            .filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> missingBreakpointHandler = message -> {
    };

    public WildUtil() {
    }

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T extends Comparable<T>> String getValueAsString(Property<T> property, Object value) {
        return property.name((T) value);
    }

    public static String createTranslationKey(String type, @Nullable Identifier id) {
        return id == null ? type + ".unregistered_sadface" : type + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    public static long getMeasuringTimeMs() {
        return getMeasuringTimeNano() / 1000000L;
    }

    public static long getMeasuringTimeNano() {
        return nanoTimeSupplier.getAsLong();
    }

    public static long getEpochTimeMs() {
        return Instant.now().toEpochMilli();
    }

    private static ExecutorService createWorker(String name) {
        int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxBackgroundThreads());
        ExecutorService executorService;
        if (i <= 0) {
            executorService = MoreExecutors.newDirectExecutorService();
        } else {
            executorService = new ForkJoinPool(i, forkJoinPool -> {
                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
                    protected void onTermination(Throwable throwable) {
                        if (throwable != null) {
                            WildUtil.LOGGER.warn("{} died", this.getName(), throwable);
                        } else {
                            WildUtil.LOGGER.debug("{} shutdown", this.getName());
                        }

                        super.onTermination(throwable);
                    }
                };
                forkJoinWorkerThread.setName("Worker-" + name + "-" + NEXT_WORKER_ID.getAndIncrement());
                return forkJoinWorkerThread;
            }, WildUtil::uncaughtExceptionHandler, true);
        }

        return executorService;
    }

    private static int getMaxBackgroundThreads() {
        String string = System.getProperty("max.bg.threads");
        if (string != null) {
            try {
                int i = Integer.parseInt(string);
                if (i >= 1 && i <= 255) {
                    return i;
                }

                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", string, 255);
            } catch (NumberFormatException var2) {
                LOGGER.error(
                        "Could not parse {} property value '{}'. Should be an integer value between 1 and {}.",
                        "max.bg.threads", string, 255);
            }
        }

        return 255;
    }

    public static ExecutorService getBootstrapExecutor() {
        return BOOTSTRAP_EXECUTOR;
    }

    public static ExecutorService getMainWorkerExecutor() {
        return MAIN_WORKER_EXECUTOR;
    }

    public static ExecutorService getIoWorkerExecutor() {
        return IO_WORKER_EXECUTOR;
    }

    public static void shutdownExecutors() {
        attemptShutdown(MAIN_WORKER_EXECUTOR);
        attemptShutdown(IO_WORKER_EXECUTOR);
    }

    private static void attemptShutdown(ExecutorService service) {
        service.shutdown();

        boolean bl;
        try {
            bl = service.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException var3) {
            bl = false;
        }

        if (!bl) {
            service.shutdownNow();
        }

    }

    private static ExecutorService createIoWorker() {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("IO-Worker-" + NEXT_WORKER_ID.getAndIncrement());
            thread.setUncaughtExceptionHandler(WildUtil::uncaughtExceptionHandler);
            return thread;
        });
    }

    public static <T> CompletableFuture<T> completeExceptionally(Throwable throwable) {
        CompletableFuture<T> completableFuture = new CompletableFuture();
        completableFuture.completeExceptionally(throwable);
        return completableFuture;
    }

    public static void throwUnchecked(Throwable t) {
        throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
    }

    private static void uncaughtExceptionHandler(Thread thread, Throwable t) {
        throwOrPause(t);
        if (t instanceof CompletionException) {
            t = t.getCause();
        }

        if (t instanceof CrashException) {
            Bootstrap.println(((CrashException) t).getReport().asString());
            System.exit(-1);
        }

        LOGGER.error(String.format("Caught exception in thread %s", thread), t);
    }

    @Nullable
    public static Type<?> getChoiceType(DSL.TypeReference typeReference, String id) {
        return !SharedConstants.useChoiceTypeRegistrations ? null : getChoiceTypeInternal(typeReference, id);
    }

    @Nullable
    private static Type<?> getChoiceTypeInternal(DSL.TypeReference typeReference, String id) {
        Type<?> type = null;

        try {
            type = Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion())).getChoiceType(typeReference, id);
        } catch (IllegalArgumentException var4) {
            LOGGER.error("No data fixer registered for {}", id);
            if (SharedConstants.isDevelopment) {
                throw var4;
            }
        }

        return type;
    }

    public static Runnable debugRunnable(String activeThreadName, Runnable task) {
        return SharedConstants.isDevelopment ? () -> {
            Thread thread = Thread.currentThread();
            String string2 = thread.getName();
            thread.setName(activeThreadName);

            try {
                task.run();
            } finally {
                thread.setName(string2);
            }

        } : task;
    }

    public static <V> Supplier<V> debugSupplier(String activeThreadName, Supplier<V> supplier) {
        return SharedConstants.isDevelopment ? () -> {
            Thread thread = Thread.currentThread();
            String string2 = thread.getName();
            thread.setName(activeThreadName);

            Object var4;
            try {
                var4 = supplier.get();
            } finally {
                thread.setName(string2);
            }

            return (V) var4;
        } : supplier;
    }

    public static net.minecraft.util.Util.OperatingSystem getOperatingSystem() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return net.minecraft.util.Util.OperatingSystem.WINDOWS;
        } else if (string.contains("mac")) {
            return net.minecraft.util.Util.OperatingSystem.OSX;
        } else if (string.contains("solaris")) {
            return net.minecraft.util.Util.OperatingSystem.SOLARIS;
        } else if (string.contains("sunos")) {
            return net.minecraft.util.Util.OperatingSystem.SOLARIS;
        } else if (string.contains("linux")) {
            return net.minecraft.util.Util.OperatingSystem.LINUX;
        } else {
            return string.contains("unix") ? net.minecraft.util.Util.OperatingSystem.LINUX : net.minecraft.util.Util.OperatingSystem.UNKNOWN;
        }
    }

    public static Stream<String> getJVMFlags() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().stream().filter(runtimeArg -> runtimeArg.startsWith("-X"));
    }

    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T next(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = iterator.next();
        if (object != null) {
            T object3 = object2;

            while (object3 != object) {
                if (iterator.hasNext()) {
                    object3 = iterator.next();
                }
            }

            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        return object2;
    }

    public static <T> T previous(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();

        T object2;
        T object3;
        for (object2 = null; iterator.hasNext(); object2 = object3) {
            object3 = iterator.next();
            if (object3 == object) {
                if (object2 == null) {
                    object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
                }
                break;
            }
        }

        return object2;
    }

    public static <T> T make(Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(T object, Consumer<T> initializer) {
        initializer.accept(object);
        return object;
    }

    public static <K> Hash.Strategy<K> identityHashStrategy() {
        return (Hash.Strategy<K>) IdentityHashStrategy.INSTANCE;
    }

    public static <V> CompletableFuture<List<V>> combineSafe(List<? extends CompletableFuture<V>> futures) {
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        } else if (futures.size() == 1) {
            return (futures.get(0)).thenApply(List::of);
        } else {
            CompletableFuture<Void> completableFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            return completableFuture.thenApply(void_ -> futures.stream().map(CompletableFuture::join).toList());
        }
    }

    public static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures) {
        CompletableFuture<List<V>> completableFuture = new CompletableFuture<>();
        return method_43370(futures, completableFuture::completeExceptionally).applyToEither(completableFuture, Function.identity());
    }

    public static <V> CompletableFuture<List<V>> method_43373(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture<List<V>> completableFuture = new CompletableFuture<>();
        return method_43370(list, throwable -> {
            for (CompletableFuture<? extends V> completableFuture2 : list) {
                completableFuture2.cancel(true);
            }

            completableFuture.completeExceptionally(throwable);
        }).applyToEither(completableFuture, Function.identity());
    }

    private static <V> CompletableFuture<List<V>> method_43370(List<? extends CompletableFuture<? extends V>> list, Consumer<Throwable> consumer) {
        List<V> list2 = Lists.newArrayListWithCapacity(list.size());
        CompletableFuture<?>[] completableFutures = new CompletableFuture[list.size()];
        list.forEach(completableFuture -> {
            int i = list2.size();
            list2.add(null);
            completableFutures[i] = completableFuture.whenComplete((object, throwable) -> {
                if (throwable != null) {
                    consumer.accept(throwable);
                } else {
                    list2.set(i, object);
                }

            });
        });
        return CompletableFuture.allOf(completableFutures).thenApply(void_ -> list2);
    }

    public static <T> Optional<T> ifPresentOrElse(Optional<T> optional, Consumer<T> presentAction, Runnable elseAction) {
        if (optional.isPresent()) {
            presentAction.accept(optional.get());
        } else {
            elseAction.run();
        }

        return optional;
    }

    public static <T> Supplier<T> debugSupplier(Supplier<T> supplier, Supplier<String> messageSupplier) {
        return supplier;
    }

    public static Runnable debugRunnable(Runnable runnable, Supplier<String> messageSupplier) {
        return runnable;
    }

    public static void error(String message) {
        LOGGER.error(message);
        if (SharedConstants.isDevelopment) {
            pause(message);
        }

    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
        if (SharedConstants.isDevelopment) {
            pause(message);
        }

    }

    public static <T extends Throwable> T throwOrPause(T t) {
        if (SharedConstants.isDevelopment) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);
            pause(t.getMessage());
        }

        return t;
    }

    public static void setMissingBreakpointHandler(Consumer<String> missingBreakpointHandler) {
        WildUtil.missingBreakpointHandler = missingBreakpointHandler;
    }

    private static void pause(String message) {
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            missingBreakpointHandler.accept(message);
        }

    }

    public static String getInnermostMessage(Throwable t) {
        if (t.getCause() != null) {
            return getInnermostMessage(t.getCause());
        } else {
            return t.getMessage() != null ? t.getMessage() : t.toString();
        }
    }

    public static <T> T getRandom(T[] array, WildAbstractRandom random) {
        return array[random.nextInt(array.length)];
    }

    public static int getRandom(int[] array, WildAbstractRandom random) {
        return array[random.nextInt(array.length)];
    }

    public static <T> T getRandom(List<T> list, WildAbstractRandom random) {
        return list.get(random.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomOrEmpty(List<T> list, WildAbstractRandom random) {
        return list.isEmpty() ? Optional.empty() : Optional.of(getRandom(list, random));
    }

    private static BooleanSupplier renameTask(Path src, Path dest) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                try {
                    Files.move(src, dest);
                    return true;
                } catch (IOException var2) {
                    WildUtil.LOGGER.error("Failed to rename", var2);
                    return false;
                }
            }

            public String toString() {
                return "rename " + src + " to " + dest;
            }
        };
    }

    private static BooleanSupplier deleteTask(Path path) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                } catch (IOException var2) {
                    WildUtil.LOGGER.warn("Failed to delete", var2);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + path;
            }
        };
    }

    private static BooleanSupplier deletionVerifyTask(Path path) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                return !Files.exists(path);
            }

            public String toString() {
                return "verify that " + path + " is deleted";
            }
        };
    }

    private static BooleanSupplier existenceCheckTask(Path path) {
        return new BooleanSupplier() {
            public boolean getAsBoolean() {
                return Files.isRegularFile(path);
            }

            public String toString() {
                return "verify that " + path + " is present";
            }
        };
    }

    private static boolean attemptTasks(BooleanSupplier... tasks) {
        for (BooleanSupplier booleanSupplier : tasks) {
            if (!booleanSupplier.getAsBoolean()) {
                LOGGER.warn("Failed to execute {}", booleanSupplier);
                return false;
            }
        }

        return true;
    }

    private static boolean attemptTasks(int retries, String taskName, BooleanSupplier... tasks) {
        for (int i = 0; i < retries; ++i) {
            if (attemptTasks(tasks)) {
                return true;
            }

            LOGGER.error("Failed to {}, retrying {}/{}", taskName, i, retries);
        }

        LOGGER.error("Failed to {}, aborting, progress might be lost", taskName);
        return false;
    }

    public static void backupAndReplace(File current, File newFile, File backup) {
        backupAndReplace(current.toPath(), newFile.toPath(), backup.toPath());
    }

    public static void backupAndReplace(Path current, Path newPath, Path backup) {
        backupAndReplace(current, newPath, backup, false);
    }

    public static void backupAndReplace(File current, File newPath, File backup, boolean noRestoreOnFail) {
        backupAndReplace(current.toPath(), newPath.toPath(), backup.toPath(), noRestoreOnFail);
    }

    public static void backupAndReplace(Path current, Path newPath, Path backup, boolean noRestoreOnFail) {
        int i = 10;
        if (!Files.exists(current)
                || attemptTasks(10, "create backup " + backup, deleteTask(backup), renameTask(current, backup), existenceCheckTask(backup))) {
            if (attemptTasks(10, "remove old " + current, deleteTask(current), deletionVerifyTask(current))) {
                if (!attemptTasks(10, "replace " + current + " with " + newPath, renameTask(newPath, current), existenceCheckTask(current)) && !noRestoreOnFail
                ) {
                    attemptTasks(10, "restore " + current + " from " + backup, renameTask(backup, current), existenceCheckTask(current));
                }

            }
        }
    }

    public static int moveCursor(String string, int cursor, int delta) {
        int i = string.length();
        if (delta >= 0) {
            for (int j = 0; cursor < i && j < delta; ++j) {
                if (Character.isHighSurrogate(string.charAt(cursor++)) && cursor < i && Character.isLowSurrogate(string.charAt(cursor))) {
                    ++cursor;
                }
            }
        } else {
            for (int j = delta; cursor > 0 && j < 0; ++j) {
                --cursor;
                if (Character.isLowSurrogate(string.charAt(cursor)) && cursor > 0 && Character.isHighSurrogate(string.charAt(cursor - 1))) {
                    --cursor;
                }
            }
        }

        return cursor;
    }

    public static Consumer<String> addPrefix(String prefix, Consumer<String> consumer) {
        return string -> consumer.accept(prefix + string);
    }

    public static DataResult<int[]> toArray(IntStream stream, int length) {
        int[] is = stream.limit(length + 1).toArray();
        if (is.length != length) {
            String string = "Input is not a list of " + length + " ints";
            return is.length >= length ? DataResult.error(string, Arrays.copyOf(is, length)) : DataResult.error(string);
        } else {
            return DataResult.success(is);
        }
    }

    public static <T> DataResult<List<T>> toArray(List<T> list, int length) {
        if (list.size() != length) {
            String string = "Input is not a list of " + length + " elements";
            return list.size() >= length ? DataResult.error(string, list.subList(0, length)) : DataResult.error(string);
        } else {
            return DataResult.success(list);
        }
    }

    public static void startTimerHack() {
        Thread thread = new Thread("Timer hack thread") {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException var2) {
                        WildUtil.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                        return;
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    public static void relativeCopy(Path src, Path dest, Path toCopy) throws IOException {
        Path path = src.relativize(toCopy);
        Path path2 = dest.resolve(path);
        Files.copy(toCopy, path2);
    }

    public static String replaceInvalidChars(String string, CharPredicate predicate) {
        return string.toLowerCase(Locale.ROOT)
                .chars()
                .mapToObj(charCode -> predicate.test((char) charCode) ? Character.toString((char) charCode) : "_")
                .collect(Collectors.joining());
    }

    public static <T, R> Function<T, R> memoize(Function<T, R> function) {
        return new Function<T, R>() {
            private final Map<T, R> cache = Maps.newHashMap();

            public R apply(T object) {
                return this.cache.computeIfAbsent(object, function);
            }

            public String toString() {
                return "memoize/1[function=" + function + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> copyShuffled(Stream<T> stream, WildAbstractRandom random) {
        ObjectArrayList<T> objectArrayList = stream.collect(ObjectArrayList.toList());
        shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> List<T> copyShuffled(Stream<T> stream, Random random) {
        ObjectArrayList<T> objectArrayList = stream.collect(ObjectArrayList.toList());
        shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static IntArrayList shuffle(IntStream stream, WildAbstractRandom random) {
        IntArrayList intArrayList = IntArrayList.wrap(stream.toArray());
        int i = intArrayList.size();

        for (int j = i; j > 1; --j) {
            int k = random.nextInt(j);
            intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
        }

        return intArrayList;
    }

    public static IntArrayList shuffle(IntStream stream, Random random) {
        IntArrayList intArrayList = IntArrayList.wrap(stream.toArray());
        int i = intArrayList.size();

        for (int j = i; j > 1; --j) {
            int k = random.nextInt(j);
            intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
        }

        return intArrayList;
    }

    public static <T> List<T> copyShuffled(T[] array, WildAbstractRandom random) {
        ObjectArrayList<T> objectArrayList = new ObjectArrayList<>(array);
        shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> List<T> copyShuffled(T[] array, Random random) {
        ObjectArrayList<T> objectArrayList = new ObjectArrayList<>(array);
        shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> List<T> copyShuffled(ObjectArrayList<T> list, Random random) {
        ObjectArrayList<T> objectArrayList = new ObjectArrayList<>(list);
        shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> void shuffle(ObjectArrayList<T> list, WildAbstractRandom random) {
        int i = list.size();

        for (int j = i; j > 1; --j) {
            int k = random.nextInt(j);
            list.set(j - 1, list.set(k, list.get(j - 1)));
        }

    }

    public static <T> void shuffle(ObjectArrayList<T> list, Random random) {
        int i = list.size();

        for (int j = i; j > 1; --j) {
            int k = random.nextInt(j);
            list.set(j - 1, list.set(k, list.get(j - 1)));
        }

    }

    enum IdentityHashStrategy implements Hash.Strategy<Object> {
        INSTANCE;

        IdentityHashStrategy() {
        }

        public int hashCode(Object o) {
            return System.identityHashCode(o);
        }

        public boolean equals(Object o, Object o2) {
            return o == o2;
        }
    }

    public enum OperatingSystem {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows") {
            @Override
            protected String[] getURLOpenCommand(URL url) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
            }
        },
        OSX("mac") {
            @Override
            protected String[] getURLOpenCommand(URL url) {
                return new String[]{"open", url.toString()};
            }
        },
        UNKNOWN("unknown");

        private final String name;

        OperatingSystem(String name) {
            this.name = name;
        }

        public void open(URL url) {
            try {
                Process process = AccessController.doPrivileged((PrivilegedAction<Process>) () -> {
                    try {
                        return Runtime.getRuntime().exec(this.getURLOpenCommand(url));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                for (String string : IOUtils.readLines(process.getErrorStream())) {
                    WildUtil.LOGGER.error(string);
                }

                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            } catch (IOException var5) {
                WildUtil.LOGGER.error("Couldn't open url '{}'", url, var5);
            }

        }

        public void open(URI uri) {
            try {
                this.open(uri.toURL());
            } catch (MalformedURLException var3) {
                WildUtil.LOGGER.error("Couldn't open uri '{}'", uri, var3);
            }

        }

        public void open(File file) {
            try {
                this.open(file.toURI().toURL());
            } catch (MalformedURLException var3) {
                WildUtil.LOGGER.error("Couldn't open file '{}'", file, var3);
            }

        }

        protected String[] getURLOpenCommand(URL url) {
            String string = url.toString();
            if ("file".equals(url.getProtocol())) {
                string = string.replace("file:", "file://");
            }

            return new String[]{"xdg-open", string};
        }

        public void open(String uri) {
            try {
                this.open(new URI(uri).toURL());
            } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
                WildUtil.LOGGER.error("Couldn't open uri '{}'", uri, var3);
            }

        }

        public String getName() {
            return this.name;
        }
    }
}
