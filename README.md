## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)


## 
工具用于辅助进行OSS-HDFS的元数据转换的校验

## 原理
 通过转换过程产生的元数据列表，与最终的OSS-HDFS中的文件进行对比来确认转换的完整性。


## 使用步骤：
1 申请一个内存较大的ECS，编译本工程(checkTool-1.0.jar)，上传到用于执行的ECS   2 客户为校验者授权，允许访问桶内的oss-hdfs转换目录（获取oss文件列表）

3 客户通过OSS-HDFS的inventory指令生成oss-hdfs的文件列表，并给与校验者，这里的列表生成出来是一个oss路径，给校验者授权访问即可 （获取hdfs文件列表）

3 客户通过OSS-HDFS的inventory指令生成oss-hdfs的文件列表，并给与校验者，这里的列表生成出来是一个oss路径，给校验者授权访问即可 （获取hdfs文件列表）

4 转换完成后，客户通过OSS-HDFS的inventory指令生成oss-hdfs的文件列表，并给与校验者，这里的列表生成出来是一个oss路径，给校验者授权访问即可 （获取hdfs文件列表）

4 执行校验命令
 java -jar checkTool-1.0.jar lightCheck   bucket01 /ossmetadir  /jindoInventoryFilePath   /transferdir
 参数一：校验方式  固定值 lightCheck
 参数二：桶名  bucket01
 参数三：OSS元数据路径 /ossmetadir ，上面step2授权的路径
 参数四：jdfs文件列表 /jindoInventoryFilePath  ，上面step 3 产生的文件（直接产生的为zip文件，需要解压）
 参数四：转换的目录 /transferdir

  
