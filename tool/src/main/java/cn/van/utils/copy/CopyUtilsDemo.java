package cn.van.utils.copy;

import cn.van.utils.copy.domain.User;
import cn.van.utils.copy.domain.UserDO;
import cn.van.utils.copy.domain.UserDTO;
import cn.van.utils.copy.util.DataUtil;
import cn.van.utils.copy.util.MapStructUtil;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: UserConvertUtilsDemo
 *
 * @author: Van
 * Date:     2019-11-08 18:05
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
public class CopyUtilsDemo {

    /**
     * https://www.cnblogs.com/Johnson-lin/p/12123012.html
     * @param args
     */
    public static void main(String[] args) {
        List<User> list = new ArrayList();
        list.add(new User(1, "van"));
        list.add(new User(2, "zhang"));
        List<User> users = new ArrayList<>();
        BeanUtils.copyProperties(list, users);
        System.out.println(users.toString());

        List<UserDO> userList = ColaBeanUtils.copyListProperties(list, UserDO::new);
        System.out.println(userList.toString());
    }
}
