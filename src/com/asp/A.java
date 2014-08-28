package com.asp;

public class A {
    protected static int s = 0;

    public A() {
        s++;
        System.out.println("s:  " + s);
    }
}