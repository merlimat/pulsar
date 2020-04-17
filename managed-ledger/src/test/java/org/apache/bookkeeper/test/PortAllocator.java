package org.apache.bookkeeper.test;

import java.net.ServerSocket;

public class PortAllocator {
    public static int nextPort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException(e); // avoid having to change signatures just to allocate port
        }
    }
}
