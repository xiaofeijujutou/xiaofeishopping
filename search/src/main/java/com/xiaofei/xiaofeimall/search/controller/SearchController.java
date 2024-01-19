package com.xiaofei.xiaofeimall.search.controller;

import com.xiaofei.xiaofeimall.search.service.MallSearchService;
import com.xiaofei.xiaofeimall.search.vo.SearchParamVo;
import com.xiaofei.xiaofeimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;


    /**
     * 根据首页携带的参数,来从es里面查数据返回给用户;
     * @param paramVo 参数
     * @return es查出来的数据
     */
    @GetMapping("/list.html")
    public String listPage(SearchParamVo paramVo, Model model, HttpServletRequest request){
        //把http请求的原生路径封装在请求体里面
        paramVo.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(paramVo);
        model.addAttribute("result", result);
        return "list";
    }
}
