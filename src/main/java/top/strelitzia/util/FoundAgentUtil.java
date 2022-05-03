package top.strelitzia.util;

import java.util.Random;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class FoundAgentUtil {

    /**
     * 静态抽卡方法，传递一个qq和垫刀数，返回随机稀有度
     *
     * @param qq
     * @param num
     * @return
     */
    public static int FoundOneByMath(Long qq, int num) {

        double sixStar;
        //五星概率8%
        double fiveStar = 8;
        //四星概率50%
        double fourStar = 50;

        //如果垫刀数大于50，则每次抽卡增加2%六星概率
        if (num > 50) {
            sixStar = 2 + (num - 50) * 2;
        } else {
            //六星概率默认2%
            sixStar = 2;
        }

        int starNum;
        //使用系统时间和qq号求和作为种子，尽可能刨除时间序列导致的连续重复结果
        Random random = new Random(System.nanoTime() + System.currentTimeMillis() / qq);
        double set = random.nextDouble();
        if (set > 1 - sixStar / 100) {
            starNum = 6;
        } else if (set <= 1 - sixStar / 100 && set > 1 - sixStar / 100 - fiveStar / 100) {
            starNum = 5;
        } else if (set <= 1 - sixStar / 100 - fiveStar / 100 && set > 1 - sixStar / 100 - fiveStar / 100 - fourStar / 100) {
            starNum = 4;
        } else {
            //增加的六星概率是从三星中抠出来的
            starNum = 3;
        }
        return starNum;
    }

}
