import test.ModuleAnnotation;

@Deprecated
@ModuleAnnotation("typeannotationtest")
module org.jboss.jandex.typeannotationtest {

    requires java.base;
    requires transitive java.desktop;

    exports test to java.base, java.desktop;

    opens test to java.base;
    opens test.exec to java.base;
    opens test.expr to java.base;

    provides test.ServiceProviderExample
        with test.ServiceProviderExample.ServiceProviderExampleImpl;

    uses test.ServiceProviderExample;

}
