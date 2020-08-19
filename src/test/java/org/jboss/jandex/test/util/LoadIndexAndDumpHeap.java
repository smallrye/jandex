package org.jboss.jandex.test.util;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class LoadIndexAndDumpHeap {
    public static void main(String[] args) throws IOException, JMException {
        if (args.length != 2) {
            printUsage();
            return;
        }

        File indexFile = new File(args[0]);

        long start = System.currentTimeMillis();
        Index index = new IndexReader(new FileInputStream(indexFile)).read();
        long time = System.currentTimeMillis() - start;

        dumpHeap(args[1], true);

        System.out.println("Reading " + index + " took " + time + "ms");
    }

    private static void printUsage() {
        System.out.println("Usage: LoadIndexAndDumpHeap <index file name> <heap dump file name>");
    }

    private static void dumpHeap(String filePath, boolean live) throws JMException {
        MBeanServer jmx = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        jmx.invoke(name, "dumpHeap", new Object[]{filePath, live},
                new String[]{String.class.getName(), boolean.class.getName()});
    }
}
