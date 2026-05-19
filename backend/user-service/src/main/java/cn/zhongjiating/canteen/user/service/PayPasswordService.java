package cn.zhongjiating.canteen.user.service;

public interface PayPasswordService {

    boolean verify(Long userId, String payPassword);
}
