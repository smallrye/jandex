import java.util.zip.ZipFile
import org.jboss.jandex.IndexReader

def jarFile = new File(basedir, 'target/jandex-maven-plugin-jar-1.0-SNAPSHOT.jar')
assert jarFile.exists() : "File ${jarFile} does not exist"
assert jarFile.length() > 0 : "File ${jarFile} is empty"

def jar = new ZipFile(jarFile)
def indexEntry = jar.getEntry("META-INF/jandex.idx")
assert indexEntry != null : "JAR ${jarFile} doesn't contain an index"

def index = new IndexReader(jar.getInputStream(indexEntry)).read()
assert index.getKnownClasses().size() == 3 : "Index in ${jarFile} does not contain exactly 3 classes"

assert index.getClassByName("org.jboss.jandex.maven.jar.SomeClass") != null
assert index.getClassByName("org.jboss.jandex.MethodParameterInfo") != null
assert index.getClassByName("io.smallrye.common.annotation.Experimental") != null
