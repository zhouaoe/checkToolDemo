/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package check;

import com.aliyun.oss.common.utils.StringUtils;

/**
 *
 * @author zhouao
 * @version : MetaInfo.java, v 0.1 2023年03月22日 20:03 zhouao Exp $
 */
public class JindoInfo {
    /*
  {"id":5601892537830295854,"path":"/hive_warehouse/xxxx.db/
  .hive-staging_hive_2022-05-27_11-57-54_805_4059608613340702202-5825/_task_tmp.-ext-10004","type":"directory","size":0,"user":"admin",
  "group":"supergroup","atime":0,"mtime":1679556665595,"permission":493,"state":0}
  {"id":1437751732396600980,"path":"/hive_warehouse/xxxx.db/
  .hive-staging_hive_2022-05-27_11-57-54_805_4059608613340702202-5825/_task_tmp.-ext-10004/_tmp.000016_0","type":"file","size":4096,
  "user":"1877924005707519","group":"admin","atime":1679408047000,"mtime":1679408047000,"permission":493,"state":0}
     */
    public String path;

    public String type;

    public Long size;

    private static String dirSuffix = "/";

    public boolean isDir() {
        if (StringUtils.compare(this.type, "directory") == 0) {
            return true;
        }
        return false;
    }

    public String getAbsPath() {
        if (isDir()) {
            return path + dirSuffix;
        } else {
            return path;
        }
    }

    public boolean matchPrefix(String targetPrefix) {
        //System.out.println("path:"+path +"  targetPrefix: "+ targetPrefix +"  res: "+path.startsWith(targetPrefix));
        return path.startsWith(targetPrefix);
    }
}