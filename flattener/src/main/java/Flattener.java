public class Flattener {
    public static void main(final String... args) throws Exception {
        Flattener work = new Flattener();
        int argIndex = 0;
        String topLevelDirectory = args[argIndex++];
        work.start(topLevelDirectory);
    }

    private void start(final String topLevelDirectory) throws Exception {
        System.out.println(topLevelDirectory);


    }
}