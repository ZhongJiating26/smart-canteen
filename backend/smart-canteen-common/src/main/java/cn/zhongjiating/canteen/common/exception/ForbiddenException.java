package cn.zhongjiating.canteen.common.exception;

import cn.zhongjiating.canteen.common.enums.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
