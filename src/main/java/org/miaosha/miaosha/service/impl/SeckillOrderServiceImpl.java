package org.miaosha.miaosha.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.miaosha.miaosha.common.enums.MqMsgTypeEnum;
import org.miaosha.miaosha.common.enums.SeckillRecordStatusEnum;
import org.miaosha.miaosha.common.exception.enums.SeckillPreOrderCodeEnum;
import org.miaosha.miaosha.common.exception.enums.UserAddressCodeEnum;
import org.miaosha.miaosha.common.util.CustomIdGenerator;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderDetailVO;
import org.miaosha.miaosha.entity.*;
import org.miaosha.miaosha.mapper.*;
import org.springframework.beans.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderAdminQueryDTO;
import org.miaosha.miaosha.common.dto.seckill.order.SeckillOrderUserQueryDTO;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.SeckillOrderCodeEnum;
import org.miaosha.miaosha.common.enums.SeckillOrderStatusEnum;
import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;
import org.miaosha.miaosha.common.mq.producer.IMqProducer;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderAdminVO;
import org.miaosha.miaosha.common.vo.seckill.order.SeckillOrderUserVO;
import org.miaosha.miaosha.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 秒杀订单服务实现类
 * 实现秒杀预订单转正式订单、支付、取消订单、超时关闭、订单分页查询、订单详情查询等业务
 */
