package com.b2c.cn.user.admin.service.chain;

import com.b2c.cn.starter.chain.ChainFilterAbstractDefine;
import com.b2c.cn.starter.exception.ClientException;
import com.b2c.cn.user.admin.common.enums.UserAdminCreateMarkEnum;
import com.b2c.cn.user.admin.dto.req.UserAdminCreateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zrq
 * 2026/2/23 9:47
 */
@Slf4j
@Component
public class UserAdminCreateRegChainFilter implements ChainFilterAbstractDefine<UserAdminCreateReqDTO> {
    @Override
    public void handler(UserAdminCreateReqDTO requestParam) {
        log.info("管理员用户创建参数校验");
        if (requestParam.getAdministratorLevel() == 0 && requestParam.getShopNumber() != 0) {
            log.error("平台管理员不能指定店铺");
            throw new ClientException("平台管理员不能指定店铺");
        } else if (requestParam.getAdministratorLevel() == 1 && requestParam.getShopNumber() == null) {
            log.error("店铺管理员必须指定店铺");
            throw new ClientException("店铺管理员必须指定店铺");
        } else if (requestParam.getAdministratorLevel() != 0 && requestParam.getAdministratorLevel() != 1) {
            log.error("管理员等级不合规");
            throw new ClientException("管理员等级不合规");
        }
        log.info("管理员用户创建参数校验通过");
    }

    @Override
    public String mark() {
        return UserAdminCreateMarkEnum.USER_ADMIN_CREATE_MASK.name();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
