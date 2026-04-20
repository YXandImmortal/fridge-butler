package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.UpdateRequest;
import com.yx.fridgebutler.dto.UserInfoDTO;

public interface UserService {
    UserInfoDTO getUserInfo();

    void updateUser(UpdateRequest request);
}
