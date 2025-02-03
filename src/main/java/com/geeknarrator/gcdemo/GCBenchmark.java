package com.geeknarrator.gcdemo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.MemoryMXBean;

public class GCBenchmark {
  private static final int ALLOCATION_SIZE = 500_000;
  private static final int LARGE_OBJECT_SIZE = 1_000_000;
  private static final Runtime runtime = Runtime.getRuntime();
  private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
  private static long startTime;
  private static List<GarbageCollectorMXBean> gcBeans;
  private static Map<String, Long> initialGCCounts;
  private static Map<String, Long> initialGCTimes;

  public static void main(String[] args) throws Exception {
    printSystemInfo();
    initGCMetrics();
    runBenchmark();
    printGCMetrics();
  }

  private static void printSystemInfo() {
    System.out.println("Java version: " + System.getProperty("java.version"));
    System.out.println("GC Collector: " + getGCCollectorName());
    System.out.println("Initial Heap: " + formatSize(runtime.totalMemory()));
    System.out.println("Max Heap: " + formatSize(runtime.maxMemory()));
    System.out.println("\nStarting benchmark...\n");
  }

  private static void initGCMetrics() {
    gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    initialGCCounts = new HashMap<>();
    initialGCTimes = new HashMap<>();

    for (GarbageCollectorMXBean gcBean : gcBeans) {
      initialGCCounts.put(gcBean.getName(), gcBean.getCollectionCount());
      initialGCTimes.put(gcBean.getName(), gcBean.getCollectionTime());
    }
    startTime = System.nanoTime();
  }

  private static void runBenchmark() throws Exception {
    try {
      // Phase 1: Small objects
      System.out.println("Phase 1: Allocating many small objects...");
      allocateSmallObjects();
      System.gc();
      Thread.sleep(1000);
      printMemoryStatus("After Phase 1");

      // Phase 2: Large objects
      System.out.println("\nPhase 2: Allocating large objects...");
      allocateLargeObjects();
      System.gc();
      Thread.sleep(1000);
      printMemoryStatus("After Phase 2");

      // Phase 3: Mixed allocation
      System.out.println("\nPhase 3: Mixed allocation with memory pressure...");
      mixedAllocation();
      System.gc();
      printMemoryStatus("After Phase 3");
    } catch (OutOfMemoryError e) {
      System.out.println("\nOOM occurred. Current memory status:");
      printMemoryStatus("At OOM");
      throw e;
    }
  }

  private static void allocateSmallObjects() {
    List<byte[]> objects = new ArrayList<>();
    for (int i = 0; i < ALLOCATION_SIZE; i++) {
      if (shouldPerformGC()) {
        objects.clear();
        System.gc();
      }
      objects.add(new byte[1024]); // 1KB objects
      if (i % 5000 == 0) {
        objects.subList(0, objects.size() / 2).clear();
        System.out.println("Allocated " + i + " small objects");
        printMemoryStatus("Small Objects Progress");
      }
    }
  }

  private static void allocateLargeObjects() {
    List<byte[]> largeObjects = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      if (shouldPerformGC()) {
        largeObjects.clear();
        System.gc();
      }
      largeObjects.add(new byte[LARGE_OBJECT_SIZE]); // 1MB objects
      System.out.println("Allocated large object " + (i + 1));
      if (i % 2 == 0) {
        largeObjects.remove(0);
      }
      printMemoryStatus("Large Object Allocation");
    }
  }

  private static void mixedAllocation() {
    List<Object> objects = new ArrayList<>();
    Random random = new Random();

    for (int i = 0; i < ALLOCATION_SIZE / 2; i++) {
      if (shouldPerformGC()) {
        objects.clear();
        System.gc();
      }

      if (random.nextDouble() < 0.9) {
        objects.add(new byte[512]);
      } else {
        objects.add(new byte[102400]);
      }

      if (i % 5000 == 0) {
        objects.subList(0, objects.size() / 2).clear();
        System.out.println("Mixed allocation iteration: " + i);
        printMemoryStatus("Mixed Allocation Progress");
      }
    }
  }

  private static boolean shouldPerformGC() {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    double usedRatio = (double) heapUsage.getUsed() / heapUsage.getMax();
    return usedRatio > 0.7; // Trigger GC if heap is 70% full
  }

  private static void printMemoryStatus(String phase) {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    System.out.printf("\n[%s] Memory Status:\n", phase);
    System.out.printf("Used: %s | Committed: %s | Max: %s\n",
        formatSize(heapUsage.getUsed()),
        formatSize(heapUsage.getCommitted()),
        formatSize(heapUsage.getMax())
    );
  }

  private static void printGCMetrics() {
    long duration = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
    System.out.println("\nBenchmark completed in " + duration + " seconds");
    System.out.println("\nGC Statistics:");

    for (GarbageCollectorMXBean gcBean : gcBeans) {
      long collections = gcBean.getCollectionCount() - initialGCCounts.get(gcBean.getName());
      long totalTime = gcBean.getCollectionTime() - initialGCTimes.get(gcBean.getName());

      System.out.println("\nCollector: " + gcBean.getName());
      System.out.println("Collection count: " + collections);
      System.out.println("Total collection time (ms): " + totalTime);
      if (collections > 0) {
        System.out.println("Average collection time (ms): " + (totalTime / collections));
      }
    }

    printMemoryStatus("Final Status");
  }

  private static String formatSize(long bytes) {
    return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
  }

  private static String getGCCollectorName() {
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    return gcBeans.stream()
        .map(GarbageCollectorMXBean::getName)
        .reduce((a, b) -> a + ", " + b)
        .orElse("Unknown");
  }
}