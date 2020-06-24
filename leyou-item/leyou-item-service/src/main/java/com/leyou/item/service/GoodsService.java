package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.bo.SpuBo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        // 搜索条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        if (null != saleable) {
            criteria.andEqualTo("saleable", saleable);
        }

        //分页条件
        PageHelper.startPage(page, rows);

        //执行查询,模糊查询，用selectByExample
        List<Spu> spus = this.spuMapper.selectByExample(example);

        //Spu转化为SpuBo
        List<SpuBo> spuBoList = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);

            //查询分类名称
            List<String> names = categoryService.queryNamesByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "-"));
            //查询品牌名称
            spuBo.setBname(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
            return spuBo;

        }).collect(Collectors.toList());
        PageInfo<SpuBo> pageInfo = new PageInfo<>(spuBoList);
        return new PageResult<>(pageInfo.getTotal(),spuBoList);
    }
}
