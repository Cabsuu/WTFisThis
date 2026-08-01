import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Modifier;

public class InspectAPI3 {
    public static void main(String[] args) throws Exception {
        File localRepo = new File(System.getProperty("user.home"), ".m2/repository/io/papermc/paper/paper-api/26.2.build.87-stable");
        File apiJar = null;
        for (File f : localRepo.listFiles()) {
            if (f.getName().endsWith(".jar") && !f.getName().contains("javadoc") && !f.getName().contains("sources")) {
                apiJar = f;
                break;
            }
        }

        File adventureRepo = new File(System.getProperty("user.home"), ".m2/repository/net/kyori/adventure-api/4.18.0");
        File advJar = null;
        if(adventureRepo.exists()){
            for (File f : adventureRepo.listFiles()) {
                if (f.getName().endsWith(".jar") && !f.getName().contains("javadoc") && !f.getName().contains("sources")) {
                    advJar = f;
                    break;
                }
            }
        }
        URL[] urls = advJar != null ? new URL[]{apiJar.toURI().toURL(), advJar.toURI().toURL()} : new URL[]{apiJar.toURI().toURL()};

        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Class<?> clazz = loader.loadClass("io.papermc.paper.registry.data.dialog.body.DialogBody");

        System.out.println("Static Methods in DialogBody:");
        for (Method m : clazz.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                System.out.println("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
            }
        }
    }
}
