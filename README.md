## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)



被工具用于辅助进行OSS-HDFS的元数据转换的校验

原理：通过转换过程产生的元数据列表，与最终的OSS-HDFS中的文件进行对比来确认转换的完整性。


使用步骤：
1 申请一个内存较大的ECS，编译本工程，上传到用于执行的ECS
2 客户为校验者授权，允许访谈桶内的oss-hdfs转换目录（获取oss文件列表）
3 转换完成后，客户通过OSS-HDFS的inventory指令生成oss-hdfs的文件列表，并给与校验者，这里的列表生成出来是一个oss路径，给校验者授权访问即可 （获取hdfs文件列表）
4 执行校验命令
