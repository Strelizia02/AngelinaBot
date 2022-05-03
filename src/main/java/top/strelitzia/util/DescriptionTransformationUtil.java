package top.strelitzia.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangzy
 * @Date 2020/12/22 14:38
 **/
public class DescriptionTransformationUtil {
    //描述转换类
    public static Integer ChangeStringToInt(String arg) {
        //中文描述转数字
        Map<String, Integer> cnToNum = new HashMap<>();
        cnToNum.put("专一", 1);
        cnToNum.put("专二", 2);
        cnToNum.put("专三", 3);
        cnToNum.put("精三", 3);
        cnToNum.put("精二", 2);
        cnToNum.put("精一", 1);
        cnToNum.put("一技能", 1);
        cnToNum.put("二技能", 2);
        cnToNum.put("三技能", 3);
        cnToNum.put("1", 1);
        cnToNum.put("2", 2);
        cnToNum.put("3", 3);
        return cnToNum.get(arg);
    }
}
