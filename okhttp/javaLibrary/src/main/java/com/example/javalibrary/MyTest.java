package com.example.javalibrary;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MyTest {
    private static volatile MyTest instance ;
    private MyTest(){}
    public static MyTest getInstance(){
        if(instance == null)
            synchronized (MyTest.class){
                if(instance == null){
                    instance = new MyTest();
                }
            }
        return instance;
    }

    public void show(){
        System.out.println("fffff");
    }

    public void showFile(String path) {
        File file = new File(path);
        if (file.isFile()) {
            return;
        }

        List<File> list = new ArrayList();
        list.add(file);
        while (list.size() > 0) {
            File ll[] = list.remove(0).listFiles();
            for (File f : ll) {
                if (f.isFile()) {
                    System.out.println("" + f);
                } else if (f.isDirectory()) {
                    list.add(f);
                }
            }
        }
    }

    public static void main(String[] args) {
 //       ConcurrentHashMap<Integer,String> map =  new ConcurrentHashMap<>();
 //       List<File> files = new ArrayList<>();
        MyTest myTest = MyTest.getInstance();
        myTest.show();
        myTest.showFile("d:" + File.separator  + "19_develop_tool"+ File.separator  + "test");

    }
}
