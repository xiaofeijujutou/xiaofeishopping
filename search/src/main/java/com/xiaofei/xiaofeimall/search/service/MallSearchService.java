package com.xiaofei.xiaofeimall.search.service;


import com.xiaofei.xiaofeimall.search.vo.SearchParamVo;
import com.xiaofei.xiaofeimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

@Service
public interface MallSearchService {
    /**
     * 从es获取检索结果
     * @param paramVo
     * @return
     */
    SearchResult search(SearchParamVo paramVo);
}
