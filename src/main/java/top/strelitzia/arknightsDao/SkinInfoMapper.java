package top.strelitzia.arknightsDao;

import org.apache.ibatis.annotations.Param;
import top.strelitzia.model.SkinInfo;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/4/7 17:14
 **/
public interface SkinInfoMapper {
    List<SkinInfo> selectSkinByInfo(String Info);

    List<String> selectAllNames();

    Integer insertBySkinInfo(SkinInfo skinInfo);

    String selectSkinById(Integer id);

    List<Integer> selectBase64IsUrl();

    Integer updateBaseStrById(@Param("id") Integer id, @Param("skinBase64") String skinBase64);
}
