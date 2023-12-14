/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package check;

import java.util.Objects;

/**
 *
 * @author zhouao
 * @version : JindodataPair.java, v 0.1 2023年04月01日 11:57 zhouao Exp $
 */
public class JindodataPair {
    public String  path;
    public boolean isConver;
    public boolean isDir;

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        JindodataPair that = (JindodataPair) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}