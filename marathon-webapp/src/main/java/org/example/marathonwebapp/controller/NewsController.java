package org.example.marathonwebapp.controller;

import org.example.marathondal.entity.NewsDO;
import org.example.marathonservice.service.NewsService;
import org.example.marathonwebapp.model.WebResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Autowired
    private NewsService newsService;

    //查询首页所需的新闻、公告
    @GetMapping("/getHomePage")
    public WebResult<List<NewsDO>> getHomePage() {
        List<NewsDO> newsList = newsService.getHomePage();
        return WebResult.success(newsList);
    }

    //查询所有(管理员
    @GetMapping("/getAll")
    public WebResult<List<NewsDO>> getAll() {
        List<NewsDO> newsList = newsService.list();
        return WebResult.success(newsList);
    }

    //新增
    @PostMapping("/add")
    public WebResult<String> add(@RequestBody NewsDO news) {
        boolean isSuccess = newsService.addNews(news);
        if (isSuccess) {
            return WebResult.success("新增成功");
        } else {
            return WebResult.fail("新增失败");
        }
    }

    //显示
    @GetMapping("/show")
    public WebResult<String> show(Long id) {
        newsService.show(id);
        return WebResult.success("显示成功");
    }

    //隐藏
    @GetMapping("/hide")
    public WebResult<String> hide(Long id) {
        newsService.hide(id);
        return WebResult.success("隐藏成功");
    }

    //删除
    @GetMapping("/delete")
    public WebResult<String> delete(Long id) {
        newsService.delete(id);
        return WebResult.success("删除成功");
    }
}
