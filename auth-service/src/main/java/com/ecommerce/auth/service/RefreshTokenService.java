package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyAndGet(String token);

    RefreshToken rotate(RefreshToken oldToken);

    void revokeAllForUser(User user);
}
