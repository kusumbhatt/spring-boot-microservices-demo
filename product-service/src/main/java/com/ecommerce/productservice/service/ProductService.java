package com.ecommerce.productservice.service;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.model.ProductRequest;
import com.ecommerce.productservice.model.ProductResponse;

public interface ProductService {
    public long addProduct(ProductRequest productRequest);
    public ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
