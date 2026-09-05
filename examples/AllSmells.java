class God {
    int one, two, three, four, five;
    void a() {} void b() {} void c() {} void d() {} void e() {}
    void f() {} void g() {} void h() {} void i() {} void j() {}
}

class DataCarrier {
    private String first, second, third;
    String getFirst() { return first; }
    void setFirst(String value) { first = value; }
    String getSecond() { return second; }
    void setSecond(String value) { second = value; }
    String getThird() { return third; }
    void setThird(String value) { third = value; }
}

class Envious {
    void calculate(Order order) {
        order.subtotal();
        order.tax();
        order.discount();
        order.shipping();
        order.total();
    }
}

class Intimate {
    void inspect(Profile profile) {
        String name = profile.name;
        int age = profile.age;
        String address = profile.address;
        String phone = profile.phone;
    }
}

class Parent {
    void deliver() {}
}

class Child extends Parent {
    @Override
    void deliver() {
        throw new UnsupportedOperationException();
    }
}

class Coordinator {
    void change(A a, B b, C c, D d, E e) {}
}

class Order {
    double subtotal() { return 0; }
    double tax() { return 0; }
    double discount() { return 0; }
    double shipping() { return 0; }
    double total() { return 0; }
}

class Profile {
    String name, address, phone;
    int age;
}

class A {}
class B {}
class C {}
class D {}
class E {}
