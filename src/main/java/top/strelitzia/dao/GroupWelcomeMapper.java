package top.strelitzia.dao;

public interface GroupWelcomeMapper {
    String getWelcomeMessage(Long groupId);

    Integer getPictureNum(Long groupId);

    Integer insertWelcomeMessage(Long groupId,String welcomeMessage);

    Integer insertPictureNum(Long groupId,Integer pictureNum);

    Integer deleteWelcomeMessage(Long groupId);
}