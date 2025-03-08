// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.dao;

import java.util.Optional;

import software.amazonaws.example.product.entity.Product;
import software.amazonaws.example.product.entity.Products;

public interface ProductDao {

  Optional<Product> getProduct(String id);

  void putProduct(Product product);

  void deleteProduct(String id);

  Products getAllProduct();
  
}
