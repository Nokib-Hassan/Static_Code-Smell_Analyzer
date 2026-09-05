package example;

class SmellyOrder {
    private String customerName;
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryNote;

    void createOrder() {
        String item = "Book";
        int quantity = 2;
        double price = 12.50;
        double subtotal = quantity * price;
        double shipping = 3.00;
        double tax = subtotal * 0.10;
        double total = subtotal + shipping + tax;
        System.out.println(item);
        System.out.println(total);
        System.out.println(customerName);
        System.out.println(deliveryAddress);
    }
}
