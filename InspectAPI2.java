import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Modifier;

public class InspectAPI2 {
    public static void main(String[] args) throws Exception {
        File localRepo = new File(System.getProperty("user.home"), ".m2/repository/io/papermc/paper/paper-api/26.2.build.87-stable");
        File apiJar = null;
        for (File f : localRepo.listFiles()) {
            if (f.getName().endsWith(".jar") && !f.getName().contains("javadoc") && !f.getName().contains("sources")) {
                apiJar = f;
                break;
            }
        }
        URLClassLoader loader = new URLClassLoader(new URL[]{apiJar.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        Class<?> clazz = loader.loadClass("io.papermc.paper.registry.data.dialog.body.DialogBody");

        System.out.println("Static Methods in DialogBody:");
        for (Method m : clazz.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                System.out.println("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
            }
        }
    }
}
