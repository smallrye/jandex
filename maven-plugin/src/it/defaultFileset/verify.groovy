import org.jboss.jandex.IndexReader

def jandexFile = new File(basedir, 'target/classes/META-INF/jandex.idx')
assert !jandexFile.exists() : "File ${jandexFile} does exist"

// ---

// this assumes that each fileSet gets its own index, which is current behavior
// not sure this is _correct_ behavior though!

jandexFile = new File(basedir, 'target/dependency/META-INF/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().size() == 1 : "Index ${jandexFile} doesn't contain exactly 1 class"
