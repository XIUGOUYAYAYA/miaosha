package org.miaosha.miaosha.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.constant.RedisConstant;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserAddDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserPageQueryDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserResetPwdDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserUpdateDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserCancelAccountDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserLoginDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserRegisterDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserUpdatePasswordDTO;
import org.miaosha.miaosha.common.enums.UserStatusEnum;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.CommonErrorCodeEnum;
import org.miaosha.miaosha.common.exception.enums.UserCodeEnum;
import org.miaosha.miaosha.common.util.JwtUtil;
import org.miaosha.miaosha.common.vo.user.CaptchaVO;
import org.miaosha.miaosha.common.vo.user.admin.AdminUserPageVO;
import org.miaosha.miaosha.common.vo.user.orignal.DashboardStatVO;
import org.miaosha.miaosha.common.vo.user.orignal.UserInfoVO;
import org.miaosha.miaosha.common.vo.user.orignal.UserLoginVO;
import org.miaosha.miaosha.entity.User;
import org.miaosha.miaosha.mapper.GoodsMapper;
import org.miaosha.miaosha.mapper.SeckillGoodsMapper;
import org.miaosha.miaosha.mapper.SeckillOrderMapper;
import org.miaosha.miaosha.mapper.UserMapper;
import org.miaosha.miaosha.service.IUserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 负责验证码生成、用户注册登录、密码修改、账号注销；管理员后台用户管理、数据大盘统计
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    // 验证码相关使用这个，JSON序列化
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    // 登录token专用，纯字符串序列化，不会自动增加双引号
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtUtil jwtUtil;


    /**
     * 生成4位图形验证码，存入Redis
     * 业务规则：生成4位数字验证码，生成图片base64，存入Redis，有效期1分钟
     * @return CaptchaVO 包含验证码唯一key + 图片base64
     */
    @Override
    public CaptchaVO createCaptcha() {
        log.info("【生成图形验证码】");
        // 1.生成4位随机数字验证码
        char[] codes = {'0','1','2','3','4','5','6','7','8','9'};
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(codes[random.nextInt(codes.length)]);
        }
        String code = sb.toString();
        log.info("【生成图形验证码】验证码内容:{}", code);

        // 2.验证码唯一key
        String captchaKey = UUID.randomUUID().toString();
        String redisKey = "captcha:img:" + captchaKey;
        // 存入redis，1分钟过期
        redisTemplate.opsForValue().set(redisKey, code, 1, TimeUnit.MINUTES);

        // 3.生成图片
        BufferedImage image = new BufferedImage(100,40,BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,100,40);
        g.setColor(Color.BLACK);
        g.drawString(code,25,25);
        g.dispose();

        // 将图片转为base64字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            log.error("【图形验证码生成失败】", e);
            throw new BusinessException(UserCodeEnum.CAPTCHA_CREATE_FAIL);
        }
        String base64Img = Base64.getEncoder().encodeToString(baos.toByteArray());

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaKey(captchaKey);
        vo.setBase64Img(base64Img);
        return vo;
    }


    /**
     * 用户注册
     * 业务规则：校验图形验证码；校验账号、手机号唯一性；密码BCrypt加密保存
     * @param dto 注册入参：username账号名、phone手机号、password密码、captchaKey、captchaCode验证码
     */
    @Override
    public void register(UserRegisterDTO dto) {
        log.info("【用户注册】username:{}", dto.getUsername());
        // 校验图形验证码
        String redisKey = "captcha:img:" + dto.getCaptchaKey();
        String redisCode = (String) redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(redisCode) || !redisCode.equalsIgnoreCase(dto.getCaptchaCode())) {
            log.warn("【注册失败】验证码错误 username:{}", dto.getUsername());
            throw new BusinessException(UserCodeEnum.CAPTCHA_ERROR);
        }
        // 校验账号名唯一
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User existUser = this.getOne(wrapper);
        if(existUser != null){
            log.warn("【注册失败】用户名已存在 username:{}", dto.getUsername());
            throw new BusinessException(UserCodeEnum.USERNAME_EXIST);
        }
        // 校验手机号唯一
        wrapper.clear();
        wrapper.eq(User::getPhone, dto.getPhone());
        existUser = this.getOne(wrapper);
        if(existUser != null){
            log.warn("【注册失败】手机号已存在 phone:{}", dto.getPhone());
            throw new BusinessException(UserCodeEnum.PHONE_EXIST);
        }

        // 组装用户实体，密码加密
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserStatusEnum.ORDINARY_USER.getCode()); // 0普通用户
        user.setStatus(UserStatusEnum.USER_NORMAL.getCode()); // 1正常
        user.setAvatar("");
        user.setDeleted((byte)0);
        boolean saveResult = this.save(user);
        log.info("【用户注册】保存结果:{}, userId:{}", saveResult, user.getId());

        // 删除验证码，防止重复注册
        redisTemplate.delete(redisKey);
    }

    /**
     * 用户登录
     * 业务规则：校验账号状态、密码；生成JWT，存入Redis实现单端登录，新登录踢掉旧登录
     * @param dto 登录DTO：账号+密码
     * @return UserLoginVO token+用户基础信息
     */
    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        log.info("【用户登录】username:{}", dto.getUsername());
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = this.getOne(wrapper);
        if(user == null){
            log.warn("【登录失败】账号不存在 username:{}", dto.getUsername());
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }
        // 校验账号状态：status=0禁用拉黑 / status=2已注销 不能登录
        if(UserStatusEnum.USER_FORBIDDEN.getCode().equals(user.getStatus())
                || UserStatusEnum.USER_CANCEL.getCode().equals(user.getStatus())
                || user.getDeleted() ==1){
            log.warn("【登录失败】账号被禁用或已注销 username:{}", dto.getUsername());
            throw new BusinessException(UserCodeEnum.USER_FORBIDDEN);
        }
        // 校验密码
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            log.warn("【登录失败】密码错误 username:{}", dto.getUsername());
            throw new BusinessException(UserCodeEnum.PASSWORD_ERROR);
        }
        // 生成JWT
        log.info("【登录生成token前】本机系统当前时间：{}", new Date());
        log.info("【登录生成token前】系统毫秒时间戳：{}", System.currentTimeMillis());
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        log.info("【登录生成token】生成的token = {}", token);

        //【单端登录改造】key=userId，value=token，新登录覆盖旧登录
        String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(tokenRedisKey, token, RedisConstant.TOKEN_EXPIRE_MINUTE, TimeUnit.MINUTES);
        log.info("【登录存入Redis】redisKey={}, 存入token={}, 过期时间{}分钟",tokenRedisKey,token,RedisConstant.TOKEN_EXPIRE_MINUTE);

        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        log.info("【用户登录成功】userId:{}", user.getId());
        return vo;
    }


    /**
     * 获取当前登录用户信息
     * 业务规则：从上下文获取userId，查询并返回用户基础信息
     */
    @Override
    public UserInfoVO getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        log.info("【查询当前登录用户信息】userId:{}", userId);
        if(userId == null){
            throw new BusinessException(UserCodeEnum.NOT_LOGIN);
        }
        User user = this.getById(userId);
        if(user == null){
            log.warn("【获取用户信息失败】用户不存在 userId:{}", userId);
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }

    /**
     * 修改密码（用户中心，需要旧密码）
     * 业务规则：校验旧密码，更新加密新密码；修改成功直接踢下线
     */
    @Override
    public void updatePassword(UserUpdatePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("【用户修改密码】userId:{}", userId);
        User user = this.getById(userId);
        if(user == null){
            log.warn("【修改密码失败】用户不存在 userId:{}", userId);
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }
        // 校验旧密码
        if(!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())){
            log.warn("【修改密码失败】旧密码错误 userId:{}", userId);
            throw new BusinessException(UserCodeEnum.OLD_PASSWORD_ERROR);
        }
        // 更新为加密后的新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
        log.info("【用户修改密码成功】userId:{}", userId);
        // 修改密码后，强制踢下线：删除userId对应的登录记录
        String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(tokenRedisKey);
    }

    /**
     * 退出登录
     * 业务规则：清除Redis登录token，清空用户上下文
     */
    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        log.info("【用户退出登录】userId:{}", userId);
        String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(tokenRedisKey);
        UserContext.clear();
    }

    /**
     * 用户注销账号：校验密码，status标记为2（已注销）
     * 业务规则：校验密码，标记账号为已注销，清除登录态
     */
    @Override
    public void cancelAccount(UserCancelAccountDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("【用户注销账号】userId:{}", userId);
        User user = this.getById(userId);
        if(user == null){
            log.warn("【注销账号失败】用户不存在 userId:{}", userId);
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }
        // 校验注销密码
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            log.warn("【注销账号失败】密码校验失败 userId:{}", userId);
            throw new BusinessException(UserCodeEnum.PASSWORD_ERROR);
        }
        // 标记账号状态：2=已注销
        user.setStatus(UserStatusEnum.USER_CANCEL.getCode());
        this.updateById(user);
        // 逻辑删除标记
        this.removeById(user.getId());

        // 清除登录态，踢下线
        String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(tokenRedisKey);
        UserContext.clear();
        log.info("【账号注销完成】userId:{}", userId);
    }

    //=====================管理员接口=====================
    /**
     * 管理员分页查询用户
     * 业务规则：后台分页，支持账号、手机号、角色、状态多条件模糊筛选，返回分页VO
     */
    @Override
    public IPage<AdminUserPageVO> adminUserPage(AdminUserPageQueryDTO dto) {
        log.info("【管理员分页查询用户】pageNum:{},pageSize:{}", dto.getPageNum(), dto.getPageSize());
        Page<User> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 账号名模糊
        if(StringUtils.hasText(dto.getUsername())){
            wrapper.like(User::getUsername, dto.getUsername());
        }
        // 手机号模糊
        if(StringUtils.hasText(dto.getPhone())){
            wrapper.like(User::getPhone, dto.getPhone());
        }
        // 角色筛选
        if(dto.getRole() != null){
            wrapper.eq(User::getRole, dto.getRole());
        }
        // 状态筛选
        if(dto.getStatus() != null){
            wrapper.eq(User::getStatus, dto.getStatus());
        }
        Page<User> userPage = this.page(page, wrapper);
        log.info("【管理员分页查询用户】查询总条数:{}", userPage.getTotal());

        // 转VO
        return userPage.convert(user -> {
            AdminUserPageVO vo = new AdminUserPageVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());
            return vo;
        });
    }

    /**
     * 管理员新增用户
     * 业务规则：校验账号唯一，密码加密，新增后台用户
     */
    @Override
    public void adminAddUser(AdminUserAddDTO dto) {
        log.info("【管理员新增后台用户】username:{}", dto.getUsername());
        // 账号名校验唯一
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User exist = this.getOne(wrapper);
        if(exist != null){
            log.warn("【新增管理员用户失败】账号已存在 username:{}", dto.getUsername());
            throw new BusinessException(UserCodeEnum.USERNAME_EXIST);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserStatusEnum.ADMIN.getCode());
        user.setStatus(dto.getStatus());
        user.setAvatar(dto.getAvatar());
        boolean saveResult = this.save(user);
        log.info("【管理员新增用户完成】saveResult:{},userId:{}", saveResult, user.getId());
    }

    /**
     * 管理员编辑用户信息，支持启用/禁用账号（拉黑恶意秒杀用户，status=0拉黑）
     * 业务规则：禁止修改自己账号；修改为拉黑状态自动踢下线
     */
    @Override
    public void adminUpdateUser(AdminUserUpdateDTO dto) {
        log.info("【管理员编辑用户】targetUserId:{}", dto.getId());
        // 校验：不能修改自己账号，防止管理员把自己拉黑
        Long currentAdminId = UserContext.getUserId();
        if(dto.getId().equals(currentAdminId)){
            log.warn("【管理员编辑用户】禁止修改自身账号 adminId:{}", currentAdminId);
            throw new BusinessException(UserCodeEnum.CANNOT_MODIFY_SELF_ADMIN);
        }

        User user = this.getById(dto.getId());
        if(user == null){
            log.warn("【编辑用户失败】用户不存在 targetUserId:{}", dto.getId());
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }
        // 只拷贝dto中非null字段，null字段保留数据库原值
        BeanUtil.copyProperties(dto, user, CopyOptions.create().ignoreNullValue());

        // 执行更新，判断结果，失败抛异常
        boolean updateSuccess = this.updateById(user);
        if (!updateSuccess) {
            log.error("【管理员编辑用户】更新数据库失败 targetUserId:{}", dto.getId());
            throw new BusinessException(UserCodeEnum.USER_UPDATE_FAILED);
        }
        log.info("【管理员编辑用户】更新成功 targetUserId:{}", dto.getId());

        //拉黑踢下线逻辑
        // 只要修改后账号状态为拉黑0，就踢下线；逻辑删除单独在adminDeleteUser处理
        boolean needKickOut = (user.getStatus() != null && user.getStatus() == UserStatusEnum.USER_FORBIDDEN.getCode());
        if (needKickOut) {
            log.info("【管理员拉黑用户，执行踢下线】targetUserId:{}", dto.getId());
            // 删除Redis登录token，旧token直接失效，强制踢下线
            String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + dto.getId();
            stringRedisTemplate.delete(tokenRedisKey);
        }
    }

    /**
     * 管理员重置用户密码
     * 业务规则：管理员权限校验；生成随机密码加密入库，重置后踢下线
     * @return 明文新密码
     */
    @Override
    public String adminResetPwd(AdminUserResetPwdDTO dto) {
        log.info("【管理员重置用户密码】targetUserId:{}", dto.getUserId());
        // 校验当前操作者是否管理员，直接从上下文拿角色，减少DB查询
        Byte role = UserContext.getUserInfo().getRole();
        if(role == null || role != UserStatusEnum.ADMIN.getCode()){
            log.warn("【重置密码】无管理员权限");
            throw new BusinessException(CommonErrorCodeEnum.NO_PERMISSION);
        }

        // 查询被重置密码用户
        User user = this.getById(dto.getUserId());
        if(user == null ){
            log.warn("【重置密码失败】用户不存在 targetUserId:{}", dto.getUserId());
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }

        // 生成新明文密码
        String newPwd = "MiaoSha@" + new Random().nextInt(9000)+1000;
        user.setPassword(passwordEncoder.encode(newPwd));
        this.updateById(user);
        log.info("【管理员重置密码成功】targetUserId:{}", dto.getUserId());

        // 踢下线：直接删除userId对应的登录记录
        String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + dto.getUserId();
        stringRedisTemplate.delete(tokenRedisKey);
        return newPwd;
    }

    /**
     * 管理员逻辑删除用户
     * 业务规则：逻辑删除标记账号，清除登录态踢下线
     */
    @Override
    public void adminDeleteUser(Long userId) {
        log.info("【管理员逻辑删除用户】userId:{}", userId);
        User user = this.getById(userId);
        if(user == null){
            log.warn("【删除用户失败】用户不存在 userId:{}", userId);
            throw new BusinessException(UserCodeEnum.USER_NOT_EXIST);
        }
        this.removeById(user.getId());
        //2已注销
        user.setStatus(UserStatusEnum.USER_CANCEL.getCode());
        this.updateById(user);
        log.info("【管理员删除用户成功】userId:{}", userId);
        // 逻辑删除用户，踢下线，清除登录凭证
        String tokenRedisKey = RedisConstant.LOGIN_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(tokenRedisKey);
    }

    /**
     * 获取首页大盘统计数据
     * 业务规则：统计商品、秒杀商品、用户、订单总数量，用于后台仪表盘展示
     */
    @Override
    public DashboardStatVO getDashboardStat() {
        log.info("【查询后台大盘统计数据】");
        DashboardStatVO vo = new DashboardStatVO();

        // 传入空LambdaQueryWrapper实例，MP自动追加逻辑删除条件
        Long goodsCount = goodsMapper.selectCount(new LambdaQueryWrapper<>());
        Long seckillCount = seckillGoodsMapper.selectCount(new LambdaQueryWrapper<>());
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<>());
        Long orderCount = seckillOrderMapper.selectCount(new LambdaQueryWrapper<>());

        vo.setBaseGoodsTotal(goodsCount);
        vo.setSeckillTotal(seckillCount);
        vo.setUserTotal(userCount);
        vo.setOrderTotal(orderCount);
        log.info("【大盘统计查询完成】商品:{},秒杀:{},用户:{},订单:{}",goodsCount,seckillCount,userCount,orderCount);
        return vo;
    }

}
