/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * This file is derived from LocalBookkeeperEnsemble from Apache BookKeeper
 * http://bookkeeper.apache.org
 */

package org.apache.pulsar.zookeeper;

import static org.apache.bookkeeper.stream.protocol.ProtocolConstants.DEFAULT_STREAM_CONF;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.netty.buffer.ByteBufAllocator;
import org.apache.bookkeeper.bookie.*;
import org.apache.bookkeeper.bookie.BookieException.InvalidCookieException;
import org.apache.bookkeeper.bookie.storage.ldb.DbLedgerStorage;
import org.apache.bookkeeper.clients.StorageClientBuilder;
import org.apache.bookkeeper.clients.admin.StorageAdminClient;
import org.apache.bookkeeper.clients.config.StorageClientSettings;
import org.apache.bookkeeper.clients.exceptions.NamespaceExistsException;
import org.apache.bookkeeper.clients.exceptions.NamespaceNotFoundException;
import org.apache.bookkeeper.common.allocator.PoolingPolicy;
import org.apache.bookkeeper.common.concurrent.FutureUtils;
import org.apache.bookkeeper.common.util.Backoff;
import org.apache.bookkeeper.common.util.Backoff.Jitter.Type;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.discover.RegistrationManager;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.meta.MetadataBookieDriver;
import org.apache.bookkeeper.meta.exceptions.MetadataException;
import org.apache.bookkeeper.proto.BookieServer;
import org.apache.bookkeeper.replication.ReplicationException;
import org.apache.bookkeeper.server.conf.BookieConfiguration;
import org.apache.bookkeeper.stats.NullStatsLogger;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.bookkeeper.stream.proto.NamespaceConfiguration;
import org.apache.bookkeeper.stream.proto.NamespaceProperties;
import org.apache.bookkeeper.stream.server.StreamStorageLifecycleComponent;
import org.apache.bookkeeper.stream.storage.api.cluster.ClusterInitializer;
import org.apache.bookkeeper.stream.storage.impl.cluster.ZkClusterInitializer;
import org.apache.bookkeeper.tls.SecurityException;
import org.apache.bookkeeper.util.DiskChecker;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalBookkeeperEnsemble {
    protected static final Logger LOG = LoggerFactory.getLogger(LocalBookkeeperEnsemble.class);
    public static final int CONNECTION_TIMEOUT = 30000;

    int numberOfBookies;
    private boolean clearOldData = false;

    private static class BasePortManager implements Supplier<Integer> {

        private int port;

        public BasePortManager(int basePort) {
            this.port = basePort;
        }

        @Override
        public synchronized Integer get() {
            return port++;
        }
    }

    private final Supplier<Integer> portManager;


    public LocalBookkeeperEnsemble(int numberOfBookies, int zkPort, Supplier<Integer> portManager) {
        this(numberOfBookies, zkPort, 4181, null, null, true, null, portManager);
    }

    public LocalBookkeeperEnsemble(int numberOfBookies, int zkPort, int bkBasePort, String zkDataDirName,
            String bkDataDirName, boolean clearOldData) {
        this(numberOfBookies, zkPort, bkBasePort, 4181, zkDataDirName, bkDataDirName, clearOldData, null);
    }

    public LocalBookkeeperEnsemble(int numberOfBookies, int zkPort, int bkBasePort, String zkDataDirName,
            String bkDataDirName, boolean clearOldData, String advertisedAddress) {
        this(numberOfBookies, zkPort, bkBasePort, 4181, zkDataDirName, bkDataDirName, clearOldData, advertisedAddress);
    }

    public LocalBookkeeperEnsemble(int numberOfBookies,
                                   int zkPort,
                                   int bkBasePort,
                                   int streamStoragePort,
                                   String zkDataDirName,
                                   String bkDataDirName,
                                   boolean clearOldData,
                                   String advertisedAddress) {
        this(numberOfBookies, zkPort, 4181, zkDataDirName, bkDataDirName, clearOldData, advertisedAddress,
                new BasePortManager(bkBasePort));
    }

    public LocalBookkeeperEnsemble(int numberOfBookies,
            int zkPort,
            int streamStoragePort,
            String zkDataDirName,
            String bkDataDirName,
            boolean clearOldData,
            String advertisedAddress,
            Supplier<Integer> portManager) {
        this.numberOfBookies = numberOfBookies;
        this.portManager = portManager;
        this.streamStoragePort = streamStoragePort;
        this.zkDataDirName = zkDataDirName;
        this.bkDataDirName = bkDataDirName;
        this.clearOldData = clearOldData;
        this.zkPort = zkPort;
        this.advertisedAddress = null == advertisedAddress ? "127.0.0.1" : advertisedAddress;
        LOG.info("Running {} bookie(s) and advertised them at {}.", this.numberOfBookies, advertisedAddress);
    }

    private String HOSTPORT;
    private String advertisedAddress;
    private int zkPort;

    NIOServerCnxnFactory serverFactory;
    ZooKeeperServer zks;
    ZooKeeper zkc;

    static int zkSessionTimeOut = 5000;
    String zkDataDirName;

    // BookKeeper variables
    String bkDataDirName;
    LocalBookie localBookies[];

    // Stream/Table Storage
    StreamStorageLifecycleComponent streamStorage;
    Integer streamStoragePort = 4181;

    private void runZookeeper(int maxCC) throws IOException {
        // create a ZooKeeper server(dataDir, dataLogDir, port)
        LOG.info("Starting ZK server");
        // ServerStats.registerAsConcrete();
        // ClientBase.setupTestEnv();

        File zkDataDir = isNotBlank(zkDataDirName) ? Files.createDirectories(Paths.get(zkDataDirName)).toFile()
                : Files.createTempDirectory("zktest").toFile();

        if (this.clearOldData) {
            cleanDirectory(zkDataDir);
        }

        try {
            // Allow all commands on ZK control port
            System.setProperty("zookeeper.4lw.commands.whitelist", "*");
            zks = new ZooKeeperServer(zkDataDir, zkDataDir, ZooKeeperServer.DEFAULT_TICK_TIME);

            serverFactory = new NIOServerCnxnFactory();
            serverFactory.configure(new InetSocketAddress(zkPort), maxCC);
            serverFactory.startup(zks);
        } catch (Exception e) {
            LOG.error("Exception while instantiating ZooKeeper", e);

            if (serverFactory != null) {
                serverFactory.shutdown();
            }
            throw new IOException(e);
        }

        this.zkPort = serverFactory.getLocalPort();
        this.HOSTPORT = "127.0.0.1:" + zkPort;

        boolean b = waitForServerUp(HOSTPORT, CONNECTION_TIMEOUT);

        LOG.info("ZooKeeper server up: {}", b);
        LOG.debug("Local ZK started (port: {}, data_directory: {})", zkPort, zkDataDir.getAbsolutePath());
    }

    private void initializeZookeper() throws IOException {
        LOG.info("Instantiate ZK Client");
        // initialize the zk client with values
        try {
            ZKConnectionWatcher zkConnectionWatcher = new ZKConnectionWatcher();
            zkc = new ZooKeeper(HOSTPORT, zkSessionTimeOut, zkConnectionWatcher);
            zkConnectionWatcher.waitForConnection();
            if (zkc.exists("/ledgers", false) == null) {
                zkc.create("/ledgers", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if (zkc.exists("/ledgers/available", false) == null) {
                zkc.create("/ledgers/available", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if (zkc.exists("/ledgers/available/readonly", false) == null) {
                zkc.create("/ledgers/available/readonly", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if (zkc.exists(ZkBookieRackAffinityMapping.BOOKIE_INFO_ROOT_PATH, false) == null) {
                zkc.create(ZkBookieRackAffinityMapping.BOOKIE_INFO_ROOT_PATH, "{}".getBytes(), Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }

            // No need to create an entry for each requested bookie anymore as the
            // BookieServers will register themselves with ZooKeeper on startup.
        } catch (KeeperException e) {
            LOG.error("Exception while creating znodes", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while creating znodes", e);
        }
    }

    private void runBookies(ServerConfiguration baseConf) throws Exception {
        LOG.info("Starting Bookie(s)");
        // Create Bookie Servers (B1, B2, B3)

        localBookies = new LocalBookie[numberOfBookies];

        for (int i = 0; i < numberOfBookies; i++) {

            File bkDataDir = isNotBlank(bkDataDirName)
                    ? Files.createDirectories(Paths.get(bkDataDirName + Integer.toString(i))).toFile()
                    : Files.createTempDirectory("bk" + Integer.toString(i) + "test").toFile();

            if (this.clearOldData) {
                cleanDirectory(bkDataDir);
            }

            int bookiePort = portManager.get();
            if (bookiePort == 0) {
                try (ServerSocket s = new ServerSocket(0)) {
                    bookiePort = s.getLocalPort();
                }
            }

            // Ensure registration Z-nodes are cleared when standalone service is restarted ungracefully
            String registrationZnode = String.format("/ledgers/available/%s:%d", baseConf.getAdvertisedAddress(), bookiePort);
            if (zkc.exists(registrationZnode, null) != null) {
                try {
                    zkc.delete(registrationZnode, -1);
                } catch (NoNodeException nne) {
                    // Ignore if z-node was just expired
                }
            }

            ServerConfiguration conf = new ServerConfiguration(baseConf);
            // override settings
            conf.setBookiePort(bookiePort);
            conf.setZkServers("127.0.0.1:" + zkPort);
            conf.setJournalDirName(bkDataDir.getPath());
            conf.setLedgerDirNames(new String[] { bkDataDir.getPath() });
            conf.setAllocatorPoolingPolicy(PoolingPolicy.UnpooledHeap);
            conf.setAllowEphemeralPorts(true);

            try {
                localBookies[i] = newBookieServer(conf);
            } catch (InvalidCookieException e) {
                // InvalidCookieException can happen if the machine IP has changed
                // Since we are running here a local bookie that is always accessed
                // from localhost, we can ignore the error
                for (String path : zkc.getChildren("/ledgers/cookies", false)) {
                    zkc.delete("/ledgers/cookies/" + path, -1);
                }

                // Also clean the on-disk cookie
                new File(new File(bkDataDir, "current"), "VERSION").delete();

                // Retry to start the bookie after cleaning the old left cookie
                localBookies[i] = newBookieServer(conf);
            }
            localBookies[i].getBookieServer().start();
            LOG.debug("Local BK[{}] started (port: {}, data_directory: {})", i, bookiePort,
                    bkDataDir.getAbsolutePath());
        }
    }

    public static LocalBookie newBookieServer(ServerConfiguration config)
            throws InterruptedException, BookieException, KeeperException, IOException,
            SecurityException, ReplicationException.CompatibilityException,
            ReplicationException.UnavailableException, MetadataException {
        return newBookieServer(config, NullStatsLogger.INSTANCE);
    }

    public static LocalBookie newBookieServer(ServerConfiguration config, StatsLogger statsLogger)
            throws InterruptedException, BookieException, KeeperException, IOException,
            SecurityException, ReplicationException.CompatibilityException,
            ReplicationException.UnavailableException, MetadataException {
        if (Objects.isNull(statsLogger)) {
            statsLogger = NullStatsLogger.INSTANCE;
        }

        final MetadataBookieDriver metadataBookieDriver =
                BookieResources.createMetadataDriver(config, statsLogger);

        final RegistrationManager registrationManager = metadataBookieDriver.createRegistrationManager();

        final LedgerManager ledgerManager = metadataBookieDriver.getLedgerManagerFactory().newLedgerManager();
        final ByteBufAllocator allocator = BookieResources.createAllocator(config);

        final DiskChecker diskChecker = BookieResources.createDiskChecker(config);
        LedgerDirsManager ledgerDirsManager = BookieResources.createLedgerDirsManager(
                config, diskChecker, statsLogger);
        LedgerDirsManager indexDirsManager = BookieResources.createIndexDirsManager(
                config, diskChecker,  statsLogger, ledgerDirsManager);

        // validate cookies
        final CookieValidation cookieValidation =
                new LegacyCookieValidation(config, registrationManager);
        // this must run before the storage is created
        cookieValidation.checkCookies(storageDirectoriesFromConfig(config));

        LedgerStorage storage = BookieResources.createLedgerStorage(
                config, ledgerManager, ledgerDirsManager, indexDirsManager, statsLogger, allocator);

        final Bookie bookie = config.isForceReadOnlyBookie()
                ? new ReadOnlyBookie(config, registrationManager, storage,
                    diskChecker,
                    ledgerDirsManager, indexDirsManager,
                    statsLogger, allocator)
                : new BookieImpl(config, registrationManager, storage,
                    diskChecker,
                    ledgerDirsManager, indexDirsManager,
                    statsLogger, allocator);

        BookieServer bs = new BookieServer(config, bookie, statsLogger, allocator);
        return new LocalBookie(config, bs, storage, ledgerManager, registrationManager, metadataBookieDriver);
    }

    public static List<File> storageDirectoriesFromConfig(ServerConfiguration conf) throws IOException {
        List<File> dirs = new ArrayList<>();

        File[] journalDirs = conf.getJournalDirs();
        if (journalDirs != null) {
            for (File j : journalDirs) {
                File cur = BookieImpl.getCurrentDirectory(j);
                BookieImpl.checkDirectoryStructure(cur);
                dirs.add(cur);
            }
        }

        File[] ledgerDirs = conf.getLedgerDirs();
        if (ledgerDirs != null) {
            for (File l : ledgerDirs) {
                File cur = BookieImpl.getCurrentDirectory(l);
                BookieImpl.checkDirectoryStructure(cur);
                dirs.add(cur);
            }
        }
        File[] indexDirs = conf.getIndexDirs();
        if (indexDirs != null) {
            for (File i : indexDirs) {
                File cur = BookieImpl.getCurrentDirectory(i);
                BookieImpl.checkDirectoryStructure(cur);
                dirs.add(cur);
            }
        }
        return dirs;
    }

    public void runStreamStorage(CompositeConfiguration conf) throws Exception {
        String zkServers = "127.0.0.1:" + zkPort;
        String metadataServiceUriStr = "zk://" + zkServers + "/ledgers";
        URI metadataServiceUri = URI.create(metadataServiceUriStr);

        // zookeeper servers
        conf.setProperty("metadataServiceUri", metadataServiceUriStr);
        // dlog settings
        conf.setProperty("dlog.bkcEnsembleSize", 1);
        conf.setProperty("dlog.bkcWriteQuorumSize", 1);
        conf.setProperty("dlog.bkcAckQuorumSize", 1);
        // stream storage port
        conf.setProperty("storageserver.grpc.port", streamStoragePort);

        // initialize the stream storage metadata
        ClusterInitializer initializer = new ZkClusterInitializer(zkServers);
        initializer.initializeCluster(metadataServiceUri, 2);

        // load the stream storage component
        ServerConfiguration serverConf = new ServerConfiguration();
        serverConf.loadConf(conf);
        BookieConfiguration bkConf = new BookieConfiguration(serverConf);

        this.streamStorage = new StreamStorageLifecycleComponent(bkConf, NullStatsLogger.INSTANCE);
        this.streamStorage.start();
        LOG.debug("Local BK stream storage started (port: {})", streamStoragePort);

        // create a default namespace
        try (StorageAdminClient admin = StorageClientBuilder.newBuilder()
             .withSettings(StorageClientSettings.newBuilder()
                 .serviceUri("bk://localhost:4181")
                 .backoffPolicy(Backoff.Jitter.of(
                     Type.EXPONENTIAL,
                     1000,
                     10000,
                     30
                 ))
                 .build())
            .buildAdmin()) {

            try {
                NamespaceProperties ns = FutureUtils.result(admin.getNamespace("default"));
                LOG.info("'default' namespace for table service : {}", ns);
            } catch (NamespaceNotFoundException nnfe) {
                LOG.info("Creating default namespace");
                try {
                    NamespaceProperties ns =
                        FutureUtils.result(admin.createNamespace("default", NamespaceConfiguration.newBuilder()
                            .setDefaultStreamConf(DEFAULT_STREAM_CONF)
                            .build()));
                    LOG.info("Successfully created 'default' namespace :\n{}", ns);
                } catch (NamespaceExistsException nee) {
                    // namespace already exists
                    LOG.warn("Namespace 'default' already existed.");
                }
            }
        }
    }

    public void start(boolean enableStreamStorage) throws  Exception {
        LOG.debug("Local ZK/BK starting ...");
        ServerConfiguration conf = new ServerConfiguration();
        // Use minimal configuration requiring less memory for unit tests
        conf.setLedgerStorageClass(DbLedgerStorage.class.getName());
        conf.setProperty("dbStorage_writeCacheMaxSizeMb", 2);
        conf.setProperty("dbStorage_readAheadCacheMaxSizeMb", 1);
        conf.setProperty("dbStorage_rocksDB_writeBufferSizeMB", 1);
        conf.setProperty("dbStorage_rocksDB_blockCacheSize", 1024 * 1024);
        conf.setFlushInterval(60000);
        conf.setJournalSyncData(false);
        conf.setProperty("journalMaxGroupWaitMSec", 0L);
        conf.setAllowLoopback(true);
        conf.setGcWaitTime(60000);
        conf.setNumAddWorkerThreads(0);
        conf.setNumReadWorkerThreads(0);
        conf.setNumHighPriorityWorkerThreads(0);
        conf.setNumJournalCallbackThreads(0);
        conf.setServerNumIOThreads(1);
        conf.setNumLongPollWorkerThreads(1);
        conf.setAllocatorPoolingPolicy(PoolingPolicy.UnpooledHeap);

        runZookeeper(1000);
        initializeZookeper();
        runBookies(conf);

        if (enableStreamStorage) {
            runStreamStorage(new CompositeConfiguration());
        }
    }

    public void start() throws Exception {
        start(false);
    }

    public void startStandalone() throws Exception {
        startStandalone(new ServerConfiguration(), false);
    }

    public void startStandalone(ServerConfiguration conf, boolean enableStreamStorage) throws Exception {
        LOG.debug("Local ZK/BK starting ...");
        conf.setAdvertisedAddress(advertisedAddress);

        runZookeeper(1000);
        initializeZookeper();
        runBookies(conf);
        if (enableStreamStorage) {
            runStreamStorage(new CompositeConfiguration());
        }
    }

    public void stopBK() throws Exception {
        LOG.debug("Local ZK/BK stopping ...");
        for (LocalBookie bookie : localBookies) {
            bookie.shutdown();
        }
    }

    public void startBK() throws Exception {
        for (int i = 0; i < numberOfBookies; i++) {

            try {
                localBookies[i] = newBookieServer(localBookies[i].getConfiguration());
            } catch (InvalidCookieException e) {
                // InvalidCookieException can happen if the machine IP has changed
                // Since we are running here a local bookie that is always accessed
                // from localhost, we can ignore the error
                for (String path : zkc.getChildren("/ledgers/cookies", false)) {
                    zkc.delete("/ledgers/cookies/" + path, -1);
                }

                // Also clean the on-disk cookie
                new File(new File(localBookies[i].getConfiguration().getJournalDirNames()[0],
                                  "current"), "VERSION").delete();

                // Retry to start the bookie after cleaning the old left cookie
                localBookies[i] = newBookieServer(localBookies[i].getConfiguration());
            }
            localBookies[i].getBookieServer().start();
        }
    }

    public void stop() throws Exception {
        if (null != streamStorage) {
            LOG.debug("Local bk stream storage stopping ...");
            streamStorage.close();
        }

        LOG.debug("Local ZK/BK stopping ...");
        for (LocalBookie bookie : localBookies) {
            bookie.shutdown();
        }

        zkc.close();
        zks.shutdown();
        serverFactory.shutdown();
        LOG.debug("Local ZK/BK stopped");
    }

    /* Watching SyncConnected event from ZooKeeper */
    public static class ZKConnectionWatcher implements Watcher {
        private CountDownLatch clientConnectLatch = new CountDownLatch(1);

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected) {
                clientConnectLatch.countDown();
            }
        }

        // Waiting for the SyncConnected event from the ZooKeeper server
        public void waitForConnection() throws IOException {
            try {
                if (!clientConnectLatch.await(zkSessionTimeOut, TimeUnit.MILLISECONDS)) {
                    throw new IOException("Couldn't connect to zookeeper server");
                }
            } catch (InterruptedException e) {
                throw new IOException("Interrupted when connecting to zookeeper server", e);
            }
        }
    }

    public static boolean waitForServerUp(String hp, long timeout) {
        long start = System.currentTimeMillis();
        String split[] = hp.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        while (true) {
            try {
                Socket sock = new Socket(host, port);
                BufferedReader reader = null;
                try {
                    OutputStream outstream = sock.getOutputStream();
                    outstream.write("stat".getBytes());
                    outstream.flush();

                    reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String line = reader.readLine();
                    if (line != null && line.startsWith("Zookeeper version:")) {
                        LOG.info("Server UP");
                        return true;
                    }
                } finally {
                    sock.close();
                    if (reader != null) {
                        reader.close();
                    }
                }
            } catch (IOException e) {
                // ignore as this is expected
                LOG.info("server " + hp + " not up " + e);
            }

            if (System.currentTimeMillis() > start + timeout) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return false;
    }

    public ZooKeeper getZkClient() {
        return zkc;
    }

    public ZooKeeperServer getZkServer() {
        return zks;
    }

    public LocalBookie[] getLocalBookies() {
        return localBookies;
    }

    public BookieServer[] getBookies() {
        return Arrays.stream(localBookies).map(l -> l.getBookieServer()).toArray(BookieServer[]::new);
    }

    public int getZookeeperPort() {
        return zkPort;
    }

    public static class LocalBookie {
                final ServerConfiguration conf;
        final BookieServer bs;
        final LedgerStorage storage;
        final LedgerManager lm;
        final RegistrationManager rm;
        final MetadataBookieDriver metadataDriver;


        LocalBookie(ServerConfiguration conf, BookieServer bs,
                    LedgerStorage storage, LedgerManager lm,
                    RegistrationManager rm, MetadataBookieDriver metadataDriver) {
            this.conf = conf;
            this.bs = bs;
            this.lm = lm;
            this.storage = storage;
            this.rm = rm;
            this.metadataDriver = metadataDriver;
        }

        public ServerConfiguration getConfiguration() { return conf; }
        public LedgerManager getLedgerManager() { return lm; }
        public BookieServer getBookieServer() { return bs; }

        public void start() throws Exception {
            bs.start();
        }

        public void shutdown() throws Exception {
            bs.shutdown();
            storage.shutdown();
            lm.close();
            rm.close();
            metadataDriver.close();
        }
    }
}
