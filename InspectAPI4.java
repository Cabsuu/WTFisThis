import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class InspectAPI4 {
    public static void main(String[] args) throws Exception {
        ArrayList<URL> urls = new ArrayList<>();
        File repo = new File(System.getProperty("user.home"), ".m2/repository");

        // Find Paper API
        File paperApi = new File(repo, "io/papermc/paper/paper-api/26.2.build.87-stable/paper-api-26.2.build.87-stable.jar");
        if(paperApi.exists()) urls.add(paperApi.toURI().toURL());

        // Find all Kyori adventure jars in 4.18.0
        File advDir = new File(repo, "net/kyori");
        if(advDir.exists()){
            findJars(advDir, urls);
        }

        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
        Class<?> clazz = loader.loadClass("io.papermc.paper.registry.data.dialog.body.DialogBody");

        System.out.println("Static Methods in DialogBody:");
        for (Method m : clazz.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                System.out.println("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
            }
        }
    }

    static void findJars(File dir, ArrayList<URL> urls) throws Exception {
        for(File f : dir.listFiles()){
            if(f.isDirectory()){
                findJars(f, urls);
            } else if(f.getName().endsWith(".jar") && !f.getName().contains("javadoc") && !f.getName().contains("sources")){
                urls.add(f.toURI().toURL());
            }
        }
    }
}
