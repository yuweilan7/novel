package io.github.xxyopen.novel.manager.cache;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.github.xxyopen.novel.core.annotation.Lock;
import io.github.xxyopen.novel.core.constant.CacheConsts;
import io.github.xxyopen.novel.core.constant.DatabaseConsts;
import io.github.xxyopen.novel.dao.entity.BookInfo;
import io.github.xxyopen.novel.dao.mapper.BookInfoMapper;
import io.github.xxyopen.novel.dto.resp.BookRankRespDto;

import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

/**
 * 小说排行榜 缓存管理类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Component
@RequiredArgsConstructor
public class BookRankCacheManager {

    private final BookInfoMapper bookInfoMapper;
    private final RedisTemplate<String,String> redisTemplate;
    /**
     * 查询小说点击榜列表，并放入缓存中
     */
//    @Cacheable(cacheManager = CacheConsts.REDIS_CACHE_MANAGER,
//        value = CacheConsts.BOOK_VISIT_RANK_CACHE_NAME)
    @Lock(prefix = "visit_rank")
    public List<BookRankRespDto> listVisitRankBooks() {

        Set<ZSetOperations.TypedTuple<String>> rankData = redisTemplate.opsForZSet().reverseRangeWithScores(CacheConsts.BOOK_VISIT_RANK_CACHE_NAME, 0, 29);
        if (CollectionUtils.isEmpty(rankData)) {
            // 如果 Redis 中没有排行榜数据，则从数据库中查询
            QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
            bookInfoQueryWrapper
                .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
                .orderByDesc(DatabaseConsts.BookTable.COLUMN_VISIT_COUNT)
                .last(DatabaseConsts.SqlEnum.LIMIT_30.getSql());
            List<BookInfo> bookInfoList = bookInfoMapper.selectList(bookInfoQueryWrapper);
            if (CollectionUtils.isEmpty(bookInfoList)) {
                return Collections.emptyList();
            }
            // 将排行榜数据存入 Redis
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            for (int i = 0; i < bookInfoList.size(); i++) {
                BookInfo bookInfo = bookInfoList.get(i);
                ZSetOperations.TypedTuple<String> tuple = new DefaultTypedTuple<>(
                    String.valueOf(bookInfo.getId()),
                    Double.valueOf(bookInfo.getVisitCount())
                );
                tuples.add(tuple);
            }
            redisTemplate.opsForZSet().add(CacheConsts.BOOK_VISIT_RANK_CACHE_NAME, tuples);
            rankData = redisTemplate.opsForZSet().reverseRangeWithScores(CacheConsts.BOOK_VISIT_RANK_CACHE_NAME, 0, 29);
        }
        // 将排行榜数据转化为 DTO
        List<BookRankRespDto> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : rankData) {
            BookInfo bookInfo = bookInfoMapper.selectById(Long.valueOf(tuple.getValue()));
            if (bookInfo != null) {
                BookRankRespDto respDto = new BookRankRespDto();
                respDto.setId(bookInfo.getId());
                respDto.setCategoryId(bookInfo.getCategoryId());
                respDto.setCategoryName(bookInfo.getCategoryName());
                respDto.setBookName(bookInfo.getBookName());
                respDto.setAuthorName(bookInfo.getAuthorName());
                respDto.setPicUrl(bookInfo.getPicUrl());
                respDto.setBookDesc(bookInfo.getBookDesc());
                respDto.setLastChapterName(bookInfo.getLastChapterName());
                respDto.setLastChapterUpdateTime(bookInfo.getUpdateTime());
                respDto.setWordCount(bookInfo.getWordCount());
                result.add(respDto);
            }
        }
        return result;
    
    }

    /**
     * 查询小说新书榜列表，并放入缓存中
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
        value = CacheConsts.BOOK_NEWEST_RANK_CACHE_NAME)
    @Lock(prefix = "newest_rank")
    public List<BookRankRespDto> listNewestRankBooks() {
        QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
        bookInfoQueryWrapper
            .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        return listRankBooks(bookInfoQueryWrapper);
    }

    /**
     * 查询小说更新榜列表，并放入缓存中
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
        value = CacheConsts.BOOK_UPDATE_RANK_CACHE_NAME)
    @Lock(prefix = "update_rank")
    public List<BookRankRespDto> listUpdateRankBooks() {
        QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
        bookInfoQueryWrapper
            .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        return listRankBooks(bookInfoQueryWrapper);
    }

    private List<BookRankRespDto> listRankBooks(QueryWrapper<BookInfo> bookInfoQueryWrapper) {
        bookInfoQueryWrapper
            .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
            .last(DatabaseConsts.SqlEnum.LIMIT_30.getSql());
        return bookInfoMapper.selectList(bookInfoQueryWrapper).stream().map(v -> {
            BookRankRespDto respDto = new BookRankRespDto();
            respDto.setId(v.getId());
            respDto.setCategoryId(v.getCategoryId());
            respDto.setCategoryName(v.getCategoryName());
            respDto.setBookName(v.getBookName());
            respDto.setAuthorName(v.getAuthorName());
            respDto.setPicUrl(v.getPicUrl());
            respDto.setBookDesc(v.getBookDesc());
            respDto.setLastChapterName(v.getLastChapterName());
            respDto.setLastChapterUpdateTime(v.getUpdateTime());
            respDto.setWordCount(v.getWordCount());
            return respDto;
        }).toList();
    }

}
