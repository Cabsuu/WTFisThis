import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class InspectAPI8 {
    public static void main(String[] args) throws Exception {
        ArrayList<URL> urls = new ArrayList<>();
        File repo = new File(System.getProperty("user.home"), ".m2/repository");
        File paperApi = new File(repo, "io/papermc/paper/paper-api/26.2.build.87-stable/paper-api-26.2.build.87-stable.jar");
        urls.add(paperApi.toURI().toURL());

        File advDir = new File(repo, "net/kyori");
        findJars(advDir, urls);

        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
        Class<?> clazz = loader.loadClass("io.papermc.paper.registry.data.dialog.body.DialogBody");

        System.out.println("Methods in DialogBody:");
        for (Method m : clazz.getMethods()) {
            System.out.println("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
        }
    }

    static void findJars(File dir, ArrayList<URL> urls) throws Exception {
        if(!dir.exists()) return;
        for(File f : dir.listFiles()){
            if(f.isDirectory()){
                findJars(f, urls);
            } else if(f.getName().endsWith(".jar") && !f.getName().contains("javadoc") && !f.getName().contains("sources")){
                urls.add(f.toURI().toURL());
            }
        }
    }
}
