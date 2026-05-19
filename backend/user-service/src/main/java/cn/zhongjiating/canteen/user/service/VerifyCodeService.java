package cn.zhongjiating.canteen.user.service;

import cn.zhongjiating.canteen.user.vo.SendCodeVO;

public interface VerifyCodeService {

    SendCodeVO sendCode(String phone);

    void verify(String phone, String verifyCode);
}
