package com.cex.trade.controller;

import com.cex.common.core.domain.Result;
import com.cex.trade.domain.entity.TradeSymbol;
import com.cex.trade.mapper.TradeSymbolMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易对Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/trade/symbol")
public class TradeSymbolController {

    @Autowired
    private TradeSymbolMapper symbolMapper;

    /**
     * 查询所有交易对
     */
    @GetMapping("/list")
    public Result<List<TradeSymbol>> getSymbolList() {
        List<TradeSymbol> symbols = symbolMapper.selectEnabled();
        return Result.success(symbols, "查询成功");
    }

    /**
     * 查询交易对详情
     */
    @GetMapping("/{symbol}")
    public Result<TradeSymbol> getSymbolDetail(@PathVariable String symbol) {
        TradeSymbol tradeSymbol = symbolMapper.selectBySymbol(symbol);
        if (tradeSymbol == null) {
            return Result.fail("交易对不存在");
        }
        return Result.success(tradeSymbol, "查询成功");
    }

    /**
     * 查询热门交易对
     */
    @GetMapping("/hot")
    public Result<List<TradeSymbol>> getHotSymbols() {
        List<TradeSymbol> symbols = symbolMapper.selectHot();
        return Result.success(symbols, "查询成功");
    }

    /**
     * 查询推荐交易对
     */
    @GetMapping("/recommend")
    public Result<List<TradeSymbol>> getRecommendSymbols() {
        List<TradeSymbol> symbols = symbolMapper.selectRecommend();
        return Result.success(symbols, "查询成功");
    }
}

