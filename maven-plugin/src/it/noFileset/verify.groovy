import org.jboss.jandex.IndexReader

def jandexFile = new File(basedir, 'target/classes/META-INF/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

def index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().isEmpty() : "Index ${jandexFile} is not empty"
