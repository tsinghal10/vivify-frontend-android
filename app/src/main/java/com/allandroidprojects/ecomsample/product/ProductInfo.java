package com.allandroidprojects.ecomsample.product;

import java.io.Serializable;

public class ProductInfo {

    private String product_id;
    private String product_title;
    private String product_class;
    private String img_url;
    private String product_price;

    public String getProduct_id() {
        return product_id;
    }

    public String getImg_url() {
        return img_url;
    }

    public String getProduct_class() {
        return product_class;
    }

    public String getProduct_title() {
        return product_title;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public void setProduct_class(String product_class) {
        this.product_class = product_class;
    }

    public void setProduct_title(String product_title) {
        this.product_title = product_title;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }

    public ProductInfo(String id, String name, String prod_class, String url, String product_price) {
        product_id = id;
        product_class = prod_class;
        product_title = name;
        img_url = url;
        this.product_price=product_price;
    }
}
