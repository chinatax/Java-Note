package cn.van.utils.copy.util;

import cn.van.utils.copy.domain.User;
import cn.van.utils.copy.domain.UserDO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: DataUtil
 *
 * @author: Van
 * Date:     2019-11-08 18:10
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
public class DataUtil {

    /**
     * 模拟查询出一条数据
     *
     * @return
     */
    public static UserDO createData() {
        List<User> list = new ArrayList();
        list.add(new User(1,"van"));
        list.add(new User(2,"zhang"));
        return new UserDO(1, "Van", LocalDateTime.now(), new BigDecimal(100L), list);
    }

    /**
     * 模拟查询出多条数据
     * @param num 数量
     * @return
     */
    public static List<UserDO> createDataList(int num) {
        List<User> list = new ArrayList();
        list.add(new User(1,"van"));
        list.add(new User(2,"zhang"));
        List<UserDO> userDOS = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            UserDO userDO = new UserDO(i+1, "Van", LocalDateTime.now(),new BigDecimal(100L),list);
            userDOS.add(userDO);
        }
        return userDOS;
    }
}
