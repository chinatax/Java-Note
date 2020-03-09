package cn.van.utils.copy;

import cn.van.utils.copy.domain.UserDO;
import cn.van.utils.copy.domain.UserDTO;
import cn.van.utils.copy.util.DataUtil;
import cn.van.utils.copy.util.MapStructUtil;
import org.springframework.beans.BeanUtils;

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
public class UserConvertUtilsDemo {
    public static void main(String[] args) {


        UserDTO userDTO = UserConvertUtils.INSTANCE.doToDTO(DataUtil.createData());
        System.out.println(userDTO);


        List<UserDO> list = DataUtil.createDataList(10000);
        List<UserDTO> dtos = new ArrayList<>();
        List<UserDTO> dtos1 = new ArrayList<>();
        List<UserDTO> dtos2 = new ArrayList<>();
        // 1
        Long startTime = System.currentTimeMillis();
        list.forEach(userDO -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(userDO, dto);
            dtos.add(dto);
        });
        // System.out.println(dtos.size());
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));
        // 2
        startTime = System.currentTimeMillis();
        list.forEach(userDO -> {
            UserDTO dto1 = new UserDTO();
            BeanCopyUtils.copy(userDO, dto1);
            // UserDTO dto1 = BeanCopyUtils.copy(userDO, UserDTO.class);
            dtos1.add(dto1);
        });
        System.out.println(dtos1.size());
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        List<UserDTO> dtos3 = MapStructUtil.INSTANCE.userDoToUserDto(list);

        // list.forEach(userDO -> {
        //     UserDTO dto2 = new UserDTO();
        //     UserConvertUtils.INSTANCE.doToDTO(userDO);
        //     dtos2.add(dto2);
        // });
        System.out.println(dtos3.get(0).getIds());
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

    }

}
