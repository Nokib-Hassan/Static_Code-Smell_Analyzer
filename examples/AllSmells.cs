class CSharpGod
{
    private int one, two, three, four, five;
    void A() {} void B() {} void C() {} void D() {} void E() {}
    void F() {} void G() {} void H() {} void I() {} void J() {}
}

class DataCarrier
{
    private string first, second, third;
    string GetFirst() { return first; }
    void SetFirst(string value) { first = value; }
    string GetSecond() { return second; }
    void SetSecond(string value) { second = value; }
    string GetThird() { return third; }
    void SetThird(string value) { third = value; }
}

class Envious
{
    void Calculate(Order order)
    {
        order.Subtotal();
        order.Tax();
        order.Discount();
        order.Shipping();
        order.Total();
    }
}

class Intimate
{
    void Inspect(Profile profile)
    {
        string name = profile.Name;
        int age = profile.Age;
        string address = profile.Address;
        string phone = profile.Phone;
    }
}

class Parent
{
    public virtual void Deliver() {}
}

class Child : Parent
{
    public override void Deliver()
    {
        throw new NotSupportedException();
    }
}

class Coordinator
{
    void Change(A a, B b, C c, D d, E e) {}
}

class Order
{
    double Subtotal() { return 0; }
    double Tax() { return 0; }
    double Discount() { return 0; }
    double Shipping() { return 0; }
    double Total() { return 0; }
}

class Profile
{
    public string Name, Address, Phone;
    public int Age;
}

class A {}
class B {}
class C {}
class D {}
class E {}
