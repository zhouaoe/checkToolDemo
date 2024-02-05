package check;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.Credentials;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author zhouao
 * @version : RunCmd.java, v 0.1 2023年03月21日 17:35 zhouao Exp $
 */
public class RunCmd {
    private static String OSS_TEST_ACCESS_KEY_ID     = "";
    private static String OSS_TEST_ACCESS_KEY_SECRET = "";
    //private static String OSS_TEST_ENDPOINT          = "";
    private static String OSS_TEST_ENDPOINT          = "cn-beijing.oss-dls.aliyuncs.com";


    private static String              bucketName = "dls-hangzhou-r-test";
    private static ClientConfiguration conf;
    private static Credentials         credentials;
    private static OSSClient           ossClient;
    private static String      logDir     = "mylog";
    private static String              role       = "AliyunOssRole";
    private static AtomicLong          fileNum    = new AtomicLong(0);
    private static AtomicLong          dirNum     = new AtomicLong(0);
    private static AtomicLong          fileSize   = new AtomicLong(0);
    ExecutorService executor = Executors.newFixedThreadPool(40);

    public static void main(String[] args) throws IOException, InterruptedException {
        printMsg("action size=" + args.length);

        String action = args[0];
        printMsg("action=" + action);

        bucketName = args[1];
        printMsg("bucketName=" + bucketName);

         if ("numberCheck".equalsIgnoreCase(action)) {
            String path = args[2];
            printMsg("meta list path=" + path);
            File file = new File(path);
            numberCheck(file);
        } else if ("analyseMeta".equalsIgnoreCase(action)) {
            String path = args[2];
            printMsg("meta list path=" + path);
            File file = new File(path);
            analyseMeta(file);
        }
        else if ("lightCheck".equalsIgnoreCase(action)) {
            String metaDirPath = args[2];
            printMsg("meta list path=" + metaDirPath);
            String jindoInventoryFilePath = args[3];
            printMsg("jindoInventoryFilePath  path=" + jindoInventoryFilePath);

            String targetPrefix=args[4];
            printMsg("targetPrefix  path=" + targetPrefix);

            File metaDir = new File(metaDirPath);
            File jindoInventoryFile = new File(jindoInventoryFilePath);
            lightCheck(metaDir, jindoInventoryFile,targetPrefix);

        } else if ("repair".equalsIgnoreCase(action)) {
            String repiarFilePath = args[2];
            printMsg("repiarFilePath=" + repiarFilePath);

            File repiarFile = new File(repiarFilePath);
            repiarFile(repiarFile);
        } else {
            printMsg("do nothing");
        }

    }

    private static void analyseMeta(File file) {

    }

    private static void repiarFile(File repiarFile) throws IOException, InterruptedException {
        Repair repair =new Repair(bucketName);
        repair.loadRepairFile(repiarFile);
        repair.repair();
    }


    private static void lightCheck(File metaDir, File jindoInventoryFile,String targetPrefix) throws IOException, InterruptedException {
        //getDirectory(file);
        File flist[] = metaDir.listFiles();
        printMsg("metaDir szie=" + flist.length);
        LightCheck lightCheck = new LightCheck(targetPrefix);
        lightCheck.loadFile(jindoInventoryFile);
        printMsg("load jindo success =");
        lightCheck.scanMetaList(flist);
        printMsg("check end ");

    }

    private static void numberCheck(File file) throws IOException, InterruptedException {
        //getDirectory(file);
        File flist[] = file.listFiles();
        printMsg("flist szie=" + flist.length);
        NumberCheck numberCheck = new NumberCheck();
        numberCheck.scanMetaList(flist);
    }

    //private static void getDirectory(File file) throws IOException {
    //    File flist[] = file.listFiles();
    //    printMsg("flist szie=" + flist.length);
    //
    //    if (flist == null || flist.length == 0) {
    //        printMsg("getDirectory res is empty!");
    //        return;
    //    }
    //    int i = 0;
    //    for (File f : flist) {
    //        if (f.isDirectory()) {
    //            //这里将列出所有的文件夹
    //            printMsg("Dir==>" + f.getAbsolutePath());
    //            getDirectory(f);
    //        } else {
    //            dealMetaFile(f);
    //            //printMsg("file==>" + f.getAbsolutePath());
    //            i++;
    //            if (i % 10000 == 0) {
    //                printMsg(i);
    //            }
    //        }
    //    }
    //    printMsg(i);
    //    printMsg("dirNum:" + dirNum);
    //    printMsg("fileNum:" + fileNum);
    //    printMsg("fileSize:" + fileSize);
    //
    //}

    public static void printMsg(Object s) {
        System.out.println(getNowTime() + s);
    }

    private static void dealMetaFile(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1 * 1024 * 1024);
        String str;
        Gson gson = new Gson();
        while ((str = reader.readLine()) != null) {
            MetaInfo meta = gson.fromJson(str, MetaInfo.class);
            if (meta.absPath.endsWith("/")) {
                dirNum.incrementAndGet();
            } else {
                fileNum.incrementAndGet();
                fileSize.addAndGet(meta.length);
            }
        }
    }

    private static String getNowTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}