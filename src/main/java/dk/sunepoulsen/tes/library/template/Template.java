package dk.sunepoulsen.tes.library.template;

public class Template {
    static Double max(Double a, Double b) {
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        if (a > b) {
            return a;
        }

        return b;
    }
}
