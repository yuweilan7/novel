package io.github.xxyopen.novel.dao.mapper;

import io.github.xxyopen.novel.dao.entity.HomeBook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 小说推荐 Mapper 接口
 * </p>
 *
 * @author xiongxiaoyang
 * @date 2022/05/12
 */

/**
 * 指定了实体类,那么会把实体类名作为表明进行mysql的查询
 * 但是又设置了@TableName
 */
public interface HomeBookMapper extends BaseMapper<HomeBook> {

}
