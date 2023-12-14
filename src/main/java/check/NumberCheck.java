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
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author zhouao
 * @version : NumberCheck.java, v 0.1 2023年03月25日 09:47 zhouao Exp $
 */
public class NumberCheck {
    private static ExecutorService executor = Executors.newFixedThreadPool(48);
    private        AtomicLong      fileNum  = new AtomicLong(0);
    private        AtomicLong      dirNum   = new AtomicLong(0);
    private        AtomicLong      fileSize = new AtomicLong(0);

    private  class ScanFile implements Runnable { //通过实现Runnable接口来实现

        private File file;

        ScanFile(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            if (file.isDirectory()) {
                //这里将列出所有的文件夹
                System.out.println("WARN Dir=meta目录里面不能有子目录=>" + file.getAbsolutePath());
            } else {
                try {
                    dealMetaFile(file);
                } catch (IOException e) {
                    System.out.println("ERR analist file fail ==>" + file.getAbsolutePath() + "\n" + e.getMessage());
                }
            }
        }

        private void dealMetaFile(File f) throws IOException {
            FileInputStream in = new FileInputStream(f);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1 * 1024 * 1024);
            String str;
            Gson gson = new Gson();
            while ((str = reader.readLine()) != null) {
                MetaInfo meta = gson.fromJson(str, MetaInfo.class);
                if (meta.absPath.endsWith("/")) {
                    dirNum.incrementAndGet();
                } else {
                    fileSize.addAndGet(meta.length);
                    long count = fileNum.incrementAndGet();
                    if (count % 6000000 == 0) {
                        System.out.println(count);
                    }
                }
            }
            in.close();
        }
    }

    public void scanMetaList(File[] flist) throws InterruptedException {
        for (File f : flist) {
            Runnable scanAMetaFile = new ScanFile(f);
            executor.submit(scanAMetaFile);
        }

        executor.shutdown();
        executor.awaitTermination(600, TimeUnit.SECONDS);
        System.out.println("dirNum:" + dirNum);
        System.out.println("fileNum:" + fileNum);
        System.out.println("fileSize:" + fileSize);
    }

}