package io.github.xxyopen.novel.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.xxyopen.novel.core.constant.CacheConsts;
import io.github.xxyopen.novel.core.constant.DatabaseConsts;
import io.github.xxyopen.novel.dao.entity.BookCategory;
import io.github.xxyopen.novel.dao.mapper.BookCategoryMapper;
import io.github.xxyopen.novel.dto.resp.BookCategoryRespDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 小说分类 缓存管理类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Component
@RequiredArgsConstructor
public class BookCategoryCacheManager {

    private final BookCategoryMapper bookCategoryMapper;

    /**
     * 根据作品方向查询小说分类列表，并放入缓存中
     */
    /**
     * 在Spring Boot中，使用@Component注解标记的Bean会被
     * Spring自动扫描并注册到Spring应用程序上下文中。
     * 这意味着在同一个应用程序上下文中，使用相同名称创建的Bean将被自动装配在一起。
     * 因此，当您在CacheConfig中声明一个名为“caffeineCacheManager”的Bean时，
     * Spring将创建一个具有相同名称的Bean，并且可以在整个应用程序中使用它。
     * 而在CacheConsts类中使用的字符串值“caffeineCacheManager”只是用来标识相应的Bean，
     * 它与Bean的名称相对应，所以可以成功对应。
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
        value = CacheConsts.BOOK_CATEGORY_LIST_CACHE_NAME)
    public List<BookCategoryRespDto> listCategory(Integer workDirection) {
        QueryWrapper<BookCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCategoryTable.COLUMN_WORK_DIRECTION, workDirection);
        return bookCategoryMapper.selectList(queryWrapper).stream().map(v ->
            BookCategoryRespDto.builder()
                .id(v.getId())
                .name(v.getName())
                .build()).toList();
    }

}
