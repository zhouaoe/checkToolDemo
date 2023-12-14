/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package check;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author zhouao
 * @version : HDFSResult.java, v 0.1 2023年03月25日 10:36 zhouao Exp $
 */
public class HDFSResult {
    private AtomicLong fileNum  = new AtomicLong(0);
    private     AtomicLong dirNum   = new AtomicLong(0);
    private     AtomicLong fileSize = new AtomicLong(0);

   Set<String> hdfsList = new HashSet(100);

}