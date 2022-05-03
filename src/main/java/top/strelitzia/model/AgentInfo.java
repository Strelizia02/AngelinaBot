package top.strelitzia.model;

import java.io.Serializable;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 * 干员信息
 **/
public class AgentInfo implements Serializable {
    private String name;
    private Integer star;
    private String pool;
    /**
     * 0->非限定
     * 1->周年限定
     * 2->联动限定
     * 3->五倍权值
     * 4->新年限定
     */
    private Integer limit;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }
}
