import org.jboss.jandex.IndexReader

// 1st execution
def jandexFile = new File(basedir, 'target/classes/META-INF/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

def index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().size() == 1 : "Index ${jandexFile} does not contain exactly 1 class"

// 2nd execution
jandexFile = new File(basedir, 'target/dependency/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().size() == 1 : "Index ${jandexFile} does not contain exactly 1 class"
