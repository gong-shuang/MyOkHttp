package com.example.javalibrary;

class Fruits{
    public String get(){
        return "vvv";
    }
}

class Apple extends Fruits{
    public String get(){
        return "HHH";
    }
}

public class MyClass {
    public static void main(String[] args) {
        System.out.println("hello!");
        Fruits fruits = new Apple();
        printGet(fruits);

    }

    public static void printGet(Fruits fruits){
        System.out.println("ddd:" + fruits.get());

    }

    public static void printGet(Apple apple){
        System.out.println("ggg:" + apple.get());

    }
}
