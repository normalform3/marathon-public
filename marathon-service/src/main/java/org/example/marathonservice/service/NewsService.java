package org.example.marathonservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.marathondal.entity.NewsDO;
import org.example.marathondal.mapper.NewsMapper;
import org.example.marathonservice.utils.RedisIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsService extends ServiceImpl<NewsMapper, NewsDO> {
    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;

    //查询首页所需的新闻、公告
    public List<NewsDO> getHomePage() {
        // 查询新闻，状态为 1，类型为 1，按创建时间降序，取前 5 条
        QueryWrapper<NewsDO> newsWrapper = new QueryWrapper<>();
        newsWrapper.eq("status", 1)
                   .eq("type", 1)
                   .orderByDesc("create_time")
                   .last("LIMIT 5");
        List<NewsDO> newsList = newsMapper.selectList(newsWrapper);

        // 查询公告，状态为 1，类型为 2，按创建时间降序，取前 5 条
        QueryWrapper<NewsDO> announcementWrapper = new QueryWrapper<>();
        announcementWrapper.eq("status", 1)
                           .eq("type", 2)
                           .orderByDesc("create_time")
                           .last("LIMIT 5");
        List<NewsDO> announcementList = newsMapper.selectList(announcementWrapper);

        // 合并新闻和公告列表
        newsList.addAll(announcementList);
        return newsList;
    }

    //添加新闻
    public boolean addNews(NewsDO news) {
        news.setId(redisIdWorker.nextId("news"));
        news.setStatus(0); // 默认状态为 0（未显示）
        return newsMapper.insert(news) > 0;
    }

    //显示
    public void show(Long id) {
        NewsDO news = newsMapper.selectById(id);
        if (news != null) {
            news.setStatus(1); // 设置为已显示状态
            newsMapper.updateById(news);
        }
    }

    //隐藏
    public void hide(Long id) {
        NewsDO news = newsMapper.selectById(id);
        if (news != null) {
            news.setStatus(0); // 设置为已隐藏状态
            newsMapper.updateById(news);
        }
    }

    //删除
    public void delete(Long id) {
        newsMapper.deleteById(id);
    }
}
