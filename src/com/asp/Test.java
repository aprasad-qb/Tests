package com.asp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Test {

    private static final class TyTest {
        public static <T> T get(T t) {
            T ret = t;
            return ret;
        }
    }

    public static void main(String[] args) throws ParseException {
        simple();

        classTest();
        dateFormat();
        switchTest("a");
        typeClassTest();
    }

    private static void classTest() {
//      B b = new B();

      String a = "a";
      a = get("a", "b", a);

      String b = "a1";
      b = get("a1", "b", b);

      System.out.println("R: " + a);
      System.out.println("R1: " + b);
    }

    private static String get(String a, String b, String c) {
        if (a.equals("a")) {
            return b;
        }
        return c;
    }

    private static void dateFormat() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        System.out.println(format.parse("20140714084639"));
    }

    private static void switchTest(String key) {
        switch (key) {
            case "a":
            case "b":
                if (key.equals("a")) {
                    System.out.println("In a");
                }
                System.out.println("In a|b");
                break;

            default:
                break;
        }
    }

    private static void typeClassTest() {
        List<String> l = new ArrayList<>();
        l.add("World");
        l.add("!!");
        Object s2 = l;
        System.out.println("Out: " + String.format("%s -- %s", "Hello", TyTest.get(s2)));
    }

    private static void simple() {
        String[] a = {"11", "22"};
        System.out.println("Format []: " + String.format("%s -- %s", a));
    }

}
