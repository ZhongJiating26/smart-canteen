package cn.zhongjiating.canteen.common.exception;

import cn.zhongjiating.canteen.common.enums.ErrorCode;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(String message) {
        this(ErrorCode.BAD_REQUEST, message);
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
