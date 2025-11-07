package com.cex.wallet.controller;

import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletAddress;
import com.cex.wallet.mapper.WalletAddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 钱包地址Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet/address")
public class WalletAddressController {

    @Autowired
    private WalletAddressMapper addressMapper;

    /**
     * 获取充值地址
     */
    @GetMapping("/deposit")
    public Result<Map<String, String>> getDepositAddress(
            @RequestParam Long userId,
            @RequestParam String coin) {
        
        // 查询是否已有地址
        WalletAddress address = addressMapper.selectDepositAddress(userId, coin);
        
        if (address == null) {
            // 生成新地址
            address = generateDepositAddress(userId, coin);
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("address", address.getAddress());
        result.put("memo", address.getMemo());
        result.put("coin", coin);
        
        return Result.success(result, "查询成功");
    }

    /**
     * 生成充值地址
     */
    private WalletAddress generateDepositAddress(Long userId, String coin) {
        // TODO: 实际应该调用RPC钱包服务生成真实地址
        // String realAddress = rpcWalletService.generateAddress(coin, "U" + userId);
        
        // 临时生成模拟地址
        String mockAddress = coin + "_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
        String memo = coin.equals("EOS") || coin.equals("XRP") ? String.valueOf(345678 + userId) : null;
        
        WalletAddress address = new WalletAddress();
        address.setUserId(userId);
        address.setCoin(coin);
        address.setAddress(mockAddress);
        address.setMemo(memo);
        address.setAddressType(1); // 充值地址
        address.setAddressSource(1); // RPC生成
        address.setEnabled(1);
        address.setUseCount(0);
        addressMapper.insert(address);
        
        log.info("生成充值地址：userId={}, coin={}, address={}", userId, coin, mockAddress);
        
        return address;
    }
}

