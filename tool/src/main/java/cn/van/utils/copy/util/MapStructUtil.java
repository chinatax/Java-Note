package cn.van.utils.copy.util;

import cn.van.utils.copy.domain.UserDO;
import cn.van.utils.copy.domain.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Copyright (C), 2015-2019, 风尘博客
 * 公众号 : 风尘博客
 * FileName: MapStructUtil
 *
 * @author: Van
 * Date:     2019-11-08 19:07
 * Description: ${DESCRIPTION}
 * Version： V1.0
 */
@Mapper
public interface MapStructUtil {

    /**
     * 实例
     */
    MapStructUtil INSTANCE = Mappers.getMapper(MapStructUtil.class);


    /**
     * 具体方法
     * @param students
     * @return
     */
    List<UserDTO> userDoToUserDto(List<UserDO> students);

}
