package cn.zhongjiating.canteen.order.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;

public final class RemoteResultErrors {

    private RemoteResultErrors() {
    }

    public static BusinessException toBusinessException(Result<?> result, String defaultMessage) {
        String message = result == null || result.getMessage() == null || result.getMessage().isBlank()
                ? defaultMessage : result.getMessage();
        Integer code = result == null ? null : result.getCode();
        return new BusinessException(errorCode(code, message), message);
    }

    public static ErrorCode errorCode(Integer code, String message) {
        if (ErrorCode.INVALID_STOCK.getMessage().equals(message)) {
            return ErrorCode.INVALID_STOCK;
        }
        if (code == null) {
            return ErrorCode.INTERNAL_ERROR;
        }
        return switch (code) {
            case 400 -> ErrorCode.BAD_REQUEST;
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 429 -> ErrorCode.RATE_LIMITED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
