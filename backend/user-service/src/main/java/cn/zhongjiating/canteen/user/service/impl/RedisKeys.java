package cn.zhongjiating.canteen.user.service.impl;

final class RedisKeys {

    private RedisKeys() {
    }

    static String sms(String phone) {
        return "sms:" + phone;
    }

    static String smsLimit(String phone) {
        return "sms:limit:" + phone;
    }

    static String tokenRefresh(Long userId) {
        return "token:refresh:" + userId;
    }

    static String tokenBlacklist(String jti) {
        return "token:blacklist:" + jti;
    }

    static String payLock(Long userId) {
        return "pay_lock:" + userId;
    }

    static String payError(Long userId) {
        return "pay_error:" + userId;
    }
}
