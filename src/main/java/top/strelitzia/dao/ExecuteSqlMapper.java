package top.strelitzia.dao;

import java.util.List;
import java.util.Map;

/**
 * @author wangzy
 * @Date 2021/2/20 10:14
 **/
public interface ExecuteSqlMapper {
    List<Map> executeSql(String sql);
}
