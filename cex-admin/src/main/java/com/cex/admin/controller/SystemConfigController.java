package com.cex.admin.controller;

import com.cex.admin.domain.entity.SysConfig;
import com.cex.admin.service.SystemConfigService;
import com.cex.common.core.domain.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@Api(tags = "系统配置管理")
public class SystemConfigController {
    
    private final SystemConfigService configService;
    
    /**
     * 查询所有配置
     */
    @GetMapping("/list")
    @ApiOperation("查询所有配置")
    public Result<List<SysConfig>> getConfigList() {
        log.info("管理员查询系统配置列表");
        List<SysConfig> configs = configService.getAllConfigs();
        return Result.success(configs);
    }
    
    /**
     * 根据配置键查询
     */
    @GetMapping("/get/{configKey}")
    @ApiOperation("根据配置键查询")
    public Result<SysConfig> getConfigByKey(@PathVariable String configKey) {
        log.info("管理员查询系统配置，key：{}", configKey);
        SysConfig config = configService.getByKey(configKey);
        return Result.success(config);
    }
    
    /**
     * 更新配置
     */
    @PostMapping("/update")
    @ApiOperation("更新配置")
    public Result<Void> updateConfig(@RequestBody SysConfig config) {
        log.info("管理员更新系统配置：{}", config);
        configService.updateConfig(config);
        return Result.success();
    }
    
    /**
     * 添加配置
     */
    @PostMapping("/add")
    @ApiOperation("添加配置")
    public Result<Void> addConfig(@RequestBody SysConfig config) {
        log.info("管理员添加系统配置：{}", config);
        configService.addConfig(config);
        return Result.success();
    }
    
    /**
     * 删除配置
     */
    @PostMapping("/delete/{id}")
    @ApiOperation("删除配置")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        log.info("管理员删除系统配置，id：{}", id);
        configService.deleteConfig(id);
        return Result.success();
    }
}

