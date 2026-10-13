package com.temshop;

record ShopItem(String name, int price, String description) {
    String priceLabel() {
        return price + "G";
    }
}
