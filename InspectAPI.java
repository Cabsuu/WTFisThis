import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

public class InspectAPI {
    public static void main(String[] args) throws Exception {
        // Find Paper API jar
        File localRepo = new File(System.getProperty("user.home"), ".m2/repository/io/papermc/paper/paper-api/26.2.build.87-stable");
        if (!localRepo.exists()) {
            System.out.println("Could not find Paper API in local maven repo. " + localRepo.getAbsolutePath());
            return;
        }
        File apiJar = null;
        for (File f : localRepo.listFiles()) {
            if (f.getName().endsWith(".jar") && !f.getName().contains("javadoc") && !f.getName().contains("sources")) {
                apiJar = f;
                break;
            }
        }
        if (apiJar == null) {
            System.out.println("No jar found in " + localRepo.getAbsolutePath());
            return;
        }

        URLClassLoader loader = new URLClassLoader(new URL[]{apiJar.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        Class<?> builderClass = loader.loadClass("io.papermc.paper.registry.data.dialog.body.ItemDialogBody$Builder");

        System.out.println("Methods in ItemDialogBody.Builder:");
        for (Method m : builderClass.getMethods()) {
            System.out.println("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ")");
        }
    }
}
