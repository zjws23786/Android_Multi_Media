package com.hua.media;

/**
 * Created by hjz on 2017/11/28 0028.
 * 洗牌人，洗牌人实现洗牌具体操作
 */

public class PPoker {

    private Poker[] poker = new Poker[54];
    private String[] point;
    private String[] color;

    public PPoker() {
        point = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "Q", "k"};
        color = new String[]{"♥", "♣", "♦", "♠"};
        int k = 0;
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 4; j++) {
                poker[k] = new Poker(point[i], color[j]);
                k++;
            }
        }
        poker[52] = new Poker("JOKER", "小");
        poker[53] = new Poker("JOKER", "大");
    }

    public Poker[] getPokerPoint() {
        for (int i = 0; i < 54; i++) {
            System.out.print(poker[i].color + poker[i].point);
        }
        return poker.clone();
    }

    public void shuffleOne() {
        String[] temC = new String[54];
        String[] temP = new String[54];
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 54; i++) {
                temC[i] = poker[i].color;
                temP[i] = poker[i].point;
            }

            for (int i = 0; i < 27; i++) {
                poker[2 * i].color = temC[i + 27];
                poker[2 * i].point = temP[i + 27];
                poker[2 * i + 1].color = temC[i];
                poker[2 * i + 1].point = temP[i];
            }
        }
    }

    public void shuffleTwo() {
        for (int i = 0; i < 53; i++) {
            int t = (int) (Math.random() * (53 - i));
            Poker tem = poker[t];
            poker[t] = poker[53 - i];
            poker[53 - i] = tem;
        }

    }

    public void shufffeThree() {

        for (int i = 0; i < 54; i++) {
            int r = (int) (Math.random() * 54);
            if (poker[i] != poker[r]) {
                Poker temp = poker[i];
                poker[i] = poker[r];
                poker[r] = temp;
            }
        }
    }
}
