package com.controller;

import com.domain.ResponseResult;
import com.domain.VO.LinkVo;
import com.domain.VO.PageVo;
import com.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/link")
public class LinkController {
    @Autowired
    private LinkService linkService;
    @GetMapping("/list")
    public ResponseResult<PageVo> pageLinkList(Integer pageNum, Integer pageSize, LinkVo linkVo){
        return linkService.pageLinkList(pageNum,pageSize,linkVo);
    }
    @PostMapping
    public ResponseResult addLink(@RequestBody LinkVo linkVo){
        return linkService.addLink(linkVo);
    }
    @GetMapping("/{id}")
    public ResponseResult selectLinkById(@PathVariable Long id){
        return linkService.selectLinkById(id);
    }
    @PutMapping
    public ResponseResult updateLink(@RequestBody LinkVo linkVo){
        return linkService.updateLink(linkVo);
    }
    @DeleteMapping("/{id}")
    public ResponseResult deleteLinkById(@PathVariable Long id){
        return linkService.deleteLinkById(id);
    }
    @PutMapping("/changeLinkStatus")
    public ResponseResult changeLinkStatus(@RequestBody LinkVo linkVo){
        return linkService.updateLink(linkVo);
    }
}
