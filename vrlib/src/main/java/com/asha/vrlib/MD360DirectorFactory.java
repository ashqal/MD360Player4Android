package com.asha.vrlib;

/**
 * Created by hzqiujiadi on 16/3/13.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360DirectorFactory {
    public static MD360Director createDirector(int index){
        switch (index){
            case 1:   return new MD360SecondDirector();
            default:  return new MD360Director();
        }
    }
}
