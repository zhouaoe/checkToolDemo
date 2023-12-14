/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package check;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zhouao
 * @version : CMDUtil.java, v 0.1 2023年03月25日 16:36 zhouao Exp $
 */
public class Repair {

    private final  String          bucket;
    private        Set<String>     repairSet = new HashSet<String>();
    private static ExecutorService executor  = Executors.newFixedThreadPool(64);

    public void executeLinuxCmd(String cmd) {
        System.out.println("got cmd job : " + cmd);
        Runtime run = Runtime.getRuntime();
        try {
            System.out.println("1=");

            Process process = run.exec(cmd);
            InputStream in = process.getInputStream();
            BufferedReader bs = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = bs.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("2=");

            int exitValue = process.exitValue();
            System.out.println("exitValue=" + exitValue);
            System.out.println("3=");

            process.destroy();
            if (exitValue == 0) {
                System.out.println("cmd success");
            } else {
                System.out.println("cmd fail");
            }
            return;
        } catch (IOException e) {
            System.err.println("excecut cmd fail!");
            e.printStackTrace();
        }
        return;
    }

    public Repair(String bucket) {
        this.bucket = bucket;
    }

    public void loadRepairFile(File repairFIle) throws IOException {
        FileInputStream in = new FileInputStream(repairFIle);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 10 * 1024 * 1024);
        String str;
        Gson gson = new Gson();
        while ((str = reader.readLine()) != null) {
            repairSet.add(str);
        }

        System.out.println("repair file size" + repairSet.size());
    }

    public void repair() throws InterruptedException {
        for (String f : repairSet) {
            Runnable compare = new RepairFile(bucket, f);
            executor.submit(compare);
        }

        executor.shutdown();
        executor.awaitTermination(600, TimeUnit.SECONDS);
        System.out.println("repair end");
    }

    private class RepairFile implements Runnable {
        private String file;
        private String bucket;

        public RepairFile(String bucket, String file) {
            this.bucket = bucket;
            this.file = file;
        }

        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            sb.append("sh /home/script/createAJindoFile.sh").append(" ").append(bucket).append(" ").append(file);
            executeLinuxCmd(sb.toString());
        }
    }
}