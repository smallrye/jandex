import org.jboss.jandex.IndexReader

// 3 file sets in 1 index
def jandexFile = new File(basedir, 'target/classes/META-INF/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

def index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().size() == 3 : "Index ${jandexFile} does not contain exactly 3 classes"
