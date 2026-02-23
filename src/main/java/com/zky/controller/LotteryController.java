package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.LotteryDrawRequestDTO;
import com.zky.domain.dto.LotterySignRequestDTO;
import com.zky.domain.vo.LotteryDrawResultVO;
import com.zky.domain.vo.LotteryGridItemVO;
import com.zky.domain.vo.LotteryInfoVO;
import com.zky.service.ILotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/lottery")
public class LotteryController {

    @Resource
    private ILotteryService lotteryService;

    @GetMapping("/info")
    public Response<LotteryInfoVO> getInfo(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/lottery/info");
        LotteryInfoVO vo = lotteryService.getLotteryInfo(userId);
        return Response.<LotteryInfoVO>builder().code("0000").info("Success").data(vo).build();
    }

    @PostMapping("/sign-in")
    public Response<Integer> signIn(@RequestBody LotterySignRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/lottery/sign-in");
        Integer count = lotteryService.signIn(request.getUserId());
        return Response.<Integer>builder().code("0000").info("Success").data(count).build();
    }

    @PostMapping("/draw")
    public Response<LotteryDrawResultVO> draw(@RequestBody LotteryDrawRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/lottery/draw");
        LotteryDrawResultVO result = lotteryService.draw(request.getUserId(), request.getGridItems());
        return Response.<LotteryDrawResultVO>builder().code("0000").info("Success").data(result).build();
    }

    @GetMapping("/grid")
    public Response<List<LotteryGridItemVO>> grid(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/lottery/grid");
        List<LotteryGridItemVO> items = lotteryService.previewGrid(userId);
        return Response.<List<LotteryGridItemVO>>builder().code("0000").info("Success").data(items).build();
    }
}
