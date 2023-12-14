/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package check;


import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author zhouao
 * @version : NumberCheck.java, v 0.1 2023年03月25日 09:47 zhouao Exp $
 */
public class LightCheck {
    private static ExecutorService executor = Executors.newFixedThreadPool(64);
    private final  String          targetPrefix;
    private        AtomicLong      fileNum  = new AtomicLong(0);
    private        AtomicLong      dirNum   = new AtomicLong(0);
    private        AtomicLong      fileSize = new AtomicLong(0);

    private AtomicLong jindoFileNum  = new AtomicLong(0);
    private AtomicLong jindoDirNum   = new AtomicLong(0);
    private AtomicLong jindoFileSize = new AtomicLong(0);

    private AtomicLong errDirNum  = new AtomicLong(0);
    private AtomicLong errFileNum = new AtomicLong(0);

    private List<String> diffDirList  = new Vector(10000);
    private List<String> diffFileList = new Vector(100000);

    //private List<String> jindoUniqueFileList = new Vector(100000);

    private String diffDirListFile  = "dir_diff.txt";
    private String diffFileListFile = "file_diff.txt";

    //2亿
    //private Set<String> pathSet = new HashSet<String>(200 * 1000 * 1000, 0.95f);
    //private Set<JindodataPair> pathSet = new HashSet<JindodataPair>(200 * 1000 * 1000, 0.95f);

    private Map<String, Boolean> pathSet = new HashMap<String, Boolean>(200 * 1000 * 1000, 0.95f);

    public LightCheck(String targetPrefix) {
        this.targetPrefix = targetPrefix;
    }

    private class CompareFile implements Runnable { //通过实现Runnable接口来实现

        private File file;

        CompareFile(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            if (file.isDirectory()) {
                //meta目录里面不能有子目录
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
                    long dirCount = dirNum.incrementAndGet();
                    if (dirCount % 100000 == 0) {
                        System.out.println("dir compare " + dirCount);
                    }

                    if (!pathSet.containsKey(meta.absPath)) {
                        long dirDiff = errDirNum.incrementAndGet();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Diff Dir Path:").append("|").append(dirDiff).append("|").append(meta.absPath);
                        System.out.println(sb);
                        diffDirList.add(meta.absPath);
                    }
                } else {
                    fileSize.addAndGet(meta.length);
                    long count = fileNum.incrementAndGet();
                    if (count % 10000000 == 0) {
                        System.out.println("file compare" + count);
                        //System.out.println(meta.absPath);
                    }

                    if (!pathSet.containsKey(meta.absPath)) {
                        long dirDiff = errFileNum.incrementAndGet();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Error File Path:").append("|").append(dirDiff).append("|").append(meta.absPath);
                        System.out.println(sb);
                        diffFileList.add(meta.absPath);
                    } else {
                        pathSet.put(meta.absPath, true);
                    }
                }
            }
            in.close();
        }
    }

    public void loadFile(File file) throws IOException {
        if (file.isDirectory()) {
            throw new IOException("jido file is dir");
        }
        System.out.println("load  :" + file.getAbsolutePath());

        FileInputStream in = new FileInputStream(file);
        //1G
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1 * 1024 * 1024 * 1024);
        String str;
        Gson gson = new Gson();
        long i = 0;
        JSONObject jsonObject;

        while ((str = reader.readLine()) != null) {
            JindoInfo jindoFile = gson.fromJson(str, JindoInfo.class);

            if (!jindoFile.matchPrefix(targetPrefix)) {
                continue;
            }

            if (!jindoFile.isDir()) {
                jindoFileNum.incrementAndGet();
                jindoFileSize.addAndGet(jindoFile.size);
                pathSet.put(jindoFile.path, false);

            } else {
                jindoDirNum.incrementAndGet();
                pathSet.put(jindoFile.path + "/", true);
            }

            //jsonObject = JSONObject.parseObject(str);
            //String s1 = jsonObject.getString("path");
            //String type =  jsonObject.getString("type");
            //if (StringUtils.compare(type, "directory") == 0) {
            //    pathSet.add(s1+"/");
            //}
            //else
            //{
            //    pathSet.add(s1);
            //}
            //100W
            if ((i++ % 10000000) == 0) {
                RunCmd.printMsg(i);
            }

        }
        System.out.println("pathSet size:" + pathSet.size());
        //System.exit(0);
        //for( x : pathSet)
        //{
        //    System.out.println(x)
        //}

        System.out.println("jindodirNum:" + jindoDirNum);
        System.out.println("jindofileNum:" + jindoFileNum);
        System.out.println("jindofileSize:" + jindoFileSize);
    }

    public void scanMetaList(File[] flist) throws InterruptedException, IOException {
        //System.exit(1);
        for (File f : flist) {
            Runnable compare = new CompareFile(f);
            executor.submit(compare);
        }

        executor.shutdown();
        executor.awaitTermination(600, TimeUnit.SECONDS);
        System.out.println("dirNum:" + dirNum);
        System.out.println("fileNum:" + fileNum);
        System.out.println("fileSize:" + fileSize);
        System.out.println("difference Dir num " + diffDirList.size());
        System.out.println("difference File num " + diffFileList.size());

        for (Map.Entry<String, Boolean> entry : pathSet.entrySet()) {
            //System.out.println("key=" + entry.getKey() + "  value=" + entry.getValue());
            if (entry.getValue() == false) {
                System.out.println(entry.getKey());
            }
        }

        //saveRes();
    }

    private void saveRes() throws IOException {
        File file = new File(diffDirListFile);
        //if (!file.exists()) {
        //    file.createNewFile();
        //}
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fileWriter);
        for (String content : diffDirList) {
            bw.write(content);
        }

        bw.close();
        System.out.println("dir res finish");

        file = new File(diffFileListFile);
        //if (!file.exists()) {
        //    file.createNewFile();
        //}
        fileWriter = new FileWriter(file.getAbsoluteFile());
        bw = new BufferedWriter(fileWriter);
        for (String content : diffFileList) {
            bw.write(content);
            bw.write("\n");
        }

        bw.close();
        System.out.println("file res finish");

    }
}