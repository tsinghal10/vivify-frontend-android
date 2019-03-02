package com.allandroidprojects.ecomsample.product;

import java.io.Serializable;

public class ProductInfo {

    private String product_id;
    private String product_title;
    private String img_url;
    private String product_price;
    private String cart_quantity;

    private String order_id, order_price, order_list;

    public String getProduct_id() {
        return product_id;
    }

    public String getImg_url() {
        return img_url;
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

    public void setProduct_title(String product_title) {
        this.product_title = product_title;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }

    public void setCart_quantity(String cart_quantity) {
        this.cart_quantity = cart_quantity;
    }

    public String getCart_quantity() {
        return cart_quantity;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getOrder_price() {
        return order_price;
    }

    public String getOrder_list() {
        return order_list;
    }

    public ProductInfo(String id, String name, String url, String product_price) {
        product_id = id;
        product_title = name;
        img_url = url;
        this.product_price = product_price;
    }

    public ProductInfo(String id, String name, String url, String product_price, String cart_quantity) {
        product_id = id;
        product_title = name;
        img_url = url;
        this.product_price = product_price;
        this.cart_quantity = cart_quantity;
    }

    public ProductInfo(String order_id, String price, String order_list) {
        this.order_list = order_list;
        this.order_id = order_id;
        order_price = price;
    }
}
