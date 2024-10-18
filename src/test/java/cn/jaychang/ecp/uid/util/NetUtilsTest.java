package cn.jaychang.ecp.uid.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class NetUtilsTest {

    @Test
    public void getLocalInetAddress() {
    }

    @Test
    public void getLocalLanAddress() {
    }

    @Test
    public void getMachineNum() {
    }

    @Test
    public void getRandomPort() {
    }

    @Test
    public void getAvailablePort() {
    }

    @Test
    public void testGetAvailablePort() {
        for (int i = 0 ; i < 1000 ; i ++) {
            int availablePort = NetUtils.getAvailablePort();
            System.out.println(availablePort);
        }
    }
}