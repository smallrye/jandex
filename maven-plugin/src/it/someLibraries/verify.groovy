import org.jboss.jandex.IndexReader

// index dir explicitly configured
def jandexFile = new File(basedir, 'target/index/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().size() > 0 : "Index ${jandexFile} contains no class"
