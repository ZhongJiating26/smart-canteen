package cn.zhongjiating.canteen.common.exception;

import cn.zhongjiating.canteen.common.enums.ErrorCode;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
