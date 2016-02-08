package com.skronawi.askthedice;

import java.util.Arrays;
import java.util.List;

public class DefaultDiceSideProvider {

    private static Integer[] sides = new Integer[]{R.string.yes, R.string.no, R.string.maybe,
        R.string.tomorrow, R.string.take_a_break, R.string.have_a_coffe, R.string.think_about_it,
        R.string.eat_a_cookie};

    public static Integer get(int index){
        return sides[index];
    }

    public static int numberOfSides(){
        return sides.length;
    }

    public static List<Integer> getSides(){
        return Arrays.asList(sides);
    }
}
