package com.hua.media;

import org.junit.Test;

/**
 * Created by hjz on 2017/11/28 0028.
 */

public class Shuffle {

    @Test
    public void shuffleTest() {
        PPoker a = new PPoker();
        System.out.println("请验牌************");
        a.getPokerPoint();
        System.out.println();
        System.out.println("洗牌中");
        a.shuffleOne();
        a.getPokerPoint();
        System.out.println();
        System.out.println("洗牌中");
        PPoker b = new PPoker();
        b.shuffleTwo();
        b.getPokerPoint();
        System.out.println();
        System.out.println("洗牌中");
        PPoker c = new PPoker();
        c.shufffeThree();
        c.getPokerPoint();
    }


}
