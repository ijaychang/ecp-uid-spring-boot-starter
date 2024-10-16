package cn.jaychang.ecp.uid.twitter;


import cn.jaychang.ecp.uid.baidu.utils.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class SnowflakeIdWorkerTest {
    private SnowflakeIdWorker snowflakeIdWorker;

    private static final long twepoch = DateUtils.parseDate("2024-10-15 00:00:00", DateUtils.DATETIME_PATTERN).getTime();

    private static final int SIZE = 7000000;

    private static final int THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private static final boolean VERBOSE = true;

    @Before
    public void before() {
        snowflakeIdWorker = new SnowflakeIdWorker(1, 1);
        snowflakeIdWorker.setClock(true);
    }

    @Test
    public void maxWorkIdValue() {
        int workIdBits = 5;
        int maxWorkIdValue = -1 ^ (-1 << workIdBits);
        System.out.println(maxWorkIdValue);
    }

    @Test
    public void getNextId() {
        long id = snowflakeIdWorker.nextId();
        printBinaryString(id);
        // eg. 531900590592000 0000000000000001111000111100001011000001110000100001000000000000
        // 0-00000000000000111100011110000101100000111-0000100001-000000000000
        long sequenceBits = 12;
        long datacenterBits = 5;
        long workIdBits = 5;
        long timestamp = (id >> (datacenterBits + workIdBits + sequenceBits)) + twepoch;
        String formattedDate = DateUtils.formatByDateTimePattern(new Date(timestamp));
        System.out.println("formattedDate=" + formattedDate);

        String uidFormat = snowflakeIdWorker.parseUID(id);

        System.out.println(uidFormat);

    }

    public void printBinaryString(long id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Long.SIZE; i++) {
            int moveRightBits = Long.SIZE - i;
            sb.append(1 & (id >> moveRightBits));
        }
        System.out.println(sb);
    }


    /**
     * Test for serially generate
     */
    @Test
    public void testSerialGenerate() {
        // Generate UID serially
        Set<Long> uidSet = new HashSet<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            doGenerate(uidSet, i);
        }

        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    /**
     * Test for parallel generate
     *
     * @throws InterruptedException
     */
    @Test
    public void testParallelGenerate() throws InterruptedException {
        final AtomicInteger control = new AtomicInteger(-1);
        final Set<Long> uidSet = new ConcurrentSkipListSet<>();

        // Initialize threads
        List<Thread> threadList = new ArrayList<>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    workerRun(uidSet, control);
                }
            });
            thread.setName("UID-generator-" + i);

            threadList.add(thread);
            thread.start();
        }

        // Wait for worker done
        for (Thread thread : threadList) {
            thread.join();
        }

        // Check generate 10w times
        Assert.assertEquals(SIZE, control.get());

        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    public int updateAndGet(AtomicInteger control) {
        int prev, next;
        do {
            prev = control.get();
            next = prev == SIZE ? SIZE : prev + 1;
        } while (!control.compareAndSet(prev, next));
        return next;
    }

    /**
     * Worker run
     */
    private void workerRun(Set<Long> uidSet, AtomicInteger control) {
        for (;;) {
            int myPosition = updateAndGet(control);
            if (myPosition == SIZE) {
                return;
            }

            doGenerate(uidSet, myPosition);
        }
    }

    /**
     * Do generating
     */
    private void doGenerate(Set<Long> uidSet, int index) {
        long uid = snowflakeIdWorker.nextId();
        String parsedInfo = snowflakeIdWorker.parseUID(uid);
        uidSet.add(uid);

        // Check UID is positive, and can be parsed
        Assert.assertTrue(uid > 0L);
        Assert.assertTrue(!StringUtils.isEmpty(parsedInfo));

        if (VERBOSE) {
            System.out.println(Thread.currentThread().getName() + " No." + index + " >>> " + parsedInfo);
        }
    }

    /**
     * Check UIDs are all unique
     */
    private void checkUniqueID(Set<Long> uidSet) {
        System.out.println(uidSet.size());
        Assert.assertEquals(SIZE, uidSet.size());
    }
}