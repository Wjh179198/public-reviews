package com.wjh.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wjh.constant.MessageConstant;
import com.wjh.constant.UserStatusConstant;
import com.wjh.constant.VoucherStatusConstant;
import com.wjh.context.BaseContext;
import com.wjh.dto.ShopOrderDTO;
import com.wjh.entity.*;
import com.wjh.mapper.*;
import com.wjh.result.PageResult;
import com.wjh.result.Result;
import com.wjh.service.ShopOrderService;
import com.wjh.utils.MoneyUtil;
import com.wjh.vo.ShopOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopOrderServiceImpl implements ShopOrderService {

    private static final String LOCK_PREFIX = "lock:order:";
    private static final long LOCK_TIMEOUT = 10L;

    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private ShopOrderMapper shopOrderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VoucherOrderMapper voucherOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ShopOrderVO> createOrder(ShopOrderDTO shopOrderDTO) {
        Long userId = BaseContext.getThreadLocal().getId();
        String lockKey = LOCK_PREFIX + userId;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_TIMEOUT));
        if (Boolean.FALSE.equals(locked)) {
            return Result.error(MessageConstant.ORDER_PROCESSING);
        }
        BigDecimal userMoney = null;
        try {
            Shop shop = shopMapper.getById(shopOrderDTO.getShopId());
            if (shop == null) {
                return Result.error(MessageConstant.SHOP_NOT_EXISTS);
            }
            BigDecimal money = shop.getPrice();
            if (shopOrderDTO.getVoucherId() != null) {
                Voucher voucher = voucherMapper.getById(shopOrderDTO.getVoucherId());
                if (voucher == null) {
                    return Result.error(MessageConstant.VOUCHER_NOT_EXISTS);
                }
                VoucherOrder voucherOrder = voucherOrderMapper.getByUserIdAndVoucherId(userId, voucher.getId());
                if (voucherOrder == null) {
                    return Result.error(MessageConstant.VOUCHER_NOT_BUY);
                }
                if (voucherOrder.getStatus().equals(VoucherStatusConstant.USED)) {
                    return Result.error(MessageConstant.VOUCHER_USED);
                }
                if(!voucherOrder.getShopId().equals(shopOrderDTO.getShopId())) {
                    return Result.error(MessageConstant.VOUCHER_NOT_BUY_THIS_SHOP);
                }
                money = money.subtract(voucher.getValue());
            }
            userMoney = MoneyUtil.getMoney(userId, stringRedisTemplate);
            if (userMoney.compareTo(money) < 0) {
                return Result.error(MessageConstant.MONEY_INVALID);
            }
            User userByShopId = userMapper.getByShopId(shop.getId());
            if (userByShopId.getStatus().equals(UserStatusConstant.BAN_USER)) {
                return Result.error(MessageConstant.SHOP_OWNER_BAN);
            }
            //更新商家用户余额
            userByShopId.setMoney(userByShopId.getMoney().add(money));
            userMapper.update(userByShopId);
            MoneyUtil.setMoney(userByShopId.getId(), userByShopId.getMoney(), stringRedisTemplate);
            //更新用户余额
            MoneyUtil.setMoney(userId, userMoney.subtract(money), stringRedisTemplate);
            User user = userMapper.getById(userId);
            user.setMoney(userMoney.subtract(money));
            userMapper.update(user);
            //更新优惠券状态
            if (shopOrderDTO.getVoucherId() != null) {
                voucherOrderMapper.updateStatus(shopOrderDTO.getVoucherId(), VoucherStatusConstant.USED);
            }
            //更新商家销售量
            shop.setSold(shop.getSold() + 1);
            shopMapper.update(shop);
            //插入订单
            ShopOrder shopOrder = new ShopOrder();
            shopOrder.setShopId(shopOrderDTO.getShopId());
            shopOrder.setUserId(userId);
            shopOrder.setVoucherId(shopOrderDTO.getVoucherId());
            shopOrder.setPrice(money);
            shopOrder.setStatus(1);
            shopOrder.setOrderTime(LocalDateTime.now());
            shopOrderMapper.insert(shopOrder);
            //构建返回对象
            ShopOrderVO shopOrderVO = new ShopOrderVO();
            shopOrderVO.setId(shopOrder.getId());
            shopOrderVO.setShopName(shop.getName());
            shopOrderVO.setPrice(money);
            shopOrderVO.setOrderTime(shopOrder.getOrderTime());
            return Result.success(MessageConstant.ORDER_SUCCESS, shopOrderVO);
        } catch (Exception e) {
            //回滚用户余额
            if (userMoney != null) {
                MoneyUtil.setMoney(userId, userMoney, stringRedisTemplate);
            }
            return Result.error(MessageConstant.ORDER_FAIL);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    public Result<PageResult> getOrderListByUserId(Long userId, Integer status, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<ShopOrderVO> shopOrderVOList = shopOrderMapper.getByUserId(userId, status);
        Page<ShopOrderVO> shopOrderVOPage = (Page<ShopOrderVO>) shopOrderVOList;
        return Result.success(new PageResult(shopOrderVOPage.getTotal(), shopOrderVOPage.getResult(), shopOrderVOPage.getPages(), pageSize));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result refundOrder(Long orderId) {
        Long userId = BaseContext.getThreadLocal().getId();
        String lockKey = LOCK_PREFIX + userId;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_TIMEOUT));
        if (Boolean.FALSE.equals(locked)) {
            return Result.error(MessageConstant.ORDER_PROCESSING);
        }
        BigDecimal useMoney = null;
        try {
            ShopOrder shopOrder = shopOrderMapper.getOrderByUserIdAndOrderId(userId, orderId);
            if(shopOrder == null) {
                return Result.error(MessageConstant.ORDER_NOT_EXISTS);
            }
            if(!BaseContext.getThreadLocal().getId().equals(shopOrder.getUserId())) {
                return Result.error(MessageConstant.NO_PERMISSION_REFUND);
            }
            //更新订单状态
            shopOrderMapper.updateStatusById(orderId, 2);
            //更新商家用户余额
            useMoney = shopOrder.getPrice();
            User userByShopId = userMapper.getByShopId(shopOrder.getShopId());
            userByShopId.setMoney(userByShopId.getMoney().subtract(useMoney));
            userMapper.update(userByShopId);
            BigDecimal shopMoney = MoneyUtil.getMoney(userByShopId.getId(), stringRedisTemplate);
            MoneyUtil.setMoney(userByShopId.getId(), shopMoney.subtract(useMoney), stringRedisTemplate);
            //更新用户余额
            User user = userMapper.getById(shopOrder.getUserId());
            user.setMoney(user.getMoney().add(useMoney));
            userMapper.update(user);
            BigDecimal userMoney = MoneyUtil.getMoney(shopOrder.getUserId(), stringRedisTemplate);
            MoneyUtil.setMoney(shopOrder.getUserId(), userMoney.add(useMoney), stringRedisTemplate);
            //修改优惠卷状态
            if (shopOrder.getVoucherId() != null) {
                voucherOrderMapper.updateStatus(shopOrder.getVoucherId(), VoucherStatusConstant.AVAILABLE);
            }
        } catch (Exception e) {
            return Result.error(MessageConstant.REFUND_FAIL);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
        return Result.success(MessageConstant.REFUND_SUCCESS);
    }
}
