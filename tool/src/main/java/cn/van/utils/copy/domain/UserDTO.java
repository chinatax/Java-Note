package cn.van.utils.copy.domain;

import lombok.Data;

import java.util.List;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: UserDTO
 *
 * @author: Van
 * Date:     2019-11-02 17:53
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@Data
public class UserDTO {
    private Integer id;
    private String userName;
    List<User> ids;
}
