package com.xiaofei.xiaofeimall.search.service;


import com.xiaofei.common.vo.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
