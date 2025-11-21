package com.cex.trade.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 钱包服务 Feign 客户端
 * 用于调用钱包服务的冻结/解冻余额接口
 * 
 * @author cex
 */
@FeignClient(name = "cex-wallet", path = "/api/wallet")
public interface WalletFeignClient {
    
    /**
     * 冻结余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID（订单号等）
     * @param remark 备注
     */
    @PostMapping("/balance/freeze")
    void freezeBalance(@RequestParam("userId") Long userId,
                       @RequestParam("coin") String coin,
                       @RequestParam("amount") BigDecimal amount,
                       @RequestParam(value = "relatedId", required = false) String relatedId,
                       @RequestParam(value = "remark", required = false) String remark);
    
    /**
     * 解冻余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID（订单号等）
     * @param remark 备注
     */
    @PostMapping("/balance/unfreeze")
    void unfreezeBalance(@RequestParam("userId") Long userId,
                         @RequestParam("coin") String coin,
                         @RequestParam("amount") BigDecimal amount,
                         @RequestParam(value = "relatedId", required = false) String relatedId,
                         @RequestParam(value = "remark", required = false) String remark);
    
    /**
     * 扣减冻结余额（成交后使用）
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID（订单号等）
     * @param remark 备注
     */
    @PostMapping("/balance/decrease-frozen")
    void decreaseFrozen(@RequestParam("userId") Long userId,
                        @RequestParam("coin") String coin,
                        @RequestParam("amount") BigDecimal amount,
                        @RequestParam(value = "relatedId", required = false) String relatedId,
                        @RequestParam(value = "remark", required = false) String remark);
    
    /**
     * 增加可用余额（成交后使用）
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID（订单号等）
     * @param remark 备注
     */
    @PostMapping("/balance/increase")
    void increaseBalance(@RequestParam("userId") Long userId,
                         @RequestParam("coin") String coin,
                         @RequestParam("amount") BigDecimal amount,
                         @RequestParam(value = "relatedId", required = false) String relatedId,
                         @RequestParam(value = "remark", required = false) String remark);
}

