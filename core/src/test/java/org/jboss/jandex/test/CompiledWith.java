package org.jboss.jandex.test;

class CompiledWith {
    private static final boolean isEcj = "ecj".equals(System.getProperty("compiler")); // see pom.xml

    static boolean ecj() {
        return isEcj;
    }
}