@Service
@Slf4j
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {


    @Resource
    private ISeckillStockService seckillStockService;
    @Resource
    private ISeckillGoodsService seckillGoodsService;
    @Resource
    private IGoodsService iGoodsService;
    @Resource
    private SeckillPreOrderMapper seckillPreOrderMapper;
    @Resource
    private SeckillRecordMapper seckillRecordMapper;
    @Resource
    private UserAddressMapper userAddressMapper;
    @Resource
    private IMqProducer iMqProducer;
    @Resource
    private IMqProducer mqProducer;
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private ISeckillRecordService iSeckillRecordService;


    /**
     * 确认下单：选择地址，生成正式订单，删除预订单
     * 业务规则：校验预订单和收货地址归属；创建正式订单、删除预订单；事务提交成功后发送库存扣减MQ消息
     * @param preOrderId 预订单id
     * @param addressId 收货地址id
     * @return Long 正式订单主键id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmSeckillOrder(Long preOrderId, Long addressId) {
        log.info("【确认秒杀下单】开始执行，preOrderId:{}, addressId:{}", preOrderId, addressId);
        Long userId = UserContext.getUserId();

        //查询预订单
        SeckillPreOrder preOrder = seckillPreOrderMapper.selectById(preOrderId);
        if (preOrder == null) {
            log.error("【确认秒杀下单失败】预订单不存在，preOrderId:{}", preOrderId);
            throw new BusinessException(SeckillPreOrderCodeEnum.PRE_ORDER_NOT_EXIST);
        }
        //校验归属用户
        if (!preOrder.getUserId().equals(userId)) {
            log.error("【确认秒杀下单失败】预订单不属于当前用户，preOrderId:{}", preOrderId);
            throw new BusinessException(SeckillPreOrderCodeEnum.PRE_ORDER_ILLEGAL_USER);
        }

        //查询收货地址，校验归属
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            log.error("【确认秒杀下单失败】收货地址不存在或者不属于当前用户，addressId:{}", addressId);
            throw new BusinessException(UserAddressCodeEnum.ADDRESS_NOT_EXIST);
        }
        //构建正式秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(userId);
        seckillOrder.setSeckillGoodsId(preOrder.getSeckillGoodsId());
        seckillOrder.setOrderNo(generateOrderNo());
        seckillOrder.setOrderStatus(SeckillOrderStatusEnum.WAIT_PAY.getCode()); //0待支付
        seckillOrder.setExpireTime(LocalDateTime.now().plusMinutes(15));

        //订单填充收货地址信息
        seckillOrder.setReceiverName(address.getReceiverName());
        seckillOrder.setReceiverPhone(address.getReceiverPhone());
        //拼接地址，null转空字符串
        String province = address.getProvince() == null ? "" : address.getProvince();
        String city = address.getCity() == null ? "" : address.getCity();
        String district = address.getDistrict() == null ? "" : address.getDistrict();
        String detail = address.getDetailAddress() == null ? "" : address.getDetailAddress();
        String fullAddress = province + city + district + detail;
        seckillOrder.setReceiverAddress(fullAddress);
        seckillOrder.setAddressId(addressId);
        seckillOrderMapper.insert(seckillOrder);
        //正式订单创建成功，物理删除预订单
        seckillPreOrderMapper.deleteById(preOrderId);
        //创建订单记录
        iSeckillRecordService.createSeckillRecord(preOrder.getSeckillGoodsId(),seckillOrder.getId());
        //组装库存扣减DTO
        SeckillStockMsgDTO stockMsgDTO = new SeckillStockMsgDTO();
        stockMsgDTO.setOrderNo(seckillOrder.getOrderNo());
        stockMsgDTO.setSeckillGoodsId(seckillOrder.getSeckillGoodsId());
        stockMsgDTO.setMsgType(MqMsgTypeEnum.STOCK_DEDUCT.getCode());

        //注册事务回调，事务提交完成后再发送MQ
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 只有事务提交成功之后，才投递库存扣减MQ消息
                iMqProducer.sendStockMsg(stockMsgDTO);
            }
        });
        log.info("【确认秒杀下单】执行完成，生成正式订单id:{}, orderNo:{}", seckillOrder.getId(), seckillOrder.getOrderNo());
        return seckillOrder.getId();
    }

    /**
     * 生成唯一订单号
     * @return 订单号字符串
     */
    private String generateOrderNo() {
        return System.currentTimeMillis() + UUID.randomUUID().toString().substring(0,6);
    }

    /**
     * 订单支付
     * 业务规则：校验订单归属与状态，仅待支付订单允许支付；乐观锁更新订单状态，防止并发重复支付
     * @param orderId 订单id
     * @return 是否支付成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId) {
        log.info("【订单支付】开始执行，orderId:{}, userId:{}", orderId, UserContext.getUserId());
        Long userId = UserContext.getUserId();
        //参数校验
        if (orderId == null || orderId <= 0) {
            log.error("【订单支付失败】订单ID非法 orderId:{}", orderId);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_ID_INVALID);
        }

        //查询订单，校验订单归属、订单状态
        SeckillOrder seckillOrder = getById(orderId);
        if (seckillOrder == null) {
            log.error("【订单支付失败】订单不存在 orderId:{}", orderId);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_NOT_EXIST);
        }
        //订单必须属于当前登录用户，防止越权支付别人订单
        if (!seckillOrder.getUserId().equals(userId)) {
            log.error("【订单支付失败】越权操作，订单所属用户:{}, 当前用户:{}", seckillOrder.getUserId(), userId);
            throw new BusinessException(SeckillOrderCodeEnum.NO_PERMISSION_OP_ORDER);
        }
        //校验订单状态：仅【待支付】可以支付
        //约定：0=待支付，1=已支付，2=超时取消，3=手动取消
        if (!SeckillOrderStatusEnum.WAIT_PAY.getCode().equals(seckillOrder.getOrderStatus())) {
            log.error("【订单支付失败】订单状态不允许支付，orderStatus:{}", seckillOrder.getOrderStatus());
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_CAN_NOT_PAY);
        }

        // ====================== 3.修改订单为已支付（乐观锁更新，防止并发重复支付） ======================
        boolean updateResult = this.lambdaUpdate()
                .eq(SeckillOrder::getId, orderId)
                .eq(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.WAIT_PAY.getCode())
                .set(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.PAID.getCode())
                .set(SeckillOrder::getPayTime, LocalDateTime.now())
                .update();
        if (!updateResult) {
            log.warn("【订单支付】更新订单状态失败，订单可能已经被支付/取消 orderId:{}", orderId);
            return false;
        }

        log.info("【订单支付】执行完成，orderId:{}, userId:{}", orderId, userId);
        return true;
    }


    /**
     * 查询订单详情（测试/前端展示用）
     * 业务规则：校验订单归属，查询订单+商品信息组装VO返回前端
     * @param orderId 订单id
     * @return 订单详情VO
     */
    @Override
    public SeckillOrderDetailVO getOrderDetail(Long orderId) {
        log.info("【查询订单详情】开始执行，orderId:{}", orderId);
        // 参数校验
        if (orderId == null || orderId <= 0) {
            log.error("【查询订单详情失败】orderId参数非法 orderId:{}", orderId);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_ID_INVALID);
        }

        // 查询订单
        SeckillOrder seckillOrder = this.getById(orderId);
        // 判空抛异常
        if (seckillOrder == null) {
            log.error("【查询订单详情失败】订单不存在 orderId:{}", orderId);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_NOT_EXIST);
        }
        // 获取当前登录用户id
        Long loginUserId = UserContext.getUserId();
        if(!seckillOrder.getUserId().equals(loginUserId)){
            log.error("【查询订单详情失败】无权限查看他人订单");
            throw new BusinessException(SeckillOrderCodeEnum.NO_PERMISSION_OP_ORDER);
        }
        // 查询秒杀商品信息
        SeckillGoods seckillGoods = seckillGoodsService.getById(seckillOrder.getSeckillGoodsId());
        Goods goods = iGoodsService.getById(seckillGoods.getGoodsId());
        // 组装VO
        SeckillOrderDetailVO detailVO = new SeckillOrderDetailVO();
        BeanUtils.copyProperties(seckillOrder, detailVO);
        if (seckillGoods != null) {
            detailVO.setGoodsName(goods.getGoodsName());
            detailVO.setOriginalPrice(goods.getOriginalPrice());
            detailVO.setSeckillPrice(seckillGoods.getSeckillPrice());
        }
        // 购买数量、实付金额，如果订单表没有冗余，固定为1（秒杀一般1人1件）
        detailVO.setCount(1);
        if(seckillGoods != null){
            detailVO.setPayAmount(seckillGoods.getSeckillPrice());
        }
        log.info("【查询订单详情】执行完成，orderId:{}", orderId);
        return detailVO;
    }

    /**
     * 手动取消秒杀订单
     * 业务规则：只能取消本人待支付订单；取消后更新秒杀记录状态，归还Redis库存，发送MQ归还数据库库存
     * @param orderNo 订单号
     * @return 取消结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo) {
        log.info("【手动取消订单】开始执行，orderNo:{}, userId:{}", orderNo, UserContext.getUserId());
        Long userId = UserContext.getUserId();

        // 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.error("【手动取消订单失败】订单号为空，userId:{}", userId);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_NO_EMPTY);
        }

        // 校验订单是否存在、归属、状态是否允许取消
        LambdaQueryWrapper<SeckillOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(SeckillOrder::getOrderNo, orderNo);
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(orderWrapper);
        if (seckillOrder == null) {
            log.warn("【手动取消订单失败】订单不存在 orderNo:{}，userId:{}", orderNo, userId);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_NOT_EXIST);
        }

        // 只能取消自己的订单
        if (!seckillOrder.getUserId().equals(userId)) {
            log.warn("【手动取消订单失败】非订单所属用户，orderNo:{}, 当前用户:{}，订单归属用户:{}",
                    orderNo, userId, seckillOrder.getUserId());
            throw new BusinessException(SeckillOrderCodeEnum.NO_PERMISSION_OP_ORDER);
        }

        // 0=待支付，只有待支付才允许手动取消；已支付/已取消/已超时都不能取消
        Integer status = seckillOrder.getOrderStatus();
        if (!SeckillOrderStatusEnum.WAIT_PAY.getCode().equals(status)) {
            log.warn("【手动取消订单失败】订单状态不允许取消 orderNo:{}, status:{}", orderNo, status);
            throw new BusinessException(SeckillOrderCodeEnum.ORDER_CAN_NOT_CANCEL);
        }

        // 更新订单状态为已取消（乐观锁，防止并发重复取消）
        boolean updateResult = this.lambdaUpdate()
                .eq(SeckillOrder::getOrderNo, orderNo)
                .eq(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.WAIT_PAY.getCode())
                .set(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.MANUAL_CANCEL.getCode())
                .update();
        if (!updateResult) {
            log.warn("【手动取消订单】更新订单状态失败，可能已被其他操作修改 orderNo:{}", orderNo);
            return false;
        }
        // 构建条件：根据订单号查询秒杀记录
        LambdaQueryWrapper<SeckillRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(SeckillRecord::getOrderId,seckillOrder.getId());
        SeckillRecord seckillRecord = iSeckillRecordService.getOne(recordWrapper);
        iSeckillRecordService.updateSeckillRecordStatus(seckillRecord.getId(), SeckillRecordStatusEnum.CANCEL.getCode());
        // 归还Redis库存 + 发送MQ消息归还数据库库存
        Long seckillGoodsId = seckillOrder.getSeckillGoodsId();
        // 归还Redis预扣库存（Lua原子脚本）
        seckillStockService.returnRedisStock(seckillGoodsId);

        // 组装归还库存MQ消息，msgType=2代表归还库存
        SeckillStockMsgDTO stockMsgDTO = new SeckillStockMsgDTO();
        stockMsgDTO.setOrderNo(orderNo);
        stockMsgDTO.setSeckillGoodsId(seckillGoodsId);
        stockMsgDTO.setMsgType(MqMsgTypeEnum.STOCK_RETURN.getCode());
        // 生产者发送消息，内部自带失败兜底本地消息表
        mqProducer.sendStockMsg(stockMsgDTO);

        log.info("【手动取消订单】执行完成，orderNo:{}, userId:{}", orderNo, userId);
        return true;
    }


    /**
     * 关闭超时未支付订单，定时任务调用
     * 业务规则：定时扫描过期待支付订单，修改订单状态，更新秒杀记录、归还Redis库存并发送归还库存MQ
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeExpireOrder() {
        log.info("【定时任务-关闭超时订单】开始执行");
        // 查询满足条件的订单：订单状态=待支付，并且过期时间 <= 当前时间
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.WAIT_PAY.getCode())
                .le(SeckillOrder::getExpireTime, LocalDateTime.now());

        List<SeckillOrder> expireOrderList = this.list(wrapper);
        if (CollectionUtils.isEmpty(expireOrderList)) {
            log.info("【定时任务-关闭超时订单】暂无超时订单需要处理");
            log.info("【定时任务-关闭超时订单】执行结束");
            return;
        }
        log.info("【定时任务-关闭超时订单】本次需要关闭的超时订单数量：{}", expireOrderList.size());

        // 遍历每一条超时订单
        for (SeckillOrder order : expireOrderList) {
            Long seckillGoodsId = order.getSeckillGoodsId();
            Long orderId = order.getId();

            // 更新订单状态为已过期，乐观锁控制：必须还是待支付才允许修改
            boolean updateOrder = this.lambdaUpdate()
                    .eq(SeckillOrder::getId, orderId)
                    .eq(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.WAIT_PAY.getCode())
                    .set(SeckillOrder::getOrderStatus, SeckillOrderStatusEnum.TIME_OUT_CANCEL.getCode())
                    .update();
            if (!updateOrder) {
                log.warn("【定时任务-关闭超时订单】订单状态已变更，跳过处理，orderId:{}", orderId);
                continue;
            }
            // 构建条件：根据订单号查询秒杀记录
            LambdaQueryWrapper<SeckillRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(SeckillRecord::getOrderId,order.getId());
            SeckillRecord seckillRecord = iSeckillRecordService.getOne(recordWrapper);
            iSeckillRecordService.updateSeckillRecordStatus(seckillRecord.getId(), SeckillRecordStatusEnum.CANCEL.getCode());
            // 同步归还Redis库存（Lua原子脚本）
            seckillStockService.returnRedisStock(seckillGoodsId);

            // 组装归还库存MQ消息，msgType=2代表归还库存
            SeckillStockMsgDTO stockMsgDTO = new SeckillStockMsgDTO();
            stockMsgDTO.setOrderNo(order.getOrderNo());
            stockMsgDTO.setSeckillGoodsId(seckillGoodsId);
            stockMsgDTO.setMsgType(MqMsgTypeEnum.STOCK_RETURN.getCode());
            // 调用你写好的生产者方法，内部自带异常兜底本地消息表
            mqProducer.sendStockMsg(stockMsgDTO);
        }

        log.info("【定时任务-关闭超时订单】执行结束");
    }


    /**
     * 用户分页查询自己订单（精简列表VO）
     * 业务规则：自动获取当前登录用户ID，只查询当前用户的秒杀订单列表，分页返回
     * @param queryDTO 查询条件DTO
     * @return 分页VO
     */
    @Override
    public IPage<SeckillOrderUserVO> userPageOrder(SeckillOrderUserQueryDTO queryDTO) {
        log.info("【用户端订单分页查询】开始执行，queryDTO:{}", queryDTO);
        // 从登录上下文获取当前登录用户ID
        Long userId = UserContext.getUserId();
        log.info("用户查询本人订单分页，userId:{}, queryDTO:{}", userId, queryDTO);

        // 组装分页对象
        Page<SeckillOrderUserVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 调用自定义关联查询SQL，直接返回VO分页，不需要手动convert拷贝
        IPage<SeckillOrderUserVO> voPage = seckillOrderMapper.selectUserOrderPage(page, queryDTO, userId);
        log.info("【用户端订单分页查询】执行完成，userId:{}", userId);
        return voPage;
    }


    /**
     * 管理员分页查询所有订单
     * 业务规则：后台管理员查询全量订单，支持分页、多条件筛选
     * @param queryDTO 查询条件DTO
     * @return 分页VO
     */
    @Override
    public IPage<SeckillOrderAdminVO> getAdminOrderPage(SeckillOrderAdminQueryDTO queryDTO) {
        log.info("【管理端订单分页查询】开始执行，queryDTO:{}", queryDTO);
        // 组装分页对象，泛型用VO
        Page<SeckillOrderAdminVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2.调用Mapper里的联表分页方法（xml三表联查，一次性拿到订单+商品信息）
        IPage<SeckillOrderAdminVO> voPage = seckillOrderMapper.selectAdminOrderPage(page, queryDTO);
        log.info("【管理端订单分页查询】执行完成");
        return voPage;
    }

}
