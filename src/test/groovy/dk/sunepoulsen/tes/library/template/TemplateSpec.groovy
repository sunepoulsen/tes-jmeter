package dk.sunepoulsen.tes.library.template

import spock.lang.Specification

class TemplateSpec extends Specification {

    void "Test template max"() {
        expect:
            Template.max(_a, _b) == _expected

        where:
            _a   | _b   | _expected
            null | null | null
            null | 5.0  | 5.0
            5.0  | null | 5.0
            5.0  | 5.0  | 5.0
            7.0  | 5.0  | 7.0
            3.0  | 5.0  | 5.0
    }
}
