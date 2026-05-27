package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.user.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.user.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.user.UserUpdateRequest;
import com.yx.fridgebutler.vo.UserInfoVO;

/**
 * 用户服务接口。
 * <p>定义用户信息查询、更新、密码修改、头像更新等业务逻辑。</p>
 */
public interface UserService {

    /**
     * 获取当前登录用户的详细信息。
     *
     * @return 用户信息视图对象
     */
    UserInfoVO getUserInfo();

    /**
     * 更新当前登录用户的基本信息。
     *
     * @param request 用户信息更新请求参数
     */
    void updateUser(UserUpdateRequest request);

    /**
     * 修改当前登录用户的密码。
     *
     * @param request 密码修改请求参数
     */
    void changePassword(UserChangePasswordRequest request);

    /**
     * 更新当前登录用户的头像。
     *
     * @param request 头像更新请求参数
     */
    void updateAvatar(UserUpdateAvatarRequest request);

    /**
     * 标记当前登录用户的新手指引已完成。
     */
    void completeGuide();
}
