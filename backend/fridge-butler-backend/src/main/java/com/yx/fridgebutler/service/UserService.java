package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.UserUpdateRequest;
import com.yx.fridgebutler.vo.UserInfoVO;

public interface UserService {
    UserInfoVO getUserInfo();

    void updateUser(UserUpdateRequest request);

    void changePassword(UserChangePasswordRequest request);

    void updateAvatar(UserUpdateAvatarRequest request);
}
