package cn.van.utils.copy.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: UserDO
 *
 * @author: Van
 * Date:     2019-11-02 17:49
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@Data
public class User {

    private int id;
    private String userName;
    /**
     * 以下两个字段用户模拟自定义转换
     */
    private LocalDateTime gmtBroth;
    private BigDecimal balance;

    List ids;

    public User(Integer id, String userName) {
        this.id = id;
        this.userName = userName;
    }
    public User(Integer id, String userName, LocalDateTime gmtBroth, BigDecimal balance, List ids) {
        this.id = id;
        this.userName = userName;
        this.gmtBroth = gmtBroth;
        this.balance = balance;
        this.ids = ids;
    }
}
