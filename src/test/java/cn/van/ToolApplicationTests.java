package cn.van;

import cn.van.entity.UserEntity;
import cn.van.utils.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ToolApplicationTests {

    @Test
    public void contextLoads() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1l);
        userEntity.setName("van");
        userEntity.setMobile("110");
        String str = JsonUtil.objectToJson(userEntity);
        System.out.println(str);
        UserEntity object = JsonUtil.jsonToPojo(str, UserEntity.class);
//        System.out.println(object.toString());

        str = "["+ str+ "]";
        List<UserEntity> list = JsonUtil.jsonToList(str,UserEntity.class);
        System.out.println(list.size());
    }

}

