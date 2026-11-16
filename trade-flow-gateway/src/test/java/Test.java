import java.util.function.Supplier;

public class Test {

    public static String getMessage() {
        System.out.println("开始生成消息");
        return "你好";
    }

    public static Supplier<String> createTask() {
        return new Supplier<String>() {
            @Override
            public String get() {
                return getMessage();
            }
        };
    }

    /*public static Supplier<String> createTask() {
        return () -> {
            return getMessage();
        };
    }*/
    public static void main(String[] args) {
        Supplier<String> task = createTask();
        String message = task.get();
        System.out.println(message);
    }
}

