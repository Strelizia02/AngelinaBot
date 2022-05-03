package top.strelitzia.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class TagsUtil {

    public static boolean isHave(List<String> str, String s) {
        int i = str.size();
        while (i-- > 0) {
            if (str.get(i).equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 利用二进制进行排列组合，很巧妙很牛逼的方法，也不需要递归
     * 比如{a,b,c}三元数组，组合结果有8种
     * 可以把abc看做一个3位二进制数，比如011，0代表数组内没有有该字符，1代表有
     * 011代表组合{b,c}，101代表{a,c}以此类推，3位二进制的8中组合正好对应三元数组的8种排列组合
     * [l>>>i&1]，[>>>]代表编码右移，也就是循环查询二进制每位的数值是1还是0，是1就把他add到数组里
     * [010]>>>0&[001] -> [010]&[001]->[000]->0，计算结果就是编码第0位是0
     * [010]>>>1&[001] -> [001]&[001]->[001]->1，计算结果就是编码第1位是1
     */
    public static List<List<String>> getAllCompose(List<String> list) {
        List<List<String>> result = new ArrayList<>();
        long n = (long) Math.pow(2, list.size());
        List<String> combine;
        for (long l = 0L; l < n; l++) {
            combine = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if ((l >>> i & 1) == 1)
                    combine.add(list.get(i));
            }
            result.add(combine);
        }
        return result;
    }

}
