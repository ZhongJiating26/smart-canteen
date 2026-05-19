package cn.zhongjiating.canteen.common.exception;

import cn.zhongjiating.canteen.common.enums.ErrorCode;

public class DuplicateSubmissionException extends BusinessException {

    public DuplicateSubmissionException() {
        super(ErrorCode.DUPLICATE_SUBMIT);
    }

    public DuplicateSubmissionException(String message) {
        super(ErrorCode.DUPLICATE_SUBMIT, message);
    }
}
