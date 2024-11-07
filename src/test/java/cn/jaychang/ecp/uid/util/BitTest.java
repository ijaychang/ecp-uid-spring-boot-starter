package cn.jaychang.ecp.uid.util;

import org.junit.Test;

/**
 * @author jaychang
 * @since 2024/11/7
 **/
public class BitTest {
    @Test
    public void test1() {
        int i = 3;
        System.out.println(Integer.bitCount(i));
        System.out.println(Integer.bitCount(-1));
        System.out.println(Integer.bitCount(Integer.MAX_VALUE));
        System.out.println(Integer.bitCount(Integer.MIN_VALUE));
    }
}
