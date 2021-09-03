package test;

public abstract class ServiceProviderExample {

    public abstract String toString();

    public static class ServiceProviderExampleImpl extends ServiceProviderExample {
        @Override
        public String toString() {
            return "exampleImpl";
        }
    }

}
