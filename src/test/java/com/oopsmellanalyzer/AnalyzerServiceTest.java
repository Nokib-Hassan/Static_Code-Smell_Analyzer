package com.oopsmellanalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oopsmellanalyzer.analysis.AnalyzerService;
import com.oopsmellanalyzer.analysis.SmellFinding;
import com.oopsmellanalyzer.input.SourceUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyzerServiceTest {
    private final AnalyzerService analyzer = new AnalyzerService();

    @Test
    void detectsLongMethodAndLargeClass() {
        String source = """
                class TooBig {
                    int one;
                    int two;
                    int three;
                    int four;
                    void veryLongMethod() {
                        int step1 = 1;
                        int step2 = 2;
                        int step3 = 3;
                        int step4 = 4;
                        int step5 = 5;
                        int step6 = 6;
                        int step7 = 7;
                        int step8 = 8;
                        int step9 = 9;
                        int step10 = 10;
                        int step11 = 11;
                        int step12 = 12;
                    }
                }
                """;

        List<SmellFinding> findings = analyzer.analyze(new SourceUnit("TooBig.java", source));

        assertEquals(2, findings.size());
        assertTrue(findings.stream().anyMatch(finding -> finding.smellName().equals("Long Method")));
        assertTrue(findings.stream().anyMatch(finding -> finding.smellName().equals("Large Class")));
    }

    @Test
    void reportsInvalidJavaAsAUsefulFinding() {
        List<SmellFinding> findings = analyzer.analyze(new SourceUnit("Broken.java", "class Broken {"));

        assertEquals(1, findings.size());
        assertEquals("Parse Error", findings.getFirst().smellName());
    }

    @Test
    void detectsTheAdditionalOopSmells() {
        String source = """
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
                        order.subtotal(); order.tax(); order.discount(); order.shipping(); order.total();
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
                class Parent { void deliver() {} }
                class Child extends Parent {
                    @Override void deliver() { throw new UnsupportedOperationException(); }
                }
                class Coordinator {
                    void change(A a, B b, C c, D d, E e) {}
                }
                class Order { double subtotal() { return 0; } double tax() { return 0; } double discount() { return 0; } double shipping() { return 0; } double total() { return 0; } }
                class Profile { String name, address, phone; int age; }
                class A {} class B {} class C {} class D {} class E {}
                """;

        List<SmellFinding> findings = analyzer.analyze(new SourceUnit("Smells.java", source));

        assertContainsSmell(findings, "God Class");
        assertContainsSmell(findings, "Data Class");
        assertContainsSmell(findings, "Feature Envy");
        assertContainsSmell(findings, "Inappropriate Intimacy");
        assertContainsSmell(findings, "Refused Bequest");
        assertContainsSmell(findings, "Shotgun Surgery");
    }

    @Test
    void detectsSmellsInCSharpSource() {
        String source = """
                class CSharpGod {
                    private int one, two, three, four, five;
                    void A() {} void B() {} void C() {} void D() {} void E() {}
                    void F() {} void G() {} void H() {} void I() {} void J() {}
                }
                class Envious {
                    void Calculate(Order order) {
                        order.Subtotal();
                        order.Tax();
                        order.Discount();
                        order.Shipping();
                        order.Total();
                    }
                }
                class DataCarrier {
                    private string first, second, third;
                    string GetFirst() { return first; }
                    void SetFirst(string value) { first = value; }
                    string GetSecond() { return second; }
                    void SetSecond(string value) { second = value; }
                    string GetThird() { return third; }
                    void SetThird(string value) { third = value; }
                }
                class Intimate {
                    void Inspect(Profile profile) {
                        string name = profile.Name;
                        int age = profile.Age;
                        string address = profile.Address;
                        string phone = profile.Phone;
                    }
                }
                class Parent { public virtual void Deliver() {} }
                class Child : Parent {
                    public override void Deliver() { throw new NotSupportedException(); }
                }
                class Coordinator {
                    void Change(A a, B b, C c, D d, E e) {}
                }
                class Order {
                    double Subtotal() { return 0; }
                    double Tax() { return 0; }
                    double Discount() { return 0; }
                    double Shipping() { return 0; }
                    double Total() { return 0; }
                }
                class Profile { public string Name, Address, Phone; public int Age; }
                class A {} class B {} class C {} class D {} class E {}
                """;

        List<SmellFinding> findings = analyzer.analyze(new SourceUnit("CSharpGod.cs", source));

        assertContainsSmell(findings, "God Class");
        assertContainsSmell(findings, "Feature Envy");
        assertContainsSmell(findings, "Data Class");
        assertContainsSmell(findings, "Inappropriate Intimacy");
        assertContainsSmell(findings, "Refused Bequest");
        assertContainsSmell(findings, "Shotgun Surgery");
    }

    private void assertContainsSmell(List<SmellFinding> findings, String smellName) {
        assertTrue(findings.stream().anyMatch(finding -> finding.smellName().equals(smellName)),
                () -> "Expected " + smellName + " in " + findings);
    }
}
