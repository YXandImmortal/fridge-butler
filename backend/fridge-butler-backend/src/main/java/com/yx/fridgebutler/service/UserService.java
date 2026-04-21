package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.ChangePasswordRequest;
import com.yx.fridgebutler.dto.UpdateAvatarRequest;
import com.yx.fridgebutler.dto.UpdateRequest;
import com.yx.fridgebutler.dto.UserInfoDTO;

public interface UserService {
    UserInfoDTO getUserInfo();

    void updateUser(UpdateRequest request);

    void changePassword(ChangePasswordRequest request);

    void updateAvatar(UpdateAvatarRequest request);
}
