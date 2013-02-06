package com.sohu.cache.common;

import org.junit.Assert;
import org.junit.Test;

import com.sohu.cache.common.JsonSerializer;
import com.sohu.cache.common.Serializer;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

/**
 * User: xiaobinghan
 * Date: 12-5-4
 * Time: ����10:28
 */
public class JsonSerializerTest {
    Serializer serializer = new JsonSerializer();
    User user = new User("name-1", "f", 12, "abcdef");

    @Test
    public void testToString() throws Exception {
        String serial = serializer.toString(user);
        for (Method method : User.class.getMethods()) {
            if (method.getName().startsWith("get") && !"getClass".equals(method.getName())) {
                Assert.assertTrue(serial.contains(method.invoke(this.user).toString()));
            }
        }
    }

    @Test
    public void testToObject() throws Exception {
        String serial = serializer.toString(user);
        User user = (User) serializer.toObject(serial);
        for (Method method : User.class.getMethods()) {
            if (method.getName().startsWith("get") && !"getClass".equals(method.getName())) {
                Assert.assertEquals(method.invoke(user), method.invoke(this.user));
            }
        }

        Assert.assertNull(serializer.toString(null));
        Assert.assertNull(serializer.toObject(""));
        Assert.assertNull(serializer.toObject(null));
    }

    @Test
    public void testParse() throws Exception {

        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        String string = "java.lang.String@@\"df1126_2001_2001@chinaren.com|r--x@sohu.com|lilyzhang566@sohu.com|yong_an@sohu.com|\"";
        serializer.toObject(string);
        int i = 0, n = 10000;
        long s = System.nanoTime();
        while (i++ < n) {
            serializer.toObject(string);
        }
        long e = System.nanoTime();
        System.out.println("spends " + decimalFormat.format(e - s) + " ns. average " + decimalFormat.format((e - s)/n) + " ns.");
    }
}
