import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

public class InspectAPI5 {
    public static void main(String[] args) throws Exception {
        ArrayList<URL> urls = new ArrayList<>();
        File repo = new File(System.getProperty("user.home"), ".m2/repository");
        File paperApi = new File(repo, "io/papermc/paper/paper-api/26.2.build.87-stable/paper-api-26.2.build.87-stable.jar");
        urls.add(paperApi.toURI().toURL());

        File advDir = new File(repo, "net/kyori");
        findJars(advDir, urls);

        System.out.println("Classes in io.papermc.paper.registry.data.dialog.body:");
        ZipFile zip = new ZipFile(paperApi);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if(name.startsWith("io/papermc/paper/registry/data/dialog/body/") && name.endsWith(".class") && !name.contains("$")){
                System.out.println("  " + name);
            }
        }
        zip.close();
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
