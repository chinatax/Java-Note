package cn.van.utils.copy;

import cn.van.utils.copy.domain.UserDO;
import cn.van.utils.copy.domain.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: UserConvertUtils
 *
 * @author: Van
 * Date:     2019-11-08 17:56
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@Mapper
public interface UserConvertUtils {
    UserConvertUtils INSTANCE = Mappers.getMapper(UserConvertUtils.class);

    /**
     * 类型转换
     *
     * @param userDO UserDO数据持久层类
     * @return 数据传输类
     */
    // @Mappings({
    //         // @Mapping(target = "userName", source = "userName")
    // })
    UserDTO doToDTO(UserDO userDO);

}
