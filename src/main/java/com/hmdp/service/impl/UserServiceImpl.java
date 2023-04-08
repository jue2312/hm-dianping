package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            // 如果不符合则返回错误信息
            return Result.fail("手机格式有误");
        }
        // 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存到session中
        session.setAttribute("code",code);
        // 发送验证码到控制台
        log.debug("验证码为："+code);
        // TODO 发送短信验证码并保存验证码
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机格式有误");
        }
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || ! cacheCode.toString().equals(code)){
            return Result.fail("验证码不一样");
        }
        //4、一致，根据手机号查询用户
        User user =query().eq("phone",phone).one();
        //5、判断用户是否存在
        if (user==null){
            //6、不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }
        //7、保存用户信息到session中
        session.setAttribute("user",user);
        // TODO 实现登录功能
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user=new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(6));
        // 保存到数据库中
        save(user);
        return user;
    }

    @Override
    public Result sign() {
        return null;
    }

    @Override
    public Result signCount() {
        return null;
    }
}
